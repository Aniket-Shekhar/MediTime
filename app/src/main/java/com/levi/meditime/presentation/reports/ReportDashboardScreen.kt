package com.levi.meditime.presentation.reports

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.levi.meditime.R
import com.levi.meditime.presentation.theme.*

@Composable
fun ReportDashboardScreen(
    onBack: (() -> Unit)? = null,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val context = LocalContext.current

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                // v3.0 Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp, start = 24.dp, end = 24.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.padding(end = 12.dp).border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Analytics",
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Health summary for you & family",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (state.reportFile != null) {
                        IconButton(
                            onClick = { 
                                try {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "com.Digifluence.MediTime.provider",
                                        state.reportFile!!
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Report"))
                                } catch (e: Exception) {
                                    android.util.Log.e("SHARE_ERROR", "Error: ${e.message}")
                                }
                            },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape).size(48.dp).border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(Icons.Rounded.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.width(12.dp))
                    }

                    IconButton(
                        onClick = { viewModel.exportReport() },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape).size(48.dp).border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), CircleShape)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Rounded.PictureAsPdf, contentDescription = "Export", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Period Selector
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimePeriod.entries.forEach { period ->
                        FilterChip(
                            selected = state.selectedPeriod == period,
                            onClick = { viewModel.setPeriod(period) },
                            label = { Text(period.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = null
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        bottom = 120.dp, 
                        start = 24.dp, 
                        end = 24.dp,
                        top = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // v3.0 Adherence Card (180dp height)
                    item {
                        AppCard(
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                            elevation = 4.dp
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Overall Adherence", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("Consistency is key!", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                    }
                                    Text("${state.adherence}%", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.ExtraBold)
                                }
                                
                                Spacer(Modifier.weight(1f))
                                
                                // v3.1 Animated Adherence Chart
                                AdherenceChart(
                                    points = state.chartPoints,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp)
                                )
                            }
                        }
                    }

                    // v3.0 Stats Grid
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            ReportStatCardSmall(
                                label = "Total", 
                                value = "${state.takenCount + state.missedCount}", 
                                icon = Icons.Rounded.List, 
                                color = if (isDark) InfoDark else InfoBlue, 
                                modifier = Modifier.weight(1f)
                            )
                            ReportStatCardSmall(
                                label = "Taken", 
                                value = "${state.takenCount}", 
                                icon = Icons.Rounded.CheckCircle, 
                                color = if (isDark) SuccessDark else SuccessGreen, 
                                modifier = Modifier.weight(1f)
                            )
                            ReportStatCardSmall(
                                label = "Missed", 
                                value = "${state.missedCount}", 
                                icon = Icons.Rounded.Cancel, 
                                color = if (isDark) ErrorDark else DangerRed,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        SectionTitle(title = "Health Insights")
                    }
                    
                    items(2) {
                        InsightCard()
                    }

                    item {
                        PrimaryButton(
                            text = "Download PDF Report", 
                            onClick = { viewModel.exportReport() }, 
                            useGradient = true,
                            icon = Icons.Rounded.FileDownload
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdherenceChart(points: List<ChartPoint>, modifier: Modifier = Modifier) {
    val spacing = 8.dp
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.Bottom
    ) {
        points.forEach { point ->
            val animatedHeight by animateFloatAsState(
                targetValue = point.value,
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                label = "height"
            )

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(animatedHeight.coerceAtLeast(0.05f))
                        .background(
                            Color.White.copy(alpha = if (point.value > 0.8f) 1f else if (point.value > 0.5f) 0.6f else 0.3f),
                            RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                        )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ReportStatCardSmall(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier.heightIn(min = 130.dp),
        backgroundColor = MaterialTheme.colorScheme.surface,
        border = true,
        elevation = 1.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(), 
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically), 
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, modifier = Modifier.size(24.dp), tint = color)
            
            Text(
                text = value, 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Bold, 
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            
            Text(
                text = label, 
                style = MaterialTheme.typography.labelSmall, 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun InsightCard() {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val successColor = if (isDark) SuccessDark else SuccessGreen
    
    AppCard(modifier = Modifier.fillMaxWidth(), border = true, elevation = 0.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(Radius.small),
                color = successColor.copy(alpha = 0.1f)
            ) {
                Icon(Icons.Rounded.TipsAndUpdates, null, modifier = Modifier.padding(10.dp), tint = successColor)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(stringResource(R.string.keep_it_up), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(stringResource(R.string.adherence_insight), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
