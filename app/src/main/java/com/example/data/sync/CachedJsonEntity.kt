package com.example.data.sync

import androidx.room.Entity

/** A last-known-good snapshot of a read response, keyed per employee, so the app can show real data while offline instead of a blank screen. */
@Entity(tableName = "cached_json", primaryKeys = ["employeeId", "dataKey"])
data class CachedJsonEntity(
    val employeeId: String,
    val dataKey: String,
    val json: String,
    val cachedAtEpochMs: Long
)
