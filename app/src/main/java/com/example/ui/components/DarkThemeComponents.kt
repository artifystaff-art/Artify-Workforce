package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

/**
 * Visual styling variants for [DarkThemeCard].
 */
enum class DarkThemeCardVariant {
    /** Standard surface card with subtle border */
    Surface,
    /** Elevated surface with enhanced contrast */
    Elevated,
    /** Outlined translucent container */
    Outlined,
    /** Highlighted container with subtle primary glow/border */
    Accent,
    /** Low-contrast recessed background container */
    Subtle
}

/**
 * Reusable Material 3 Card tailored to the Artify Workforce specifications with Light & Dark support.
 */
@Composable
fun DarkThemeCard(
    modifier: Modifier = Modifier,
    variant: DarkThemeCardVariant = DarkThemeCardVariant.Surface,
    shape: Shape = RoundedCornerShape(20.dp),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    testTag: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val containerColor = when (variant) {
        DarkThemeCardVariant.Surface -> MaterialTheme.colorScheme.surface
        DarkThemeCardVariant.Elevated -> MaterialTheme.colorScheme.surfaceVariant
        DarkThemeCardVariant.Outlined -> Color.Transparent
        DarkThemeCardVariant.Accent -> MaterialTheme.colorScheme.surface
        DarkThemeCardVariant.Subtle -> if (isDark) SophisticatedDarkBg else SophisticatedLightBg
    }

    val border = when (variant) {
        DarkThemeCardVariant.Surface -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        DarkThemeCardVariant.Elevated -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
        DarkThemeCardVariant.Outlined -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
        DarkThemeCardVariant.Accent -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
        DarkThemeCardVariant.Subtle -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    }

    val cardModifier = modifier
        .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = cardModifier,
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = border,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(contentPadding),
                content = content
            )
        }
    } else {
        Card(
            modifier = cardModifier,
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = border,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(contentPadding),
                content = content
            )
        }
    }
}

/**
 * Visual variants for buttons in the Artify Workforce design system.
 */
enum class DarkThemeButtonVariant {
    Primary,
    Secondary,
    Success,
    Warning,
    Error,
    Neutral
}

/**
 * Size scale for [DarkThemeButton] and [DarkThemeOutlinedButton].
 */
enum class DarkThemeButtonSize(
    val height: Dp,
    val horizontalPadding: Dp,
    val fontSize: androidx.compose.ui.unit.TextUnit,
    val iconSize: Dp
) {
    Small(height = 36.dp, horizontalPadding = 12.dp, fontSize = 11.sp, iconSize = 14.dp),
    Medium(height = 46.dp, horizontalPadding = 20.dp, fontSize = 13.sp, iconSize = 18.dp),
    Large(height = 54.dp, horizontalPadding = 24.dp, fontSize = 14.sp, iconSize = 20.dp)
}

/**
 * Reusable Material 3 Filled Button following Artify Workforce branding guidelines.
 */
@Composable
fun DarkThemeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: DarkThemeButtonVariant = DarkThemeButtonVariant.Primary,
    size: DarkThemeButtonSize = DarkThemeButtonSize.Medium,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    shape: Shape = RoundedCornerShape(50),
    testTag: String? = null
) {
    val isDark = LocalIsDarkTheme.current
    val (containerColor, contentColor) = when (variant) {
        DarkThemeButtonVariant.Primary -> Pair(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
        DarkThemeButtonVariant.Secondary -> Pair(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.onSecondary)
        DarkThemeButtonVariant.Success -> Pair(if (isDark) SophisticatedSuccess else SophisticatedLightSuccess, if (isDark) Color(0xFF1D2E1F) else Color.White)
        DarkThemeButtonVariant.Warning -> Pair(if (isDark) SophisticatedWarning else SophisticatedLightWarning, if (isDark) Color(0xFF382500) else Color.White)
        DarkThemeButtonVariant.Error -> Pair(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.onError)
        DarkThemeButtonVariant.Neutral -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    val buttonModifier = modifier
        .height(size.height)
        .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)

    Button(
        onClick = onClick,
        modifier = buttonModifier,
        enabled = enabled && !isLoading,
        shape = shape,
        contentPadding = PaddingValues(horizontal = size.horizontalPadding),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.outlineVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(size.iconSize),
                color = contentColor,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Processing...",
                fontSize = size.fontSize,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(size.iconSize)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    fontSize = size.fontSize,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(size.iconSize)
                    )
                }
            }
        }
    }
}

/**
 * Reusable Material 3 Outlined Button following Artify Workforce branding guidelines.
 */
@Composable
fun DarkThemeOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: DarkThemeButtonVariant = DarkThemeButtonVariant.Neutral,
    size: DarkThemeButtonSize = DarkThemeButtonSize.Medium,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    shape: Shape = RoundedCornerShape(50),
    testTag: String? = null
) {
    val isDark = LocalIsDarkTheme.current
    val (borderColor, textColor) = when (variant) {
        DarkThemeButtonVariant.Primary -> Pair(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), MaterialTheme.colorScheme.primary)
        DarkThemeButtonVariant.Secondary -> Pair(MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f), MaterialTheme.colorScheme.secondary)
        DarkThemeButtonVariant.Success -> Pair(
            (if (isDark) SophisticatedSuccess else SophisticatedLightSuccess).copy(alpha = 0.6f),
            if (isDark) SophisticatedSuccess else SophisticatedLightSuccess
        )
        DarkThemeButtonVariant.Warning -> Pair(
            (if (isDark) SophisticatedWarning else SophisticatedLightWarning).copy(alpha = 0.6f),
            if (isDark) SophisticatedWarning else SophisticatedLightWarning
        )
        DarkThemeButtonVariant.Error -> Pair(MaterialTheme.colorScheme.error.copy(alpha = 0.6f), MaterialTheme.colorScheme.error)
        DarkThemeButtonVariant.Neutral -> Pair(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    val buttonModifier = modifier
        .height(size.height)
        .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)

    OutlinedButton(
        onClick = onClick,
        modifier = buttonModifier,
        enabled = enabled && !isLoading,
        shape = shape,
        contentPadding = PaddingValues(horizontal = size.horizontalPadding),
        border = BorderStroke(1.dp, if (enabled) borderColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = textColor,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(size.iconSize),
                color = textColor,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Processing...",
                fontSize = size.fontSize,
                fontWeight = FontWeight.SemiBold
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(size.iconSize)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    fontSize = size.fontSize,
                    fontWeight = FontWeight.SemiBold
                )
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(size.iconSize)
                    )
                }
            }
        }
    }
}

/**
 * Reusable Material 3 Text Button.
 */
@Composable
fun DarkThemeTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    testTag: String? = null
) {
    val buttonModifier = modifier
        .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)

    TextButton(
        onClick = onClick,
        modifier = buttonModifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = color,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Reusable Material 3 Icon Button with circular background option.
 */
@Composable
fun DarkThemeIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    withContainer: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    testTag: String? = null
) {
    val buttonModifier = modifier
        .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)

    if (withContainer) {
        Surface(
            onClick = onClick,
            modifier = buttonModifier.size(40.dp),
            shape = CircleShape,
            color = containerColor,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    } else {
        IconButton(
            onClick = onClick,
            modifier = buttonModifier
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint
            )
        }
    }
}

/**
 * Status types for badge/chip rendering.
 */
enum class DarkThemeStatusType {
    Success,
    Warning,
    Error,
    Primary,
    Secondary,
    Neutral
}

/**
 * Reusable Status Chip / Badge component with pill shape, dot indicator, and brand colors.
 */
@Composable
fun DarkThemeStatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    statusType: DarkThemeStatusType = DarkThemeStatusType.Neutral,
    showDot: Boolean = true,
    icon: ImageVector? = null
) {
    val isDark = LocalIsDarkTheme.current
    val (bg, fg, border) = when (statusType) {
        DarkThemeStatusType.Success -> Triple(
            if (isDark) SophisticatedSuccessContainer else SophisticatedLightSuccessContainer,
            if (isDark) SophisticatedSuccess else SophisticatedLightSuccess,
            if (isDark) SophisticatedSuccessBorder else SophisticatedLightSuccessBorder
        )
        DarkThemeStatusType.Warning -> Triple(
            if (isDark) SophisticatedWarningContainer else SophisticatedLightWarningContainer,
            if (isDark) SophisticatedWarning else SophisticatedLightWarning,
            (if (isDark) SophisticatedWarning else SophisticatedLightWarning).copy(alpha = 0.4f)
        )
        DarkThemeStatusType.Error -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
        )
        DarkThemeStatusType.Primary -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )
        DarkThemeStatusType.Secondary -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
        )
        DarkThemeStatusType.Neutral -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = bg,
        border = BorderStroke(1.dp, border),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (showDot) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(fg)
                )
                Spacer(modifier = Modifier.width(5.dp))
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = fg,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                color = fg,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            )
        }
    }
}

/**
 * Reusable Outlined Text Field with custom styling and validation borders.
 */
@Composable
fun DarkThemeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    readOnly: Boolean = false,
    shape: Shape = RoundedCornerShape(14.dp),
    testTag: String? = null
) {
    val fieldModifier = modifier
        .fillMaxWidth()
        .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = fieldModifier,
            label = if (label != null) { { Text(label) } } else null,
            placeholder = if (placeholder != null) {
                { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) }
            } else null,
            leadingIcon = if (leadingIcon != null) {
                { Icon(leadingIcon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else null,
            trailingIcon = trailingIcon,
            isError = isError,
            singleLine = singleLine,
            maxLines = maxLines,
            readOnly = readOnly,
            shape = shape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                errorContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        if (isError && errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

/**
 * Reusable Metric Chip / Card to showcase operational KPI numbers.
 */
@Composable
fun DarkThemeMetricCard(
    count: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    testTag: String? = null
) {
    Surface(
        modifier = modifier.then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
            Text(
                text = count,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Reusable Material 3 Section Header.
 */
@Composable
fun DarkThemeSectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Reusable Material 3 Dialog Container.
 */
@Composable
fun DarkThemeDialog(
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Dialog",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                content()
            }
        }
    }
}

/**
 * Reusable subtle 1dp Divider matching the current theme palette.
 */
@Composable
fun DarkThemeDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = 1.dp,
        color = color
    )
}

