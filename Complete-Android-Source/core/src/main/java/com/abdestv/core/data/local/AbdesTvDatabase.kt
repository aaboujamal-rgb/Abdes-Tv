package com.abdestv.core.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.abdestv.core.model.ChannelEntity
import com.abdestv.core.model.EpgProgramEntity
import com.abdestv.core.model.IptvProfileEntity
import com.abdestv.core.model.SeriesEntity
import com.abdestv.core.model.WatchHistoryEntity

@Database(
    entities = [
        IptvProfileEntity::class,
        ChannelEntity::class,
        SeriesEntity::class,
        WatchHistoryEntity::class,
        EpgProgramEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AbdesTvDatabase : RoomDatabase() {
    abstract fun dao(): AbdesTvDao

    companion object {
        @Volatile
        private var INSTANCE: AbdesTvDatabase? = null

        fun getInstance(context: Context): AbdesTvDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AbdesTvDatabase::class.java,
                    "abdes_tv_iptv.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
