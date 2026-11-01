package com.abdestv.core.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abdestv.core.model.ChannelEntity
import com.abdestv.core.model.ContentType
import com.abdestv.core.model.EpgProgramEntity
import com.abdestv.core.model.EpisodeItem
import com.abdestv.core.model.IptvProfileEntity
import com.abdestv.core.model.SeriesEntity
import com.abdestv.core.model.WatchHistoryEntity
import com.abdestv.core.player.AbdesPlayerManager
import com.abdestv.core.repository.IptvRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SharedIptvViewModel(application: Application) : AndroidViewModel(application) {

    val repository = IptvRepository(application)
    val playerManager = AbdesPlayerManager(application)

    val profiles: StateFlow<List<IptvProfileEntity>> = repository.observeProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProfile: StateFlow<IptvProfileEntity?> = repository.observeActiveProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _statusMessageFr = MutableStateFlow<String?>(null)
    val statusMessageFr: StateFlow<String?> = _statusMessageFr.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedLiveCategory = MutableStateFlow("Toutes")
    val selectedLiveCategory: StateFlow<String> = _selectedLiveCategory.asStateFlow()

    private val _selectedMovieCategory = MutableStateFlow("Toutes")
    val selectedMovieCategory: StateFlow<String> = _selectedMovieCategory.asStateFlow()

    private val _selectedSeriesCategory = MutableStateFlow("Toutes")
    val selectedSeriesCategory: StateFlow<String> = _selectedSeriesCategory.asStateFlow()

    private val _selectedSeriesForEpisodes = MutableStateFlow<SeriesEntity?>(null)
    val selectedSeriesForEpisodes: StateFlow<SeriesEntity?> = _selectedSeriesForEpisodes.asStateFlow()

    private val _seriesEpisodes = MutableStateFlow<List<EpisodeItem>>(emptyList())
    val seriesEpisodes: StateFlow<List<EpisodeItem>> = _seriesEpisodes.asStateFlow()

    private val _isLoadingEpisodes = MutableStateFlow(false)
    val isLoadingEpisodes: StateFlow<Boolean> = _isLoadingEpisodes.asStateFlow()

    val liveChannels: StateFlow<List<ChannelEntity>> = activeProfile
        .flatMapLatest { profile ->
            if (profile == null) flowOf(emptyList())
            else repository.observeChannelsByType(profile.id, ContentType.LIVE)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vodMovies: StateFlow<List<ChannelEntity>> = activeProfile
        .flatMapLatest { profile ->
            if (profile == null) flowOf(emptyList())
            else repository.observeChannelsByType(profile.id, ContentType.MOVIE)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val m3uSeriesItems: StateFlow<List<ChannelEntity>> = activeProfile
        .flatMapLatest { profile ->
            if (profile == null) flowOf(emptyList())
            else repository.observeChannelsByType(profile.id, ContentType.SERIES)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val xtreamSeriesItems: StateFlow<List<SeriesEntity>> = activeProfile
        .flatMapLatest { profile ->
            if (profile == null) flowOf(emptyList())
            else repository.observeSeries(profile.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<ChannelEntity>> = activeProfile
        .flatMapLatest { profile ->
            if (profile == null) flowOf(emptyList())
            else repository.observeFavorites(profile.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hiddenChannels: StateFlow<List<ChannelEntity>> = activeProfile
        .flatMapLatest { profile ->
            if (profile == null) flowOf(emptyList())
            else repository.observeHiddenChannels(profile.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchHistory: StateFlow<List<WatchHistoryEntity>> = activeProfile
        .flatMapLatest { profile ->
            if (profile == null) flowOf(emptyList())
            else repository.observeWatchHistory(profile.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val epgPrograms: StateFlow<List<EpgProgramEntity>> = activeProfile
        .flatMapLatest { profile ->
            if (profile == null) flowOf(emptyList())
            else repository.observeEpg(profile.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchResults: StateFlow<List<ChannelEntity>> = combine(activeProfile, _searchQuery) { profile, query ->
        profile to query.trim()
    }.flatMapLatest { (profile, query) ->
        if (profile == null || query.isBlank()) flowOf(emptyList())
        else repository.searchChannels(profile.id, query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectLiveCategory(cat: String) {
        _selectedLiveCategory.value = cat
    }

    fun selectMovieCategory(cat: String) {
        _selectedMovieCategory.value = cat
    }

    fun selectSeriesCategory(cat: String) {
        _selectedSeriesCategory.value = cat
    }

    fun clearStatusMessage() {
        _statusMessageFr.value = null
    }

    fun loginXtreamCodes(
        profileName: String,
        serverUrl: String,
        username: String,
        password: String,
        customEpgUrl: String
    ) {
        viewModelScope.launch {
            _isSyncing.value = true
            _statusMessageFr.value = "Authentification Xtream Codes et synchronisation du catalogue..."
            val res = repository.createAndSyncXtreamProfile(profileName, serverUrl, username, password, customEpgUrl)
            _isSyncing.value = false
            res.onSuccess {
                _statusMessageFr.value = "Profil « ${it.profileName} » connecté et synchronisé avec succès."
            }.onFailure { err ->
                _statusMessageFr.value = "Erreur Xtream Codes : ${err.localizedMessage ?: "Connexion impossible"}"
            }
        }
    }

    fun loginM3uUrl(profileName: String, m3uUrl: String, epgUrl: String) {
        viewModelScope.launch {
            _isSyncing.value = true
            _statusMessageFr.value = "Téléchargement et analyse de la playlist M3U..."
            val res = repository.createAndSyncM3uUrlProfile(profileName, m3uUrl, epgUrl)
            _isSyncing.value = false
            res.onSuccess {
                _statusMessageFr.value = "Playlist « ${it.profileName} » importée avec succès."
            }.onFailure { err ->
                _statusMessageFr.value = "Erreur M3U : ${err.localizedMessage ?: "Impossible de lire l'URL"}"
            }
        }
    }

    fun loginM3uFile(profileName: String, uri: Uri, epgUrl: String) {
        viewModelScope.launch {
            _isSyncing.value = true
            _statusMessageFr.value = "Importation du fichier M3U local..."
            val res = repository.createAndSyncM3uFileProfile(profileName, uri, epgUrl)
            _isSyncing.value = false
            res.onSuccess {
                _statusMessageFr.value = "Fichier M3U « ${it.profileName} » chargé avec succès."
            }.onFailure { err ->
                _statusMessageFr.value = "Erreur Fichier M3U : ${err.localizedMessage ?: "Lecture impossible"}"
            }
        }
    }

    fun loadLegalPublicTestStreams() {
        viewModelScope.launch {
            _isSyncing.value = true
            _statusMessageFr.value = "Chargement des flux de démonstration publics officiels..."
            val res = repository.createLegalPublicTestProfile()
            _isSyncing.value = false
            res.onSuccess {
                _statusMessageFr.value = "Flux publics de test chargés."
            }.onFailure { err ->
                _statusMessageFr.value = "Erreur : ${err.localizedMessage}"
            }
        }
    }

    fun refreshActiveProfile() {
        val profile = activeProfile.value ?: return
        viewModelScope.launch {
            _isSyncing.value = true
            _statusMessageFr.value = "Actualisation des chaînes, VOD et guide EPG..."
            val res = repository.syncProfileCatalog(profile)
            _isSyncing.value = false
            res.onSuccess {
                _statusMessageFr.value = "Catalogue et EPG mis à jour."
            }.onFailure { err ->
                _statusMessageFr.value = "Échec de l'actualisation : ${err.localizedMessage}"
            }
        }
    }

    fun switchProfile(profileId: Long) {
        viewModelScope.launch {
            repository.selectActiveProfile(profileId)
            _statusMessageFr.value = "Profil actif modifié."
        }
    }

    fun deleteProfile(profileId: Long) {
        viewModelScope.launch {
            repository.deleteProfile(profileId)
            _statusMessageFr.value = "Profil supprimé."
        }
    }

    fun playChannel(channel: ChannelEntity, resumePositionMs: Long = 0L) {
        playerManager.playChannel(channel, resumePositionMs)
        viewModelScope.launch {
            repository.recordWatchProgress(channel, resumePositionMs, playerManager.durationMs.value)
        }
    }

    fun playEpisode(series: SeriesEntity, episode: EpisodeItem) {
        val profileId = activeProfile.value?.id ?: 0L
        val tempChannel = ChannelEntity(
            id = "series_${series.seriesId}_ep_${episode.id}",
            profileId = profileId,
            name = "${series.name} • S${episode.seasonNum}E${episode.episodeNum} - ${episode.title}",
            categoryName = series.categoryName,
            streamUrl = episode.streamUrl,
            logoUrl = episode.thumbnailUrl.ifBlank { series.coverUrl },
            contentType = ContentType.SERIES.name,
            plot = episode.plot.ifBlank { series.plot }
        )
        playChannel(tempChannel)
    }

    fun playNextChannel() {
        val current = playerManager.currentChannel.value ?: return
        val pool = when (current.contentType) {
            ContentType.MOVIE.name -> vodMovies.value
            ContentType.SERIES.name -> m3uSeriesItems.value
            else -> liveChannels.value
        }
        if (pool.isEmpty()) return
        val idx = pool.indexOfFirst { it.id == current.id }
        val next = pool[(idx + 1).mod(pool.size)]
        playChannel(next)
    }

    fun playPreviousChannel() {
        val current = playerManager.currentChannel.value ?: return
        val pool = when (current.contentType) {
            ContentType.MOVIE.name -> vodMovies.value
            ContentType.SERIES.name -> m3uSeriesItems.value
            else -> liveChannels.value
        }
        if (pool.isEmpty()) return
        val idx = pool.indexOfFirst { it.id == current.id }
        val prev = pool[(idx - 1).mod(pool.size)]
        playChannel(prev)
    }

    fun toggleFavorite(channel: ChannelEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(channel)
        }
    }

    fun hideChannel(channel: ChannelEntity) {
        viewModelScope.launch {
            repository.toggleHidden(channel)
            _statusMessageFr.value = "Chaîne « ${channel.name} » masquée."
        }
    }

    fun restoreAllHiddenChannels() {
        val profile = activeProfile.value ?: return
        viewModelScope.launch {
            repository.restoreHiddenChannels(profile.id)
            _statusMessageFr.value = "Toutes les chaînes masquées ont été restaurées."
        }
    }

    fun clearHistory() {
        val profile = activeProfile.value ?: return
        viewModelScope.launch {
            repository.clearWatchHistory(profile.id)
            _statusMessageFr.value = "Historique de lecture effacé."
        }
    }

    fun openSeriesDetails(series: SeriesEntity) {
        val profile = activeProfile.value ?: return
        _selectedSeriesForEpisodes.value = series
        _isLoadingEpisodes.value = true
        viewModelScope.launch {
            val res = repository.fetchSeriesEpisodes(profile, series.seriesId)
            _isLoadingEpisodes.value = false
            _seriesEpisodes.value = res.getOrDefault(emptyList())
        }
    }

    fun closeSeriesDetails() {
        _selectedSeriesForEpisodes.value = null
        _seriesEpisodes.value = emptyList()
    }

    fun stopAndRecordPlayback() {
        val current = playerManager.currentChannel.value
        val pos = playerManager.currentPositionMs.value
        val dur = playerManager.durationMs.value
        if (current != null) {
            viewModelScope.launch {
                repository.recordWatchProgress(current, pos, dur)
            }
        }
        playerManager.stopPlayback()
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}
