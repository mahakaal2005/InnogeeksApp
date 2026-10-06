package com.example.innogeeks.feature_domains.presentation.domains.components

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.innogeeks.core.presentation.components.SectionLabel
import com.example.innogeeks.core.presentation.components.StatTile
import com.example.innogeeks.core.domain.model.Domain
import com.example.innogeeks.core.domain.model.DomainMember
import com.example.innogeeks.core.domain.model.DomainMemberRole
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.delay

// Everything shown on a domain's detail page.
@Composable
fun DomainDetail(
    domain: Domain,
    accent: Color,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        DomainSignatureIcon(domainId = domain.id, accent = accent, hazeState = hazeState, height = 140.dp)

        val coordinators = domain.members.filter { it.role == DomainMemberRole.COORDINATOR }
        val team = domain.members.filter { it.role == DomainMemberRole.TEAM }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(value = domain.memberCount, caption = "Members", hazeState = hazeState, modifier = Modifier.weight(1f))
            StatTile(value = coordinators.size, caption = "Coordinators", hazeState = hazeState, modifier = Modifier.weight(1f))
            StatTile(value = team.size, caption = "Core Team", hazeState = hazeState, modifier = Modifier.weight(1f))
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionLabel("Tech Stack")
            StaggeredChipFlow(items = domain.techStack, accent = accent)
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SectionLabel("About")
            Text(
                text = domain.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (team.isNotEmpty()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("Core Team")
                team.forEach { member ->
                    MemberRow(name = member.name, initials = member.initials, accent = accent, subtitle = "3rd Year · ${domain.name}")
                }
            }
        }

        if (coordinators.isNotEmpty()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("Coordinators")
                coordinators.forEach { member ->
                    MemberRow(name = member.name, initials = member.initials, accent = accent, subtitle = "2nd Year · ${domain.name}")
                }
            }
        }
    }
}

// Chips pop in one after another, wrapping onto as many lines as needed.
@Composable
private fun StaggeredChipFlow(
    items: List<String>,
    accent: Color,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items.forEachIndexed { index, item ->
            var visible by remember(items) { mutableStateOf(false) }
            LaunchedEffect(items) {
                delay(60L * index)
                visible = true
            }
            val scale by animateFloatAsState(
                targetValue = if (visible) 1f else 0.7f,
                animationSpec = tween(260),
                label = "chipScale"
            )
            val alpha by animateFloatAsState(
                targetValue = if (visible) 1f else 0f,
                animationSpec = tween(260),
                label = "chipAlpha"
            )

            Text(
                text = item,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
                    .clip(RoundedCornerShape(percent = 50))
                    .background(accent.copy(alpha = 0.14f))
                    .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(percent = 50))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }
    }
}

// One row per core team/coordinator/team member — same avatar-and-caption shape as the old lead footer.
@Composable
private fun MemberRow(
    name: String,
    initials: String,
    accent: Color,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.18f))
                .border(1.dp, accent.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 620)
@Composable
private fun DomainDetailPreview() {
    InnogeeksTheme {
        DomainDetail(
            domain = Domain(
                id = "webd",
                name = "Web Dev",
                tagline = "React, Node & everything between",
                description = "Web Dev builds and maintains all of Innogeeks' web-facing tools, from the club site to event portals.",
                accentIndex = 0,
                memberCount = 18,
                techStack = listOf("React", "Node.js", "Tailwind", "MongoDB", "TypeScript"),
                members = listOf(
                    DomainMember("Priya Sharma", "PS", DomainMemberRole.COORDINATOR),
                    DomainMember("Rahul Deshmukh", "RD", DomainMemberRole.COORDINATOR),
                    DomainMember("Ananya Iyer", "AI", DomainMemberRole.TEAM)
                )
            ),
            accent = MaterialTheme.colorScheme.primary,
            hazeState = remember { HazeState() },
            modifier = Modifier.padding(16.dp)
        )
    }
}
