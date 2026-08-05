package com.levi.meditime.presentation.family

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.levi.meditime.domain.model.FamilyMember
import com.levi.meditime.presentation.theme.*

@Composable
fun FamilyListScreen(
    onBack: () -> Unit,
    onMemberClick: (FamilyMember) -> Unit,
    onAddMember: () -> Unit,
    viewModel: FamilyViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 60.dp, start = Spacing.section, end = Spacing.section, bottom = Spacing.card),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.width(Spacing.card))
                Text(
                    text = "Family",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    bottom = 120.dp, 
                    start = Spacing.section, 
                    end = Spacing.section,
                    top = Spacing.small
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.section)
            ) {
                item {
                    FamilySummaryCard()
                }

                item {
                    SectionTitle(title = "Members")
                }

                if (state.members.isEmpty()) {
                    item {
                        EmptyState(
                            illustration = { 
                                Icon(Icons.Rounded.Groups, null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) 
                            },
                            title = "No family members",
                            description = "Add your loved ones to manage their medication together.",
                            actionText = "Add Member",
                            onActionClick = onAddMember
                        )
                    }
                } else {
                    items(state.members) { member ->
                        FamilyMemberCard(
                            member = member,
                            onClick = { onMemberClick(member) }
                        )
                    }
                }
            }
        }

        // FAB Bottom Right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 120.dp, end = Spacing.section)
        ) {
            PremiumFloatingActionButton(
                icon = Icons.Rounded.Add,
                onClick = onAddMember,
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun FamilySummaryCard() {
    AppCard(
        modifier = Modifier.fillMaxWidth().height(140.dp),
        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
        elevation = 0.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Total Medicines", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("14 Medications", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(Modifier.height(Spacing.medium))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                    Column {
                        Text("Pending", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                        Text("3 Today", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Column {
                        Text("Completed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                        Text("11 Today", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
            Box(
                modifier = Modifier.size(64.dp).background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Group, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
fun FamilyMemberCard(member: FamilyMember, onClick: () -> Unit) {
    AppCard(
        modifier = Modifier.fillMaxWidth().height(90.dp),
        onClick = onClick,
        elevation = Shadow.card
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.size(56.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                AsyncImage(
                    model = "https://ui-avatars.com/api/?name=${member.name}&background=4C8DFF&color=fff",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(Modifier.width(Spacing.card))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(member.name, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodyLarge, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    text = "${member.relationship.name} • 3 Medicines", 
                    color = MaterialTheme.colorScheme.onSurfaceVariant, 
                    style = MaterialTheme.typography.labelSmall
                )
            }
            
            StatusBadge("Next: 08:30 PM", MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), MaterialTheme.colorScheme.primary)
            
            Spacer(Modifier.width(Spacing.small))
            Icon(Icons.Rounded.ChevronRight, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
        }
    }
}

@Composable
fun StatusBadge(label: String, bgColor: Color, textColor: Color) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label, 
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
