package com.abdestv.core.repository

import android.content.Context
import android.net.Uri
import com.abdestv.core.data.local.AbdesTvDatabase
import com.abdestv.core.model.ChannelEntity
import com.abdestv.core.model.ConnectionType
import com.abdestv.core.model.ContentType
import com.abdestv.core.model.EpgProgramEntity
import com.abdestv.core.model.EpisodeItem
import com.abdestv.core.model.IptvProfileEntity
import com.abdestv.core.model.SeriesEntity
import com.abdestv.core.model.WatchHistoryEntity
import com.abdestv.core.network.XtreamCodesClient
import com.abdestv.core.parser.M3uParser
import com.abdestv.core.parser.XmltvParser
import com.abdestv.core.security.SecureCredentialStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayInputStream
import java.io.IOException

class IptvRepository(private val context: Context) {

    private val database = AbdesTvDatabase.getInstance(context)
    private val dao = database.dao()
    val credentialStore = SecureCredentialStore(context)
    private val xtreamClient = XtreamCodesClient()
    private val okHttpClient: OkHttpClient = XtreamCodesClient.defaultOkHttpClient()

    fun observeProfiles(): Flow<List<IptvProfileEntity>> = dao.observeProfiles()

    fun observeActiveProfile(): Flow<IptvProfileEntity?> = dao.observeActiveProfile()

    fun observeChannelsByType(profileId: Long, contentType: ContentType): Flow<List<ChannelEntity>> =
        dao.observeChannelsByType(profileId, contentType.name)

    fun observeHiddenChannels(profileId: Long): Flow<List<ChannelEntity>> =
        dao.observeHiddenChannels(profileId)

    fun observeFavorites(profileId: Long): Flow<List<ChannelEntity>> =
        dao.observeFavorites(profileId)

    fun observeSeries(profileId: Long): Flow<List<SeriesEntity>> =
        dao.observeSeries(profileId)

    fun observeWatchHistory(profileId: Long): Flow<List<WatchHistoryEntity>> =
        dao.observeWatchHistory(profileId)

    fun searchChannels(profileId: Long, query: String): Flow<List<ChannelEntity>> =
        dao.searchChannels(profileId, query)

    fun searchSeries(profileId: Long, query: String): Flow<List<SeriesEntity>> =
        dao.searchSeries(profileId, query)

    fun observeEpg(profileId: Long): Flow<List<EpgProgramEntity>> {
        val now = System.currentTimeMillis()
        return dao.observeCurrentAndUpcomingEpg(profileId, now, now + 12 * 3600 * 1000L)
    }

    suspend fun selectActiveProfile(profileId: Long) {
        dao.setActiveProfile(profileId)
    }

    suspend fun deleteProfile(profileId: Long) {
        credentialStore.deletePassword(profileId)
        dao.deleteAllChannelsForProfile(profileId)
        dao.deleteSeriesForProfile(profileId)
        dao.clearEpgForProfile(profileId)
        dao.deleteProfile(profileId)
    }

    suspend fun createAndSyncXtreamProfile(
        profileName: String,
        serverUrl: String,
        username: String,
        password: String,
        customEpgUrl: String = ""
    ): Result<IptvProfileEntity> = withContext(Dispatchers.IO) {
        runCatching {
            val status = xtreamClient.authenticate(serverUrl, username, password).getOrThrow()
            val resolvedEpgUrl = customEpgUrl.ifBlank {
                XtreamCodesClient.buildXmltvUrl(serverUrl, username, password)
            }
            val newProfile = IptvProfileEntity(
                profileName = profileName.ifBlank { "Xtream • $username" },
                connectionType = ConnectionType.XTREAM_CODES.name,
                serverUrl = XtreamCodesClient.normalizeBaseUrl(serverUrl),
                username = username.trim(),
                epgUrl = resolvedEpgUrl,
                isActive = true,
                expirationTimestamp = status.expDateEpochSec?.times(1000L),
                maxConnections = status.maxConnections,
                lastSyncedAt = System.currentTimeMillis()
            )
            val profileId = dao.insertProfile(newProfile)
            credentialStore.savePassword(profileId, password.trim())
            dao.setActiveProfile(profileId)

            val savedProfile = newProfile.copy(id = profileId)
            syncProfileCatalog(savedProfile)
            savedProfile
        }
    }

    suspend fun createAndSyncM3uUrlProfile(
        profileName: String,
        m3uUrl: String,
        epgUrl: String = ""
    ): Result<IptvProfileEntity> = withContext(Dispatchers.IO) {
        runCatching {
            val newProfile = IptvProfileEntity(
                profileName = profileName.ifBlank { "Playlist M3U" },
                connectionType = ConnectionType.M3U_URL.name,
                m3uUrl = m3uUrl.trim(),
                epgUrl = epgUrl.trim(),
                isActive = true,
                lastSyncedAt = System.currentTimeMillis()
            )
            val profileId = dao.insertProfile(newProfile)
            dao.setActiveProfile(profileId)
            val saved = newProfile.copy(id = profileId)
            syncProfileCatalog(saved)
            saved
        }
    }

    suspend fun createAndSyncM3uFileProfile(
        profileName: String,
        fileUri: Uri,
        epgUrl: String = ""
    ): Result<IptvProfileEntity> = withContext(Dispatchers.IO) {
        runCatching {
            val newProfile = IptvProfileEntity(
                profileName = profileName.ifBlank { "Fichier M3U Local" },
                connectionType = ConnectionType.M3U_FILE.name,
                localFileUri = fileUri.toString(),
                epgUrl = epgUrl.trim(),
                isActive = true,
                lastSyncedAt = System.currentTimeMillis()
            )
            val profileId = dao.insertProfile(newProfile)
            dao.setActiveProfile(profileId)
            val saved = newProfile.copy(id = profileId)
            syncProfileCatalog(saved)
            saved
        }
    }

    suspend fun createLegalPublicTestProfile(): Result<IptvProfileEntity> = withContext(Dispatchers.IO) {
        runCatching {
            val sampleM3u = M3uParser.buildPublicLegalSampleM3u()
            val newProfile = IptvProfileEntity(
                profileName = "Flux Publics Officiels (Test)",
                connectionType = ConnectionType.M3U_URL.name,
                m3uUrl = "internal://public_sample_m3u",
                isActive = true,
                lastSyncedAt = System.currentTimeMillis()
            )
            val profileId = dao.insertProfile(newProfile)
            dao.setActiveProfile(profileId)
            val parseResult = M3uParser.parse(
                ByteArrayInputStream(sampleM3u.toByteArray(Charsets.UTF_8)),
                profileId
            )
            dao.deleteAllChannelsForProfile(profileId)
            dao.insertChannels(parseResult.channels)
            newProfile.copy(id = profileId)
        }
    }

    suspend fun syncProfileCatalog(profile: IptvProfileEntity): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            when (ConnectionType.valueOf(profile.connectionType)) {
                ConnectionType.XTREAM_CODES -> {
                    val password = credentialStore.getPassword(profile.id)
                    val live = xtreamClient.fetchLiveChannels(profile.id, profile.serverUrl, profile.username, password)
                    val movies = xtreamClient.fetchVodMovies(profile.id, profile.serverUrl, profile.username, password)
                    val series = xtreamClient.fetchSeries(profile.id, profile.serverUrl, profile.username, password)

                    dao.replaceChannelsPreservingFlags(profile.id, ContentType.LIVE.name, live)
                    dao.replaceChannelsPreservingFlags(profile.id, ContentType.MOVIE.name, movies)
                    dao.deleteSeriesForProfile(profile.id)
                    dao.insertSeries(series)

                    if (profile.epgUrl.isNotBlank()) {
                        runCatching { syncXmltvEpg(profile.id, profile.epgUrl) }
                    }
                }
                ConnectionType.M3U_URL -> {
                    if (profile.m3uUrl == "internal://public_sample_m3u") {
                        val sample = M3uParser.buildPublicLegalSampleM3u()
                        val parsed = M3uParser.parse(ByteArrayInputStream(sample.toByteArray(Charsets.UTF_8)), profile.id)
                        dao.deleteAllChannelsForProfile(profile.id)
                        dao.insertChannels(parsed.channels)
                    } else {
                        val request = Request.Builder()
                            .url(profile.m3uUrl)
                            .header("User-Agent", "AbdesTV/2.0 (Android; M3UClient)")
                            .get()
                            .build()
                        okHttpClient.newCall(request).execute().use { response ->
                            if (!response.isSuccessful) {
                                throw IOException("Échec du téléchargement de la playlist M3U (HTTP ${response.code}).")
                            }
                            val stream = response.body?.byteStream()
                                ?: throw IOException("Playlist M3U vide.")
                            val parsed = M3uParser.parse(stream, profile.id)
                            val live = parsed.channels.filter { it.contentType == ContentType.LIVE.name }
                            val movies = parsed.channels.filter { it.contentType == ContentType.MOVIE.name }
                            val series = parsed.channels.filter { it.contentType == ContentType.SERIES.name }

                            dao.replaceChannelsPreservingFlags(profile.id, ContentType.LIVE.name, live)
                            dao.replaceChannelsPreservingFlags(profile.id, ContentType.MOVIE.name, movies)
                            dao.replaceChannelsPreservingFlags(profile.id, ContentType.SERIES.name, series)

                            val epgTarget = profile.epgUrl.ifBlank { parsed.detectedXmltvUrl.orEmpty() }
                            if (epgTarget.isNotBlank()) {
                                runCatching { syncXmltvEpg(profile.id, epgTarget) }
                            }
                        }
                    }
                }
                ConnectionType.M3U_FILE -> {
                    val uri = Uri.parse(profile.localFileUri)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val parsed = M3uParser.parse(stream, profile.id)
                        dao.deleteAllChannelsForProfile(profile.id)
                        dao.insertChannels(parsed.channels)
                        val epgTarget = profile.epgUrl.ifBlank { parsed.detectedXmltvUrl.orEmpty() }
                        if (epgTarget.isNotBlank()) {
                            runCatching { syncXmltvEpg(profile.id, epgTarget) }
                        }
                    } ?: throw IOException("Impossible d'ouvrir le fichier M3U local.")
                }
            }
            dao.updateProfile(profile.copy(lastSyncedAt = System.currentTimeMillis()))
        }
    }

    suspend fun syncXmltvEpg(profileId: Long, epgUrl: String) = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(epgUrl)
            .header("User-Agent", "AbdesTV/2.0 (Android; XMLTV)")
            .get()
            .build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@use
            val stream = response.body?.byteStream() ?: return@use
            val programs = XmltvParser.parse(stream, profileId)
            if (programs.isNotEmpty()) {
                dao.clearEpgForProfile(profileId)
                dao.insertEpgPrograms(programs)
            }
        }
    }

    suspend fun fetchSeriesEpisodes(profile: IptvProfileEntity, seriesId: Int): Result<List<EpisodeItem>> {
        val password = credentialStore.getPassword(profile.id)
        return xtreamClient.fetchSeriesEpisodes(profile.serverUrl, profile.username, password, seriesId)
    }

    suspend fun toggleFavorite(channel: ChannelEntity) {
        dao.setChannelFavorite(channel.id, channel.profileId, !channel.isFavorite)
    }

    suspend fun toggleHidden(channel: ChannelEntity) {
        dao.setChannelHidden(channel.id, channel.profileId, !channel.isHidden)
    }

    suspend fun restoreHiddenChannels(profileId: Long) {
        dao.unhideAllChannels(profileId)
    }

    suspend fun recordWatchProgress(
        channel: ChannelEntity,
        positionMs: Long,
        durationMs: Long
    ) {
        dao.upsertWatchHistory(
            WatchHistoryEntity(
                itemId = channel.id,
                profileId = channel.profileId,
                title = channel.name,
                categoryName = channel.categoryName,
                streamUrl = channel.streamUrl,
                logoUrl = channel.logoUrl,
                contentType = channel.contentType,
                positionMs = positionMs,
                durationMs = durationMs,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearWatchHistory(profileId: Long) {
        dao.clearWatchHistory(profileId)
    }
}
