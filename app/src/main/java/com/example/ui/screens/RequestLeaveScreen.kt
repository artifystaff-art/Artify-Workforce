package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.UserEntity
import com.example.data.repository.WorkforceRepository
import com.example.model.LeaveType
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestLeaveScreen(
    repository: WorkforceRepository,
    currentUser: UserEntity,
    onBackClick: () -> Unit,
    onRequestSubmitted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    // Form State
    var selectedLeaveType by remember { mutableStateOf(LeaveType.SICK_LEAVE) }
    
    // Default dates initialized to today and tomorrow
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val todayCalendar = remember { Calendar.getInstance() }
    val defaultStartStr = remember { dateFormat.format(todayCalendar.time) }
    val defaultEndStr = remember {
        val nextDay = Calendar.getInstance()
        nextDay.add(Calendar.DAY_OF_YEAR, 1)
        dateFormat.format(nextDay.time)
    }

    var startDate by remember { mutableStateOf(defaultStartStr) }
    var endDate by remember { mutableStateOf(defaultEndStr) }
    var reasonText by remember { mutableStateOf("") }
    
    // UI State
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showDatePickerFor by remember { mutableStateOf<String?>(null) } // "start" or "end"

    // Calculate total days between start and end
    val totalDays = remember(startDate, endDate) {
        try {
            val start = dateFormat.parse(startDate)
            val end = dateFormat.parse(endDate)
            if (start != null && end != null) {
                val diffMillis = end.time - start.time
                val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt() + 1
                if (diffDays > 0) diffDays else 0
            } else 1
        } catch (e: Exception) {
            1
        }
    }

    // Leave Types configuration metadata
    val leaveTypes = remember {
        listOf(
            LeaveTypeUiItem(
                type = LeaveType.SICK_LEAVE,
                title = "Sick",
                subtitle = "Medical & recovery",
                icon = Icons.Default.LocalHospital,
                color = Color(0xFF00E676),
                bgColor = Color(0xFF00E676).copy(alpha = 0.12f),
                borderColor = Color(0xFF00E676).copy(alpha = 0.4f),
                defaultReasons = listOf(
                    "Medical consultation and rest",
                    "Fever & viral infection recovery",
                    "Hospital appointment / checkup"
                )
            ),
            LeaveTypeUiItem(
                type = LeaveType.CASUAL_LEAVE,
                title = "Casual",
                subtitle = "Personal & urgent affairs",
                icon = Icons.Default.HelpCenter,
                color = Color(0xFFFFB74D),
                bgColor = Color(0xFFFFB74D).copy(alpha = 0.12f),
                borderColor = Color(0xFFFFB74D).copy(alpha = 0.4f),
                defaultReasons = listOf(
                    "Urgent family emergency",
                    "Personal legal & government documentation",
                    "Home maintenance / urgent affairs"
                )
            ),
            LeaveTypeUiItem(
                type = LeaveType.ANNUAL_LEAVE,
                title = "Annual",
                subtitle = "Earned vacation & PTO",
                icon = Icons.Default.WbSunny,
                color = Color(0xFF64B5F6),
                bgColor = Color(0xFF64B5F6).copy(alpha = 0.12f),
                borderColor = Color(0xFF64B5F6).copy(alpha = 0.4f),
                defaultReasons = listOf(
                    "Scheduled annual vacation",
                    "Family holiday travel",
                    "Earned annual rest leave"
                )
            ),
            LeaveTypeUiItem(
                type = LeaveType.TRANSIT,
                title = "Transit",
                subtitle = "Inter-site travel duty",
                icon = Icons.Default.DirectionsTransit,
                color = Color(0xFFBA68C8),
                bgColor = Color(0xFFBA68C8).copy(alpha = 0.12f),
                borderColor = Color(0xFFBA68C8).copy(alpha = 0.4f),
                defaultReasons = listOf(
                    "Inter-site travel: Muscat to Sohar",
                    "Relocation transit between active project sites",
                    "Logistics convoy & equipment transport"
                )
            )
        )
    }

    val currentTypeConfig = leaveTypes.firstOrNull { it.type == selectedLeaveType } ?: leaveTypes[0]

    // Form Submission Handler
    fun handleSubmit() {
        if (reasonText.isBlank()) {
            errorMessage = "Please enter a reason for your leave request."
            return
        }
        if (totalDays <= 0) {
            errorMessage = "End date must be the same as or after start date."
            return
        }

        errorMessage = null
        isSubmitting = true

        coroutineScope.launch {
            val result = repository.submitLeaveRequest(
                employee = currentUser,
                type = selectedLeaveType,
                startDate = startDate,
                endDate = endDate,
                totalDays = totalDays,
                reason = reasonText.trim()
            )

            isSubmitting = false
            result.onSuccess {
                showSuccessDialog = true
            }.onFailure { error ->
                errorMessage = error.message ?: "Failed to save leave request to Room database."
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Request Leave",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = SophisticatedTextPrimary
                        )
                        Text(
                            text = "${currentUser.fullName} • ID: ${currentUser.employeeId}",
                            fontSize = 12.sp,
                            color = SophisticatedTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("request_leave_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = SophisticatedTextPrimary
                        )
                    }
                },
                actions = {
                    // Room Database Badge
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SophisticatedPrimaryContainer.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, SophisticatedPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(SophisticatedSuccess)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Room DB",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SophisticatedPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SophisticatedDarkSurface,
                    titleContentColor = SophisticatedTextPrimary
                )
            )
        },
        containerColor = SophisticatedDarkBg
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .testTag("request_leave_form_screen")
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Error Banner if validation or database fails
            if (errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SophisticatedErrorContainer,
                    border = BorderStroke(1.dp, SophisticatedError.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = SophisticatedError,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = SophisticatedError,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 1. Leave Type Selector Section
            Text(
                text = "1. SELECT LEAVE TYPE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SophisticatedTextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 2x2 Grid of Leave Types
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LeaveTypeCard(
                        item = leaveTypes[0], // Sick
                        isSelected = selectedLeaveType == leaveTypes[0].type,
                        onClick = { selectedLeaveType = leaveTypes[0].type },
                        modifier = Modifier.weight(1f)
                    )
                    LeaveTypeCard(
                        item = leaveTypes[1], // Casual
                        isSelected = selectedLeaveType == leaveTypes[1].type,
                        onClick = { selectedLeaveType = leaveTypes[1].type },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LeaveTypeCard(
                        item = leaveTypes[2], // Annual
                        isSelected = selectedLeaveType == leaveTypes[2].type,
                        onClick = { selectedLeaveType = leaveTypes[2].type },
                        modifier = Modifier.weight(1f)
                    )
                    LeaveTypeCard(
                        item = leaveTypes[3], // Transit
                        isSelected = selectedLeaveType == leaveTypes[3].type,
                        onClick = { selectedLeaveType = leaveTypes[3].type },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Date Range Section
            Text(
                text = "2. DATE RANGE & DURATION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SophisticatedTextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Quick Duration Preset Chips
            Text(
                text = "Quick Presets:",
                fontSize = 11.sp,
                color = SophisticatedTextMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    PresetChip(
                        label = "1 Day (Today)",
                        onClick = {
                            val cal = Calendar.getInstance()
                            val dateStr = dateFormat.format(cal.time)
                            startDate = dateStr
                            endDate = dateStr
                        }
                    )
                }
                item {
                    PresetChip(
                        label = "1 Day (Tomorrow)",
                        onClick = {
                            val cal = Calendar.getInstance()
                            cal.add(Calendar.DAY_OF_YEAR, 1)
                            val dateStr = dateFormat.format(cal.time)
                            startDate = dateStr
                            endDate = dateStr
                        }
                    )
                }
                item {
                    PresetChip(
                        label = "2 Days",
                        onClick = {
                            val cal = Calendar.getInstance()
                            cal.add(Calendar.DAY_OF_YEAR, 1)
                            startDate = dateFormat.format(cal.time)
                            cal.add(Calendar.DAY_OF_YEAR, 1)
                            endDate = dateFormat.format(cal.time)
                        }
                    )
                }
                item {
                    PresetChip(
                        label = "3 Days",
                        onClick = {
                            val cal = Calendar.getInstance()
                            cal.add(Calendar.DAY_OF_YEAR, 1)
                            startDate = dateFormat.format(cal.time)
                            cal.add(Calendar.DAY_OF_YEAR, 2)
                            endDate = dateFormat.format(cal.time)
                        }
                    )
                }
                item {
                    PresetChip(
                        label = "5 Days (Work Week)",
                        onClick = {
                            val cal = Calendar.getInstance()
                            cal.add(Calendar.DAY_OF_YEAR, 1)
                            startDate = dateFormat.format(cal.time)
                            cal.add(Calendar.DAY_OF_YEAR, 4)
                            endDate = dateFormat.format(cal.time)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Start Date and End Date Input Fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Start Date Picker Card
                DatePickerField(
                    label = "START DATE",
                    dateValue = startDate,
                    onDateChange = { startDate = it },
                    onOpenDialog = { showDatePickerFor = "start" },
                    modifier = Modifier.weight(1f),
                    testTag = "leave_start_date_input"
                )

                // End Date Picker Card
                DatePickerField(
                    label = "END DATE",
                    dateValue = endDate,
                    onDateChange = { endDate = it },
                    onOpenDialog = { showDatePickerFor = "end" },
                    modifier = Modifier.weight(1f),
                    testTag = "leave_end_date_input"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Calculated Duration Indicator Pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (totalDays > 0) SophisticatedPrimaryContainer.copy(alpha = 0.5f) else SophisticatedErrorContainer,
                border = BorderStroke(
                    1.dp,
                    if (totalDays > 0) SophisticatedPrimary.copy(alpha = 0.3f) else SophisticatedError.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = if (totalDays > 0) SophisticatedPrimary else SophisticatedError,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (totalDays > 0) "Total Requested Duration:" else "Invalid Date Range:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = SophisticatedTextPrimary
                        )
                    }

                    Text(
                        text = if (totalDays > 0) "$totalDays ${if (totalDays == 1) "Day" else "Days"}" else "End date precedes start date",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (totalDays > 0) SophisticatedPrimary else SophisticatedError
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Reason Text Field Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "3. REASON FOR ABSENCE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SophisticatedTextSecondary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Required",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SophisticatedWarning
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            // Suggested template reasons based on selected type
            Text(
                text = "Tap a suggested reason or type below:",
                fontSize = 11.sp,
                color = SophisticatedTextMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(currentTypeConfig.defaultReasons) { reasonOption ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SophisticatedDarkSurface,
                        border = BorderStroke(1.dp, SophisticatedDarkBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { reasonText = reasonOption }
                    ) {
                        Text(
                            text = reasonOption,
                            fontSize = 11.sp,
                            color = SophisticatedTextSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = reasonText,
                onValueChange = { reasonText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("leave_reason_input"),
                placeholder = {
                    Text(
                        "Explain the reason for taking leave (e.g. medical diagnosis, family travel, etc.)...",
                        fontSize = 13.sp,
                        color = SophisticatedTextMuted
                    )
                },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SophisticatedDarkSurface,
                    unfocusedContainerColor = SophisticatedDarkSurface,
                    focusedBorderColor = SophisticatedPrimary,
                    unfocusedBorderColor = SophisticatedDarkBorder,
                    focusedTextColor = SophisticatedTextPrimary,
                    unfocusedTextColor = SophisticatedTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Summary Preview Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SophisticatedDarkSurface),
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
                            text = "REQUEST SUMMARY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SophisticatedPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = currentTypeConfig.bgColor,
                            border = BorderStroke(1.dp, currentTypeConfig.borderColor)
                        ) {
                            Text(
                                text = currentTypeConfig.title,
                                color = currentTypeConfig.color,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    SummaryRow("Employee", "${currentUser.fullName} (${currentUser.employeeId})")
                    SummaryRow("Period", "$startDate  ➔  $endDate ($totalDays days)")
                    SummaryRow("Destination Site", currentUser.department ?: "Civil Works")
                    SummaryRow("Target Database", "Local SQLite Room Database")
                    SummaryRow("Approval Workflow", "Auto-routes to Supervisor for review")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = { handleSubmit() },
                enabled = !isSubmitting,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SophisticatedPrimary,
                    contentColor = SophisticatedOnPrimary,
                    disabledContainerColor = SophisticatedPrimary.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_leave_request_btn")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = SophisticatedOnPrimary,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Saving to Room DB...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Submit Leave Request",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    // Success Confirmation Dialog
    if (showSuccessDialog) {
        Dialog(onDismissRequest = {
            showSuccessDialog = false
            onRequestSubmitted()
        }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = SophisticatedDarkSurface,
                border = BorderStroke(1.dp, SophisticatedDarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("leave_submit_success_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(SophisticatedSuccessContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SophisticatedSuccess,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Leave Request Saved!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SophisticatedTextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Your $totalDays-day ${currentTypeConfig.title} request from $startDate to $endDate has been saved to the local Room database and forwarded to supervisors for review.",
                        fontSize = 13.sp,
                        color = SophisticatedTextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            showSuccessDialog = false
                            onRequestSubmitted()
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SophisticatedPrimary,
                            contentColor = SophisticatedOnPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("leave_success_done_btn")
                    ) {
                        Text("View Leave Records", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Simple Date Picker Dialog Helper
    showDatePickerFor?.let { target ->
        DatePickerModalDialog(
            initialDate = if (target == "start") startDate else endDate,
            title = if (target == "start") "Select Start Date" else "Select End Date",
            onDateSelected = { newDate ->
                if (target == "start") {
                    startDate = newDate
                    // If end date is now before start date, update end date to match
                    if (endDate < newDate) {
                        endDate = newDate
                    }
                } else {
                    endDate = newDate
                }
                showDatePickerFor = null
            },
            onDismiss = { showDatePickerFor = null }
        )
    }
}

private data class LeaveTypeUiItem(
    val type: LeaveType,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val bgColor: Color,
    val borderColor: Color,
    val defaultReasons: List<String>
)

@Composable
private fun LeaveTypeCard(
    item: LeaveTypeUiItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("leave_type_card_${item.type.name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) item.bgColor else SophisticatedDarkSurface,
        border = BorderStroke(
            1.5.dp,
            if (isSelected) item.color else SophisticatedDarkBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(item.bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = item.color,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(item.color),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = SophisticatedDarkBg,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) item.color else SophisticatedTextPrimary
            )
            Text(
                text = item.subtitle,
                fontSize = 10.sp,
                color = SophisticatedTextSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PresetChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = SophisticatedDarkSurface,
        border = BorderStroke(1.dp, SophisticatedDarkBorder),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = SophisticatedTextPrimary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun DatePickerField(
    label: String,
    dateValue: String,
    onDateChange: (String) -> Unit,
    onOpenDialog: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = SophisticatedTextSecondary,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = SophisticatedDarkSurface,
            border = BorderStroke(1.dp, SophisticatedDarkBorder),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { onOpenDialog() }
                .testTag(testTag)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateValue.ifBlank { "YYYY-MM-DD" },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SophisticatedTextPrimary
                )
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Pick Date",
                    tint = SophisticatedPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = SophisticatedTextSecondary
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = SophisticatedTextPrimary
        )
    }
}

@Composable
private fun DatePickerModalDialog(
    initialDate: String,
    title: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Quick interactive date calendar dialog with year/month/day buttons
    val calendar = remember {
        val cal = Calendar.getInstance()
        try {
            val parts = initialDate.split("-")
            if (parts.size == 3) {
                cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            }
        } catch (e: Exception) {
            // fallback to current
        }
        cal
    }

    var selectedYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) } // 0-based
    var selectedDay by remember { mutableIntStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }

    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SophisticatedDarkSurface,
            border = BorderStroke(1.dp, SophisticatedDarkBorder),
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = SophisticatedTextPrimary
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Current selected formatted display
                val formattedSelected = remember(selectedYear, selectedMonth, selectedDay) {
                    String.format(Locale.US, "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SophisticatedPrimaryContainer,
                    border = BorderStroke(1.dp, SophisticatedPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = formattedSelected,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SophisticatedPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Month Selector
                Text("Select Month (${monthNames[selectedMonth]})", fontSize = 11.sp, color = SophisticatedTextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(monthNames.indices.toList()) { mIndex ->
                        val isSel = selectedMonth == mIndex
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) SophisticatedPrimary else SophisticatedDarkBg,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedMonth = mIndex }
                        ) {
                            Text(
                                text = monthNames[mIndex],
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) SophisticatedOnPrimary else SophisticatedTextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Day Selector (1 to 31)
                val daysInMonth = remember(selectedYear, selectedMonth) {
                    val cal = Calendar.getInstance()
                    cal.set(selectedYear, selectedMonth, 1)
                    cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                }

                Text("Select Day", fontSize = 11.sp, color = SophisticatedTextSecondary)
                Spacer(modifier = Modifier.height(4.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items((1..daysInMonth).toList()) { d ->
                        val isSel = selectedDay == d
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) SophisticatedPrimary else SophisticatedDarkBg,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedDay = d }
                        ) {
                            Text(
                                text = "$d",
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) SophisticatedOnPrimary else SophisticatedTextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onDateSelected(formattedSelected) },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SophisticatedPrimary,
                            contentColor = SophisticatedOnPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Confirm", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
