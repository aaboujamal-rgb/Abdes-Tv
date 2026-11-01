package com.abdestv.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.abdestv.core.model.ChannelEntity
import com.abdestv.core.model.EpgProgramEntity
import com.abdestv.core.model.IptvProfileEntity
import com.abdestv.core.model.SeriesEntity
import com.abdestv.core.model.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AbdesTvDao {

    @Query("SELECT * FROM iptv_profiles ORDER BY isActive DESC, createdAt DESC")
    fun observeProfiles(): Flow<List<IptvProfileEntity>>

    @Query("SELECT * FROM iptv_profiles WHERE isActive = 1 LIMIT 1")
    fun observeActiveProfile(): Flow<IptvProfileEntity?>

    @Query("SELECT * FROM iptv_profiles WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveProfileOnce(): IptvProfileEntity?

    @Query("SELECT * FROM iptv_profiles WHERE id = :profileId LIMIT 1")
    suspend fun getProfileById(profileId: Long): IptvProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: IptvProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: IptvProfileEntity)

    @Query("UPDATE iptv_profiles SET isActive = CASE WHEN id = :profileId THEN 1 ELSE 0 END")
    suspend fun setActiveProfile(profileId: Long)

    @Query("DELETE FROM iptv_profiles WHERE id = :profileId")
    suspend fun deleteProfile(profileId: Long)

    @Query("SELECT * FROM channels WHERE profileId = :profileId AND contentType = :contentType AND isHidden = 0 ORDER BY isFavorite DESC, name ASC")
    fun observeChannelsByType(profileId: Long, contentType: String): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE profileId = :profileId AND isHidden = 1 ORDER BY name ASC")
    fun observeHiddenChannels(profileId: Long): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE profileId = :profileId AND isFavorite = 1 AND isHidden = 0 ORDER BY name ASC")
    fun observeFavorites(profileId: Long): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE profileId = :profileId AND isHidden = 0 AND (name LIKE '%' || :query || '%' OR categoryName LIKE '%' || :query || '%') ORDER BY isFavorite DESC, name ASC LIMIT 250")
    fun searchChannels(profileId: Long, query: String): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE profileId = :profileId AND contentType = :contentType")
    suspend fun getExistingChannels(profileId: Long, contentType: String): List<ChannelEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)

    @Query("DELETE FROM channels WHERE profileId = :profileId AND contentType = :contentType")
    suspend fun deleteChannelsByType(profileId: Long, contentType: String)

    @Query("DELETE FROM channels WHERE profileId = :profileId")
    suspend fun deleteAllChannelsForProfile(profileId: Long)

    @Query("UPDATE channels SET isFavorite = :isFavorite WHERE id = :channelId AND profileId = :profileId")
    suspend fun setChannelFavorite(channelId: String, profileId: Long, isFavorite: Boolean)

    @Query("UPDATE channels SET isHidden = :isHidden WHERE id = :channelId AND profileId = :profileId")
    suspend fun setChannelHidden(channelId: String, profileId: Long, isHidden: Boolean)

    @Query("UPDATE channels SET isHidden = 0 WHERE profileId = :profileId")
    suspend fun unhideAllChannels(profileId: Long)

    @Query("SELECT * FROM series_items WHERE profileId = :profileId ORDER BY isFavorite DESC, name ASC")
    fun observeSeries(profileId: Long): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series_items WHERE profileId = :profileId AND (name LIKE '%' || :query || '%' OR categoryName LIKE '%' || :query || '%') ORDER BY name ASC LIMIT 150")
    fun searchSeries(profileId: Long, query: String): Flow<List<SeriesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(series: List<SeriesEntity>)

    @Query("DELETE FROM series_items WHERE profileId = :profileId")
    suspend fun deleteSeriesForProfile(profileId: Long)

    @Query("UPDATE series_items SET isFavorite = :isFavorite WHERE seriesId = :seriesId AND profileId = :profileId")
    suspend fun setSeriesFavorite(seriesId: Int, profileId: Long, isFavorite: Boolean)

    @Query("SELECT * FROM watch_history WHERE profileId = :profileId ORDER BY updatedAt DESC LIMIT 40")
    fun observeWatchHistory(profileId: Long): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWatchHistory(item: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE profileId = :profileId")
    suspend fun clearWatchHistory(profileId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpgPrograms(programs: List<EpgProgramEntity>)

    @Query("DELETE FROM epg_programs WHERE profileId = :profileId")
    suspend fun clearEpgForProfile(profileId: Long)

    @Query("SELECT * FROM epg_programs WHERE profileId = :profileId AND endTimeEpochMs > :nowMs AND startTimeEpochMs < :windowEndMs ORDER BY startTimeEpochMs ASC")
    fun observeCurrentAndUpcomingEpg(profileId: Long, nowMs: Long, windowEndMs: Long): Flow<List<EpgProgramEntity>>

    @Transaction
    suspend fun replaceChannelsPreservingFlags(
        profileId: Long,
        contentType: String,
        newChannels: List<ChannelEntity>
    ) {
        val existing = getExistingChannels(profileId, contentType).associateBy { it.id }
        val merged = newChannels.map { item ->
            val prev = existing[item.id]
            if (prev != null) {
                item.copy(isFavorite = prev.isFavorite, isHidden = prev.isHidden)
            } else {
                item
            }
        }
        deleteChannelsByType(profileId, contentType)
        insertChannels(merged)
    }
}
