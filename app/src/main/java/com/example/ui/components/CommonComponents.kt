package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.ProjectEntity
import com.example.model.*
import com.example.server.ServerAuthorityEngine
import com.example.server.ServerGeofenceResult
import com.example.ui.theme.*

@Composable
fun ArtifyTopHeader(
    userName: String,
    employeeId: String,
    role: String,
    onLogoutClick: () -> Unit,
    notificationCount: Int = 0,
    onNotificationClick: () -> Unit = {}
) {
    val roleColor = when (role) {
        UserRole.SUPERVISOR.name -> SophisticatedWarning
        UserRole.STAFF.name -> SophisticatedSecondary
        else -> SophisticatedPrimary
    }

    val initials = userName.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercase() }
        .joinToString("")
        .ifEmpty { "AW" }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SophisticatedDarkBg,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ARTIFY WORKFORCE",
                        color = SophisticatedPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Good Day, $userName",
                        color = SophisticatedTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier.testTag("notification_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (notificationCount > 0) {
                                    Badge(
                                        containerColor = SophisticatedError,
                                        contentColor = Color(0xFF601410)
                                    ) {
                                        Text(notificationCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = SophisticatedTextSecondary
                            )
                        }
                    }

                    // Avatar Circle matching theme
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SophisticatedDarkBorder)
                            .border(2.dp, SophisticatedPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = SophisticatedTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    IconButton(
                        onClick = onLogoutClick,
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = SophisticatedTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-header strip with Role pill & Authoritative server handshake
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SophisticatedBadgeBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedDarkBorder)
                    ) {
                        Text(
                            text = "ID: $employeeId",
                            color = SophisticatedTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = roleColor.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, roleColor.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = role,
                            color = roleColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = SophisticatedSuccessContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedSuccessBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(SophisticatedSuccess)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SERVER AUTHORITATIVE",
                            color = SophisticatedSuccess,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GeofenceRadarCard(
    project: ProjectEntity?,
    geofenceResult: ServerGeofenceResult?,
    latitude: Double,
    longitude: Double,
    accuracy: Float,
    isMockLocation: Boolean,
    onRefreshLocation: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isInside = geofenceResult?.isInside == true && geofenceResult.isAccuracyAcceptable && !isMockLocation

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("geofence_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = SophisticatedDarkSurface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedDarkBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CURRENT PROJECT",
                        color = SophisticatedTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = project?.projectName ?: "Loading Project...",
                        color = SophisticatedTextPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = project?.address ?: "",
                        color = SophisticatedTextMuted,
                        fontSize = 12.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = SophisticatedBadgeBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedDarkBorder)
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
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "ACTIVE",
                            color = SophisticatedSuccess,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Location Verified Status Inset Card matching HTML
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = SophisticatedDarkBg.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedDarkBorder.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isInside) SophisticatedPrimaryContainer else SophisticatedErrorContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isInside) Icons.Default.LocationOn else Icons.Default.LocationOff,
                                contentDescription = "Location Pin",
                                tint = if (isInside) SophisticatedPrimary else SophisticatedError,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isInside) "✓ On-Site Verified" else "⚡ Off-Site • Head Office Notified",
                                color = if (isInside) SophisticatedSuccess else Color(0xFFFFB74D),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (isInside)
                                    "Inside ${project?.geofenceRadiusMeters?.toInt() ?: 150}m Project Boundary • Head Office Payroll Synced"
                                else
                                    "${geofenceResult?.distanceMeters?.toInt() ?: 0}m from boundary • Logged for Head Office Payroll review",
                                color = SophisticatedTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onRefreshLocation,
                        modifier = Modifier.testTag("refresh_location_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh GPS Location",
                            tint = SophisticatedPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Radar Visual representation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SophisticatedDarkBg)
                    .border(1.dp, SophisticatedDarkBorder, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "radar")
                val pulseRadius by infiniteTransition.animateFloat(
                    initialValue = 20f,
                    targetValue = 90f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "pulse"
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)

                    // Background grid circles
                    drawCircle(
                        color = Color(0xFF333038),
                        radius = 80f,
                        center = center,
                        style = Stroke(width = 1.5f)
                    )
                    drawCircle(
                        color = Color(0xFF26242B),
                        radius = 45f,
                        center = center,
                        style = Stroke(width = 1.5f)
                    )

                    // Project perimeter circle
                    drawCircle(
                        color = SophisticatedPrimary.copy(alpha = 0.15f),
                        radius = 65f,
                        center = center
                    )
                    drawCircle(
                        color = SophisticatedPrimary.copy(alpha = 0.6f),
                        radius = 65f,
                        center = center,
                        style = Stroke(width = 1.5f)
                    )

                    // Animated scanning pulse
                    drawCircle(
                        color = if (isInside) SophisticatedSuccess.copy(alpha = 0.3f) else SophisticatedError.copy(alpha = 0.3f),
                        radius = pulseRadius,
                        center = center,
                        style = Stroke(width = 1.5f)
                    )

                    // Center site pin
                    drawCircle(
                        color = SophisticatedPrimary,
                        radius = 5f,
                        center = center
                    )

                    // User location relative pin
                    val userOffset = if (isInside) {
                        Offset(center.x + 14f, center.y - 10f)
                    } else {
                        Offset(center.x + 80f, center.y + 12f)
                    }

                    drawCircle(
                        color = if (isInside) SophisticatedSuccess else SophisticatedError,
                        radius = 7f,
                        center = userOffset
                    )
                }

                // Overlay status text
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Radius: ${project?.geofenceRadiusMeters?.toInt() ?: 150}m",
                        color = SophisticatedTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "GPS: ${String.format("%.4f", latitude)}, ${String.format("%.4f", longitude)} (±${accuracy.toInt()}m)",
                        color = SophisticatedTextMuted,
                        fontSize = 9.sp
                    )
                }

                val dist = geofenceResult?.distanceMeters?.toInt() ?: 0
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(50),
                    color = if (isInside) SophisticatedSuccessContainer else Color(0xFF3E2723),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isInside) SophisticatedSuccessBorder else Color(0xFFFFB74D).copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = if (isInside) "On-Site (${dist}m)" else "Off-Site (${dist}m) • Payroll Logged",
                        color = if (isInside) SophisticatedSuccess else Color(0xFFFFB74D),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SelfieCaptureDialog(
    eventType: ShiftEventType,
    projectName: String,
    onDismiss: () -> Unit,
    onCaptureComplete: (selfieHash: String) -> Unit
) {
    CameraXSelfieDialog(
        eventType = eventType,
        projectName = projectName,
        onDismiss = onDismiss,
        onCaptureComplete = onCaptureComplete
    )
}

@Composable
fun AttendanceStatusBadge(state: String) {
    val (bg, fg, border, label) = when (state) {
        AttendanceState.APPROVED.name -> Quadruple(
            SophisticatedSuccessContainer,
            SophisticatedSuccess,
            SophisticatedSuccessBorder,
            "Approved"
        )
        AttendanceState.PENDING_APPROVAL.name -> Quadruple(
            SophisticatedWarningContainer,
            SophisticatedWarning,
            SophisticatedWarning.copy(alpha = 0.4f),
            "Pending Approval"
        )
        AttendanceState.REJECTED.name -> Quadruple(
            SophisticatedErrorContainer,
            SophisticatedError,
            SophisticatedError.copy(alpha = 0.4f),
            "Rejected"
        )
        AttendanceState.FLAGGED.name -> Quadruple(
            SophisticatedErrorContainer,
            SophisticatedWarning,
            SophisticatedWarning.copy(alpha = 0.4f),
            "Flagged"
        )
        AttendanceState.SUBMITTED.name -> Quadruple(
            SophisticatedPrimaryContainer,
            SophisticatedPrimary,
            SophisticatedPrimary.copy(alpha = 0.4f),
            "Submitted"
        )
        else -> Quadruple(
            SophisticatedBadgeBg,
            SophisticatedTextSecondary,
            SophisticatedDarkBorder,
            state
        )
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(1.dp, border)
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

