package com.levi.meditime.presentation.theme

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.levi.meditime.presentation.navigation.Screen

// Amber Vigil Constants
object GessoRadius {
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val full = 9999.dp
}

object GessoSpacing {
    val s1 = 8.dp
    val s2 = 16.dp
    val s3 = 24.dp
    val s4 = 32.dp
    val s6 = 48.dp
    val s8 = 64.dp
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    elevation: Dp = 1.dp,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(GessoRadius.md),
    onClick: (() -> Unit)? = null,
    border: Boolean = true,
    contentPadding: PaddingValues? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val cardModifier = modifier
        .scale(scale)
        .shadow(elevation, shape)
        .background(backgroundColor, shape)
        .then(
            if (border) Modifier.border(1.dp, borderColor, shape) else Modifier
        )
        .then(
            if (onClick != null) Modifier.clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            ) else Modifier
        )
        .padding(contentPadding ?: PaddingValues(GessoSpacing.s4))

    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Column(
            modifier = cardModifier,
            content = content
        )
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = GessoPrimary,
    contentColor: Color = Color.Black,
    icon: ImageVector? = null,
    loading: Boolean = false,
    enabled: Boolean = true,
    useGradient: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "btnScale")

    val backgroundModifier = if (useGradient) {
        Modifier.background(
            Brush.horizontalGradient(listOf(GessoPrimary, GessoSecondary)),
            RoundedCornerShape(GessoRadius.md)
        )
    } else {
        Modifier.background(containerColor, RoundedCornerShape(GessoRadius.md))
    }

    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier
            .height(64.dp)
            .scale(scale)
            .then(backgroundModifier),
        shape = RoundedCornerShape(GessoRadius.md),
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = contentColor,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = contentColor.copy(alpha = 0.5f)
        ),
        enabled = enabled && !loading,
        contentPadding = PaddingValues(horizontal = GessoSpacing.s6)
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = contentColor, strokeWidth = 2.dp)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(icon, null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(GessoSpacing.s1))
                }
                Text(
                    text = text, 
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "secBtnScale")

    OutlinedButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier
            .height(64.dp)
            .scale(scale),
        shape = RoundedCornerShape(GessoRadius.md),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
        interactionSource = interactionSource,
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
        contentPadding = PaddingValues(horizontal = GessoSpacing.s6)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(GessoSpacing.s1))
            }
            Text(
                text = text, 
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun GessoInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingIcon = leadingIcon?.let { { Icon(it, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) } },
        trailingIcon = trailingIcon,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(GessoRadius.md),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            errorContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        textStyle = MaterialTheme.typography.bodyLarge
    )
}

@Composable
fun GessoBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(GessoRadius.full),
        modifier = modifier
    ) {
        Text(
            text = text.uppercase(),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            letterSpacing = 0.04.sp
        )
    }
}

@Composable
fun EmptyState(
    illustration: @Composable () -> Unit,
    title: String,
    description: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        illustration()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(24.dp))
            PrimaryButton(text = actionText, onClick = onActionClick)
        }
    }
}

@Composable
fun AnimatedFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Rounded.Add,
    text: String? = null
) {
    val haptic = LocalHapticFeedback.current
    FloatingActionButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.Black,
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (text != null) 20.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            if (text != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = text, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TimeWheelPicker(
    modifier: Modifier = Modifier,
    initialHour: Int = 8,
    initialMinute: Int = 0,
    onTimeSelected: (Int, Int) -> Unit
) {
    var hour by remember { mutableStateOf(initialHour) }
    var minute by remember { mutableStateOf(initialMinute) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(onClick = { 
            hour = (hour + 1) % 24
            onTimeSelected(hour, minute)
        }) {
            Text(text = String.format("%02d", hour), style = MaterialTheme.typography.headlineMedium)
        }
        Text(" : ", style = MaterialTheme.typography.headlineMedium)
        OutlinedButton(onClick = { 
            minute = (minute + 15) % 60
            onTimeSelected(hour, minute)
        }) {
            Text(text = String.format("%02d", minute), style = MaterialTheme.typography.headlineMedium)
        }
    }
}

fun Modifier.dashedBorder(
    color: Color,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(GessoRadius.md),
    strokeWidth: Dp = 1.dp,
    dashLength: Dp = 4.dp,
    gapLength: Dp = 4.dp
): Modifier = this.drawBehind {
    val stroke = Stroke(
        width = strokeWidth.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength.toPx(), gapLength.toPx()), 0f)
    )
    val outline = shape.createOutline(size, layoutDirection, this)
    drawOutline(outline, color, style = stroke)
}

@Composable
fun ShimmerItem(
    modifier: Modifier = Modifier,
    height: Dp = 80.dp,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(GessoRadius.md)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(MaterialTheme.colorScheme.surfaceVariant, shape)
    )
}

@Composable
fun FloatingBottomNavigation(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onAddMedicine: () -> Unit,
    onScanPrescription: () -> Unit,
    onAddAppointment: () -> Unit,
    onAddFamily: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isExpanded by remember { mutableStateOf(false) }
    
    val items = listOf(
        NavigationItem("Home", Screen.Dashboard.route, Icons.Rounded.Home, Icons.Rounded.Home),
        NavigationItem("Meds", Screen.Medicines.route, Icons.Rounded.Medication, Icons.Rounded.Medication),
        NavigationItem(null, "", Icons.Rounded.Add, Icons.Rounded.Add),
        NavigationItem("Stats", Screen.Reports.route, Icons.Rounded.Assessment, Icons.Rounded.Assessment),
        NavigationItem("Profile", Screen.Account.route, Icons.Rounded.Person, Icons.Rounded.Person)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
        ) {
            AppCard(
                modifier = Modifier.padding(bottom = 80.dp),
                border = true,
                elevation = 4.dp
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    FabMenuItem("Add Medicine", Icons.Rounded.AddCircle, GessoPrimary) {
                        onAddMedicine()
                        isExpanded = false
                    }
                    FabMenuItem("Scan Prescription", Icons.Rounded.QrCodeScanner, GessoAccentText) {
                        onScanPrescription()
                        isExpanded = false
                    }
                    FabMenuItem("Doctor Appointment", Icons.Rounded.MedicalServices, GessoSecondary) {
                        onAddAppointment()
                        isExpanded = false
                    }
                    FabMenuItem("Add Family", Icons.Rounded.GroupAdd, GessoSuccess) {
                        onAddFamily()
                        isExpanded = false
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .height(64.dp)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shape = RoundedCornerShape(GessoRadius.full),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    if (item.title == null) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        val selected = currentRoute == item.route
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (currentRoute != item.route) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onNavigate(item.route)
                                        isExpanded = false
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title,
                                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .offset(y = (-14).dp)
                .size(60.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isExpanded = !isExpanded
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Add",
                tint = Color.Black,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun FabMenuItem(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = color.copy(alpha = 0.12f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.width(16.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

data class NavigationItem(
    val title: String?,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = GessoSpacing.s2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onActionClick() }
            )
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = { 
        Icon(
            Icons.Rounded.ChevronRight, 
            null, 
            modifier = Modifier.size(24.dp), 
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        ) 
    }
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(GessoRadius.sm),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurface)
            }
        }
        Spacer(modifier = Modifier.width(GessoSpacing.s2))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (trailing != null) {
            trailing()
        }
    }
}

@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    elevation: Dp = 1.dp,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(GessoRadius.md),
    onClick: (() -> Unit)? = null,
    border: Boolean = true,
    contentPadding: PaddingValues? = null,
    content: @Composable (ColumnScope.() -> Unit)
) = AppCard(modifier, backgroundColor, contentColor, elevation, shape, onClick, border, contentPadding, content)

@Composable
fun PremiumButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.Black,
    enabled: Boolean = true
) = PrimaryButton(text, onClick, modifier, containerColor, contentColor, icon, false, enabled, false)

@Composable
fun PremiumFloatingActionButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary
) {
    val haptic = LocalHapticFeedback.current
    FloatingActionButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier.size(56.dp),
        containerColor = containerColor,
        contentColor = Color.Black,
        shape = CircleShape,
        elevation = FloatingActionButtonDefaults.elevation(2.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light Mode")
@Composable
fun DesignSystemPreview() {
    MediTimeMedicineReminderTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionTitle(title = "Design System Components")
                AppCard {
                    Text("This is a standard card", style = MaterialTheme.typography.bodyLarge)
                }
                PrimaryButton(text = "Primary Button", onClick = {})
                SecondaryButton(text = "Secondary Button", onClick = {})
                GessoInput(value = "", onValueChange = {}, placeholder = "Input Field")
                GessoBadge(text = "Status Badge")
                SettingsItem(icon = Icons.Rounded.Settings, title = "Settings Item", subtitle = "This is a subtitle")
            }
        }
    }
}
