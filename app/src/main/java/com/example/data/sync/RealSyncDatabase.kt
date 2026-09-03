package com.example.data.sync

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Offline pending-write queue for the REAL (backend-authenticated) flow only.
 * Deliberately separate from the Demo Mode `AppDatabase` — this holds writes
 * awaiting the Artify Central Backend, not a self-contained fake dataset.
 */
@Database(
    entities = [PendingAttendanceEventEntity::class, PendingLeaveRequestEntity::class, CachedJsonEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RealSyncDatabase : RoomDatabase() {
    abstract fun pendingSyncDao(): PendingSyncDao
    abstract fun cacheDao(): CacheDao

    companion object {
        @Volatile private var instance: RealSyncDatabase? = null

        // Renamed rather than migrated from the pre-cache-table (v1) database: this app is
        // still in active internal testing, test devices have been getting incremental APK
        // updates rather than clean reinstalls, and a hand-written migration that doesn't
        // exactly match what Room expects throws at first open — crashing the app the moment
        // a worker/supervisor dashboard is constructed after sign-in. A fresh file for the
        // current (v2) schema is created cleanly by Room with no migration involved at all,
        // at the one-time cost of any not-yet-synced queue items left in the old file.
        fun getInstance(context: Context): RealSyncDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context.applicationContext, RealSyncDatabase::class.java, "artify_real_sync_v2.db")
                    .build()
                    .also { instance = it }
            }
    }
}
