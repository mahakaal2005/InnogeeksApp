package com.example.innogeeks.feature_resources.presentation.resources

import com.example.innogeeks.core.presentation.UiText

sealed interface ResourcesEvent {
    data class OpenUrl(val url: String) : ResourcesEvent
    data class CopyToClipboard(val text: String) : ResourcesEvent
    data class ShowMessage(val message: UiText) : ResourcesEvent

    // The snackbar offers Undo until the ViewModel's timer closes it with DismissUndo.
    data class ShowRemoved(val id: String, val title: String) : ResourcesEvent
    data class DismissUndo(val id: String) : ResourcesEvent
}
