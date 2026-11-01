package com.abdestv.core.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.abdestv.core.model.AudioSubtitleTrack
import com.abdestv.core.model.ChannelEntity
import com.abdestv.core.model.VideoAspectRatio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(UnstableApi::class)
class AbdesPlayerManager(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var retryJob: Job? = null
    private var progressJob: Job? = null
    private var retryCount = 0
    private var usedFallbackUrl = false

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val okHttpDataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
        .setUserAgent("AbdesTV/2.0 (Android; Media3 ExoPlayer)")

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(appContext)
        .setMediaSourceFactory(
            DefaultMediaSourceFactory(DefaultDataSource.Factory(appContext, okHttpDataSourceFactory))
        )
        .setLoadControl(
            DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    15_000,
                    50_000,
                    2_000,
                    4_000
                )
                .build()
        )
        .build()

    private val _currentChannel = MutableStateFlow<ChannelEntity?>(null)
    val currentChannel: StateFlow<ChannelEntity?> = _currentChannel.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _errorMessageFr = MutableStateFlow<String?>(null)
    val errorMessageFr: StateFlow<String?> = _errorMessageFr.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _aspectRatio = MutableStateFlow(VideoAspectRatio.FIT)
    val aspectRatio: StateFlow<VideoAspectRatio> = _aspectRatio.asStateFlow()

    private val _audioTracks = MutableStateFlow<List<AudioSubtitleTrack>>(emptyList())
    val audioTracks: StateFlow<List<AudioSubtitleTrack>> = _audioTracks.asStateFlow()

    private val _subtitleTracks = MutableStateFlow<List<AudioSubtitleTrack>>(emptyList())
    val subtitleTracks: StateFlow<List<AudioSubtitleTrack>> = _subtitleTracks.asStateFlow()

    private val _volume = MutableStateFlow(1.0f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                _isBuffering.value = (playbackState == Player.STATE_BUFFERING)
                if (playbackState == Player.STATE_READY) {
                    _errorMessageFr.value = null
                    retryCount = 0
                    _durationMs.value = exoPlayer.duration.coerceAtLeast(0L)
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onTracksChanged(tracks: Tracks) {
                extractTracks(tracks)
            }

            override fun onPlayerError(error: PlaybackException) {
                handlePlaybackError(error)
            }
        })
        startProgressTicker()
    }

    fun playChannel(channel: ChannelEntity, startPositionMs: Long = 0L) {
        retryJob?.cancel()
        retryCount = 0
        usedFallbackUrl = false
        _errorMessageFr.value = null
        _currentChannel.value = channel
        okHttpDataSourceFactory.setUserAgent(channel.userAgent.ifBlank { "AbdesTV/2.0 (Android; Media3)" })

        loadUrl(channel.streamUrl, startPositionMs)
    }

    private fun loadUrl(url: String, startPositionMs: Long = 0L) {
        val mediaItem = MediaItem.fromUri(url)
        exoPlayer.setMediaItem(mediaItem, startPositionMs.coerceAtLeast(0L))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    private fun handlePlaybackError(error: PlaybackException) {
        val ch = _currentChannel.value
        if (ch != null && !usedFallbackUrl && ch.fallbackStreamUrl.isNotBlank()) {
            usedFallbackUrl = true
            _errorMessageFr.value = "Basculement automatique vers le flux HLS de secours..."
            loadUrl(ch.fallbackStreamUrl, _currentPositionMs.value)
            return
        }

        if (retryCount < 2 && ch != null) {
            retryCount++
            _errorMessageFr.value = "Reconnexion automatique au flux (${retryCount}/2)..."
            retryJob?.cancel()
            retryJob = scope.launch {
                delay(2000L * retryCount)
                val targetUrl = if (usedFallbackUrl && ch.fallbackStreamUrl.isNotBlank()) {
                    ch.fallbackStreamUrl
                } else {
                    ch.streamUrl
                }
                loadUrl(targetUrl, _currentPositionMs.value)
            }
        } else {
            _errorMessageFr.value = when (error.errorCode) {
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
                    "Connexion au serveur IPTV interrompue. Vérifiez votre réseau ou l'état du serveur."
                PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS ->
                    "Le serveur a refusé l'accès au flux (abonnement expiré ou limite de connexions atteinte)."
                PlaybackException.ERROR_CODE_DECODING_FAILED,
                PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED ->
                    "Format vidéo ou codec non pris en charge pour ce flux."
                else -> "Erreur de lecture du flux (${error.errorCodeName}). Appuyez sur Réessayer."
            }
        }
    }

    fun retryPlayback() {
        val ch = _currentChannel.value ?: return
        retryCount = 0
        _errorMessageFr.value = null
        loadUrl(ch.streamUrl, _currentPositionMs.value)
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            if (exoPlayer.playbackState == Player.STATE_IDLE) {
                exoPlayer.prepare()
            }
            exoPlayer.play()
        }
    }

    fun seekTo(positionMs: Long) {
        if (exoPlayer.isCurrentMediaItemSeekable) {
            exoPlayer.seekTo(positionMs.coerceAtLeast(0L))
            _currentPositionMs.value = positionMs.coerceAtLeast(0L)
        }
    }

    fun seekRelative(deltaMs: Long) {
        if (exoPlayer.isCurrentMediaItemSeekable) {
            val target = (exoPlayer.currentPosition + deltaMs).coerceIn(0L, exoPlayer.duration.coerceAtLeast(0L))
            exoPlayer.seekTo(target)
            _currentPositionMs.value = target
        }
    }

    fun setPlayerVolume(value: Float) {
        val clamped = value.coerceIn(0f, 1f)
        exoPlayer.volume = clamped
        _volume.value = clamped
    }

    fun cycleAspectRatio(): VideoAspectRatio {
        val values = VideoAspectRatio.entries
        val next = values[(values.indexOf(_aspectRatio.value) + 1) % values.size]
        _aspectRatio.value = next
        return next
    }

    fun selectTrack(track: AudioSubtitleTrack) {
        val currentGroups = exoPlayer.currentTracks.groups
        if (track.groupIndex !in currentGroups.indices) return
        val group = currentGroups[track.groupIndex]
        val override = TrackSelectionOverride(group.mediaTrackGroup, listOf(track.trackIndex))
        val trackType = if (track.isAudio) C.TRACK_TYPE_AUDIO else C.TRACK_TYPE_TEXT

        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
            .buildUpon()
            .setTrackTypeDisabled(trackType, false)
            .clearOverridesOfType(trackType)
            .addOverride(override)
            .build()
    }

    fun disableSubtitles() {
        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
            .buildUpon()
            .clearOverridesOfType(C.TRACK_TYPE_TEXT)
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
            .build()
    }

    private fun extractTracks(tracks: Tracks) {
        val audios = mutableListOf<AudioSubtitleTrack>()
        val subtitles = mutableListOf<AudioSubtitleTrack>()

        tracks.groups.forEachIndexed { groupIdx, group ->
            val type = group.type
            if (type == C.TRACK_TYPE_AUDIO || type == C.TRACK_TYPE_TEXT) {
                for (trackIdx in 0 until group.length) {
                    val format = group.getTrackFormat(trackIdx)
                    val langCode = format.language ?: "und"
                    val langDisplay = Locale(langCode).getDisplayLanguage(Locale.FRENCH)
                        .replaceFirstChar { it.uppercase() }
                        .ifBlank { "Piste ${trackIdx + 1}" }
                    val label = buildString {
                        append(format.label ?: langDisplay)
                        if (format.channelCount > 0 && type == C.TRACK_TYPE_AUDIO) {
                            append(" (${format.channelCount}ch)")
                        }
                    }
                    val item = AudioSubtitleTrack(
                        groupIndex = groupIdx,
                        trackIndex = trackIdx,
                        id = "${groupIdx}_$trackIdx",
                        label = label,
                        language = langDisplay,
                        isSelected = group.isTrackSelected(trackIdx),
                        isAudio = (type == C.TRACK_TYPE_AUDIO)
                    )
                    if (type == C.TRACK_TYPE_AUDIO) audios.add(item) else subtitles.add(item)
                }
            }
        }
        _audioTracks.value = audios
        _subtitleTracks.value = subtitles
    }

    private fun startProgressTicker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                if (exoPlayer.isPlaying) {
                    _currentPositionMs.value = exoPlayer.currentPosition.coerceAtLeast(0L)
                    _durationMs.value = exoPlayer.duration.coerceAtLeast(0L)
                }
                delay(1000L)
            }
        }
    }

    fun stopPlayback() {
        retryJob?.cancel()
        exoPlayer.stop()
        _currentChannel.value = null
    }

    fun release() {
        retryJob?.cancel()
        progressJob?.cancel()
        exoPlayer.release()
    }
}
