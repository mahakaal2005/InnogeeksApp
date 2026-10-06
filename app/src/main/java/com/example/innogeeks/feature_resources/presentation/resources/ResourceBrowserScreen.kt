package com.example.innogeeks.feature_resources.presentation.resources

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.innogeeks.core.presentation.components.SectionLabel
import com.example.innogeeks.core.presentation.components.liquidGlass
import com.example.innogeeks.core.domain.model.Domain
import com.example.innogeeks.feature_resources.domain.model.ResourceItem
import com.example.innogeeks.feature_resources.domain.model.ResourceType
import com.example.innogeeks.feature_resources.presentation.resources.components.ResourceSearchBar
import com.example.innogeeks.feature_resources.presentation.resources.components.accentColor
import com.example.innogeeks.feature_resources.presentation.resources.components.domainIconRes
import com.example.innogeeks.feature_resources.presentation.resources.components.label
import com.example.innogeeks.feature_resources.presentation.resources.components.suggestResources
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

// Per-domain resource feed: hero counts, a type filter bar, and the feed itself
// (grouped by type when "All" is active, flat otherwise) — mirrors specs/UI_CLAUDE/resources_tab.html screen 2.
@Composable
fun ResourceBrowserScreen(
    domain: Domain,
    resources: List<ResourceItem>,
    hazeState: HazeState,
    onBack: () -> Unit,
    onResourceClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    initialQuery: String = ""
) {
    val scheme = MaterialTheme.colorScheme
    var activeType by remember(domain.id) { mutableStateOf<ResourceType?>(null) }
    var query by rememberSaveable(domain.id) { mutableStateOf(initialQuery) }

    val typesPresent = ResourceType.entries.filter { type -> resources.any { it.type == type } }
    val visible = resources.filter { resource ->
        (activeType == null || resource.type == activeType) &&
            (query.isBlank() ||
                resource.title.contains(query, ignoreCase = true) ||
                resource.description.contains(query, ignoreCase = true) ||
                resource.author.contains(query, ignoreCase = true))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .hazeSource(hazeState)
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .liquidGlass(hazeState = hazeState, cornerRadius = 18.dp)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = scheme.onSurface)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = domain.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    )
                    Text(
                        text = "${resources.size} resource${if (resources.size != 1) "s" else ""} · ${typesPresent.size} types",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
                Image(
                    painter = painterResource(id = domainIconRes(domain.id)),
                    contentDescription = null,
                    modifier = Modifier.size(26.dp)
                )
            }

            ResourceSearchBar(
                query = query,
                onQueryChange = { query = it },
                hazeState = hazeState,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(hazeState = hazeState, cornerRadius = 14.dp)
                    .padding(5.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TypeFilterChip(
                    label = "All",
                    isActive = activeType == null,
                    onClick = { activeType = null },
                    modifier = Modifier.weight(1f)
                )
                typesPresent.forEach { type ->
                    TypeFilterChip(
                        label = type.label(),
                        isActive = activeType == type,
                        accent = type.accentColor(),
                        onClick = { activeType = type },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.padding(top = 6.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 4.dp)
        ) {
            if (visible.isEmpty() && query.isNotBlank()) {
                val suggestions = remember(query, resources) { suggestResources(query, resources) }
                if (suggestions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No matches for \"$query\".",
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(modifier = Modifier.padding(top = 24.dp)) {
                        Text(
                            text = "No matches for \"$query\".",
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 14.dp)
                        )
                        SectionLabel("Did you mean")
                        suggestions.forEach { resource ->
                            ResourceRowCard(resource = resource, hazeState = hazeState, onClick = { onResourceClick(resource.id) })
                        }
                    }
                }
            } else if (visible.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Nothing here yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant
                    )
                }
            } else if (activeType == null) {
                val order = listOf(ResourceType.LINK, ResourceType.VIDEO, ResourceType.PDF, ResourceType.NOTES, ResourceType.GITHUB)
                order.forEach { type ->
                    val group = visible.filter { it.type == type }
                    if (group.isNotEmpty()) {
                        Column(modifier = Modifier.padding(bottom = 14.dp)) {
                            SectionLabel("${type.label()}s")
                            group.forEach { resource ->
                                ResourceRowCard(resource = resource, hazeState = hazeState, onClick = { onResourceClick(resource.id) })
                            }
                        }
                    }
                }
            } else {
                visible.forEach { resource ->
                    ResourceRowCard(resource = resource, hazeState = hazeState, onClick = { onResourceClick(resource.id) })
                }
            }
            Spacer(modifier = Modifier.padding(top = 100.dp))
        }
    }
}

@Composable
private fun TypeFilterChip(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color? = null
) {
    val scheme = MaterialTheme.colorScheme
    val chipAccent = accent ?: scheme.secondary
    val background = if (isActive) chipAccent else Color.Transparent
    val contentColor = if (isActive) Color.Black.copy(alpha = 0.8f) else scheme.onSurfaceVariant

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ResourceRowCard(
    resource: ResourceItem,
    hazeState: HazeState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val accent = resource.type.accentColor()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .liquidGlass(hazeState = hazeState, cornerRadius = 16.dp)
            .clickable(onClick = onClick)
            .padding(13.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(accent.copy(alpha = 0.18f))
                .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = resource.emoji, fontSize = 20.sp)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = resource.title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp,
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = resource.description,
                fontSize = 10.5.sp,
                lineHeight = 14.sp,
                color = scheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 3.dp)
            )
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                ResourceChip(text = resource.type.label(), accent = accent)
                ResourceChip(text = resource.level, accent = scheme.outline)
            }
        }

        // TODO: bookmarking — pending backend sync + offline cache, tracked separately.
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(scheme.surfaceContainerHigh)
                .border(1.dp, scheme.outlineVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = null,
                tint = scheme.outline,
                modifier = Modifier.size(11.dp)
            )
        }
    }
}

@Composable
private fun ResourceChip(text: String, accent: Color, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        fontSize = 8.5.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.4.sp,
        color = accent,
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(accent.copy(alpha = 0.12f))
            .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(percent = 50))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

private val previewDomain = Domain(
    id = "webd", name = "Web Dev", tagline = "React, Node & everything between",
    description = "Web Dev builds and maintains all of Innogeeks' web-facing tools.",
    accentIndex = 0, memberCount = 18, techStack = emptyList(), members = emptyList()
)

private val previewResources = listOf(
    ResourceItem("w1", "webd", ResourceType.LINK, "🌐", "The Odin Project", "Full-stack web dev curriculum — HTML, CSS, JS, Node, React.", "Ritesh Kumar", "Aug 2026", "Beginner", "#"),
    ResourceItem("w2", "webd", ResourceType.PDF, "📄", "CSS Grid & Flexbox Cheatsheet", "Compact visual reference card for CSS layout.", "Neha Singh", "Jul 2026", "Beginner", "#"),
    ResourceItem("w3", "webd", ResourceType.VIDEO, "▶️", "JS Event Loop — Visualised", "Explains the call stack, task queue, and microtasks.", "Aditya Sharma", "Jun 2026", "Intermediate", "#")
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun ResourceBrowserScreenPreview() {
    InnogeeksTheme {
        ResourceBrowserScreen(
            domain = previewDomain,
            resources = previewResources,
            hazeState = HazeState(),
            onBack = {},
            onResourceClick = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun ResourceBrowserScreenSearchResultsPreview() {
    InnogeeksTheme {
        ResourceBrowserScreen(
            domain = previewDomain,
            resources = previewResources,
            hazeState = HazeState(),
            onBack = {},
            onResourceClick = {},
            initialQuery = "css"
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun ResourceBrowserScreenSearchSuggestionsPreview() {
    InnogeeksTheme {
        ResourceBrowserScreen(
            domain = previewDomain,
            resources = previewResources,
            hazeState = HazeState(),
            onBack = {},
            onResourceClick = {},
            initialQuery = "The Odn Projct"
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun ResourceBrowserScreenSearchNoMatchPreview() {
    InnogeeksTheme {
        ResourceBrowserScreen(
            domain = previewDomain,
            resources = previewResources,
            hazeState = HazeState(),
            onBack = {},
            onResourceClick = {},
            initialQuery = "xzq quantum blockchain"
        )
    }
}
