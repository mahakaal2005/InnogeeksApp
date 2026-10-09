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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.innogeeks.core.presentation.components.SectionLabel
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.feature_resources.domain.model.ResourceItem
import com.example.innogeeks.feature_resources.domain.model.ResourceType
import com.example.innogeeks.feature_resources.domain.model.resourceLevels
import com.example.innogeeks.feature_resources.presentation.resources.ResourceEditorState
import com.example.innogeeks.feature_resources.presentation.resources.ResourcesAction
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState
import edu.kiet.innogeeks.R

// Paste a link and the type and source fill in; the preview row shows exactly how the item will look.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceComposerSheet(
    editor: ResourceEditorState,
    domainName: String,
    hazeState: HazeState,
    onAction: (ResourcesAction) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = { onAction(ResourcesAction.OnEditorDismiss) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        ResourceComposerContent(editor, domainName, hazeState, onAction)
    }
}

@Composable
fun ResourceComposerContent(
    editor: ResourceEditorState,
    domainName: String,
    hazeState: HazeState,
    onAction: (ResourcesAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val clipboard = LocalClipboardManager.current
    val preview = ResourceItem(
        id = "preview", domainId = "", type = editor.type,
        title = editor.title.ifBlank { stringResource(R.string.resources_composer_title_placeholder) },
        description = editor.description, author = editor.source, date = "", level = editor.level, url = editor.url
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .imePadding()
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
    ) {
        Text(
            text = stringResource(if (editor.isEditing) R.string.resources_composer_edit_title else R.string.resources_composer_add_title),
            style = MaterialTheme.typography.titleLarge,
            color = scheme.onSurface
        )
        Text(
            text = stringResource(if (editor.isEditing) R.string.resources_composer_edit_sub else R.string.resources_composer_add_sub),
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
        )

        SectionLabel(stringResource(R.string.resources_composer_preview), modifier = Modifier.padding(bottom = 6.dp))
        ResourceRowCard(resource = preview, hazeState = hazeState, onClick = {}, glass = false)

        OutlinedTextField(
            value = editor.url,
            onValueChange = { onAction(ResourcesAction.OnUrlChange(it)) },
            label = { Text(stringResource(R.string.resources_field_link)) },
            placeholder = { Text("https://") },
            isError = editor.showLinkError,
            supportingText = if (editor.showLinkError) {{ Text(stringResource(R.string.resources_error_link)) }} else null,
            trailingIcon = {
                TextButton(onClick = { clipboard.getText()?.text?.trim()?.takeIf { it.isNotEmpty() }?.let { onAction(ResourcesAction.OnUrlChange(it)) } }) {
                    Text(stringResource(R.string.resources_field_paste))
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        )
        OutlinedTextField(
            value = editor.title,
            onValueChange = { onAction(ResourcesAction.OnTitleChange(it)) },
            label = { Text(stringResource(R.string.resources_field_title)) },
            isError = editor.showTitleError,
            supportingText = if (editor.showTitleError) {{ Text(stringResource(R.string.resources_error_title)) }} else null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        OutlinedTextField(
            value = editor.description,
            onValueChange = { onAction(ResourcesAction.OnDescriptionChange(it)) },
            label = { Text(stringResource(R.string.resources_field_description)) },
            minLines = 2,
            maxLines = 4,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        OutlinedTextField(
            value = editor.source,
            onValueChange = { onAction(ResourcesAction.OnSourceChange(it)) },
            label = { Text(stringResource(R.string.resources_field_source)) },
            placeholder = { Text(stringResource(R.string.resources_field_source_hint)) },
            trailingIcon = if (editor.sourceAuto) {{ AutoBadge() }} else null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )

        Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PickField(
                label = stringResource(R.string.resources_field_type),
                value = editor.type.label(),
                auto = editor.typeAuto,
                options = ResourceType.entries.map { it.label() to it },
                onSelect = { onAction(ResourcesAction.OnTypeChange(it)) },
                modifier = Modifier.weight(1f)
            )
            PickField(
                label = stringResource(R.string.resources_field_level),
                value = editor.level,
                auto = false,
                options = resourceLevels.map { it to it },
                onSelect = { onAction(ResourcesAction.OnLevelChange(it)) },
                modifier = Modifier.weight(1f)
            )
        }

        editor.error?.let {
            Text(
                text = it.asString(),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.error,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Button(
            onClick = { onAction(ResourcesAction.OnSaveClick) },
            enabled = !editor.isSaving,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
        ) {
            Text(
                text = when {
                    editor.isSaving -> stringResource(R.string.resources_saving)
                    editor.isEditing -> stringResource(R.string.resources_save_changes)
                    else -> stringResource(R.string.resources_save_add, domainName)
                }
            )
        }
    }
}

@Composable
private fun AutoBadge() {
    Text(
        text = stringResource(R.string.resources_auto),
        fontSize = 10.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
            .padding(end = 12.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 5.dp, vertical = 1.dp)
    )
}

@Composable
private fun <T> PickField(
    label: String,
    value: String,
    auto: Boolean,
    options: List<Pair<String, T>>,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    var open by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .border(1.dp, scheme.outline, RoundedCornerShape(4.dp))
                .clickable { open = true }
                .padding(start = 14.dp, top = 8.dp, bottom = 8.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = label, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                    if (auto) {
                        Box(modifier = Modifier.padding(start = 5.dp)) { AutoBadgeInline() }
                    }
                }
                Text(text = value, style = MaterialTheme.typography.bodyLarge, color = scheme.onSurface)
            }
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { (text, option) ->
                DropdownMenuItem(text = { Text(text) }, onClick = { open = false; onSelect(option) })
            }
        }
    }
}

@Composable
private fun AutoBadgeInline() {
    Text(
        text = stringResource(R.string.resources_auto),
        fontSize = 9.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 4.dp)
    )
}

private val previewEditor = ResourceEditorState(
    url = "https://www.youtube.com/watch?v=Ft0kd", title = "Compose state explained",
    description = "Remember, mutableStateOf and hoisting in 20 minutes.", source = "YouTube",
    type = ResourceType.VIDEO, typeAuto = true, sourceAuto = true
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun ResourceComposerEmptyPreview() {
    InnogeeksTheme { ResourceComposerContent(ResourceEditorState(), "App Dev", HazeState(), {}) }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun ResourceComposerAutoFilledPreview() {
    InnogeeksTheme { ResourceComposerContent(previewEditor, "App Dev", HazeState(), {}) }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun ResourceComposerErrorsPreview() {
    InnogeeksTheme { ResourceComposerContent(ResourceEditorState(url = "compose.dev/learn", submitted = true), "App Dev", HazeState(), {}) }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun ResourceComposerSavingPreview() {
    InnogeeksTheme { ResourceComposerContent(previewEditor.copy(isSaving = true), "App Dev", HazeState(), {}) }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun ResourceComposerEditPreview() {
    InnogeeksTheme {
        ResourceComposerContent(
            previewEditor.copy(editingId = "p1", typeAuto = false, sourceAuto = false, error = UiText.DynamicString("Couldn't save. Check your connection and try again.")),
            "App Dev", HazeState(), {}
        )
    }
}
