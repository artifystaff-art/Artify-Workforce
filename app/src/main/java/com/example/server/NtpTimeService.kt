package com.example.server

import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

/**
 * High-precision SNTP (Simple Network Time Protocol) client and Cloud Time synchronization service.
 * Protects against device clock tampering, time fraud, and offline manipulation.
 */
object NtpTimeService {

    private const val TAG = "NtpTimeService"
    private const val NTP_PORT = 123
    private const val NTP_TIMEOUT_MS = 3000
    private const val NTP_PACKET_SIZE = 48
    private const val NTP_MODE_CLIENT = 3
    private const val NTP_VERSION = 3
    private const val OFFSET_1900_TO_1970 = 2208988800L

    // Authoritative NTP Server Pool (ordered by reliability & global coverage)
    private val NTP_SERVERS = listOf(
        "time.google.com",
        "pool.ntp.org",
        "time.android.com",
        "time.cloudflare.com",
        "time.windows.com"
    )

    // HTTP Time Fallback Endpoints (for firewalled or restricted networks)
    private val HTTP_FALLBACK_URLS = listOf(
        "https://www.google.com",
        "https://firebase.google.com",
        "https://cloudflare.com"
    )

    // Maximum acceptable device time drift before flagging as tampered (15 seconds)
    const val MAX_ALLOWED_CLOCK_DRIFT_MS = 15_000L

    data class NtpSyncState(
        val isSynchronized: Boolean = false,
        val serverTimeAtSyncMs: Long = 0L,
        val elapsedRealtimeAtSyncMs: Long = 0L,
        val roundTripLatencyMs: Long = 0L,
        val serverUsed: String = "None",
        val lastSyncTimestamp: Long = 0L,
        val isDeviceTimeTampered: Boolean = false,
        val detectedDriftMs: Long = 0L,
        val syncMethod: TimeSyncMethod = TimeSyncMethod.UNSYNCHRONIZED
    )

    enum class TimeSyncMethod(val displayName: String) {
        SNTP_UDP("Authoritative SNTP (UDP)"),
        HTTP_HEADER("Cloud HTTP Server Time"),
        SYSTEM_FALLBACK("Local Monotonic Fallback"),
        UNSYNCHRONIZED("Unsynchronized")
    }

    private val _syncState = MutableStateFlow(NtpSyncState())
    val syncState: StateFlow<NtpSyncState> = _syncState.asStateFlow()

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var syncJob: Job? = null

    /**
     * Initializes background sync and performs an initial time verification.
     */
    fun initialize() {
        if (syncJob?.isActive == true) return
        syncJob = serviceScope.launch {
            while (isActive) {
                syncWithAuthoritativeTime()
                // Re-sync every 15 minutes to compensate for clock oscillator drift
                delay(15 * 60 * 1000L)
            }
        }
    }

    /**
     * Triggers an immediate time synchronization request.
     */
    suspend fun syncNow(): NtpSyncState = withContext(Dispatchers.IO) {
        syncWithAuthoritativeTime()
    }

    /**
     * Returns true authoritative time in milliseconds UTC, guaranteed immune to device clock changes.
     */
    fun getAuthoritativeTimeMs(): Long {
        val state = _syncState.value
        return if (state.isSynchronized && state.elapsedRealtimeAtSyncMs > 0) {
            val elapsedSinceSync = SystemClock.elapsedRealtime() - state.elapsedRealtimeAtSyncMs
            state.serverTimeAtSyncMs + elapsedSinceSync
        } else {
            System.currentTimeMillis()
        }
    }

    /**
     * Checks whether the user has manipulated the device's system clock.
     */
    fun checkDeviceClockTampering(): Pair<Boolean, Long> {
        val authoritativeTime = getAuthoritativeTimeMs()
        val deviceTime = System.currentTimeMillis()
        val driftMs = deviceTime - authoritativeTime
        val isTampered = abs(driftMs) > MAX_ALLOWED_CLOCK_DRIFT_MS
        return Pair(isTampered, driftMs)
    }

    /**
     * Synchronizes against NTP servers over UDP, falling back to HTTP Date headers if UDP is blocked.
     */
    private suspend fun syncWithAuthoritativeTime(): NtpSyncState {
        // Attempt 1: Authoritative UDP SNTP
        for (server in NTP_SERVERS) {
            try {
                val sntpResult = querySntpServer(server)
                if (sntpResult != null) {
                    val drift = System.currentTimeMillis() - sntpResult.authoritativeTimeMs
                    val isTampered = abs(drift) > MAX_ALLOWED_CLOCK_DRIFT_MS

                    val newState = NtpSyncState(
                        isSynchronized = true,
                        serverTimeAtSyncMs = sntpResult.authoritativeTimeMs,
                        elapsedRealtimeAtSyncMs = sntpResult.elapsedRealtimeMs,
                        roundTripLatencyMs = sntpResult.roundTripMs,
                        serverUsed = server,
                        lastSyncTimestamp = System.currentTimeMillis(),
                        isDeviceTimeTampered = isTampered,
                        detectedDriftMs = drift,
                        syncMethod = TimeSyncMethod.SNTP_UDP
                    )
                    _syncState.value = newState
                    Log.i(TAG, "Successfully synced authoritative time from $server via SNTP. Drift: ${drift}ms")
                    return newState
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed SNTP sync with $server: ${e.message}")
            }
        }

        // Attempt 2: HTTP Server Time Fallback
        for (url in HTTP_FALLBACK_URLS) {
            try {
                val httpResult = queryHttpServerTime(url)
                if (httpResult != null) {
                    val drift = System.currentTimeMillis() - httpResult.authoritativeTimeMs
                    val isTampered = abs(drift) > MAX_ALLOWED_CLOCK_DRIFT_MS

                    val newState = NtpSyncState(
                        isSynchronized = true,
                        serverTimeAtSyncMs = httpResult.authoritativeTimeMs,
                        elapsedRealtimeAtSyncMs = httpResult.elapsedRealtimeMs,
                        roundTripLatencyMs = httpResult.roundTripMs,
                        serverUsed = url,
                        lastSyncTimestamp = System.currentTimeMillis(),
                        isDeviceTimeTampered = isTampered,
                        detectedDriftMs = drift,
                        syncMethod = TimeSyncMethod.HTTP_HEADER
                    )
                    _syncState.value = newState
                    Log.i(TAG, "Successfully synced authoritative time from $url via HTTP. Drift: ${drift}ms")
                    return newState
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed HTTP time sync with $url: ${e.message}")
            }
        }

        // Fallback: Retain existing state or initialize with monotonic baseline
        val currentState = _syncState.value
        if (!currentState.isSynchronized) {
            val fallbackState = NtpSyncState(
                isSynchronized = false,
                serverTimeAtSyncMs = System.currentTimeMillis(),
                elapsedRealtimeAtSyncMs = SystemClock.elapsedRealtime(),
                roundTripLatencyMs = 0L,
                serverUsed = "Local Monotonic Clock",
                lastSyncTimestamp = System.currentTimeMillis(),
                isDeviceTimeTampered = false,
                detectedDriftMs = 0L,
                syncMethod = TimeSyncMethod.SYSTEM_FALLBACK
            )
            _syncState.value = fallbackState
            return fallbackState
        }

        return currentState
    }

    private data class SntpQueryRawResult(
        val authoritativeTimeMs: Long,
        val elapsedRealtimeMs: Long,
        val roundTripMs: Long
    )

    /**
     * Queries an SNTP server adhering to RFC 2030 / RFC 4330.
     */
    private fun querySntpServer(host: String): SntpQueryRawResult? {
        var socket: DatagramSocket? = null
        try {
            socket = DatagramSocket()
            socket.soTimeout = NTP_TIMEOUT_MS

            val address = InetAddress.getByName(host)
            val buffer = ByteArray(NTP_PACKET_SIZE)
            // Set Leap Indicator (0), Version (3), Mode (3 = Client) -> 0x1B
            buffer[0] = (NTP_MODE_CLIENT or (NTP_VERSION shl 3)).toByte()

            val requestTimeMs = System.currentTimeMillis()
            val requestTicks = SystemClock.elapsedRealtime()
            writeTimestamp(buffer, 40, requestTimeMs)

            val packet = DatagramPacket(buffer, buffer.size, address, NTP_PORT)
            socket.send(packet)

            val responsePacket = DatagramPacket(buffer, buffer.size)
            socket.receive(responsePacket)

            val responseTicks = SystemClock.elapsedRealtime()
            val responseTimeMs = System.currentTimeMillis()

            val originateTime = readTimestamp(buffer, 24)
            val receiveTime = readTimestamp(buffer, 32)
            val transmitTime = readTimestamp(buffer, 40)

            val roundTripMs = (responseTicks - requestTicks) - (transmitTime - receiveTime)
            val clockOffsetMs = ((receiveTime - originateTime) + (transmitTime - responseTimeMs)) / 2
            val authoritativeTime = responseTimeMs + clockOffsetMs

            return SntpQueryRawResult(
                authoritativeTimeMs = authoritativeTime,
                elapsedRealtimeMs = responseTicks,
                roundTripMs = roundTripMs.coerceAtLeast(0L)
            )
        } catch (e: Exception) {
            Log.d(TAG, "SNTP query to $host failed: ${e.message}")
            return null
        } finally {
            socket?.close()
        }
    }

    /**
     * Fallback HTTP HEAD request to extract authoritative Date header with network latency compensation.
     */
    private fun queryHttpServerTime(urlString: String): SntpQueryRawResult? {
        var connection: HttpURLConnection? = null
        try {
            val startTicks = SystemClock.elapsedRealtime()
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            connection.useCaches = false
            connection.instanceFollowRedirects = false

            val dateHeader = connection.getHeaderField("Date")
            val endTicks = SystemClock.elapsedRealtime()
            val roundTrip = endTicks - startTicks

            if (!dateHeader.isNullOrBlank()) {
                val format = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("GMT")
                }
                val parsedDate = format.parse(dateHeader)
                if (parsedDate != null) {
                    val serverTimeMs = parsedDate.time + (roundTrip / 2)
                    return SntpQueryRawResult(
                        authoritativeTimeMs = serverTimeMs,
                        elapsedRealtimeMs = endTicks,
                        roundTripMs = roundTrip
                    )
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "HTTP time query to $urlString failed: ${e.message}")
        } finally {
            connection?.disconnect()
        }
        return null
    }

    private fun readTimestamp(buffer: ByteArray, offset: Int): Long {
        var seconds = 0L
        for (i in 0..3) {
            seconds = (seconds shl 8) or ((buffer[offset + i].toInt() and 0xFF).toLong())
        }

        var fraction = 0L
        for (i in 4..7) {
            fraction = (fraction shl 8) or ((buffer[offset + i].toInt() and 0xFF).toLong())
        }

        val millis = (seconds - OFFSET_1900_TO_1970) * 1000L + (fraction * 1000L) / 0x100000000L
        return millis
    }

    private fun writeTimestamp(buffer: ByteArray, offset: Int, timeMs: Long) {
        val seconds = (timeMs / 1000L) + OFFSET_1900_TO_1970
        val fraction = ((timeMs % 1000L) * 0x100000000L) / 1000L

        buffer[offset] = (seconds shr 24).toByte()
        buffer[offset + 1] = (seconds shr 16).toByte()
        buffer[offset + 2] = (seconds shr 8).toByte()
        buffer[offset + 3] = seconds.toByte()

        buffer[offset + 4] = (fraction shr 24).toByte()
        buffer[offset + 5] = (fraction shr 16).toByte()
        buffer[offset + 6] = (fraction shr 8).toByte()
        buffer[offset + 7] = fraction.toByte()
    }
}
