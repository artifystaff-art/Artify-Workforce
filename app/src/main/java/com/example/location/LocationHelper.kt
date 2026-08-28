package com.example.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

data class DeviceLocationSnapshot(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val isMock: Boolean,
    val isRealGps: Boolean
)

class LocationHelper(private val context: Context) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): DeviceLocationSnapshot? {
        return try {
            val tokenSource = CancellationTokenSource()
            val location: Location? = fusedClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                tokenSource.token
            ).await()

            if (location != null) {
                val isMock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    location.isMock
                } else {
                    @Suppress("DEPRECATION")
                    location.isFromMockProvider
                }
                DeviceLocationSnapshot(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy,
                    isMock = isMock,
                    isRealGps = true
                )
            } else {
                // Try last known location
                val lastLoc = fusedClient.lastLocation.await()
                if (lastLoc != null) {
                    val isMock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        lastLoc.isMock
                    } else {
                        @Suppress("DEPRECATION")
                        lastLoc.isFromMockProvider
                    }
                    DeviceLocationSnapshot(
                        latitude = lastLoc.latitude,
                        longitude = lastLoc.longitude,
                        accuracy = lastLoc.accuracy,
                        isMock = isMock,
                        isRealGps = true
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
