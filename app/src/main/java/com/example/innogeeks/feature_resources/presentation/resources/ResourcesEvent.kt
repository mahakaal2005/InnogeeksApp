package com.example.innogeeks.feature_resources.presentation.resources

import com.example.innogeeks.core.presentation.UiText

sealed interface ResourcesEvent {
    data class OpenUrl(val url: String) : ResourcesEvent
    data class CopyToClipboard(val text: String) : ResourcesEvent
    data class ShowMessage(val message: UiText) : ResourcesEvent

    // The snackbar offers Undo; its outcome comes back as OnUndoRemove or OnRemovalCommitted.
    data class ShowRemoved(val id: String, val title: String) : ResourcesEvent
}
