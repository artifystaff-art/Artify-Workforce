package com.example.data.sync

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Offline pending-write queue for the REAL (backend-authenticated) flow only.
 * Deliberately separate from the Demo Mode `AppDatabase` — this holds writes
 * awaiting the Artify Central Backend, not a self-contained fake dataset.
 */
@Database(
    entities = [PendingAttendanceEventEntity::class, PendingLeaveRequestEntity::class, CachedJsonEntity::class],
    version = 2,
    exportSchema = false
)
abstract class RealSyncDatabase : RoomDatabase() {
    abstract fun pendingSyncDao(): PendingSyncDao
    abstract fun cacheDao(): CacheDao

    companion object {
        @Volatile private var instance: RealSyncDatabase? = null

        // Adds the offline read-cache table only — must never touch the existing pending-write
        // queue tables, since they can hold real unsynced attendance/leave records.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `cached_json` (" +
                        "`employeeId` TEXT NOT NULL, `dataKey` TEXT NOT NULL, `json` TEXT NOT NULL, " +
                        "`cachedAtEpochMs` INTEGER NOT NULL, PRIMARY KEY(`employeeId`, `dataKey`))"
                )
            }
        }

        fun getInstance(context: Context): RealSyncDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context.applicationContext, RealSyncDatabase::class.java, "artify_real_sync.db")
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
    }
}
