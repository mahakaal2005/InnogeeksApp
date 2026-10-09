package com.example.innogeeks.feature_resources.presentation.resources.components

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.innogeeks.core.presentation.components.liquidGlass
import com.example.innogeeks.feature_resources.domain.model.ResourceItem
import com.example.innogeeks.feature_resources.domain.model.ResourceType
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState
import edu.kiet.innogeeks.R

// One resource in the feed: accent icon tile, title, one-line blurb, "Source · Level" and an optional NEW badge.
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ResourceRowCard(
    resource: ResourceItem,
    hazeState: HazeState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isNew: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    glass: Boolean = true // false inside a sheet: it is its own window, so there is nothing to blur
) {
    val scheme = MaterialTheme.colorScheme
    val accent = resource.type.accentColor()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (glass) Modifier.liquidGlass(hazeState = hazeState, cornerRadius = 16.dp)
                else Modifier.clip(RoundedCornerShape(16.dp)).background(scheme.surfaceContainerHighest)
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
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
            Icon(imageVector = resource.type.icon(), contentDescription = resource.type.label(), tint = accent, modifier = Modifier.size(22.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = resource.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (isNew) {
                    Text(
                        text = stringResource(R.string.resources_new_badge),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = scheme.onPrimary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(scheme.primary)
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }
            }
            if (resource.description.isNotBlank()) {
                Text(
                    text = resource.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Text(
                text = listOf(resource.author, resource.level).filter { it.isNotBlank() }.joinToString(" \u00b7 "),
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 5.dp)
            )
        }
    }
}

internal val previewRowResource = ResourceItem(
    "p1", "appd", ResourceType.VIDEO, "Compose state explained",
    "Remember, mutableStateOf and hoisting in 20 minutes.", "YouTube", "Oct 2026", "Beginner", "https://youtu.be/x"
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ResourceRowCardPreview() {
    InnogeeksTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ResourceRowCard(previewRowResource, HazeState(), onClick = {}, isNew = true)
            ResourceRowCard(previewRowResource.copy(type = ResourceType.GITHUB, title = "Now in Android", author = "Android", level = "Advanced"), HazeState(), onClick = {})
            ResourceRowCard(previewRowResource.copy(type = ResourceType.PDF, description = "", title = "A very long resource title that should truncate before it runs out of room"), HazeState(), onClick = {})
        }
    }
}
