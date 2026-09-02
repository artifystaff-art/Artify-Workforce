package com.example.data.sync

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface CacheDao {
    @Upsert
    suspend fun upsert(entity: CachedJsonEntity)

    @Query("SELECT * FROM cached_json WHERE employeeId = :employeeId AND dataKey = :dataKey")
    suspend fun get(employeeId: String, dataKey: String): CachedJsonEntity?
}
