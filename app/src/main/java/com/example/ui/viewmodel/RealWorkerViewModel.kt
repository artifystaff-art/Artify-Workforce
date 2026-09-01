package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.BackendResult
import com.example.data.repository.BackendWorkforceRepository
import com.example.data.sync.RealSyncManager
import com.example.data.sync.SyncQueueStatus
import com.example.location.DeviceLocationSnapshot
import com.example.location.LocationHelper
import com.example.network.AttendanceEventSummary
import com.example.network.AttendanceShiftDto
import com.example.network.LeaveRequestDto
import com.example.network.NotificationDto
import com.example.network.ProfileDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.util.UUID

/** A shift id prefixed this way represents a queued, not-yet-synced local clock-in — never a real server row. */
const val LOCAL_PENDING_SHIFT_PREFIX = "local-pending-"

data class RealWorkerUiState(
    val activeShift: AttendanceShiftDto? = null,
    val shiftHistory: List<AttendanceShiftDto> = emptyList(),
    val leaveHistory: List<LeaveRequestDto> = emptyList(),
    val isProcessing: Boolean = false,
    val isLoading: Boolean = true,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val showStartShiftDialog: Boolean = false,
    val showEndShiftDialog: Boolean = false,
    val syncQueue: SyncQueueStatus = SyncQueueStatus(),
    val notifications: List<NotificationDto> = emptyList(),
    val profile: ProfileDto? = null,
    val selfieUrlCache: Map<String, String> = emptyMap(),
    val locationStatus: DeviceLocationSnapshot? = null,
    val isLocationLoading: Boolean = false
)

/** Drives the real (backend-authenticated) worker attendance/leave flow, with offline queueing. */
class RealWorkerViewModel(
    private val repository: BackendWorkforceRepository,
    private val locationHelper: LocationHelper,
    private val syncManager: RealSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RealWorkerUiState())
    val uiState: StateFlow<RealWorkerUiState> = _uiState.asStateFlow()

    /** True while a clock-in has been queued locally and hasn't synced yet — clock-out must also queue until it does. */
    private var pendingLocalClockIn = false

    init {
        syncManager.start(viewModelScope)
        viewModelScope.launch {
            syncManager.status.collect { status ->
                val wasPending = pendingLocalClockIn
                _uiState.value = _uiState.value.copy(syncQueue = status)
                if (status.pendingCount == 0 && status.failedCount == 0 && !status.isSyncing) {
                    pendingLocalClockIn = false
                    if (wasPending) refresh() // queue just drained — replace the local placeholder with server truth
                }
            }
        }
        viewModelScope.launch {
            syncManager.refreshCounts()
            val queuedOpenClockIn = syncManager.findQueuedOpenClockIn()
            if (queuedOpenClockIn != null) {
                pendingLocalClockIn = true
                _uiState.value = _uiState.value.copy(activeShift = localPendingShift(queuedOpenClockIn.clientEventId), isLoading = false)
            } else {
                refresh()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            if (!pendingLocalClockIn) {
                when (val result = repository.myShifts()) {
                    is BackendResult.Success -> {
                        val active = result.value.firstOrNull { it.status == "OPEN" }
                        val history = result.value.filter { it.status != "OPEN" }
                        _uiState.value = _uiState.value.copy(activeShift = active, shiftHistory = history, isLoading = false)
                    }
                    is BackendResult.Failure -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
            when (val result = repository.myLeaveRequests()) {
                is BackendResult.Success -> _uiState.value = _uiState.value.copy(leaveHistory = result.value)
                is BackendResult.Failure -> { /* keep prior leave list; attendance error already surfaced */ }
            }
            when (val result = repository.myNotifications()) {
                is BackendResult.Success -> _uiState.value = _uiState.value.copy(notifications = result.value)
                is BackendResult.Failure -> { /* keep prior notifications */ }
            }
            if (_uiState.value.profile == null) {
                when (val result = repository.myProfile()) {
                    is BackendResult.Success -> _uiState.value = _uiState.value.copy(profile = result.value)
                    is BackendResult.Failure -> { /* profile stays null; screen shows fallback text */ }
                }
            }
        }
    }

    /** Advisory-only: shows the worker their live GPS status. Never gates clock-in/out — the server remains authoritative. */
    fun refreshLocationStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLocationLoading = true)
            val location = locationHelper.getCurrentLocation()
            _uiState.value = _uiState.value.copy(locationStatus = location, isLocationLoading = false)
        }
    }

    fun setStartShiftDialog(show: Boolean) { _uiState.value = _uiState.value.copy(showStartShiftDialog = show) }
    fun setEndShiftDialog(show: Boolean) { _uiState.value = _uiState.value.copy(showEndShiftDialog = show) }
    fun clearFeedback() { _uiState.value = _uiState.value.copy(statusMessage = null, errorMessage = null) }
    fun syncNow() { viewModelScope.launch { syncManager.syncNow(force = true) } }

    /** Lazily resolves and caches a signed URL for a selfie evidence path. */
    fun loadSelfieUrl(storagePath: String) {
        if (_uiState.value.selfieUrlCache.containsKey(storagePath)) return
        viewModelScope.launch {
            when (val result = repository.getMySelfieUrl(storagePath)) {
                is BackendResult.Success -> _uiState.value = _uiState.value.copy(
                    selfieUrlCache = _uiState.value.selfieUrlCache + (storagePath to result.value)
                )
                is BackendResult.Failure -> { /* leave unresolved; UI shows a placeholder */ }
            }
        }
    }

    fun startShift(selfieFilePath: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, errorMessage = null, showStartShiftDialog = false)
            val location = locationHelper.getCurrentLocation()
            val selfieBase64 = encodeSelfie(selfieFilePath)
            val clientEventId = UUID.randomUUID().toString()
            val deviceTimestamp = Instant.now().toString()

            val result = repository.clockIn(
                clientEventId = clientEventId, deviceTimestamp = deviceTimestamp,
                latitude = location?.latitude, longitude = location?.longitude,
                accuracy = location?.accuracy, isMockLocation = location?.isMock ?: false, selfieBase64 = selfieBase64
            )
            when (result) {
                is BackendResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false, activeShift = result.value.shift,
                        statusMessage = complianceMessage("Shift started", result.value.geofenceStatus, result.value.distanceMeters)
                    )
                }
                is BackendResult.Failure -> {
                    if (result.isNetworkError) {
                        syncManager.queueClockEvent(clientEventId, "clock_in", deviceTimestamp, location?.latitude, location?.longitude, location?.accuracy, location?.isMock ?: false, selfieFilePath)
                        pendingLocalClockIn = true
                        _uiState.value = _uiState.value.copy(
                            isProcessing = false,
                            activeShift = localPendingShift(clientEventId),
                            statusMessage = "You're offline — shift queued and will sync automatically once connected."
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isProcessing = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun endShift(selfieFilePath: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, errorMessage = null, showEndShiftDialog = false)
            val location = locationHelper.getCurrentLocation()
            val selfieBase64 = encodeSelfie(selfieFilePath)
            val clientEventId = UUID.randomUUID().toString()
            val deviceTimestamp = Instant.now().toString()

            val result = repository.clockOut(
                clientEventId = clientEventId, deviceTimestamp = deviceTimestamp,
                latitude = location?.latitude, longitude = location?.longitude,
                accuracy = location?.accuracy, isMockLocation = location?.isMock ?: false, selfieBase64 = selfieBase64
            )
            when (result) {
                is BackendResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false, activeShift = null,
                        statusMessage = complianceMessage("Shift ended — submitted for approval", result.value.geofenceStatus, result.value.distanceMeters)
                    )
                    refresh()
                }
                is BackendResult.Failure -> {
                    if (result.isNetworkError) {
                        syncManager.queueClockEvent(clientEventId, "clock_out", deviceTimestamp, location?.latitude, location?.longitude, location?.accuracy, location?.isMock ?: false, selfieFilePath)
                        _uiState.value = _uiState.value.copy(
                            isProcessing = false, activeShift = null,
                            statusMessage = "You're offline — clock-out queued and will sync automatically once connected."
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isProcessing = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun submitLeave(leaveType: String, startDate: String, endDate: String, reason: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, errorMessage = null)
            val clientRequestId = UUID.randomUUID().toString()
            when (val result = repository.submitLeave(clientRequestId, leaveType, startDate, endDate, reason)) {
                is BackendResult.Success -> {
                    _uiState.value = _uiState.value.copy(isProcessing = false, statusMessage = "Leave request submitted.")
                    refresh()
                }
                is BackendResult.Failure -> {
                    if (result.isNetworkError) {
                        syncManager.queueLeaveRequest(clientRequestId, leaveType, startDate, endDate, reason)
                        _uiState.value = _uiState.value.copy(isProcessing = false, statusMessage = "You're offline — leave request queued and will submit automatically once connected.")
                    } else {
                        _uiState.value = _uiState.value.copy(isProcessing = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    private fun localPendingShift(clientEventId: String): AttendanceShiftDto = AttendanceShiftDto(
        id = LOCAL_PENDING_SHIFT_PREFIX + clientEventId,
        employeeId = "", projectId = "", shiftDate = Instant.now().toString().take(10),
        clockInEventId = clientEventId, status = "OPEN", complianceFlag = "COMPLIANT",
        clockIn = AttendanceEventSummary(serverTimestamp = null, geofenceStatus = null, distanceFromProjectMeters = null)
    )

    /** The clock-in/out result is never blocked by compliance, but the worker should still see the flag. */
    private fun complianceMessage(prefix: String, geofenceStatus: String?, distanceMeters: Double?): String {
        return when (geofenceStatus) {
            "INSIDE_GEOFENCE", null -> "$prefix — on-site, compliant."
            "OUTSIDE_GEOFENCE" -> "$prefix — flagged: outside site radius (${distanceMeters?.toInt() ?: "?"}m away)."
            "MOCK_LOCATION_DETECTED" -> "$prefix — flagged: mock/spoofed location detected."
            "ACCURACY_TOO_LOW" -> "$prefix — flagged: GPS accuracy too low."
            "GPS_UNAVAILABLE" -> "$prefix — flagged: GPS was unavailable."
            else -> "$prefix — flagged for review."
        }
    }

    private fun encodeSelfie(filePath: String): String? {
        return try {
            val bytes = File(filePath).readBytes()
            android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }
}
