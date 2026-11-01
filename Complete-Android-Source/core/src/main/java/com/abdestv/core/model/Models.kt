package com.abdestv.core.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ConnectionType(val labelFr: String) {
    XTREAM_CODES("Xtream Codes API"),
    M3U_URL("Lien Playlist M3U / M3U8"),
    M3U_FILE("Fichier M3U Local")
}

enum class ContentType(val labelFr: String) {
    LIVE("TV en Direct"),
    MOVIE("Films"),
    SERIES("Séries")
}

enum class VideoAspectRatio(val labelFr: String) {
    FIT("Ajuster (16:9)"),
    FILL("Plein Écran (Étirer)"),
    ZOOM("Zoom Cinéma"),
    ORIGINAL("Format Original")
}

@Entity(tableName = "iptv_profiles")
data class IptvProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val profileName: String,
    val connectionType: String,
    val serverUrl: String = "",
    val username: String = "",
    val m3uUrl: String = "",
    val localFileUri: String = "",
    val epgUrl: String = "",
    val isActive: Boolean = false,
    val expirationTimestamp: Long? = null,
    val maxConnections: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long = 0L
)

@Entity(
    tableName = "channels",
    primaryKeys = ["id", "profileId"],
    indices = [
        Index(value = ["profileId", "contentType"]),
        Index(value = ["profileId", "categoryName"]),
        Index(value = ["profileId", "isFavorite"]),
        Index(value = ["profileId", "epgChannelId"])
    ]
)
data class ChannelEntity(
    val id: String,
    val profileId: Long,
    val streamId: Int = 0,
    val name: String,
    val categoryId: String = "all",
    val categoryName: String = "Général",
    val streamUrl: String,
    val fallbackStreamUrl: String = "",
    val logoUrl: String = "",
    val backdropUrl: String = "",
    val epgChannelId: String = "",
    val contentType: String = ContentType.LIVE.name,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val userAgent: String = "AbdesTV/2.0 (Android; Media3)",
    val containerExtension: String = "ts",
    val rating: String = "",
    val releaseYear: String = "",
    val plot: String = "",
    val durationSecs: Int = 0,
    val addedTimestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "series_items",
    primaryKeys = ["seriesId", "profileId"],
    indices = [Index(value = ["profileId", "categoryName"])]
)
data class SeriesEntity(
    val seriesId: Int,
    val profileId: Long,
    val name: String,
    val categoryId: String = "series",
    val categoryName: String = "Séries",
    val coverUrl: String = "",
    val backdropUrl: String = "",
    val plot: String = "",
    val cast: String = "",
    val director: String = "",
    val genre: String = "",
    val releaseDate: String = "",
    val rating: String = "",
    val isFavorite: Boolean = false
)

data class EpisodeItem(
    val id: String,
    val episodeNum: Int,
    val seasonNum: Int,
    val title: String,
    val containerExtension: String,
    val streamUrl: String,
    val plot: String = "",
    val durationSecs: Int = 0,
    val thumbnailUrl: String = ""
)

@Entity(
    tableName = "watch_history",
    primaryKeys = ["itemId", "profileId"],
    indices = [Index(value = ["profileId", "updatedAt"])]
)
data class WatchHistoryEntity(
    val itemId: String,
    val profileId: Long,
    val title: String,
    val categoryName: String,
    val streamUrl: String,
    val logoUrl: String,
    val contentType: String,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val progressFraction: Float
        get() = if (durationMs > 0L) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}

@Entity(
    tableName = "epg_programs",
    primaryKeys = ["id"],
    indices = [Index(value = ["profileId", "epgChannelId", "startTimeEpochMs"])]
)
data class EpgProgramEntity(
    val id: String,
    val profileId: Long,
    val epgChannelId: String,
    val title: String,
    val description: String = "",
    val startTimeEpochMs: Long,
    val endTimeEpochMs: Long,
    val category: String = ""
) {
    fun progressAt(nowMs: Long = System.currentTimeMillis()): Float {
        val total = (endTimeEpochMs - startTimeEpochMs).coerceAtLeast(1L)
        return ((nowMs - startTimeEpochMs).toFloat() / total.toFloat()).coerceIn(0f, 1f)
    }
}

data class AudioSubtitleTrack(
    val groupIndex: Int,
    val trackIndex: Int,
    val id: String,
    val label: String,
    val language: String,
    val isSelected: Boolean,
    val isAudio: Boolean
)

data class XtreamAccountStatus(
    val username: String,
    val status: String,
    val expDateEpochSec: Long?,
    val activeConnections: String,
    val maxConnections: String,
    val serverProtocol: String,
    val timezone: String
)
