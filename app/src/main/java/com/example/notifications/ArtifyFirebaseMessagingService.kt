package com.example.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Service that receives Firebase Cloud Messaging push events and payloads.
 */
class ArtifyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a notification payload
        var title = remoteMessage.notification?.title
        var body = remoteMessage.notification?.body

        // Check if message contains data payload
        val data = remoteMessage.data
        if (data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: $data")
            if (title.isNullOrBlank()) {
                title = data["title"] ?: "Artify Workforce Alert"
            }
            if (body.isNullOrBlank()) {
                body = data["body"] ?: data["message"] ?: "New workforce notification received."
            }
        }

        if (title.isNullOrBlank()) {
            title = "Workforce Update"
        }
        if (body.isNullOrBlank()) {
            body = "You have an updated workforce notification."
        }

        val notificationId = data["notificationId"]?.hashCode()
            ?: (System.currentTimeMillis() % 100000).toInt()

        FcmNotificationManager.showSystemNotification(
            context = applicationContext,
            title = title,
            body = body,
            notificationId = notificationId,
            extraData = data
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM Token: $token")
    }

    companion object {
        private const val TAG = "ArtifyFcmService"
    }
}
