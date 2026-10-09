package com.example.innogeeks.feature_resources.presentation.resources.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.innogeeks.feature_resources.domain.model.ResourceItem
import com.example.innogeeks.ui.theme.InnogeeksTheme
import edu.kiet.innogeeks.R

// Hold a row: what you can do with it. Remove here, like swiping, offers Undo afterwards.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceActionSheet(
    resource: ResourceItem,
    onEdit: () -> Unit,
    onCopyLink: () -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        ResourceActionContent(resource, onEdit, onCopyLink, onRemove)
    }
}

@Composable
fun ResourceActionContent(
    resource: ResourceItem,
    onEdit: () -> Unit,
    onCopyLink: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val accent = resource.type.accentColor()

    Column(modifier = modifier.fillMaxWidth().navigationBarsPadding().padding(start = 24.dp, end = 24.dp, bottom = 16.dp)) {
        Row(
            modifier = Modifier.padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
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
                Icon(imageVector = resource.type.icon(), contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            }
            Column {
                Text(
                    text = resource.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = listOf(resource.author, resource.level).filter { it.isNotBlank() }.joinToString(" \u00b7 "),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
            }
        }
        HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.5f))
        ActionRow(Icons.Filled.Edit, stringResource(R.string.resources_edit), scheme.onSurface, onEdit)
        ActionRow(Icons.Filled.ContentCopy, stringResource(R.string.resources_copy_link), scheme.onSurface, onCopyLink)
        ActionRow(Icons.Filled.Delete, stringResource(R.string.resources_remove), scheme.error, onRemove)
    }
}

@Composable
private fun ActionRow(icon: ImageVector, label: String, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ResourceActionContentPreview() {
    InnogeeksTheme { ResourceActionContent(previewRowResource, onEdit = {}, onCopyLink = {}, onRemove = {}) }
}
