package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MockTest::class,
        MockAttempt::class,
        StudyMaterial::class,
        AppPreferences::class,
        AiGuidanceCache::class,
        ExamCountdown::class,
        StudyNote::class,
        StudySession::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SscDatabase : RoomDatabase() {
    abstract fun sscPrepDao(): SscPrepDao

    companion object {
        @Volatile
        private var INSTANCE: SscDatabase? = null

        fun getDatabase(context: Context): SscDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SscDatabase::class.java,
                    "ssc_prep_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
