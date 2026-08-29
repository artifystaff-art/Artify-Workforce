package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val employeeId: String,
    val role: String, // WORKER, STAFF, SUPERVISOR
    val fullName: String,
    val phone: String,
    val email: String,
    val passwordHash: String,
    val companyId: String,
    val companyName: String,
    val assignedProjectId: String,
    val department: String,
    val status: String = "ACTIVE",
    val avatarUrl: String = "",
    val createdAtUtc: Long
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val projectId: String,
    val projectName: String,
    val code: String,
    val latitude: Double,
    val longitude: Double,
    val geofenceRadiusMeters: Double = 150.0,
    val maxGpsAccuracyMeters: Double = 100.0,
    val address: String,
    val status: String = "ACTIVE",
    val companyId: String
)

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey val attendanceId: String,
    val employeeId: String,
    val employeeName: String,
    val employeeRole: String,
    val projectId: String,
    val projectName: String,
    val shiftDate: String, // YYYY-MM-DD
    val startTimeUtc: Long?,
    val startTimeFormatted: String?,
    val endTimeUtc: Long?,
    val endTimeFormatted: String?,
    val totalWorkedMinutes: Int = 0,
    val startSelfieData: String? = null,
    val endSelfieData: String? = null,
    val startLatitude: Double? = null,
    val startLongitude: Double? = null,
    val startAccuracy: Float? = null,
    val startGeofenceStatus: String? = null,
    val startDistanceFromProjectMeters: Double? = null,
    val isStartMockLocation: Boolean = false,
    val endLatitude: Double? = null,
    val endLongitude: Double? = null,
    val endAccuracy: Float? = null,
    val endGeofenceStatus: String? = null,
    val endDistanceFromProjectMeters: Double? = null,
    val isEndMockLocation: Boolean = false,
    val state: String, // AttendanceState
    val verificationStatus: String, // VerificationStatus
    val deviceId: String = "ANDROID-DEV-101",
    val appVersion: String = "1.0.0",
    val supervisorComment: String? = null,
    val rejectionReason: String? = null,
    val reviewedBySupervisorId: String? = null,
    val reviewedAtUtc: Long? = null,
    val syncedToErp: Boolean = false,
    val erpIdempotencyKey: String? = null,
    val syncedToFirestore: Boolean = false,
    val firestoreSyncStatus: String = "QUEUED_OFFLINE", // QUEUED_OFFLINE, SYNCING, SYNCED, FAILED
    val firestoreSyncedAtUtc: Long? = null,
    val createdAtUtc: Long
)

@Entity(tableName = "leave_requests")
data class LeaveRequestEntity(
    @PrimaryKey val requestId: String,
    val employeeId: String,
    val employeeName: String,
    val employeeRole: String,
    val type: String, // LeaveType
    val startDate: String, // YYYY-MM-DD
    val endDate: String, // YYYY-MM-DD
    val totalDays: Int,
    val reason: String,
    val status: String, // LeaveStatus: PENDING, APPROVED, REJECTED
    val submittedAtUtc: Long,
    val approvedAtUtc: Long? = null,
    val approvedBy: String? = null,
    val rejectionReason: String? = null
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val auditId: String,
    val actorId: String,
    val actorName: String,
    val actorRole: String,
    val action: String, // AuditAction
    val entityType: String,
    val entityId: String,
    val serverTimestampUtc: Long,
    val details: String,
    val deviceId: String = "ANDROID-DEV-101"
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val notificationId: String,
    val recipientId: String, // employeeId or ALL_SUPERVISORS or ALL
    val title: String,
    val message: String,
    val type: String, // ATTENDANCE, LEAVE, SHIFT, SYSTEM
    val timestampUtc: Long,
    val isRead: Boolean = false
)

@Entity(tableName = "erp_outbox")
data class ErpOutboxEntity(
    @PrimaryKey val idempotencyKey: String,
    val eventId: String,
    val eventType: String,
    val payloadJson: String,
    val status: String = "PENDING", // PENDING, SYNCED, FAILED
    val attempts: Int = 0,
    val createdAtUtc: Long,
    val lastAttemptUtc: Long? = null,
    val erpResponseRef: String? = null
)
