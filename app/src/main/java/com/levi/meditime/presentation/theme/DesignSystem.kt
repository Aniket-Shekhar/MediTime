package com.levi.meditime.presentation.theme

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
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

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    elevation: Dp = Shadow.card,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(Radius.large),
    onClick: (() -> Unit)? = null,
    border: Boolean = false,
    contentPadding: PaddingValues? = null,
    isGlass: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val accessibilityMode = LocalAccessibilityMode.current
    val haptic = LocalHapticFeedback.current
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    var cardModifier = modifier
        .scale(scale)
        .then(if (elevation > 0.dp && !isGlass) Modifier.shadow(elevation, shape) else Modifier)
        .clip(shape)
    
    cardModifier = if (isGlass) {
        cardModifier.background(
            Brush.linearGradient(listOf(BentoCardGradientStart.copy(alpha = 0.9f), BentoCardGradientEnd.copy(alpha = 0.8f)))
        )
    } else {
        cardModifier.background(backgroundColor)
    }
    
    if (border || isGlass) {
        cardModifier = cardModifier.border(
            width = 1.dp, 
            color = if (isGlass) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
            shape = shape
        )
    }
    
    if (onClick != null) {
        cardModifier = cardModifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
        )
    }

    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Column(
            modifier = cardModifier.then(
                if (contentPadding != null) Modifier.padding(contentPadding)
                else Modifier.padding(if (accessibilityMode) Spacing.section else Spacing.card)
            ),
            content = content
        )
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.White,
    icon: ImageVector? = null,
    loading: Boolean = false,
    enabled: Boolean = true,
    useGradient: Boolean = false
) {
    val accessibilityMode = LocalAccessibilityMode.current
    val haptic = LocalHapticFeedback.current
    val height = if (accessibilityMode) 72.dp else 56.dp
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "btnScale")

    val buttonModifier = modifier
        .height(height)
        .fillMaxWidth()

    val finalModifier = if (useGradient && enabled && !loading) {
        buttonModifier.background(
            Brush.horizontalGradient(listOf(PrimaryTeal, PrimaryTealDark)),
            RoundedCornerShape(Radius.small)
        )
    } else {
        buttonModifier
    }

    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = finalModifier,
        shape = RoundedCornerShape(Radius.small),
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (useGradient) Color.Transparent else containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.3f),
            disabledContentColor = contentColor.copy(alpha = 0.5f)
        ),
        enabled = enabled && !loading,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        // Inner Box with scale ensures the visual press effect doesn't shrink the actual Button hit target
        Box(
            modifier = Modifier.fillMaxSize().scale(scale),
            contentAlignment = Alignment.Center
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = contentColor, strokeWidth = 2.dp)
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically, 
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (icon != null) {
                        Icon(icon, null, modifier = Modifier.size(if (accessibilityMode) 32.dp else 22.dp))
                        Spacer(Modifier.width(Spacing.small))
                    }
                    Text(
                        text = text, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = if (accessibilityMode) 20.sp else 16.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
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
    val accessibilityMode = LocalAccessibilityMode.current
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
            .height(if (accessibilityMode) 72.dp else 56.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(Radius.small),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        interactionSource = interactionSource,
        enabled = enabled,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().scale(scale),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically, 
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(if (accessibilityMode) 32.dp else 22.dp))
                    Spacer(Modifier.width(Spacing.small))
                }
                Text(
                    text = text, 
                    fontWeight = FontWeight.SemiBold, 
                    fontSize = if (accessibilityMode) 20.sp else 16.sp, 
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
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
            .padding(horizontal = 16.dp, vertical = 24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Expandable Menu Overlay
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
        ) {
            Column(
                modifier = Modifier
                    .padding(bottom = 90.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FabMenuItem("Add Medicine", Icons.Rounded.AddCircle, PrimaryTeal) {
                    onAddMedicine()
                    isExpanded = false
                }
                FabMenuItem("Scan Prescription", Icons.Rounded.QrCodeScanner, InfoBlue) {
                    onScanPrescription()
                    isExpanded = false
                }
                FabMenuItem("Doctor Appointment", Icons.Rounded.MedicalServices, WarningOrange) {
                    onAddAppointment()
                    isExpanded = false
                }
                FabMenuItem("Add Family", Icons.Rounded.GroupAdd, SuccessGreen) {
                    onAddFamily()
                    isExpanded = false
                }
            }
        }

        Surface(
            modifier = Modifier
                .height(84.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(Radius.bottomNav),
                    spotColor = Color.Black.copy(alpha = 0.15f)
                ),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(Radius.bottomNav)
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
                        val scale by animateFloatAsState(
                            targetValue = if (selected) 1.15f else 1f,
                            animationSpec = tween(Motion.normal),
                            label = "navScale"
                        )
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(Radius.bottomNav))
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
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.scale(scale)
                            ) {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title,
                                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Center FAB
        val rotation by animateFloatAsState(if (isExpanded) 45f else 0f, label = "fabRotation")
        Box(
            modifier = Modifier
                .offset(y = (-30).dp)
                .size(64.dp)
                .shadow(Shadow.fab, CircleShape)
                .background(
                    Brush.verticalGradient(listOf(PrimaryTeal, PrimaryTealDark)),
                    CircleShape
                )
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isExpanded = !isExpanded
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Add",
                tint = Color.White,
                modifier = Modifier
                    .size(36.dp)
                    .graphicsLayer(rotationZ = rotation)
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
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = color.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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
            .padding(horizontal = Spacing.tiny, vertical = Spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
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
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    AppCard(
        modifier = modifier.height(100.dp),
        backgroundColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(Radius.medium),
        elevation = Shadow.card,
        border = true,
        contentPadding = PaddingValues(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            
            Text(
                text = value, 
                fontWeight = FontWeight.ExtraBold, 
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp,
                maxLines = 1
            )
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = label, 
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
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
    val accessibilityMode = LocalAccessibilityMode.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        illustration()
        Spacer(Modifier.height(Spacing.section))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(Spacing.small))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionText != null && onActionClick != null) {
            Spacer(Modifier.height(Spacing.section))
            PrimaryButton(text = actionText, onClick = onActionClick, modifier = Modifier.width(if (accessibilityMode) 280.dp else 220.dp))
        }
    }
}

@Composable
fun ShimmerItem(
    modifier: Modifier = Modifier,
    height: Dp = 100.dp,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(Radius.medium)
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = androidx.compose.ui.geometry.Offset.Zero,
        end = androidx.compose.ui.geometry.Offset(x = translateAnim, y = translateAnim)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(brush, shape)
    )
}

@Composable
fun ErrorBanner(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
        border = true,
        elevation = 0.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.ErrorOutline, null, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.width(Spacing.card))
            Text(
                text = message, 
                style = MaterialTheme.typography.bodySmall, 
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onRetry) {
                Text("Retry", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun TimeWheelPicker(
    onTimeSelected: (Int, Int) -> Unit
) {
    var selectedHour by remember { mutableIntStateOf(8) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var isPm by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        WheelColumn(
            items = (1..12).toList(),
            initialIndex = 7,
            onItemSelected = { selectedHour = it }
        )
        Text(":", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = Spacing.small))
        WheelColumn(
            items = (0..59).map { it.toString().padStart(2, '0') },
            initialIndex = 0,
            onItemSelected = { selectedMinute = it.toInt() }
        )
        Spacer(Modifier.width(Spacing.card))
        Column {
            Text(
                "AM",
                modifier = Modifier
                    .clickable { isPm = false }
                    .padding(Spacing.small),
                color = if (!isPm) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (!isPm) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                "PM",
                modifier = Modifier
                    .clickable { isPm = true }
                    .padding(Spacing.small),
                color = if (isPm) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isPm) FontWeight.Bold else FontWeight.Normal
            )
        }
    }

    LaunchedEffect(selectedHour, selectedMinute, isPm) {
        val hour24 = if (isPm) {
            if (selectedHour == 12) 12 else selectedHour + 12
        } else {
            if (selectedHour == 12) 0 else selectedHour
        }
        onTimeSelected(hour24, selectedMinute)
    }
}

@Composable
fun <T> WheelColumn(
    items: List<T>,
    initialIndex: Int,
    onItemSelected: (T) -> Unit
) {
    val itemHeight = 40.dp
    val listState = rememberLazyListState(initialIndex)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val density = androidx.compose.ui.platform.LocalDensity.current

    Box(
        modifier = Modifier
            .width(60.dp)
            .height(itemHeight * 3),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(Radius.small))
        )

        LazyColumn(
            state = listState,
            flingBehavior = snapFlingBehavior,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(items.size + 2) { index ->
                if (index == 0 || index == items.size + 1) {
                    Spacer(modifier = Modifier.height(itemHeight))
                } else {
                    val item = items[index - 1]
                    Text(
                        text = item.toString(),
                        modifier = Modifier
                            .height(itemHeight)
                            .wrapContentHeight(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val itemHeightPx = with(density) { itemHeight.toPx() }
            val index = listState.firstVisibleItemIndex + 
                if (listState.firstVisibleItemScrollOffset > itemHeightPx / 2) 1 else 0
            
            if (index in items.indices) {
                onItemSelected(items[index])
            }
        }
    }
}

fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: Dp = 1.dp,
    dashLength: Dp = 4.dp,
    gapLength: Dp = 4.dp,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(Radius.medium)
) = this.drawBehind {
    val stroke = Stroke(
        width = strokeWidth.toPx(),
        pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(dashLength.toPx(), gapLength.toPx()),
            0f
        )
    )
    
    val outline = shape.createOutline(size, layoutDirection, this)
    drawOutline(outline, color, style = stroke)
}
@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    elevation: Dp = Shadow.card,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(Radius.large),
    onClick: (() -> Unit)? = null,
    border: Boolean = false,
    contentPadding: PaddingValues? = null,
    isGlass: Boolean = false,
    content: @Composable (ColumnScope.() -> Unit)
) = AppCard(modifier, backgroundColor, contentColor, elevation, shape, onClick, border, contentPadding, isGlass, content)

@Composable
fun PremiumButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.White,
    enabled: Boolean = true
) = PrimaryButton(text, onClick, modifier, containerColor, contentColor, icon, false, enabled)

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
        modifier = modifier.shadow(Shadow.fab, CircleShape),
        containerColor = containerColor,
        contentColor = Color.White,
        shape = CircleShape,
        elevation = FloatingActionButtonDefaults.elevation(0.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
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
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        ) 
    }
) {
    val accessibilityMode = LocalAccessibilityMode.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (accessibilityMode) 80.dp else 64.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(Radius.small),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.width(Spacing.card))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
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
fun AnimatedFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    FloatingActionButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier
            .padding(Spacing.card)
            .size(64.dp),
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(Shadow.fab)
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(32.dp))
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun DesignSystemPreview() {
    MediTimeMedicineReminderTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionTitle(title = "Design System Components")
                AppCard {
                    Text("This is a standard card", modifier = Modifier.padding(16.dp))
                }
                PrimaryButton(text = "Primary Button", onClick = {})
                SecondaryButton(text = "Secondary Button", onClick = {})
                SettingsItem(icon = Icons.Rounded.Settings, title = "Settings Item", subtitle = "This is a subtitle")
            }
        }
    }
}
