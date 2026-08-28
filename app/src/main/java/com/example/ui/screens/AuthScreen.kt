package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ProjectEntity
import com.example.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    projects: List<ProjectEntity>,
    modifier: Modifier = Modifier
) {
    val uiState by authViewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Quick Demo / Login, 1 = Register

    // Login Form State
    var email by remember { mutableStateOf("worker@artify.demo") }
    var password by remember { mutableStateOf("password123") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Register Form State
    var regFullName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regRole by remember { mutableStateOf(UserRole.WORKER) }
    var regProjectId by remember { mutableStateOf("PRJ-001") }
    var regDepartment by remember { mutableStateOf("Site Engineering") }
    var regPassword by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SophisticatedDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Logo & Title
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(SophisticatedPrimaryContainer)
                    .border(2.dp, SophisticatedPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = "Artify Logo",
                    tint = SophisticatedPrimary,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "ARTIFY WORKFORCE",
                color = SophisticatedPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Text(
                text = "Enterprise Workforce & Attendance Verification",
                color = SophisticatedTextSecondary,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tab Selector: Login vs Register
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SophisticatedDarkSurface,
                contentColor = SophisticatedPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = SophisticatedPrimary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, SophisticatedDarkBorder, RoundedCornerShape(16.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Quick Sign In",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 0) SophisticatedPrimary else SophisticatedTextSecondary
                        )
                    },
                    modifier = Modifier.testTag("tab_login")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Register Employee",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 1) SophisticatedPrimary else SophisticatedTextSecondary
                        )
                    },
                    modifier = Modifier.testTag("tab_register")
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Error / Success Feedback
            uiState.errorMessage?.let { err ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SophisticatedErrorContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedError.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Text(
                        text = err,
                        color = SophisticatedError,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            uiState.successMessage?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SophisticatedSuccessContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedSuccessBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Text(
                        text = msg,
                        color = SophisticatedSuccess,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (selectedTab == 0) {
                // Quick Demo Login Cards
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SophisticatedDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedDarkBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "DEMO ROLE QUICK ACCESS",
                            color = SophisticatedPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Worker Quick Card
                        RoleQuickLoginCard(
                            role = "Worker",
                            email = "worker@artify.demo",
                            name = "Ahmed Ali Al-Balushi",
                            site = "Muscat Site A",
                            color = SophisticatedPrimary,
                            onClick = { authViewModel.quickLoginAs(UserRole.WORKER) }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Staff Quick Card
                        RoleQuickLoginCard(
                            role = "Staff",
                            email = "staff@artify.demo",
                            name = "Fatima Al-Harthy",
                            site = "Al Khuwair Tower",
                            color = SophisticatedSecondary,
                            onClick = { authViewModel.quickLoginAs(UserRole.STAFF) }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Supervisor Quick Card
                        RoleQuickLoginCard(
                            role = "Supervisor",
                            email = "supervisor@artify.demo",
                            name = "Tariq Al-Said",
                            site = "Central HR / Head Office",
                            color = SophisticatedWarning,
                            onClick = { authViewModel.quickLoginAs(UserRole.SUPERVISOR) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Standard Login Fields
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SophisticatedDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedDarkBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "MANUAL CREDENTIAL SIGN IN",
                            color = SophisticatedTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Work Email") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SophisticatedTextSecondary) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_email_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SophisticatedTextPrimary,
                                unfocusedTextColor = SophisticatedTextSecondary,
                                focusedBorderColor = SophisticatedPrimary,
                                unfocusedBorderColor = SophisticatedDarkBorder,
                                focusedLabelColor = SophisticatedPrimary,
                                unfocusedLabelColor = SophisticatedTextSecondary,
                                cursorColor = SophisticatedPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SophisticatedTextSecondary) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password visibility",
                                        tint = SophisticatedTextSecondary
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SophisticatedTextPrimary,
                                unfocusedTextColor = SophisticatedTextSecondary,
                                focusedBorderColor = SophisticatedPrimary,
                                unfocusedBorderColor = SophisticatedDarkBorder,
                                focusedLabelColor = SophisticatedPrimary,
                                unfocusedLabelColor = SophisticatedTextSecondary,
                                cursorColor = SophisticatedPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = { authViewModel.login(email, password) },
                            enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("login_submit_btn"),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SophisticatedPrimary,
                                contentColor = SophisticatedOnPrimary
                            )
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(color = SophisticatedOnPrimary, modifier = Modifier.size(22.dp))
                            } else {
                                Text("SIGN IN TO WORKFORCE", fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 0.5.sp)
                            }
                        }
                    }
                }
            } else {
                // Registration Form
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SophisticatedDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedDarkBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "CREATE EMPLOYEE ACCOUNT",
                            color = SophisticatedPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Role Selector
                        Text("Select Role", color = SophisticatedTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            UserRole.values().forEach { r ->
                                val isSel = regRole == r
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 3.dp)
                                        .clip(RoundedCornerShape(50))
                                        .clickable { regRole = r },
                                    color = if (isSel) SophisticatedPrimary else SophisticatedBadgeBg,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSel) SophisticatedPrimary else SophisticatedDarkBorder
                                    )
                                ) {
                                    Text(
                                        text = r.displayName,
                                        color = if (isSel) SophisticatedOnPrimary else SophisticatedTextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = regFullName,
                            onValueChange = { regFullName = it },
                            label = { Text("Full Name") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SophisticatedTextPrimary,
                                unfocusedTextColor = SophisticatedTextSecondary,
                                focusedBorderColor = SophisticatedPrimary,
                                unfocusedBorderColor = SophisticatedDarkBorder,
                                focusedLabelColor = SophisticatedPrimary,
                                unfocusedLabelColor = SophisticatedTextSecondary,
                                cursorColor = SophisticatedPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Work Email") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_email_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SophisticatedTextPrimary,
                                unfocusedTextColor = SophisticatedTextSecondary,
                                focusedBorderColor = SophisticatedPrimary,
                                unfocusedBorderColor = SophisticatedDarkBorder,
                                focusedLabelColor = SophisticatedPrimary,
                                unfocusedLabelColor = SophisticatedTextSecondary,
                                cursorColor = SophisticatedPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regPhone,
                            onValueChange = { regPhone = it },
                            label = { Text("Phone Number") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SophisticatedTextPrimary,
                                unfocusedTextColor = SophisticatedTextSecondary,
                                focusedBorderColor = SophisticatedPrimary,
                                unfocusedBorderColor = SophisticatedDarkBorder,
                                focusedLabelColor = SophisticatedPrimary,
                                unfocusedLabelColor = SophisticatedTextSecondary,
                                cursorColor = SophisticatedPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regDepartment,
                            onValueChange = { regDepartment = it },
                            label = { Text("Department / Trade") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SophisticatedTextPrimary,
                                unfocusedTextColor = SophisticatedTextSecondary,
                                focusedBorderColor = SophisticatedPrimary,
                                unfocusedBorderColor = SophisticatedDarkBorder,
                                focusedLabelColor = SophisticatedPrimary,
                                unfocusedLabelColor = SophisticatedTextSecondary,
                                cursorColor = SophisticatedPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SophisticatedTextPrimary,
                                unfocusedTextColor = SophisticatedTextSecondary,
                                focusedBorderColor = SophisticatedPrimary,
                                unfocusedBorderColor = SophisticatedDarkBorder,
                                focusedLabelColor = SophisticatedPrimary,
                                unfocusedLabelColor = SophisticatedTextSecondary,
                                cursorColor = SophisticatedPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                authViewModel.register(
                                    fullName = regFullName,
                                    email = regEmail,
                                    phone = regPhone,
                                    role = regRole,
                                    assignedProjectId = regProjectId,
                                    department = regDepartment,
                                    password = regPassword
                                )
                            },
                            enabled = !uiState.isLoading && regFullName.isNotBlank() && regEmail.isNotBlank() && regPassword.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("reg_submit_btn"),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SophisticatedPrimary,
                                contentColor = SophisticatedOnPrimary
                            )
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(color = SophisticatedOnPrimary, modifier = Modifier.size(22.dp))
                            } else {
                                Text("REGISTER & GENERATE ID", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun RoleQuickLoginCard(
    role: String,
    email: String,
    name: String,
    site: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("quick_login_${role.lowercase()}"),
        color = SophisticatedDarkBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedDarkBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (role) {
                            "Supervisor" -> Icons.Default.AdminPanelSettings
                            "Staff" -> Icons.Default.Badge
                            else -> Icons.Default.Engineering
                        },
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = name,
                            color = SophisticatedTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "($role)",
                            color = color,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "$email • $site",
                        color = SophisticatedTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = SophisticatedTextSecondary
            )
        }
    }
}
