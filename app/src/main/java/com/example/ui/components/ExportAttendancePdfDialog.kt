package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.AttendanceEntity
import com.example.data.entity.ProjectEntity
import com.example.data.entity.UserEntity
import com.example.export.AttendancePdfExporter
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportAttendancePdfDialog(
    attendanceList: List<AttendanceEntity>,
    projects: List<ProjectEntity>,
    supervisorUser: UserEntity?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Extract available distinct months from attendanceList
    val availableMonths = remember(attendanceList) {
        val months = attendanceList.mapNotNull {
            if (it.shiftDate.length >= 7) it.shiftDate.substring(0, 7) else null
        }.distinct().sortedDescending().toMutableList()

        if (!months.contains("2026-09")) {
            months.add(0, "2026-09")
        }
        months
    }

    var selectedMonth by remember { mutableStateOf(availableMonths.firstOrNull() ?: "2026-09") }
    var selectedProject by remember { mutableStateOf("ALL") }
    var selectedStatus by remember { mutableStateOf("ALL") }

    var isExporting by remember { mutableStateOf(false) }
    var exportResult by remember { mutableStateOf<AttendancePdfExporter.ExportResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Filtered matching records count preview
    val matchingRecords = remember(selectedMonth, selectedProject, selectedStatus, attendanceList) {
        attendanceList.filter { record ->
            val monthMatch = if (selectedMonth == "ALL") true else record.shiftDate.startsWith(selectedMonth)
            val projectMatch = if (selectedProject == "ALL") true else (record.projectName.equals(selectedProject, ignoreCase = true) || record.projectId == selectedProject)
            val statusMatch = when (selectedStatus) {
                "ALL" -> true
                "APPROVED" -> record.state == "APPROVED"
                "PENDING" -> record.state == "PENDING_APPROVAL"
                "REJECTED" -> record.state == "REJECTED"
                else -> true
            }
            monthMatch && projectMatch && statusMatch
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .testTag("export_pdf_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = SophisticatedDarkSurface,
            border = BorderStroke(1.dp, SophisticatedDarkBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SophisticatedPrimary.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, SophisticatedPrimary.copy(alpha = 0.3f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = SophisticatedPrimary,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Export Attendance PDF",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = SophisticatedTextPrimary
                            )
                            Text(
                                text = "Monthly Compliance & Payroll Report",
                                fontSize = 11.sp,
                                color = SophisticatedTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_export_pdf_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = SophisticatedTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = SophisticatedDarkBorder)
                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (exportResult != null) {
                        // --- EXPORT SUCCESS CARD ---
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = SophisticatedSuccessContainer,
                            border = BorderStroke(1.dp, SophisticatedSuccessBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SophisticatedSuccess,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "PDF Generated Successfully!",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = SophisticatedSuccess
                                        )
                                        Text(
                                            text = exportResult?.fileName ?: "",
                                            fontSize = 12.sp,
                                            color = SophisticatedTextPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Quick Metrics in Generated PDF
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SophisticatedDarkBg,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Period", fontSize = 10.sp, color = SophisticatedTextMuted)
                                            Text(exportResult?.monthLabel ?: "", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SophisticatedTextPrimary)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Records", fontSize = 10.sp, color = SophisticatedTextMuted)
                                            Text("${exportResult?.totalRecords ?: 0}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SophisticatedPrimary)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Hours", fontSize = 10.sp, color = SophisticatedTextMuted)
                                            Text(exportResult?.totalHoursFormatted ?: "0h", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SophisticatedSuccess)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Size", fontSize = 10.sp, color = SophisticatedTextMuted)
                                            Text(exportResult?.fileSizeFormatted ?: "", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SophisticatedTextSecondary)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Action Buttons for the Generated PDF
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // View / Open PDF
                                    Button(
                                        onClick = {
                                            exportResult?.file?.let {
                                                AttendancePdfExporter.openPdfFile(context, it)
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("open_generated_pdf_btn"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SophisticatedPrimary,
                                            contentColor = SophisticatedOnPrimary
                                        )
                                    ) {
                                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Open PDF", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // Share PDF (System Intent)
                                    OutlinedButton(
                                        onClick = {
                                            exportResult?.file?.let {
                                                AttendancePdfExporter.sharePdfFile(
                                                    context = context,
                                                    file = it,
                                                    subject = "Artify Monthly Attendance Report - ${exportResult?.monthLabel}"
                                                )
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("share_generated_pdf_btn"),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, SophisticatedPrimary.copy(alpha = 0.6f)),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = SophisticatedDarkBg,
                                            contentColor = SophisticatedPrimary
                                        )
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Share PDF", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SophisticatedErrorContainer,
                            border = BorderStroke(1.dp, SophisticatedError.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = SophisticatedError, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(errorMessage ?: "", color = SophisticatedError, fontSize = 12.sp)
                            }
                        }
                    }

                    // --- 1. SELECT MONTH ---
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "1. Select Month",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SophisticatedTextPrimary
                            )
                            Text(
                                text = "Required",
                                fontSize = 11.sp,
                                color = SophisticatedPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Month Selection Grid / Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            availableMonths.take(3).forEach { monthStr ->
                                val isSel = selectedMonth == monthStr
                                val label = try {
                                    val inputFormat = SimpleDateFormat("yyyy-MM", Locale.US)
                                    val outputFormat = SimpleDateFormat("MMM yyyy", Locale.US)
                                    val date = inputFormat.parse(monthStr)
                                    if (date != null) outputFormat.format(date) else monthStr
                                } catch (e: Exception) {
                                    monthStr
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSel) SophisticatedPrimaryContainer else SophisticatedDarkBg,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSel) SophisticatedPrimary else SophisticatedDarkBorder
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedMonth = monthStr
                                            exportResult = null
                                        }
                                        .testTag("month_chip_$monthStr")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) SophisticatedPrimary else SophisticatedTextPrimary
                                        )
                                        Text(
                                            text = monthStr,
                                            fontSize = 10.sp,
                                            color = if (isSel) SophisticatedPrimary.copy(alpha = 0.8f) else SophisticatedTextMuted
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Option for All Months
                        val isAllSelected = selectedMonth == "ALL"
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isAllSelected) SophisticatedPrimaryContainer else SophisticatedDarkBg,
                            border = BorderStroke(
                                1.dp,
                                if (isAllSelected) SophisticatedPrimary else SophisticatedDarkBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedMonth = "ALL"
                                    exportResult = null
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "All Recorded Months (Full History)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isAllSelected) SophisticatedPrimary else SophisticatedTextSecondary
                                )
                                if (isAllSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = SophisticatedPrimary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // --- 2. SELECT SITE / PROJECT FILTER ---
                    Column {
                        Text(
                            text = "2. Filter by Site / Project",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SophisticatedTextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val projectOptions = listOf("ALL") + projects.map { it.projectName }
                            projectOptions.take(3).forEach { proj ->
                                val isSel = selectedProject == proj
                                val displayLabel = if (proj == "ALL") "All Sites" else proj.take(14)

                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (isSel) SophisticatedPrimaryContainer else SophisticatedDarkBg,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSel) SophisticatedPrimary else SophisticatedDarkBorder
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(50))
                                        .clickable {
                                            selectedProject = proj
                                            exportResult = null
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = displayLabel,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) SophisticatedPrimary else SophisticatedTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // --- 3. SELECT STATUS FILTER ---
                    Column {
                        Text(
                            text = "3. Filter by Approval Status",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SophisticatedTextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "ALL" to "All Statuses",
                                "APPROVED" to "Approved",
                                "PENDING" to "Pending"
                            ).forEach { (statusKey, statusLabel) ->
                                val isSel = selectedStatus == statusKey

                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (isSel) SophisticatedPrimaryContainer else SophisticatedDarkBg,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSel) SophisticatedPrimary else SophisticatedDarkBorder
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(50))
                                        .clickable {
                                            selectedStatus = statusKey
                                            exportResult = null
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = statusLabel,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) SophisticatedPrimary else SophisticatedTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // --- 4. PREVIEW SUMMARY BANNER ---
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SophisticatedDarkBg,
                        border = BorderStroke(1.dp, SophisticatedDarkBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Ready to Export:",
                                    fontSize = 12.sp,
                                    color = SophisticatedTextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${matchingRecords.size} Records Found",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (matchingRecords.isNotEmpty()) SophisticatedSuccess else SophisticatedWarning
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            val totalHrs = matchingRecords.sumOf { it.totalWorkedMinutes } / 60.0
                            Text(
                                text = "• Total Hours: ${String.format(Locale.US, "%.1f", totalHrs)} hrs\n" +
                                        "• Geofence & Biometric Verification: Included in report\n" +
                                        "• Format: High-Resolution Vector PDF (A4 Printable)",
                                fontSize = 11.sp,
                                color = SophisticatedTextMuted,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Footer Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SophisticatedTextSecondary),
                        border = BorderStroke(1.dp, SophisticatedDarkBorder)
                    ) {
                        Text("Dismiss", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            isExporting = true
                            errorMessage = null
                            try {
                                val result = AttendancePdfExporter.generateMonthlyAttendancePdf(
                                    context = context,
                                    selectedMonth = selectedMonth,
                                    attendanceList = attendanceList,
                                    supervisor = supervisorUser,
                                    projectFilter = selectedProject,
                                    statusFilter = selectedStatus
                                )
                                result.onSuccess { res ->
                                    exportResult = res
                                    isExporting = false
                                }.onFailure { error ->
                                    errorMessage = error.message ?: "Failed to generate PDF."
                                    isExporting = false
                                }
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Export error occurred."
                                isExporting = false
                            }
                        },
                        enabled = !isExporting && matchingRecords.isNotEmpty(),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .testTag("generate_pdf_confirm_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SophisticatedPrimary,
                            contentColor = SophisticatedOnPrimary,
                            disabledContainerColor = SophisticatedPrimary.copy(alpha = 0.3f),
                            disabledContentColor = SophisticatedTextMuted
                        )
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = SophisticatedOnPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generating PDF...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        } else {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
