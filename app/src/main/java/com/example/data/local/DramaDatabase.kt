package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.EpisodeDao
import com.example.data.local.dao.ReferralDao
import com.example.data.local.dao.SeriesDao
import com.example.data.local.dao.WatchHistoryDao
import com.example.data.local.entity.EpisodeEntity
import com.example.data.local.entity.ReferralEntity
import com.example.data.local.entity.ReferralRedemptionEntity
import com.example.data.local.entity.SeriesEntity
import com.example.data.local.entity.WatchHistoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SeriesEntity::class,
        EpisodeEntity::class,
        WatchHistoryEntity::class,
        ReferralEntity::class,
        ReferralRedemptionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class DramaDatabase : RoomDatabase() {

    abstract fun seriesDao(): SeriesDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun referralDao(): ReferralDao

    companion object {
        @Volatile
        private var INSTANCE: DramaDatabase? = null

        fun getInstance(context: Context): DramaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): DramaDatabase {
            val appContext = context.applicationContext
            return Room.databaseBuilder(
                appContext,
                DramaDatabase::class.java,
                "dramastream.db"
            ).fallbackToDestructiveMigration(true)
                .build().also { db ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedDatabaseIfEmpty(db, appContext.packageName)
                    }
                }
        }

        suspend fun seedDatabaseIfEmpty(db: DramaDatabase, packageName: String) {
            val seriesCount = db.seriesDao().getSeriesCount()
            if (seriesCount == 0) {
                db.seriesDao().insertSeriesList(SeedData.getInitialSeries())
                db.episodeDao().insertEpisodes(SeedData.getInitialEpisodes(packageName))
            }
        }
    }
}
