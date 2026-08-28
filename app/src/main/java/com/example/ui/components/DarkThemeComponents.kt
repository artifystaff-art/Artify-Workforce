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
    /** Standard dark surface card with subtle border */
    Surface,
    /** Elevated darker surface with enhanced contrast */
    Elevated,
    /** Outlined translucent container */
    Outlined,
    /** Highlighted container with subtle primary glow/border */
    Accent,
    /** Low-contrast recessed background container */
    Subtle
}

/**
 * Reusable Material 3 Card tailored to the Artify Workforce Dark Theme specifications.
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
    val containerColor = when (variant) {
        DarkThemeCardVariant.Surface -> SophisticatedDarkSurface
        DarkThemeCardVariant.Elevated -> SophisticatedDarkSurfaceHigh
        DarkThemeCardVariant.Outlined -> Color.Transparent
        DarkThemeCardVariant.Accent -> SophisticatedDarkSurface
        DarkThemeCardVariant.Subtle -> SophisticatedDarkBg
    }

    val border = when (variant) {
        DarkThemeCardVariant.Surface -> BorderStroke(1.dp, SophisticatedDarkBorder)
        DarkThemeCardVariant.Elevated -> BorderStroke(1.dp, SophisticatedDarkBorderLight)
        DarkThemeCardVariant.Outlined -> BorderStroke(1.dp, SophisticatedDarkBorder)
        DarkThemeCardVariant.Accent -> BorderStroke(1.dp, SophisticatedPrimary.copy(alpha = 0.5f))
        DarkThemeCardVariant.Subtle -> BorderStroke(1.dp, SophisticatedDarkBorder.copy(alpha = 0.5f))
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
    val (containerColor, contentColor) = when (variant) {
        DarkThemeButtonVariant.Primary -> Pair(SophisticatedPrimary, SophisticatedOnPrimary)
        DarkThemeButtonVariant.Secondary -> Pair(SophisticatedSecondary, Color(0xFF332D41))
        DarkThemeButtonVariant.Success -> Pair(SophisticatedSuccess, Color(0xFF1D2E1F))
        DarkThemeButtonVariant.Warning -> Pair(SophisticatedWarning, Color(0xFF382500))
        DarkThemeButtonVariant.Error -> Pair(SophisticatedError, Color(0xFF601410))
        DarkThemeButtonVariant.Neutral -> Pair(SophisticatedDarkSurfaceHigh, SophisticatedTextPrimary)
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
            disabledContainerColor = SophisticatedDarkBorder,
            disabledContentColor = SophisticatedTextMuted
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
    val (borderColor, textColor) = when (variant) {
        DarkThemeButtonVariant.Primary -> Pair(SophisticatedPrimary.copy(alpha = 0.6f), SophisticatedPrimary)
        DarkThemeButtonVariant.Secondary -> Pair(SophisticatedSecondary.copy(alpha = 0.6f), SophisticatedSecondary)
        DarkThemeButtonVariant.Success -> Pair(SophisticatedSuccess.copy(alpha = 0.6f), SophisticatedSuccess)
        DarkThemeButtonVariant.Warning -> Pair(SophisticatedWarning.copy(alpha = 0.6f), SophisticatedWarning)
        DarkThemeButtonVariant.Error -> Pair(SophisticatedError.copy(alpha = 0.6f), SophisticatedError)
        DarkThemeButtonVariant.Neutral -> Pair(SophisticatedDarkBorder, SophisticatedTextSecondary)
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
        border = BorderStroke(1.dp, if (enabled) borderColor else SophisticatedDarkBorder.copy(alpha = 0.5f)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = textColor,
            disabledContentColor = SophisticatedTextMuted
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
    color: Color = SophisticatedPrimary,
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
            disabledContentColor = SophisticatedTextMuted
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
 * Reusable Material 3 Icon Button with custom dark-themed circular background option.
 */
@Composable
fun DarkThemeIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = SophisticatedTextSecondary,
    withContainer: Boolean = false,
    containerColor: Color = SophisticatedDarkSurface,
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
            border = BorderStroke(1.dp, SophisticatedDarkBorder)
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
    val (bg, fg, border) = when (statusType) {
        DarkThemeStatusType.Success -> Triple(SophisticatedSuccessContainer, SophisticatedSuccess, SophisticatedSuccessBorder)
        DarkThemeStatusType.Warning -> Triple(SophisticatedWarningContainer, SophisticatedWarning, SophisticatedWarning.copy(alpha = 0.4f))
        DarkThemeStatusType.Error -> Triple(SophisticatedErrorContainer, SophisticatedError, SophisticatedError.copy(alpha = 0.4f))
        DarkThemeStatusType.Primary -> Triple(SophisticatedPrimaryContainer, SophisticatedPrimary, SophisticatedPrimary.copy(alpha = 0.4f))
        DarkThemeStatusType.Secondary -> Triple(SophisticatedBadgeBg, SophisticatedSecondary, SophisticatedSecondary.copy(alpha = 0.3f))
        DarkThemeStatusType.Neutral -> Triple(SophisticatedBadgeBg, SophisticatedTextSecondary, SophisticatedDarkBorder)
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
 * Reusable Dark Theme Outlined Text Field with custom styling and validation borders.
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
                { Text(placeholder, color = SophisticatedTextMuted) }
            } else null,
            leadingIcon = if (leadingIcon != null) {
                { Icon(leadingIcon, contentDescription = null, tint = SophisticatedTextSecondary) }
            } else null,
            trailingIcon = trailingIcon,
            isError = isError,
            singleLine = singleLine,
            maxLines = maxLines,
            readOnly = readOnly,
            shape = shape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = SophisticatedTextPrimary,
                unfocusedTextColor = SophisticatedTextPrimary,
                focusedBorderColor = SophisticatedPrimary,
                unfocusedBorderColor = SophisticatedDarkBorder,
                errorBorderColor = SophisticatedError,
                focusedLabelColor = SophisticatedPrimary,
                unfocusedLabelColor = SophisticatedTextSecondary,
                cursorColor = SophisticatedPrimary,
                focusedContainerColor = SophisticatedDarkSurface,
                unfocusedContainerColor = SophisticatedDarkSurface,
                errorContainerColor = SophisticatedDarkSurface
            )
        )

        if (isError && errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                color = SophisticatedError,
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
        color = SophisticatedDarkSurface,
        border = BorderStroke(1.dp, SophisticatedDarkBorder)
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
                color = SophisticatedTextSecondary,
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
                color = SophisticatedTextPrimary
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = SophisticatedTextSecondary
                )
            }
        }
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    color = SophisticatedPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Reusable Material 3 Dialog Container for the Dark Theme.
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
            color = SophisticatedDarkSurface,
            border = BorderStroke(1.dp, SophisticatedDarkBorder),
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
                            color = SophisticatedTextPrimary
                        )
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                color = SophisticatedTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Dialog",
                            tint = SophisticatedTextSecondary
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
 * Reusable subtle 1dp Divider matching the dark theme palette.
 */
@Composable
fun DarkThemeDivider(
    modifier: Modifier = Modifier,
    color: Color = SophisticatedDarkBorder
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = 1.dp,
        color = color
    )
}
