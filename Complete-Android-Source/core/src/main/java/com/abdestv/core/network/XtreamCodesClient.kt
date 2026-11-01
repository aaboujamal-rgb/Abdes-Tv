package com.abdestv.core.network

import com.abdestv.core.model.ChannelEntity
import com.abdestv.core.model.ContentType
import com.abdestv.core.model.EpisodeItem
import com.abdestv.core.model.SeriesEntity
import com.abdestv.core.model.XtreamAccountStatus
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

class XtreamCodesClient(
    private val okHttpClient: OkHttpClient = defaultOkHttpClient()
) {
    companion object {
        fun defaultOkHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(25, TimeUnit.SECONDS)
                .readTimeout(45, TimeUnit.SECONDS)
                .writeTimeout(25, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build()
        }

        fun normalizeBaseUrl(rawUrl: String): String {
            var trimmed = rawUrl.trim().removeSuffix("/")
            if (!trimmed.startsWith("http://", ignoreCase = true) &&
                !trimmed.startsWith("https://", ignoreCase = true)
            ) {
                trimmed = "http://$trimmed"
            }
            return trimmed
        }

        fun buildXmltvUrl(serverUrl: String, username: String, password: String): String {
            val base = normalizeBaseUrl(serverUrl)
            return "$base/xmltv.php?username=${encodeParam(username)}&password=${encodeParam(password)}"
        }

        private fun encodeParam(value: String): String {
            return java.net.URLEncoder.encode(value.trim(), "UTF-8")
        }
    }

    suspend fun authenticate(
        serverUrl: String,
        username: String,
        password: String
    ): Result<XtreamAccountStatus> = withContext(Dispatchers.IO) {
        runCatching {
            val base = normalizeBaseUrl(serverUrl)
            val url = "$base/player_api.php?username=${encodeParam(username)}&password=${encodeParam(password)}"
            val json = executeGetJson(url).asJsonObject
            val userInfo = json.getAsJsonObject("user_info")
                ?: throw IOException("Réponse Xtream Codes invalide : champ user_info manquant.")
            val auth = userInfo.get("auth")?.asInt ?: 0
            val status = userInfo.get("status")?.asString ?: "Unknown"
            if (auth == 0 && !status.equals("Active", ignoreCase = true)) {
                throw IOException("Identifiants Xtream Codes refusés (statut : $status).")
            }
            val serverInfo = json.getAsJsonObject("server_info")
            XtreamAccountStatus(
                username = userInfo.get("username")?.asString ?: username,
                status = status,
                expDateEpochSec = userInfo.get("exp_date")?.takeIf { !it.isJsonNull }?.asString?.toLongOrNull(),
                activeConnections = userInfo.get("active_cons")?.takeIf { !it.isJsonNull }?.asString ?: "0",
                maxConnections = userInfo.get("max_connections")?.takeIf { !it.isJsonNull }?.asString ?: "1",
                serverProtocol = serverInfo?.get("server_protocol")?.takeIf { !it.isJsonNull }?.asString ?: "http",
                timezone = serverInfo?.get("timezone")?.takeIf { !it.isJsonNull }?.asString ?: "Europe/Paris"
            )
        }
    }

    suspend fun fetchLiveChannels(
        profileId: Long,
        serverUrl: String,
        username: String,
        password: String
    ): List<ChannelEntity> = withContext(Dispatchers.IO) {
        val base = normalizeBaseUrl(serverUrl)
        val categoriesMap = fetchCategoryMap(base, username, password, "get_live_categories")
        val streamsUrl = "$base/player_api.php?username=${encodeParam(username)}&password=${encodeParam(password)}&action=get_live_streams"
        val element = executeGetJson(streamsUrl)
        if (!element.isJsonArray) return@withContext emptyList()

        element.asJsonArray.mapNotNull { item ->
            val obj = item.takeIf { it.isJsonObject }?.asJsonObject ?: return@mapNotNull null
            val streamId = obj.optInt("stream_id")
            if (streamId <= 0) return@mapNotNull null
            val name = obj.optString("name", "Chaîne #$streamId")
            val categoryId = obj.optString("category_id", "live")
            val categoryName = categoriesMap[categoryId] ?: "TV en Direct"
            val logo = obj.optString("stream_icon", "")
            val epgId = obj.optString("epg_channel_id", "")

            val tsUrl = "$base/live/${username.trim()}/${password.trim()}/$streamId.ts"
            val hlsUrl = "$base/live/${username.trim()}/${password.trim()}/$streamId.m3u8"

            ChannelEntity(
                id = "xtream_live_$streamId",
                profileId = profileId,
                streamId = streamId,
                name = name,
                categoryId = categoryId,
                categoryName = categoryName,
                streamUrl = tsUrl,
                fallbackStreamUrl = hlsUrl,
                logoUrl = logo,
                epgChannelId = epgId,
                contentType = ContentType.LIVE.name,
                containerExtension = "ts"
            )
        }
    }

    suspend fun fetchVodMovies(
        profileId: Long,
        serverUrl: String,
        username: String,
        password: String
    ): List<ChannelEntity> = withContext(Dispatchers.IO) {
        val base = normalizeBaseUrl(serverUrl)
        val categoriesMap = fetchCategoryMap(base, username, password, "get_vod_categories")
        val streamsUrl = "$base/player_api.php?username=${encodeParam(username)}&password=${encodeParam(password)}&action=get_vod_streams"
        val element = executeGetJson(streamsUrl)
        if (!element.isJsonArray) return@withContext emptyList()

        element.asJsonArray.mapNotNull { item ->
            val obj = item.takeIf { it.isJsonObject }?.asJsonObject ?: return@mapNotNull null
            val streamId = obj.optInt("stream_id")
            if (streamId <= 0) return@mapNotNull null
            val name = obj.optString("name", "Film #$streamId")
            val categoryId = obj.optString("category_id", "vod")
            val categoryName = categoriesMap[categoryId] ?: "Films"
            val logo = obj.optString("stream_icon", "")
            val ext = obj.optString("container_extension", "mp4").ifBlank { "mp4" }
            val rating = obj.optString("rating", "")
            val added = obj.optString("added", "").toLongOrNull()?.times(1000L) ?: System.currentTimeMillis()

            val movieUrl = "$base/movie/${username.trim()}/${password.trim()}/$streamId.$ext"

            ChannelEntity(
                id = "xtream_vod_$streamId",
                profileId = profileId,
                streamId = streamId,
                name = name,
                categoryId = categoryId,
                categoryName = categoryName,
                streamUrl = movieUrl,
                logoUrl = logo,
                contentType = ContentType.MOVIE.name,
                containerExtension = ext,
                rating = rating,
                addedTimestamp = added
            )
        }
    }

    suspend fun fetchSeries(
        profileId: Long,
        serverUrl: String,
        username: String,
        password: String
    ): List<SeriesEntity> = withContext(Dispatchers.IO) {
        val base = normalizeBaseUrl(serverUrl)
        val categoriesMap = fetchCategoryMap(base, username, password, "get_series_categories")
        val seriesUrl = "$base/player_api.php?username=${encodeParam(username)}&password=${encodeParam(password)}&action=get_series"
        val element = executeGetJson(seriesUrl)
        if (!element.isJsonArray) return@withContext emptyList()

        element.asJsonArray.mapNotNull { item ->
            val obj = item.takeIf { it.isJsonObject }?.asJsonObject ?: return@mapNotNull null
            val seriesId = obj.optInt("series_id")
            if (seriesId <= 0) return@mapNotNull null
            val categoryId = obj.optString("category_id", "series")
            val categoryName = categoriesMap[categoryId] ?: "Séries"
            SeriesEntity(
                seriesId = seriesId,
                profileId = profileId,
                name = obj.optString("name", "Série #$seriesId"),
                categoryId = categoryId,
                categoryName = categoryName,
                coverUrl = obj.optString("cover", ""),
                plot = obj.optString("plot", ""),
                cast = obj.optString("cast", ""),
                director = obj.optString("director", ""),
                genre = obj.optString("genre", categoryName),
                releaseDate = obj.optString("releaseDate", ""),
                rating = obj.optString("rating", "")
            )
        }
    }

    suspend fun fetchSeriesEpisodes(
        serverUrl: String,
        username: String,
        password: String,
        seriesId: Int
    ): Result<List<EpisodeItem>> = withContext(Dispatchers.IO) {
        runCatching {
            val base = normalizeBaseUrl(serverUrl)
            val infoUrl = "$base/player_api.php?username=${encodeParam(username)}&password=${encodeParam(password)}&action=get_series_info&series_id=$seriesId"
            val root = executeGetJson(infoUrl)
            if (!root.isJsonObject) return@runCatching emptyList()
            val episodesElement = root.asJsonObject.get("episodes") ?: return@runCatching emptyList()
            val episodes = mutableListOf<EpisodeItem>()

            if (episodesElement.isJsonObject) {
                for ((seasonKey, seasonArr) in episodesElement.asJsonObject.entrySet()) {
                    val seasonNum = seasonKey.toIntOrNull() ?: 1
                    if (seasonArr.isJsonArray) {
                        seasonArr.asJsonArray.forEach { epEl ->
                            parseEpisodeObject(epEl, seasonNum, base, username, password)?.let {
                                episodes.add(it)
                            }
                        }
                    }
                }
            } else if (episodesElement.isJsonArray) {
                episodesElement.asJsonArray.forEachIndexed { index, seasonArr ->
                    if (seasonArr.isJsonArray) {
                        seasonArr.asJsonArray.forEach { epEl ->
                            parseEpisodeObject(epEl, index + 1, base, username, password)?.let {
                                episodes.add(it)
                            }
                        }
                    }
                }
            }
            episodes.sortedWith(compareBy({ it.seasonNum }, { it.episodeNum }))
        }
    }

    private fun parseEpisodeObject(
        epEl: JsonElement,
        defaultSeason: Int,
        base: String,
        username: String,
        password: String
    ): EpisodeItem? {
        if (!epEl.isJsonObject) return null
        val obj = epEl.asJsonObject
        val epId = obj.optString("id", "")
        if (epId.isBlank()) return null
        val epNum = obj.optInt("episode_num", 1)
        val season = obj.optInt("season", defaultSeason)
        val title = obj.optString("title", "S${season}E${epNum}")
        val ext = obj.optString("container_extension", "mp4").ifBlank { "mp4" }
        val infoObj = obj.get("info")?.takeIf { it.isJsonObject }?.asJsonObject
        val plot = infoObj?.optString("plot", "") ?: ""
        val thumb = infoObj?.optString("movie_image", "") ?: ""
        val durationSecs = infoObj?.optInt("duration_secs", 0) ?: 0
        val streamUrl = "$base/series/${username.trim()}/${password.trim()}/$epId.$ext"

        return EpisodeItem(
            id = epId,
            episodeNum = epNum,
            seasonNum = season,
            title = title,
            containerExtension = ext,
            streamUrl = streamUrl,
            plot = plot,
            durationSecs = durationSecs,
            thumbnailUrl = thumb
        )
    }

    private fun fetchCategoryMap(
        base: String,
        username: String,
        password: String,
        action: String
    ): Map<String, String> {
        return try {
            val url = "$base/player_api.php?username=${encodeParam(username)}&password=${encodeParam(password)}&action=$action"
            val el = executeGetJson(url)
            if (!el.isJsonArray) return emptyMap()
            val map = mutableMapOf<String, String>()
            el.asJsonArray.forEach { item ->
                if (item.isJsonObject) {
                    val obj = item.asJsonObject
                    val id = obj.optString("category_id", "")
                    val name = obj.optString("category_name", "")
                    if (id.isNotBlank() && name.isNotBlank()) {
                        map[id] = name
                    }
                }
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun executeGetJson(url: String): JsonElement {
        val httpUrl = url.toHttpUrlOrNull() ?: throw IOException("URL invalide : $url")
        val request = Request.Builder()
            .url(httpUrl)
            .header("User-Agent", "AbdesTV/2.0 (Android; XtreamClient)")
            .header("Accept", "application/json")
            .get()
            .build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Erreur HTTP ${response.code} lors de la connexion au serveur.")
            }
            val bodyStr = response.body?.string()
                ?: throw IOException("Réponse vide reçue du serveur IPTV.")
            return JsonParser.parseString(bodyStr)
        }
    }

    private fun JsonObject.optString(key: String, fallback: String = ""): String {
        val el = this.get(key) ?: return fallback
        if (el.isJsonNull) return fallback
        return runCatching { el.asString }.getOrDefault(fallback)
    }

    private fun JsonObject.optInt(key: String, fallback: Int = 0): Int {
        val el = this.get(key) ?: return fallback
        if (el.isJsonNull) return fallback
        return runCatching { el.asInt }.getOrElse {
            runCatching { el.asString.toInt() }.getOrDefault(fallback)
        }
    }
}
