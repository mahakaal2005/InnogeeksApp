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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.key
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
import com.example.innogeeks.feature_resources.presentation.resources.components.ResourceRowCard
import com.example.innogeeks.feature_resources.presentation.resources.components.ResourceSearchBar
import com.example.innogeeks.feature_resources.presentation.resources.components.accentColor
import com.example.innogeeks.feature_resources.presentation.resources.components.domainIconRes
import com.example.innogeeks.feature_resources.presentation.resources.components.groupLabel
import com.example.innogeeks.feature_resources.presentation.resources.components.label
import com.example.innogeeks.feature_resources.presentation.resources.components.suggestResources
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import androidx.compose.ui.res.stringResource
import edu.kiet.innogeeks.R
import androidx.compose.ui.res.pluralStringResource

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
    initialQuery: String = "",
    canEdit: Boolean = false, // only the coordinator of this very domain
    newIds: Set<String> = emptySet(),
    showTip: Boolean = false,
    onAddClick: () -> Unit = {},
    onResourceLongClick: (String) -> Unit = {},
    onResourceRemove: (String) -> Unit = {},
    onDismissTip: () -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()
    val feedRow: @Composable (ResourceItem) -> Unit = { resource ->
        key(resource.id) {
            FeedRow(
                resource = resource,
                hazeState = hazeState,
                canEdit = canEdit,
                isNew = resource.id in newIds,
                onClick = { onResourceClick(resource.id) },
                onLongClick = { onResourceLongClick(resource.id) },
                onRemove = { onResourceRemove(resource.id) }
            )
        }
    }
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

    Box(modifier = modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
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
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back), tint = scheme.onSurface)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = domain.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    )
                    Text(
                        text = stringResource(
                            R.string.resources_summary,
                            pluralStringResource(R.plurals.resources_count, resources.size, resources.size),
                            typesPresent.size
                        ),
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

            if (canEdit) {
                CoordinatorStrip(domainName = domain.name, modifier = Modifier.padding(bottom = 10.dp))
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
                    label = stringResource(R.string.resources_all),
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
                .verticalScroll(scrollState)
                .padding(horizontal = 18.dp, vertical = 4.dp)
        ) {
            if (canEdit && showTip) {
                TipRow(onDismiss = onDismissTip, modifier = Modifier.padding(bottom = 8.dp))
            }
            if (visible.isEmpty() && query.isNotBlank()) {
                val suggestions = remember(query, resources) { suggestResources(query, resources) }
                if (suggestions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.resources_no_matches, query),
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(modifier = Modifier.padding(top = 24.dp)) {
                        Text(
                            text = stringResource(R.string.resources_no_matches, query),
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 14.dp)
                        )
                        SectionLabel(stringResource(R.string.resources_did_you_mean))
                        suggestions.forEach { resource ->
                            feedRow(resource)
                        }
                    }
                }
            } else if (visible.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.resources_nothing_here_yet),
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
                            SectionLabel(type.groupLabel())
                            group.forEach { resource ->
                                feedRow(resource)
                            }
                        }
                    }
                }
            } else {
                visible.forEach { resource ->
                    feedRow(resource)
                }
            }
            Spacer(modifier = Modifier.padding(top = if (canEdit) 170.dp else 100.dp))
        }
    }
    if (canEdit) {
        AddFab(
            expanded = !scrollState.isScrollInProgress,
            onClick = onAddClick,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 18.dp, bottom = 96.dp)
        )
    }
    }
}

// Swipe left to remove (Undo follows); a plain row for everyone who can't edit.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedRow(
    resource: ResourceItem,
    hazeState: HazeState,
    canEdit: Boolean,
    isNew: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRemove: () -> Unit
) {
    if (!canEdit) {
        ResourceRowCard(resource, hazeState, onClick, modifier = Modifier.padding(bottom = 8.dp))
        return
    }
    val scheme = MaterialTheme.colorScheme
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { target ->
            if (target == SwipeToDismissBoxValue.EndToStart) { onRemove(); true } else false
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        modifier = Modifier.padding(bottom = 8.dp).clip(RoundedCornerShape(16.dp)),
        backgroundContent = {
            // The rows are glass, so the red must exist only while a swipe is under way.
            if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Row(
                modifier = Modifier.fillMaxSize().background(scheme.error.copy(alpha = 0.22f)).padding(end = 22.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End)
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null, tint = scheme.error, modifier = Modifier.size(20.dp))
                Text(text = stringResource(R.string.resources_remove), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = scheme.error)
            }
        }
    ) {
        ResourceRowCard(resource, hazeState, onClick, isNew = isNew, onLongClick = onLongClick)
    }
}

@Composable
private fun CoordinatorStrip(domainName: String, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(scheme.primary.copy(alpha = 0.10f))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(16.dp))
        Text(
            text = stringResource(R.string.resources_coordinate_strip, domainName),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = scheme.primary
        )
    }
}

@Composable
private fun TipRow(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, scheme.primary.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(start = 12.dp, top = 2.dp, bottom = 2.dp, end = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.resources_tip),
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDismiss) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.resources_tip_dismiss), tint = scheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}

// Shrinks to a plain "+" while the list scrolls so it never sits on a title.
@Composable
private fun AddFab(expanded: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        expanded = expanded,
        icon = { Icon(Icons.Filled.Add, contentDescription = null) },
        text = { Text(stringResource(R.string.resources_add), fontWeight = FontWeight.Bold) },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier
    )
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

private val previewDomain = Domain(
    id = "webd", name = "Web Dev", tagline = "React, Node & everything between",
    description = "Web Dev builds and maintains all of Innogeeks' web-facing tools.",
    accentIndex = 0, memberCount = 18, techStack = emptyList(), members = emptyList()
)

private val previewResources = listOf(
    ResourceItem("w1", "webd", ResourceType.LINK, "The Odin Project", "Full-stack web dev curriculum — HTML, CSS, JS, Node, React.", "Ritesh Kumar", "Aug 2026", "Beginner", "#"),
    ResourceItem("w2", "webd", ResourceType.PDF, "CSS Grid & Flexbox Cheatsheet", "Compact visual reference card for CSS layout.", "Neha Singh", "Jul 2026", "Beginner", "#"),
    ResourceItem("w3", "webd", ResourceType.VIDEO, "JS Event Loop — Visualised", "Explains the call stack, task queue, and microtasks.", "Aditya Sharma", "Jun 2026", "Intermediate", "#")
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


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun ResourceBrowserScreenCoordinatorPreview() {
    InnogeeksTheme {
        ResourceBrowserScreen(
            domain = previewDomain,
            resources = previewResources,
            hazeState = HazeState(),
            onBack = {},
            onResourceClick = {},
            canEdit = true,
            newIds = setOf("w3"),
            showTip = true
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun ResourceBrowserScreenCoordinatorNoTipPreview() {
    InnogeeksTheme {
        ResourceBrowserScreen(
            domain = previewDomain,
            resources = previewResources,
            hazeState = HazeState(),
            onBack = {},
            onResourceClick = {},
            canEdit = true
        )
    }
}
