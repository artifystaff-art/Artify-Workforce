package com.example.server

import com.example.data.entity.ProjectEntity
import com.example.model.GeofenceStatus
import com.example.model.VerificationStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.*

object LocationUtils {
    private const val EARTH_RADIUS_METERS = 6371000.0

    /**
     * Calculates the great-circle distance between two GPS coordinates using the Haversine formula.
     */
    fun calculateDistanceMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }
}

data class ServerGeofenceResult(
    val status: GeofenceStatus,
    val distanceMeters: Double,
    val isInside: Boolean,
    val isAccuracyAcceptable: Boolean,
    val verificationStatus: VerificationStatus,
    val flagReason: String? = null
)

data class ServerTimestampResult(
    val timestampUtc: Long,
    val displayFormatted: String,
    val dateString: String,
    val timezone: String = "UTC+4 (Gulf Standard Time)",
    val isNtpSynchronized: Boolean = false,
    val isDeviceTimeTampered: Boolean = false,
    val timeSource: String = "NTP / Authoritative Time",
    val clockDriftMs: Long = 0L
)

object ServerAuthorityEngine {

    // Network / Server simulation offset (simulating authoritative atomic server time)
    private var simulatedServerTimeOffsetMillis: Long = 0L

    fun getServerTimestamp(): ServerTimestampResult {
        val ntpSyncState = NtpTimeService.syncState.value
        val (isTampered, driftMs) = NtpTimeService.checkDeviceClockTampering()
        val serverTimeUtc = NtpTimeService.getAuthoritativeTimeMs() + simulatedServerTimeOffsetMillis
        val date = Date(serverTimeUtc)

        val displayFormat = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.ENGLISH).apply {
            timeZone = TimeZone.getTimeZone("GMT+4")
        }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).apply {
            timeZone = TimeZone.getTimeZone("GMT+4")
        }

        return ServerTimestampResult(
            timestampUtc = serverTimeUtc,
            displayFormatted = displayFormat.format(date),
            dateString = dateFormat.format(date),
            isNtpSynchronized = ntpSyncState.isSynchronized,
            isDeviceTimeTampered = isTampered,
            timeSource = if (ntpSyncState.isSynchronized) ntpSyncState.serverUsed else ntpSyncState.syncMethod.displayName,
            clockDriftMs = driftMs
        )
    }

    /**
     * Authoritative server-side geofence calculation.
     * The mobile client can never override or forge this validation.
     */
    fun evaluateGeofence(
        latitude: Double?,
        longitude: Double?,
        accuracyMeters: Float?,
        isMockLocation: Boolean,
        project: ProjectEntity
    ): ServerGeofenceResult {
        if (latitude == null || longitude == null) {
            return ServerGeofenceResult(
                status = GeofenceStatus.GPS_UNAVAILABLE,
                distanceMeters = -1.0,
                isInside = false,
                isAccuracyAcceptable = false,
                verificationStatus = VerificationStatus.REJECTED,
                flagReason = "GPS signal unavailable or location permissions disabled."
            )
        }

        if (isMockLocation) {
            val dist = LocationUtils.calculateDistanceMeters(
                latitude,
                longitude,
                project.latitude,
                project.longitude
            )
            return ServerGeofenceResult(
                status = GeofenceStatus.MOCK_LOCATION_DETECTED,
                distanceMeters = dist,
                isInside = false,
                isAccuracyAcceptable = false,
                verificationStatus = VerificationStatus.FLAGGED,
                flagReason = "Mock/Spoofed location provider detected on client device."
            )
        }

        val accuracy = accuracyMeters ?: Float.MAX_VALUE
        val isAccuracyAcceptable = accuracy <= project.maxGpsAccuracyMeters

        val distance = LocationUtils.calculateDistanceMeters(
            latitude,
            longitude,
            project.latitude,
            project.longitude
        )

        val isInsideRadius = distance <= project.geofenceRadiusMeters

        return when {
            !isAccuracyAcceptable -> {
                ServerGeofenceResult(
                    status = GeofenceStatus.ACCURACY_TOO_LOW,
                    distanceMeters = distance,
                    isInside = isInsideRadius,
                    isAccuracyAcceptable = false,
                    verificationStatus = VerificationStatus.FLAGGED,
                    flagReason = "GPS accuracy ($accuracy m) is above acceptable limit (${project.maxGpsAccuracyMeters.toInt()} m)."
                )
            }
            isInsideRadius -> {
                ServerGeofenceResult(
                    status = GeofenceStatus.INSIDE_GEOFENCE,
                    distanceMeters = distance,
                    isInside = true,
                    isAccuracyAcceptable = true,
                    verificationStatus = VerificationStatus.VERIFIED
                )
            }
            else -> {
                ServerGeofenceResult(
                    status = GeofenceStatus.OUTSIDE_GEOFENCE,
                    distanceMeters = distance,
                    isInside = false,
                    isAccuracyAcceptable = true,
                    verificationStatus = VerificationStatus.REJECTED,
                    flagReason = "Employee is ${(distance - project.geofenceRadiusMeters).toInt()} m outside approved geofence perimeter."
                )
            }
        }
    }

    /**
     * Generates an idempotent key for company ERP synchronization.
     */
    fun generateErpIdempotencyKey(
        companyId: String,
        employeeId: String,
        attendanceId: String
    ): String {
        return "${companyId}_${employeeId}_${attendanceId}"
    }
}
