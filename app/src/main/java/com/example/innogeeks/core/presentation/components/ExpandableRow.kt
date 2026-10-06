package com.example.innogeeks.core.presentation.components

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState
import androidx.compose.ui.res.stringResource
import edu.kiet.innogeeks.R

// Slot-based accordion shared by Domains, Past Events and Profile.
// State is hoisted so a parent can enforce "only one row open at a time".
@Composable
fun ExpandableRow(
    title: String,
    subtitle: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(320),
        label = "chevronRotation"
    )

    // Border and chevron glow into primary when open.
    val borderColor by animateColorAsState(
        targetValue = if (isExpanded) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        } else {
            MaterialTheme.colorScheme.outlineVariant
        },
        animationSpec = tween(320),
        label = "borderColor"
    )

    val chevronColor by animateColorAsState(
        targetValue = if (isExpanded) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline
        },
        animationSpec = tween(320),
        label = "chevronColor"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(hazeState = hazeState, cornerRadius = 14.dp)
            // Expand-state glow, layered on top of the glass modifier's own border.
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            // Animates the height jump when the content block is added or removed.
            .animateContentSize(animationSpec = tween(400))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // clickable before padding so the whole row is the touch target.
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggle
                )
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            leading?.invoke()

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = stringResource(if (isExpanded) R.string.common_collapse else R.string.common_expand),
                tint = chevronColor,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(chevronRotation)
            )
        }

        if (isExpanded) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                content = content
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ExpandableRowCollapsedPreview() {
    InnogeeksTheme {
        ExpandableRow(
            title = "Web Development",
            subtitle = "Full-stack web engineering",
            isExpanded = false,
            onToggle = {},
            hazeState = remember { HazeState() },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Hidden content")
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ExpandableRowExpandedPreview() {
    InnogeeksTheme {
        ExpandableRow(
            title = "Web Development",
            subtitle = "Full-stack web engineering",
            isExpanded = true,
            onToggle = {},
            hazeState = remember { HazeState() },
            modifier = Modifier.padding(16.dp),
            leading = {
                Text(
                    text = "🌐",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        ) {
            Text(
                text = "React, Next.js, Node and Postgres. We ship the club site and internal tools.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Interactive preview so the accordion can be toggled inside the preview pane.
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ExpandableRowInteractivePreview() {
    InnogeeksTheme {
        var expanded by remember { mutableStateOf(false) }
        ExpandableRow(
            title = "Machine Learning",
            subtitle = "Models, data and inference",
            isExpanded = expanded,
            onToggle = { expanded = !expanded },
            hazeState = remember { HazeState() },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Tap the row to toggle.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
