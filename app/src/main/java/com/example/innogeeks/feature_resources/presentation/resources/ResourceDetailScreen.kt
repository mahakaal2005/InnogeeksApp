package com.example.innogeeks.feature_resources.presentation.resources

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.innogeeks.core.presentation.components.GlowBlob
import com.example.innogeeks.core.presentation.components.liquidGlass
import com.example.innogeeks.feature_resources.domain.model.ResourceItem
import com.example.innogeeks.feature_resources.domain.model.ResourceType
import com.example.innogeeks.feature_resources.presentation.resources.components.accentColor
import com.example.innogeeks.feature_resources.presentation.resources.components.label
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import androidx.compose.ui.res.stringResource
import edu.kiet.innogeeks.R

// Full-page resource detail: icon + title + type/level chips, description, meta rows, open button.
@Composable
fun ResourceDetailScreen(
    resource: ResourceItem,
    hazeState: HazeState,
    onBack: () -> Unit,
    onOpenResource: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val accent = resource.type.accentColor()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
            .hazeSource(hazeState)
    ) {
        GlowBlob(
            color = accent,
            modifier = Modifier.size(360.dp).offset(x = (-110).dp, y = (-40).dp)
        )
        GlowBlob(
            color = scheme.primary,
            modifier = Modifier.size(400.dp).align(Alignment.BottomEnd).offset(x = 110.dp, y = 130.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .liquidGlass(hazeState = hazeState, cornerRadius = 19.dp)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back), tint = scheme.onSurface)
                }
                Text(
                    text = resource.type.label(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(accent.copy(alpha = 0.2f))
                        .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = resource.emoji, fontSize = 26.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = resource.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    )
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DetailChip(text = resource.type.label(), accent = accent)
                        DetailChip(text = resource.level, accent = scheme.outline)
                    }
                }
            }

            Text(
                text = resource.description,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 18.dp, bottom = 18.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(hazeState = hazeState, cornerRadius = 16.dp)
                    .padding(horizontal = 14.dp)
            ) {
                MetaRow(icon = Icons.Filled.CalendarMonth, label = stringResource(R.string.resources_meta_added), value = resource.date)
                MetaRow(icon = Icons.Filled.Person, label = stringResource(R.string.resources_meta_by), value = resource.author)
                MetaRow(icon = Icons.Filled.Speed, label = stringResource(R.string.resources_meta_level), value = resource.level, showDivider = false)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 140.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(scheme.secondary)
                    .clickable(onClick = { onOpenResource(resource.url) })
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.OpenInNew,
                    contentDescription = null,
                    tint = scheme.onSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(R.string.resources_open_resource),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSecondary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun MetaRow(
    icon: ImageVector,
    label: String,
    value: String,
    showDivider: Boolean = true
) {
    val scheme = MaterialTheme.colorScheme
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(scheme.surfaceContainerHigh)
                    .border(1.dp, scheme.outlineVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = scheme.outline, modifier = Modifier.size(13.dp))
            }
            Text(
                text = label.uppercase(),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = scheme.outline,
                modifier = Modifier.width(52.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface
            )
        }
        if (showDivider) {
            HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun DetailChip(text: String, accent: Color, modifier: Modifier = Modifier) {
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

private val previewResource = ResourceItem(
    id = "w1", domainId = "webd", type = ResourceType.LINK, emoji = "🌐",
    title = "The Odin Project",
    description = "Full-stack web dev curriculum — HTML, CSS, JS, Node, React. Free and open source, structured like a real bootcamp.",
    author = "Ritesh Kumar", date = "Aug 2026", level = "Beginner", url = "https://www.theodinproject.com"
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun ResourceDetailScreenPreview() {
    InnogeeksTheme {
        ResourceDetailScreen(
            resource = previewResource,
            hazeState = HazeState(),
            onBack = {},
            onOpenResource = {}
        )
    }
}
