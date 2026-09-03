package com.example.data.sync

import android.content.Context
import com.example.network.AttendanceShiftDto
import com.example.network.LeaveRequestDto
import com.example.network.NotificationDto
import com.example.network.ProfileDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Last-known-good snapshot of the worker's own read data (shifts, leave, profile,
 * notifications), so the app still shows real content when opened offline — not just
 * the in-flight pending-write queue. Never used as a source of truth once the server
 * is reachable again; every successful network read overwrites it immediately.
 */
class OfflineCache(context: Context) {
    private val dao = RealSyncDatabase.getInstance(context).cacheDao()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    private val shiftsAdapter = moshi.adapter<List<AttendanceShiftDto>>(
        Types.newParameterizedType(List::class.java, AttendanceShiftDto::class.java)
    )
    private val leaveAdapter = moshi.adapter<List<LeaveRequestDto>>(
        Types.newParameterizedType(List::class.java, LeaveRequestDto::class.java)
    )
    private val notificationsAdapter = moshi.adapter<List<NotificationDto>>(
        Types.newParameterizedType(List::class.java, NotificationDto::class.java)
    )
    private val profileAdapter = moshi.adapter(ProfileDto::class.java)

    suspend fun cacheShifts(employeeId: String, shifts: List<AttendanceShiftDto>) =
        dao.upsert(CachedJsonEntity(employeeId, KEY_SHIFTS, shiftsAdapter.toJson(shifts), System.currentTimeMillis()))

    suspend fun getCachedShifts(employeeId: String): List<AttendanceShiftDto>? =
        dao.get(employeeId, KEY_SHIFTS)?.let { runCatching { shiftsAdapter.fromJson(it.json) }.getOrNull() }

    suspend fun cacheLeave(employeeId: String, leave: List<LeaveRequestDto>) =
        dao.upsert(CachedJsonEntity(employeeId, KEY_LEAVE, leaveAdapter.toJson(leave), System.currentTimeMillis()))

    suspend fun getCachedLeave(employeeId: String): List<LeaveRequestDto>? =
        dao.get(employeeId, KEY_LEAVE)?.let { runCatching { leaveAdapter.fromJson(it.json) }.getOrNull() }

    suspend fun cacheNotifications(employeeId: String, notifications: List<NotificationDto>) =
        dao.upsert(CachedJsonEntity(employeeId, KEY_NOTIFICATIONS, notificationsAdapter.toJson(notifications), System.currentTimeMillis()))

    suspend fun getCachedNotifications(employeeId: String): List<NotificationDto>? =
        dao.get(employeeId, KEY_NOTIFICATIONS)?.let { runCatching { notificationsAdapter.fromJson(it.json) }.getOrNull() }

    suspend fun cacheProfile(employeeId: String, profile: ProfileDto) =
        dao.upsert(CachedJsonEntity(employeeId, KEY_PROFILE, profileAdapter.toJson(profile), System.currentTimeMillis()))

    suspend fun getCachedProfile(employeeId: String): ProfileDto? =
        dao.get(employeeId, KEY_PROFILE)?.let { runCatching { profileAdapter.fromJson(it.json) }.getOrNull() }

    private companion object {
        const val KEY_SHIFTS = "shifts"
        const val KEY_LEAVE = "leave"
        const val KEY_NOTIFICATIONS = "notifications"
        const val KEY_PROFILE = "profile"
    }
}
