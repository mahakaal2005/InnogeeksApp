package com.example.innogeeks.feature_home.presentation.memberhome.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.innogeeks.core.domain.resources.ResourceKind
import com.example.innogeeks.feature_home.presentation.memberhome.ShortcutUi
import com.example.innogeeks.ui.theme.InnogeeksTheme
import edu.kiet.innogeeks.R

// Swipeable resource cards for the member's own domain, ending in an "All N" card.
@Composable
fun ResourceCarousel(
    shortcuts: List<ShortcutUi>,
    totalCount: Int,
    onResourceClick: (String) -> Unit,
    onAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(shortcuts, key = { it.id }) { shortcut ->
            ResourceCard(shortcut = shortcut, onClick = { onResourceClick(shortcut.url) })
        }
        item(key = "all") { AllResourcesCard(count = totalCount, onClick = onAllClick) }
    }
}

@Composable
private fun ResourceCard(shortcut: ShortcutUi, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = Modifier
            .width(200.dp)
            .height(170.dp)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.colorScheme.surfaceContainer)
                )
            )
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), shape)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = shortcut.kind.icon(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Column {
            Text(
                text = shortcut.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.member_home_resource_meta, shortcut.kind.label(), shortcut.author),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun AllResourcesCard(count: Int, onClick: () -> Unit) {
    val dash = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    Column(
        modifier = Modifier
            .width(130.dp)
            .height(170.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .drawBehind {
                drawRoundRect(
                    color = dash,
                    cornerRadius = CornerRadius(20.dp.toPx()),
                    style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)))
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(26.dp)
        )
        Text(
            text = stringResource(R.string.member_home_all_resources, count),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

private fun ResourceKind.icon(): ImageVector = when (this) {
    ResourceKind.LINK -> Icons.Filled.Link
    ResourceKind.PDF -> Icons.Filled.PictureAsPdf
    ResourceKind.VIDEO -> Icons.Filled.PlayCircle
    ResourceKind.NOTES -> Icons.Filled.Description
    ResourceKind.GITHUB -> Icons.Filled.Code
}

@Composable
private fun ResourceKind.label(): String = stringResource(
    when (this) {
        ResourceKind.LINK -> R.string.resource_type_link
        ResourceKind.PDF -> R.string.resource_type_pdf
        ResourceKind.VIDEO -> R.string.resource_type_video
        ResourceKind.NOTES -> R.string.resource_type_notes
        ResourceKind.GITHUB -> R.string.resource_type_github
    }
)

internal val previewShortcuts = listOf(
    ShortcutUi("r1", "The Jetpack Compose Beginner Crash Course", "Philipp Lackner", ResourceKind.VIDEO, ""),
    ShortcutUi("r2", "Android Basics with Compose", "Google", ResourceKind.LINK, ""),
    ShortcutUi("r3", "Kotlin Notes for Professionals", "GoalKicker", ResourceKind.PDF, "")
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ResourceCarouselPreview() {
    InnogeeksTheme {
        ResourceCarousel(shortcuts = previewShortcuts, totalCount = 50, onResourceClick = {}, onAllClick = {})
    }
}
