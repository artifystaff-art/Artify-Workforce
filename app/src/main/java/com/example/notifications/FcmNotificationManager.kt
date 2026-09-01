package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.entity.LeaveRequestEntity
import com.example.data.entity.NotificationEntity
import com.example.data.entity.UserEntity
import com.example.model.LeaveStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

/**
 * Enterprise FCM Push Notification Manager.
 * Orchestrates Firebase Cloud Messaging topic subscriptions, device tokens,
 * push dispatch to employees on leave decisions, and system tray notifications.
 */
object FcmNotificationManager {

    private const val TAG = "FcmNotificationMgr"
    const val CHANNEL_ID_WORKFORCE = "workforce_notifications_channel"
    const val CHANNEL_NAME_WORKFORCE = "Workforce & Leave Alerts"
    const val CHANNEL_DESC_WORKFORCE = "Real-time automated alerts for leave approvals, shift changes, and workforce updates."

    const val CHANNEL_ID_SUPERVISOR_ALERTS = "supervisor_leave_alerts_channel"
    const val CHANNEL_NAME_SUPERVISOR_ALERTS = "Supervisor Action Required (Leave Requests)"
    const val CHANNEL_DESC_SUPERVISOR_ALERTS = "High-priority instant alerts notifying supervisors when team members submit new leave requests."

    private val firestore by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Firestore not available in current environment: ${e.message}")
            null
        }
    }

    /**
     * Initializes Android Notification Channels (API 26+).
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Standard Workforce Channel
            val workforceChannel = NotificationChannel(
                CHANNEL_ID_WORKFORCE,
                CHANNEL_NAME_WORKFORCE,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC_WORKFORCE
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(workforceChannel)

            // High-Priority Supervisor Alerts Channel
            val supervisorChannel = NotificationChannel(
                CHANNEL_ID_SUPERVISOR_ALERTS,
                CHANNEL_NAME_SUPERVISOR_ALERTS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC_SUPERVISOR_ALERTS
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
            }
            notificationManager.createNotificationChannel(supervisorChannel)

            Log.d(TAG, "Notification channels registered: $CHANNEL_ID_WORKFORCE, $CHANNEL_ID_SUPERVISOR_ALERTS")
        }
    }

    /**
     * Registers current FCM token and subscribes the employee to their dedicated notification topic.
     */
    fun registerUserForPushNotifications(context: Context, user: UserEntity) {
        createNotificationChannels(context)
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }

                val token = task.result
                Log.i(TAG, "FCM Device Token for ${user.fullName} (${user.employeeId}): $token")

                // Save token to Firestore employee profile for direct targeting
                saveFcmTokenToFirestore(user.employeeId, token)
            }

            // Subscribe to personal employee topic (e.g., employee_EMP001)
            val userTopic = "employee_${user.employeeId.lowercase()}"
            FirebaseMessaging.getInstance().subscribeToTopic(userTopic).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Subscribed to FCM topic: $userTopic")
                }
            }

            // If user is supervisor, also subscribe to supervisors topic
            if (user.role == "SUPERVISOR") {
                FirebaseMessaging.getInstance().subscribeToTopic("all_supervisors").addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Subscribed to FCM topic: all_supervisors")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register FCM topics: ${e.message}")
        }
    }

    /**
     * Dispatches an automated push notification to the employee when their attendance record is reviewed/approved by a supervisor.
     */
    fun dispatchAttendanceApprovalPushNotification(
        context: Context,
        attendance: com.example.data.entity.AttendanceEntity,
        supervisor: UserEntity,
        isApproved: Boolean,
        commentOrReason: String? = null
    ) {
        val title = if (isApproved) "✅ Shift Attendance Approved" else "❌ Shift Attendance Rejected"
        val statusText = if (isApproved) "APPROVED" else "REJECTED"
        val body = if (isApproved) {
            "Your shift attendance on ${attendance.shiftDate} (${attendance.totalWorkedMinutes} mins) was approved by ${supervisor.fullName}." +
                (if (!commentOrReason.isNullOrBlank()) " Note: $commentOrReason" else "")
        } else {
            "Your shift attendance on ${attendance.shiftDate} was rejected by ${supervisor.fullName}. Reason: ${commentOrReason ?: "Not specified"}"
        }

        val notificationId = "NOTIF-PUSH-" + UUID.randomUUID().toString().take(8)

        // 1. Post to Firestore collection fcm_notifications for server-side push distribution & audit
        val payload = hashMapOf(
            "notificationId" to notificationId,
            "recipientId" to attendance.employeeId,
            "employeeName" to attendance.employeeName,
            "title" to title,
            "body" to body,
            "type" to "ATTENDANCE",
            "status" to statusText,
            "attendanceId" to attendance.attendanceId,
            "shiftDate" to attendance.shiftDate,
            "supervisorId" to supervisor.employeeId,
            "supervisorName" to supervisor.fullName,
            "timestampUtc" to System.currentTimeMillis(),
            "fcmTopic" to "employee_${attendance.employeeId.lowercase()}",
            "delivered" to true
        )

        try {
            firestore?.collection("fcm_notifications")
                ?.document(notificationId)
                ?.set(payload, SetOptions.merge())
                ?.addOnSuccessListener {
                    Log.d(TAG, "FCM attendance notification record dispatched to Firestore: $notificationId")
                }
                ?.addOnFailureListener { e ->
                    Log.w(TAG, "Error storing FCM attendance record to Firestore: ${e.message}")
                }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore FCM dispatch skipped: ${e.message}")
        }

        // 2. Display immediate high-priority Heads-up Notification on device
        showSystemNotification(
            context = context,
            title = title,
            body = body,
            notificationId = attendance.attendanceId.hashCode(),
            extraData = mapOf(
                "attendanceId" to attendance.attendanceId,
                "type" to "ATTENDANCE",
                "status" to statusText,
                "employeeId" to attendance.employeeId
            )
        )
    }

    /**
     * Dispatches an automated push notification to the employee when their leave request is reviewed.
     */
    fun dispatchLeaveStatusPushNotification(
        context: Context,
        leave: LeaveRequestEntity,
        supervisor: UserEntity,
        isApproved: Boolean,
        reason: String? = null
    ) {
        val title = if (isApproved) "✅ Leave Request Approved" else "❌ Leave Request Rejected"
        val statusText = if (isApproved) "APPROVED" else "REJECTED"
        val body = if (isApproved) {
            "Your ${leave.type} request (${leave.startDate} to ${leave.endDate}) has been approved by ${supervisor.fullName}."
        } else {
            "Your ${leave.type} request was rejected by ${supervisor.fullName}. Reason: ${reason ?: "Not specified"}"
        }

        val notificationId = "NOTIF-PUSH-" + UUID.randomUUID().toString().take(8)

        // 1. Post to Firestore for Cloud Messaging backend triggers & persistent sync
        val payload = hashMapOf(
            "notificationId" to notificationId,
            "recipientId" to leave.employeeId,
            "employeeName" to leave.employeeName,
            "title" to title,
            "body" to body,
            "type" to "LEAVE",
            "status" to statusText,
            "requestId" to leave.requestId,
            "startDate" to leave.startDate,
            "endDate" to leave.endDate,
            "supervisorId" to supervisor.employeeId,
            "supervisorName" to supervisor.fullName,
            "timestampUtc" to System.currentTimeMillis(),
            "fcmTopic" to "employee_${leave.employeeId.lowercase()}",
            "delivered" to true
        )

        try {
            firestore?.collection("fcm_notifications")
                ?.document(notificationId)
                ?.set(payload, SetOptions.merge())
                ?.addOnSuccessListener {
                    Log.d(TAG, "FCM leave notification record dispatched to Firestore: $notificationId")
                }
                ?.addOnFailureListener { e ->
                    Log.w(TAG, "Error storing FCM record to Firestore: ${e.message}")
                }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore FCM dispatch skipped: ${e.message}")
        }

        // 2. Display immediate high-priority Heads-up Notification on device
        showSystemNotification(
            context = context,
            title = title,
            body = body,
            notificationId = leave.requestId.hashCode(),
            channelId = CHANNEL_ID_WORKFORCE,
            extraData = mapOf(
                "requestId" to leave.requestId,
                "type" to "LEAVE",
                "status" to statusText
            )
        )
    }

    /**
     * Dispatches an automated push notification to ALL SUPERVISORS when a worker submits a new leave request.
     */
    fun dispatchNewLeaveRequestAlertToSupervisors(
        context: Context,
        leave: LeaveRequestEntity,
        employee: UserEntity
    ) {
        val title = "🔔 New Leave Request: ${employee.fullName}"
        val body = "${employee.fullName} (${employee.employeeId}) requested ${leave.totalDays} day(s) of ${leave.type} (${leave.startDate} to ${leave.endDate})." +
            if (leave.reason.isNotBlank()) " Reason: ${leave.reason}" else ""

        val notificationId = "NOTIF-SUP-LV-" + UUID.randomUUID().toString().take(8)

        // 1. Post to Firestore for Cloud Messaging backend triggers targeting 'all_supervisors' topic
        val payload = hashMapOf(
            "notificationId" to notificationId,
            "recipientId" to "ALL_SUPERVISORS",
            "employeeId" to employee.employeeId,
            "employeeName" to employee.fullName,
            "title" to title,
            "body" to body,
            "type" to "LEAVE_REQUEST",
            "status" to "PENDING",
            "requestId" to leave.requestId,
            "leaveType" to leave.type,
            "startDate" to leave.startDate,
            "endDate" to leave.endDate,
            "totalDays" to leave.totalDays,
            "reason" to leave.reason,
            "timestampUtc" to System.currentTimeMillis(),
            "fcmTopic" to "all_supervisors",
            "priority" to "HIGH",
            "delivered" to true
        )

        try {
            firestore?.collection("fcm_notifications")
                ?.document(notificationId)
                ?.set(payload, SetOptions.merge())
                ?.addOnSuccessListener {
                    Log.d(TAG, "Supervisor FCM leave notification dispatched to Firestore topic 'all_supervisors': $notificationId")
                }
                ?.addOnFailureListener { e ->
                    Log.w(TAG, "Error storing Supervisor FCM record to Firestore: ${e.message}")
                }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore supervisor FCM dispatch skipped: ${e.message}")
        }

        // 2. Display immediate high-priority Heads-up Notification on device targeting supervisor leave channel
        showSystemNotification(
            context = context,
            title = title,
            body = body,
            notificationId = leave.requestId.hashCode(),
            channelId = CHANNEL_ID_SUPERVISOR_ALERTS,
            extraData = mapOf(
                "requestId" to leave.requestId,
                "type" to "LEAVE_REQUEST",
                "employeeId" to employee.employeeId,
                "employeeName" to employee.fullName,
                "target_screen" to "SUPERVISOR_LEAVE"
            )
        )
    }

    /**
     * Displays a rich Android system notification in the status bar and notification drawer.
     */
    fun showSystemNotification(
        context: Context,
        title: String,
        body: String,
        notificationId: Int = (System.currentTimeMillis() % 100000).toInt(),
        channelId: String = CHANNEL_ID_WORKFORCE,
        extraData: Map<String, String> = emptyMap()
    ) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            extraData.forEach { (key, value) -> putExtra(key, value) }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, notificationBuilder.build())
            Log.d(TAG, "System notification posted: $title")
        } catch (e: SecurityException) {
            Log.w(TAG, "POST_NOTIFICATIONS permission not granted: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to display system notification: ${e.message}", e)
        }
    }

    private fun saveFcmTokenToFirestore(employeeId: String, token: String) {
        try {
            val tokenData = hashMapOf(
                "fcmToken" to token,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore?.collection("employee_tokens")
                ?.document(employeeId)
                ?.set(tokenData, SetOptions.merge())
                ?.addOnSuccessListener {
                    Log.d(TAG, "FCM token saved for employee $employeeId")
                }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to save FCM token to Firestore: ${e.message}")
        }
    }
}
