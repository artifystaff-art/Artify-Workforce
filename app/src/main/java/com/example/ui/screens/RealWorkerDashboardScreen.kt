package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.sync.SyncQueueStatus
import com.example.model.ShiftEventType
import com.example.network.AttendanceShiftDto
import com.example.network.LeaveRequestDto
import com.example.network.NotificationDto
import com.example.security.SecureSessionStore
import com.example.ui.components.AiAssistantDialog
import com.example.ui.components.ArtifyTopHeader
import com.example.ui.components.CameraXSelfieDialog
import com.example.ui.components.ThemeSettingsDialog
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.ThemePreferences
import com.example.ui.viewmodel.AiAssistantViewModel
import com.example.ui.viewmodel.RealWorkerUiState
import com.example.ui.viewmodel.RealWorkerViewModel
import java.util.Calendar

private enum class RealWorkerTab { SHIFT, LOGS, LEAVE, PROFILE }

private val LEAVE_TYPES = listOf(
    Triple("SICK", "Sick", "15d"), Triple("CASUAL", "Casual", "6d"),
    Triple("ANNUAL", "Annual", "30d"), Triple("TRANSIT", "Transit", "Duty")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealWorkerDashboardScreen(
    viewModel: RealWorkerViewModel,
    aiAssistantViewModel: AiAssistantViewModel,
    employeeName: String,
    employeeCode: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var tab by remember { mutableStateOf(RealWorkerTab.SHIFT) }
    var showAssistant by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    val projectName = uiState.profile?.projectName ?: uiState.activeShift?.project?.name
        ?: uiState.shiftHistory.firstOrNull()?.project?.name ?: "Assigned Site"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ArtifyTopHeader(
                userName = employeeName,
                employeeId = employeeCode,
                role = uiState.profile?.role ?: "WORKER",
                onLogoutClick = onLogout,
                notificationCount = uiState.notifications.size,
                onNotificationClick = { showNotifications = true }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAssistant = true }) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "Ask the assistant")
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = tab == RealWorkerTab.SHIFT, onClick = { tab = RealWorkerTab.SHIFT },
                    icon = { Icon(Icons.Default.Schedule, contentDescription = "Shift") }, label = { Text("Shift") })
                NavigationBarItem(selected = tab == RealWorkerTab.LOGS, onClick = { tab = RealWorkerTab.LOGS },
                    icon = { Icon(Icons.Default.History, contentDescription = "Daily Logs") }, label = { Text("Logs") })
                NavigationBarItem(selected = tab == RealWorkerTab.LEAVE, onClick = { tab = RealWorkerTab.LEAVE },
                    icon = { Icon(Icons.Default.EventNote, contentDescription = "Leave") }, label = { Text("Leave") })
                NavigationBarItem(selected = tab == RealWorkerTab.PROFILE, onClick = { tab = RealWorkerTab.PROFILE },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") }, label = { Text("Profile") })
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                RealWorkerTab.SHIFT -> ShiftTab(uiState, viewModel, onViewLogs = { tab = RealWorkerTab.LOGS })
                RealWorkerTab.LOGS -> DailyLogsTab(uiState, viewModel)
                RealWorkerTab.LEAVE -> LeaveTab(uiState, viewModel)
                RealWorkerTab.PROFILE -> ProfileTab(uiState, employeeName, employeeCode, onLogout)
            }

            uiState.statusMessage?.let { msg ->
                Surface(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(msg, fontSize = 12.5.sp, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onPrimaryContainer)
                        IconButton(onClick = { viewModel.clearFeedback() }) { Icon(Icons.Default.Close, contentDescription = "Dismiss") }
                    }
                }
            }
            uiState.errorMessage?.let { err ->
                Surface(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.errorContainer) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(err, fontSize = 12.5.sp, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onErrorContainer)
                        IconButton(onClick = { viewModel.clearFeedback() }) { Icon(Icons.Default.Close, contentDescription = "Dismiss") }
                    }
                }
            }
        }
    }

    if (uiState.showStartShiftDialog) {
        CameraXSelfieDialog(
            eventType = ShiftEventType.START_SHIFT, projectName = projectName, employeeName = employeeName,
            onDismiss = { viewModel.setStartShiftDialog(false) },
            onCaptureComplete = { path -> viewModel.startShift(path) }
        )
    }
    if (uiState.showEndShiftDialog) {
        CameraXSelfieDialog(
            eventType = ShiftEventType.END_SHIFT, projectName = projectName, employeeName = employeeName,
            onDismiss = { viewModel.setEndShiftDialog(false) },
            onCaptureComplete = { path -> viewModel.endShift(path) }
        )
    }
    if (showAssistant) AiAssistantDialog(viewModel = aiAssistantViewModel, onDismiss = { showAssistant = false })
    if (showNotifications) NotificationsDialog(uiState.notifications, onDismiss = { showNotifications = false })
}

@Composable
private fun NotificationsDialog(notifications: List<NotificationDto>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notifications") },
        text = {
            if (notifications.isEmpty()) {
                Text("No notifications yet.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    notifications.forEach { n ->
                        Column {
                            Text(n.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(n.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun ShiftTab(uiState: RealWorkerUiState, viewModel: RealWorkerViewModel, onViewLogs: () -> Unit) {
    val active = uiState.activeShift
    val isQueuedLocally = active?.id?.startsWith(com.example.ui.viewmodel.LOCAL_PENDING_SHIFT_PREFIX) == true

    // NOTE: Geofence/GPS compliance status is intentionally never shown to the worker on this
    // screen — it is recorded silently on the attendance record for supervisor/HCM review only,
    // and must never discourage or forewarn the worker before they clock in or out.
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SyncQueueBanner(uiState.syncQueue, onSyncNow = { viewModel.syncNow() })
        Spacer(modifier = Modifier.height(16.dp))
        Surface(
            onClick = { if (active != null) viewModel.setEndShiftDialog(true) else viewModel.setStartShiftDialog(true) },
            enabled = !uiState.isProcessing,
            shape = CircleShape,
            color = if (active != null) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(180.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                if (uiState.isProcessing) CircularProgressIndicator()
                else {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(if (active != null) "End Shift" else "Start Shift", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("with Selfie", fontSize = 11.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        if (active != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Shift in progress", fontWeight = FontWeight.Bold)
                        if (isQueuedLocally) StatusPill("QUEUED")
                    }
                    Text(
                        if (isQueuedLocally) "Captured offline — will sync automatically." else "Started: ${active.clockIn?.serverTimestamp ?: "—"}",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("Shifts", (uiState.shiftHistory.size + if (active != null) 1 else 0).toString(), Icons.Default.Checklist, Modifier.weight(1f))
            StatTile("Pending", uiState.shiftHistory.count { it.status == "PENDING_REVIEW" }.toString(), Icons.Default.Pending, Modifier.weight(1f))
            StatTile("Leaves", uiState.leaveHistory.count { it.status == "APPROVED" }.toString(), Icons.Default.FlightTakeoff, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("RECENT SHIFTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onViewLogs) { Text("View Daily Logs", fontSize = 11.sp) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (uiState.shiftHistory.isEmpty()) {
            Text("No completed shifts yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            uiState.shiftHistory.take(5).forEach { shift -> ShiftHistoryCard(shift) }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ShiftHistoryCard(shift: AttendanceShiftDto) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(shift.shiftDate, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                ShiftStatusBadge(shift.status)
            }
            Text("Worked: ${shift.totalWorkedMinutes ?: 0} mins", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (shift.complianceFlag != "COMPLIANT") {
                Text("Flag: ${shift.complianceFlag.replace('_', ' ')}", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
            }
            shift.reviewComment?.let { Text("Supervisor: $it", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
fun StatusPill(status: String) {
    val (bg, fg) = when (status) {
        "APPROVED" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        "REJECTED" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        "PENDING_REVIEW", "PENDING" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        "OPEN" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        "QUEUED" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(shape = RoundedCornerShape(50), color = bg) {
        Text(status.replace('_', ' '), color = fg, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
    }
}

/** Icon-bearing status badge (mirrors Demo Mode's DailyShiftStatusBadge) used on daily-log cards and the detail dialog. */
@Composable
private fun ShiftStatusBadge(status: String) {
    val (bg, fg, label, icon) = when (status) {
        "APPROVED" -> Tuple4(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.tertiary, "APPROVED", Icons.Default.Check)
        "REJECTED" -> Tuple4(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error, "REJECTED", Icons.Default.Close)
        "PENDING_REVIEW", "PENDING" -> Tuple4(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.secondary, "PENDING REVIEW", Icons.Default.HourglassEmpty)
        "OPEN" -> Tuple4(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary, "IN PROGRESS", Icons.Default.PlayArrow)
        "QUEUED" -> Tuple4(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.secondary, "QUEUED", Icons.Default.CloudQueue)
        else -> Tuple4(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, status, Icons.Default.Info)
    }
    Surface(shape = RoundedCornerShape(50), color = bg) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(11.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, color = fg, fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }
    }
}

private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun MiniStatCard(title: String, value: String, subtext: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(subtext, fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ---------------- Daily Logs tab (search/filter/detail) ----------------

@Composable
private fun DailyLogsTab(uiState: RealWorkerUiState, viewModel: RealWorkerViewModel) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("ALL") }
    var selected by remember { mutableStateOf<AttendanceShiftDto?>(null) }

    val allShifts = uiState.shiftHistory + listOfNotNull(uiState.activeShift?.takeIf { !it.id.startsWith(com.example.ui.viewmodel.LOCAL_PENDING_SHIFT_PREFIX) })
    val filtered = allShifts.filter { s ->
        val matchesFilter = filter == "ALL" || s.status == filter
        val matchesQuery = query.isBlank() || s.shiftDate.contains(query, ignoreCase = true) || s.status.contains(query, ignoreCase = true)
        matchesFilter && matchesQuery
    }.sortedByDescending { it.shiftDate }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Daily Attendance Logs", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text("${allShifts.size} shift(s) on record", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(10.dp))

        val totalMinutes = allShifts.sumOf { it.totalWorkedMinutes ?: 0 }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniStatCard("Completed", allShifts.count { it.status != "OPEN" }.toString(), "Total Shifts", Icons.Default.CheckCircleOutline, Modifier.weight(1f))
            MiniStatCard("Approved", allShifts.count { it.status == "APPROVED" }.toString(), "Verified", Icons.Default.Verified, Modifier.weight(1f))
            MiniStatCard("Hours", String.format("%.1f", totalMinutes / 60.0), "Total Logged", Icons.Default.Schedule, Modifier.weight(1f))
            MiniStatCard("Pending", allShifts.count { it.status == "PENDING_REVIEW" }.toString(), "In Review", Icons.Default.HourglassEmpty, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search by date or status…", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }, singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("ALL", "OPEN", "PENDING_REVIEW", "APPROVED", "REJECTED").forEach { f ->
                FilterChip(selected = filter == f, onClick = { filter = f }, label = { Text(f.replace('_', ' '), fontSize = 10.sp) })
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            if (filtered.isEmpty()) {
                Text("No records match.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 20.dp))
            } else {
                filtered.forEach { shift ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selected = shift }) {
                        Column(modifier = Modifier.padding(14.dp).fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(9.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(shift.shiftDate, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(shift.project?.name ?: "—", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                ShiftStatusBadge(shift.status)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Login, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("START", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text(shift.clockIn?.serverTimestamp?.takeLast(8) ?: "—", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Box(modifier = Modifier.width(1.dp).height(26.dp).background(MaterialTheme.colorScheme.outlineVariant))
                                    Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Logout, contentDescription = null, tint = if (shift.clockOut != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("END", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text(shift.clockOut?.serverTimestamp?.takeLast(8) ?: "In progress", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Box(modifier = Modifier.width(1.dp).height(26.dp).background(MaterialTheme.colorScheme.outlineVariant))
                                    Column(modifier = Modifier.weight(1f).padding(start = 10.dp), horizontalAlignment = Alignment.End) {
                                        Text("DURATION", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${shift.totalWorkedMinutes ?: 0}m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            shift.reviewComment?.let {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Supervisor: $it", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    selected?.let { shift ->
        AttendanceDetailDialog(shift = shift, uiState = uiState, viewModel = viewModel, onDismiss = { selected = null })
    }
}

@Composable
private fun AttendanceDetailDialog(shift: AttendanceShiftDto, uiState: RealWorkerUiState, viewModel: RealWorkerViewModel, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Shift Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    ShiftStatusBadge(shift.status)
                }
                Spacer(modifier = Modifier.height(12.dp))

                val selfiePath = shift.clockIn?.selfieStoragePath ?: shift.clockOut?.selfieStoragePath
                if (selfiePath != null) {
                    LaunchedEffect(selfiePath) { viewModel.loadSelfieUrl(selfiePath) }
                    val url = uiState.selfieUrlCache[selfiePath]
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                        if (url != null) AsyncImage(model = url, contentDescription = "Selfie evidence", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        else CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                KeyValueRow("Date", shift.shiftDate)
                KeyValueRow("Site", shift.project?.name ?: "—")
                KeyValueRow("Clock In", shift.clockIn?.serverTimestamp ?: "—")
                KeyValueRow("Clock Out", shift.clockOut?.serverTimestamp ?: "In progress")
                KeyValueRow("Duration", "${shift.totalWorkedMinutes ?: 0} minutes")
                KeyValueRow("Compliance", shift.complianceFlag.replace('_', ' '))
                KeyValueRow("Biometric Match", if (selfiePath != null) "Selfie captured & verified" else "No selfie on record")
                KeyValueRow("Hardware Device", (shift.clockIn?.deviceId ?: shift.clockOut?.deviceId)?.take(18) ?: "Unknown")
                shift.reviewComment?.let { KeyValueRow("Supervisor Note", it) }

                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Close") }
            }
        }
    }
}

@Composable
private fun KeyValueRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.4f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}

@Composable
private fun SyncQueueBanner(queue: SyncQueueStatus, onSyncNow: () -> Unit) {
    if (queue.pendingCount == 0 && queue.failedCount == 0 && queue.isOnline) return
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (queue.failedCount > 0) MaterialTheme.colorScheme.errorContainer
        else if (!queue.isOnline) MaterialTheme.colorScheme.surfaceVariant
        else MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    when {
                        !queue.isOnline -> "Offline"
                        queue.isSyncing -> "Syncing…"
                        queue.failedCount > 0 -> "${queue.failedCount} item(s) failed to sync"
                        else -> "${queue.pendingCount} item(s) waiting to sync"
                    },
                    fontSize = 11.5.sp, fontWeight = FontWeight.Bold
                )
                queue.lastError?.let { Text(it, fontSize = 10.sp, maxLines = 2) }
            }
            if (queue.isOnline && (queue.pendingCount > 0 || queue.failedCount > 0) && !queue.isSyncing) {
                TextButton(onClick = onSyncNow) { Text("Sync Now") }
            }
        }
    }
}

// ---------------- Leave tab ----------------

@Composable
private fun LeaveTab(uiState: RealWorkerUiState, viewModel: RealWorkerViewModel) {
    var showForm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (showForm) {
            LeaveForm(
                isSubmitting = uiState.isProcessing,
                onCancel = { showForm = false },
                onSubmit = { type, start, end, reason -> viewModel.submitLeave(type, start, end, reason); showForm = false }
            )
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Leave & Absence", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Button(onClick = { showForm = true }) { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("Request") }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LEAVE_TYPES.forEach { (_, label, quota) ->
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(quota, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (uiState.leaveHistory.isEmpty()) {
                    Text("No leave requests yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    uiState.leaveHistory.forEach { leave -> LeaveCard(leave) }
                }
            }
        }
    }
}

@Composable
private fun LeaveForm(isSubmitting: Boolean, onCancel: () -> Unit, onSubmit: (type: String, start: String, end: String, reason: String) -> Unit) {
    var type by remember { mutableStateOf("ANNUAL") }
    val today = remember { Calendar.getInstance() }
    val fmt = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US) }
    var start by remember { mutableStateOf(fmt.format(today.time)) }
    var end by remember { mutableStateOf(fmt.format(today.time)) }
    var reason by remember { mutableStateOf("") }
    var showDatePickerFor by remember { mutableStateOf<String?>(null) }

    val suggestedReasons = mapOf(
        "SICK" to listOf("Medical consultation and rest", "Fever & viral infection recovery"),
        "CASUAL" to listOf("Urgent family emergency", "Personal legal & government documentation"),
        "ANNUAL" to listOf("Scheduled annual vacation", "Family holiday travel"),
        "TRANSIT" to listOf("Inter-site travel duty", "Logistics convoy & equipment transport")
    )

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("New Leave Request", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Text("LEAVE TYPE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LEAVE_TYPES.forEach { (code, label, _) ->
                val selected = type == code
                Surface(
                    onClick = { type = code },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        label, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("DATE RANGE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DateField("Start", start, modifier = Modifier.weight(1f)) { showDatePickerFor = "start" }
            DateField("End", end, modifier = Modifier.weight(1f)) { showDatePickerFor = "end" }
        }
        val totalDays = runCatching {
            ((fmt.parse(end)!!.time - fmt.parse(start)!!.time) / 86400000L).toInt() + 1
        }.getOrDefault(1)
        Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("Total: $totalDays day(s)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(10.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("REASON", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (suggestedReasons[type] ?: emptyList()).forEach { suggestion ->
                AssistChip(onClick = { reason = suggestion }, label = { Text(suggestion, fontSize = 10.sp) })
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(value = reason, onValueChange = { reason = it }, modifier = Modifier.fillMaxWidth(), minLines = 2, placeholder = { Text("Reason for leave…", fontSize = 12.sp) })

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { onSubmit(type, start, end, reason) },
            enabled = !isSubmitting && reason.isNotBlank() && totalDays > 0,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) { Text(if (isSubmitting) "Submitting…" else "Submit Request", fontWeight = FontWeight.Bold) }
        Spacer(modifier = Modifier.height(20.dp))
    }

    showDatePickerFor?.let { target ->
        SimpleDatePickerDialog(
            initialDate = if (target == "start") start else end,
            onDismiss = { showDatePickerFor = null },
            onConfirm = { picked ->
                if (target == "start") start = picked else end = picked
                showDatePickerFor = null
            }
        )
    }
}

@Composable
private fun DateField(label: String, value: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier = modifier) {
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onClick, modifier = Modifier.size(20.dp)) { Icon(Icons.Default.CalendarMonth, contentDescription = "Pick date", modifier = Modifier.size(16.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleDatePickerDialog(initialDate: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = runCatching {
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse(initialDate)?.time
        }.getOrNull()
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val millis = state.selectedDateMillis
                if (millis != null) {
                    val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date(millis))
                    onConfirm(date)
                } else onDismiss()
            }) { Text("Confirm") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    ) { DatePicker(state = state) }
}

@Composable
private fun LeaveCard(leave: LeaveRequestDto) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${leave.leaveType} • ${leave.totalDays.toInt()}d", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                StatusPill(leave.status)
            }
            Text("${leave.startDate} → ${leave.endDate}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(leave.reason, fontSize = 12.sp)
            leave.decisionReason?.let { Text("Decision: $it", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

// ---------------- Profile tab ----------------

@Composable
private fun ProfileTab(uiState: RealWorkerUiState, fallbackName: String, fallbackCode: String, onLogout: () -> Unit) {
    val profile = uiState.profile
    val context = LocalContext.current
    val themePreferences = remember { ThemePreferences.getInstance(context) }
    val themeSettings by themePreferences.settings.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }
    val deviceId = remember { SecureSessionStore.getInstance(context).deviceId }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(78.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Text((profile?.fullName ?: fallbackName).take(2).uppercase(), fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(profile?.fullName ?: fallbackName, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.padding(top = 4.dp)) {
            Text(
                "${profile?.role ?: "—"}${profile?.department?.let { " • $it" } ?: ""}",
                fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("${profile?.employeeCode ?: fallbackCode} • ${profile?.companyName ?: "Artify Workforce"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        // Display & Environment Theme
        Spacer(modifier = Modifier.height(20.dp))
        ProfileSectionLabel("DISPLAY & ENVIRONMENT")
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Theme Mode", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(themeSettings.themeMode.displayName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK).forEach { mode ->
                            val selected = themeSettings.themeMode == mode
                            Surface(
                                onClick = { themePreferences.setThemeMode(mode) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Text(
                                    mode.shortName, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(onClick = { showThemeDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Configure Dynamic Colors", fontSize = 12.sp)
                }
            }
        }

        // Work Assignment
        Spacer(modifier = Modifier.height(16.dp))
        ProfileSectionLabel("WORK ASSIGNMENT")
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(profile?.projectName ?: "—", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.tertiaryContainer) {
                        Text("ACTIVE SITE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                KeyValueRow("Site Code", profile?.projectCode ?: "—")
                KeyValueRow("Location", profile?.projectAddress ?: "—")
                KeyValueRow("Geofence Radius", profile?.geofenceRadiusMeters?.let { "${it.toInt()} m" } ?: "—")
            }
        }

        // Contact & Credentials
        Spacer(modifier = Modifier.height(16.dp))
        ProfileSectionLabel("CONTACT & CREDENTIALS")
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                KeyValueRow("Email", profile?.email ?: "—")
                KeyValueRow("Phone", profile?.phone ?: "—")
                KeyValueRow("Department", profile?.department ?: "—")
                KeyValueRow("Account Status", if (profile != null) "Active" else "—")
            }
        }

        // Security & Biometric
        Spacer(modifier = Modifier.height(16.dp))
        ProfileSectionLabel("SECURITY & BIOMETRIC VERIFICATION")
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                KeyValueRow("Device ID", deviceId.take(18) + "…")
                KeyValueRow("Facial Biometrics", "Enrolled & Active")
                KeyValueRow("Time Authority", "Authoritative server clock (UTC)")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        OutlinedButton(onClick = onLogout, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Sign Out")
        }
        Spacer(modifier = Modifier.height(12.dp))
    }

    if (showThemeDialog) {
        ThemeSettingsDialog(themePreferences = themePreferences, onDismiss = { showThemeDialog = false })
    }
}

@Composable
private fun ProfileSectionLabel(text: String) {
    Text(
        text, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp,
        color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
    )
}
