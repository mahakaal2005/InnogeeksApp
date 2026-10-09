package com.example.innogeeks.feature_resources.presentation.resources

import androidx.compose.runtime.Immutable
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.feature_resources.domain.model.ResourceType

// The add/edit sheet. Errors appear only after a Save attempt, never while the user is still typing.
@Immutable
data class ResourceEditorState(
    val editingId: String? = null, // null while adding
    val url: String = "",
    val title: String = "",
    val description: String = "",
    val source: String = "",
    val type: ResourceType = ResourceType.LINK,
    val level: String = "Beginner",
    val typeTouched: Boolean = false,
    val sourceTouched: Boolean = false,
    val typeAuto: Boolean = false, // true while type came from the pasted link
    val sourceAuto: Boolean = false,
    val submitted: Boolean = false,
    val isSaving: Boolean = false,
    val error: UiText? = null
) {
    val isEditing: Boolean get() = editingId != null
    val linkValid: Boolean get() = url.trim().startsWith("https://") || url.trim().startsWith("http://")
    val showTitleError: Boolean get() = submitted && title.isBlank()
    val showLinkError: Boolean get() = submitted && !linkValid
}
