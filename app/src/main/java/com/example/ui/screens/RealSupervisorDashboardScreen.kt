package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.network.AttendanceShiftDto
import com.example.network.AuditLogDto
import com.example.network.ErpEventDto
import com.example.network.LeaveRequestDto
import com.example.network.SiteDto
import com.example.network.SupervisorMetricsDto
import com.example.ui.components.AiAssistantDialog
import com.example.ui.components.ArtifyTopHeader
import com.example.ui.viewmodel.AiAssistantViewModel
import com.example.ui.viewmodel.RealSupervisorUiState
import com.example.ui.viewmodel.RealSupervisorViewModel

private enum class SupTab { APPROVALS, LEAVE, SITES, ROSTER, AUDIT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealSupervisorDashboardScreen(
    viewModel: RealSupervisorViewModel,
    aiAssistantViewModel: AiAssistantViewModel,
    supervisorName: String,
    supervisorCode: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var tab by remember { mutableStateOf(SupTab.APPROVALS) }
    var rejectDialogFor by remember { mutableStateOf<Pair<String, Boolean>?>(null) } // id, isAttendance
    var approveDialogForShift by remember { mutableStateOf<String?>(null) }
    var inspectShift by remember { mutableStateOf<AttendanceShiftDto?>(null) }
    var showAssistant by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ArtifyTopHeader(
                userName = supervisorName,
                employeeId = supervisorCode,
                role = "SUPERVISOR",
                onLogoutClick = onLogout,
                notificationCount = uiState.pendingAttendance.size + uiState.pendingLeave.size
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAssistant = true }) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "Ask the assistant")
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = tab == SupTab.APPROVALS, onClick = { tab = SupTab.APPROVALS },
                    icon = { Icon(Icons.Default.Verified, contentDescription = "Approvals") }, label = { Text("Approvals") })
                NavigationBarItem(selected = tab == SupTab.LEAVE, onClick = { tab = SupTab.LEAVE },
                    icon = { Icon(Icons.Default.EventAvailable, contentDescription = "Leave") }, label = { Text("Leave") })
                NavigationBarItem(selected = tab == SupTab.SITES, onClick = { tab = SupTab.SITES },
                    icon = { Icon(Icons.Default.LocationCity, contentDescription = "Sites") }, label = { Text("Sites") })
                NavigationBarItem(selected = tab == SupTab.ROSTER, onClick = { tab = SupTab.ROSTER },
                    icon = { Icon(Icons.Default.People, contentDescription = "Roster") }, label = { Text("Roster") })
                NavigationBarItem(selected = tab == SupTab.AUDIT, onClick = { tab = SupTab.AUDIT },
                    icon = { Icon(Icons.Default.SyncAlt, contentDescription = "Audit & ERP") }, label = { Text("ERP") })
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            MetricsBar(uiState.metrics)
            Box(modifier = Modifier.weight(1f)) {
                when (tab) {
                    SupTab.APPROVALS -> ApprovalsTab(
                        uiState,
                        onInspect = { inspectShift = it },
                        onApprove = { approveDialogForShift = it },
                        onReject = { rejectDialogFor = it to true }
                    )
                    SupTab.LEAVE -> LeaveApprovalTab(uiState, onApprove = { viewModel.reviewLeave(it, true, null) }, onReject = { rejectDialogFor = it to false })
                    SupTab.SITES -> SitesTab(uiState.sites)
                    SupTab.ROSTER -> RosterTab(uiState)
                    SupTab.AUDIT -> AuditErpTab(uiState.auditLogs, uiState.erpEvents)
                }

                uiState.statusMessage?.let { msg ->
                    Surface(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(msg, fontSize = 12.5.sp, modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.clearFeedback() }) { Icon(Icons.Default.Close, contentDescription = "Dismiss") }
                        }
                    }
                }
                uiState.errorMessage?.let { err ->
                    Surface(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.errorContainer) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(err, fontSize = 12.5.sp, modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.clearFeedback() }) { Icon(Icons.Default.Close, contentDescription = "Dismiss") }
                        }
                    }
                }
            }
        }
    }

    rejectDialogFor?.let { (id, isAttendance) ->
        MandatoryReasonPrompt(
            title = if (isAttendance) "Reject Attendance" else "Reject Leave",
            onDismiss = { rejectDialogFor = null },
            onConfirm = { reason ->
                if (isAttendance) viewModel.reviewAttendance(id, false, reason) else viewModel.reviewLeave(id, false, reason)
                rejectDialogFor = null
            }
        )
    }
    approveDialogForShift?.let { shiftId ->
        ApproveCommentPrompt(
            onDismiss = { approveDialogForShift = null },
            onConfirm = { comment -> viewModel.reviewAttendance(shiftId, true, comment); approveDialogForShift = null }
        )
    }
    inspectShift?.let { shift ->
        InspectAttendanceDialog(
            shift = shift, uiState = uiState, viewModel = viewModel,
            onDismiss = { inspectShift = null },
            onApprove = { approveDialogForShift = shift.id; inspectShift = null },
            onReject = { rejectDialogFor = shift.id to true; inspectShift = null }
        )
    }
    if (showAssistant) AiAssistantDialog(viewModel = aiAssistantViewModel, onDismiss = { showAssistant = false })
}

@Composable
private fun MetricsBar(metrics: SupervisorMetricsDto) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            MetricStat("Present", metrics.present, MaterialTheme.colorScheme.primary)
            MetricStat("Working", metrics.working, MaterialTheme.colorScheme.tertiary)
            MetricStat("Attn. Pending", metrics.attendancePending, MaterialTheme.colorScheme.secondary)
            MetricStat("Leave Pending", metrics.leavePending, MaterialTheme.colorScheme.secondary)
            MetricStat("On Leave", metrics.onLeave, MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MetricStat(label: String, value: Int, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString(), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = color)
        Text(label, fontSize = 8.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MandatoryReasonPrompt(title: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var reason by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason (required)") }, minLines = 2) },
        confirmButton = { TextButton(onClick = { onConfirm(reason) }, enabled = reason.isNotBlank()) { Text("Confirm") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ApproveCommentPrompt(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var comment by remember { mutableStateOf("Verified shift attendance and selfie evidence.") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Approve Attendance") },
        text = { OutlinedTextField(value = comment, onValueChange = { comment = it }, label = { Text("Supervisor note (optional)") }, minLines = 2) },
        confirmButton = { TextButton(onClick = { onConfirm(comment) }) { Text("Confirm Approval") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ApprovalsTab(uiState: RealSupervisorUiState, onInspect: (AttendanceShiftDto) -> Unit, onApprove: (String) -> Unit, onReject: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Pending Attendance Submissions (${uiState.pendingAttendance.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text("Review selfie biometric evidence & verified server timestamps", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(12.dp))
        if (uiState.pendingAttendance.isEmpty()) {
            Text("Nothing pending review.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        uiState.pendingAttendance.forEach { shift -> AttendanceApprovalCard(shift, onInspect, onApprove, onReject) }
    }
}

@Composable
private fun AttendanceApprovalCard(shift: AttendanceShiftDto, onInspect: (AttendanceShiftDto) -> Unit, onApprove: (String) -> Unit, onReject: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text(shift.employee?.fullName ?: "Unknown", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("${shift.employee?.employeeCode ?: ""} • ${shift.employee?.role ?: ""}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.secondaryContainer) {
                    Text("PENDING", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("Site: ${shift.project?.name ?: "—"}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            Text(
                "Shift Duration: ${shift.totalWorkedMinutes ?: 0} mins (${formatShiftTime(shift.clockIn?.serverTimestamp) ?: "—"} to ${formatShiftTime(shift.clockOut?.serverTimestamp) ?: "—"})",
                fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (shift.clockIn?.selfieStoragePath != null || shift.clockOut?.selfieStoragePath != null) VerifiedPill("Selfie Verified")
                if (shift.complianceFlag == "COMPLIANT") VerifiedPill("Geofence Verified")
                else Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.errorContainer) {
                    Text(shift.complianceFlag.replace('_', ' '), fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.End) {
                OutlinedButton(onClick = { onInspect(shift) }) { Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("Inspect Evidence") }
                Spacer(modifier = Modifier.width(6.dp))
                OutlinedButton(onClick = { onReject(shift.id) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Reject") }
                Spacer(modifier = Modifier.width(6.dp))
                Button(onClick = { onApprove(shift.id) }) { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("Approve") }
            }
        }
    }
}

@Composable
private fun VerifiedPill(label: String) {
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.tertiaryContainer) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(10.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(label, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
        }
    }
}

@Composable
private fun InspectAttendanceDialog(
    shift: AttendanceShiftDto, uiState: RealSupervisorUiState, viewModel: RealSupervisorViewModel,
    onDismiss: () -> Unit, onApprove: () -> Unit, onReject: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                Text("Biometric Evidence Inspection", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(shift.employee?.fullName ?: "Unknown", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${shift.employee?.employeeCode ?: ""} • ${shift.project?.name ?: ""}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))

                val selfiePath = shift.clockIn?.selfieStoragePath ?: shift.clockOut?.selfieStoragePath
                if (selfiePath != null) {
                    LaunchedEffect(selfiePath) { viewModel.loadSelfieUrl(selfiePath) }
                    val url = uiState.selfieUrlCache[selfiePath]
                    Box(modifier = Modifier.fillMaxWidth().height(180.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                        if (url != null) AsyncImage(model = url, contentDescription = "Selfie evidence", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        else CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                InspectRow("Shift Date", shift.shiftDate)
                InspectRow("Clock In", formatShiftTime(shift.clockIn?.serverTimestamp) ?: "—")
                InspectRow("Clock Out", formatShiftTime(shift.clockOut?.serverTimestamp) ?: "In progress")
                InspectRow("Duration", "${shift.totalWorkedMinutes ?: 0} minutes")
                InspectRow("Compliance", shift.complianceFlag.replace('_', ' '))

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f)) { Text("Reject") }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(onClick = onApprove, modifier = Modifier.weight(1.4f)) { Text("Approve & Sync ERP") }
                }
            }
        }
    }
}

@Composable
private fun InspectRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.4f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}

/** Formats a backend ISO-8601 timestamp (any offset) as a local HH:mm:ss clock time. */
private fun formatShiftTime(iso: String?): String? {
    if (iso.isNullOrBlank()) return null
    return try {
        val instant = java.time.OffsetDateTime.parse(iso).toInstant()
        java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
            .withZone(java.time.ZoneId.systemDefault())
            .format(instant)
    } catch (e: Exception) {
        null
    }
}

@Composable
private fun LeaveApprovalTab(uiState: RealSupervisorUiState, onApprove: (String) -> Unit, onReject: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Employee Leave & Absence Review", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text("Approve or reject workforce absence requests with formal audit logs", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(12.dp))
        if (uiState.pendingLeave.isEmpty()) {
            Text("No pending leave requests.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        uiState.pendingLeave.forEach { leave -> LeaveApprovalCard(leave, onApprove, onReject) }
    }
}

@Composable
private fun LeaveApprovalCard(leave: LeaveRequestDto, onApprove: (String) -> Unit, onReject: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text(leave.employee?.fullName ?: "Unknown", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("ID: ${leave.employee?.employeeCode ?: "—"} • ${leave.leaveType} LEAVE", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.secondaryContainer) {
                    Text("PENDING", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("Period: ${leave.startDate} to ${leave.endDate} (${leave.totalDays.toInt()} days)", fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
            Text("Reason: ${leave.reason}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.End) {
                OutlinedButton(onClick = { onReject(leave.id) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Reject") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { onApprove(leave.id) }) { Text("Approve") }
            }
        }
    }
}

@Composable
private fun SitesTab(sites: List<SiteDto>) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Active Sites & Headcount", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text("Active project locations and live site rosters", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(12.dp))
        if (sites.isEmpty()) Text("No sites configured yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        sites.forEach { site ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(site.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primaryContainer) {
                            Text("${site.employees?.size ?: 0} Workers", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }
                    Text(site.address ?: "—", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Geofence: ${site.geofenceRadiusMeters.toInt()} m", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (!site.employees.isNullOrEmpty()) {
                        Text("Assigned: ${site.employees.joinToString(", ")}", fontSize = 11.sp, maxLines = 2)
                    }
                }
            }
        }
    }
}

@Composable
private fun RosterTab(uiState: RealSupervisorUiState) {
    var query by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("ALL") }
    val filtered = uiState.attendanceRoster.filter { shift ->
        val matchesQuery = query.isBlank() ||
            (shift.employee?.fullName?.contains(query, true) == true) ||
            (shift.employee?.employeeCode?.contains(query, true) == true) ||
            (shift.project?.name?.contains(query, true) == true)
        val matchesStatus = statusFilter == "ALL" || shift.status == statusFilter
        matchesQuery && matchesStatus
    }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Workforce Attendance Roster", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text("${uiState.attendanceRoster.size} record(s) logged", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Search by name, ID, or site…", fontSize = 12.sp) }, singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("ALL", "APPROVED", "PENDING_REVIEW", "REJECTED").forEach { f ->
                FilterChip(selected = statusFilter == f, onClick = { statusFilter = f }, label = { Text(f.replace('_', ' '), fontSize = 10.sp) })
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            if (filtered.isEmpty()) {
                Text("No matching attendance records.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 16.dp))
            } else {
                filtered.forEach { shift -> AttendanceRosterCard(shift) }
            }
        }
    }
}

@Composable
private fun AttendanceRosterCard(shift: AttendanceShiftDto) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(shift.employee?.fullName ?: "Unknown", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("${shift.employee?.employeeCode ?: "—"} • ${shift.project?.name ?: "—"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${shift.shiftDate} • ${shift.totalWorkedMinutes ?: 0} mins", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StatusPill(shift.status)
        }
    }
}

@Composable
private fun AuditErpTab(auditLogs: List<AuditLogDto>, erpEvents: List<ErpEventDto>) {
    var subTab by remember { mutableIntStateOf(0) }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Governance & ERP Integration", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(10.dp))
        TabRow(selectedTabIndex = subTab) {
            Tab(selected = subTab == 0, onClick = { subTab = 0 }, text = { Text("Audit (${auditLogs.size})", fontSize = 11.sp) })
            Tab(selected = subTab == 1, onClick = { subTab = 1 }, text = { Text("ERP Outbox (${erpEvents.size})", fontSize = 11.sp) })
        }
        Spacer(modifier = Modifier.height(12.dp))
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            if (subTab == 0) {
                if (auditLogs.isEmpty()) Text("No audit entries yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                auditLogs.forEach { log ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(log.action.replace('_', ' '), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                Text(log.actorRole ?: "", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            log.reason?.let { Text(it, fontSize = 11.sp) }
                            Text(log.createdAt, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                if (erpEvents.isEmpty()) Text("No ERP outbox events yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                erpEvents.forEach { event ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(event.eventType, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                StatusPill(event.status)
                            }
                            Text("Idempotency: ${event.idempotencyKey}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("ERP Ref: ${event.responseRef ?: "PENDING"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
