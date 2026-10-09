package com.example.innogeeks.feature_resources.presentation.resources

import com.example.innogeeks.feature_resources.domain.model.ResourceType

sealed interface ResourcesAction {
    data class OnResourceItemClicked(val url: String) : ResourcesAction
    data object OnRetry : ResourcesAction

    data object OnAddClick : ResourcesAction
    data class OnEditClick(val id: String) : ResourcesAction
    data class OnLongPress(val id: String) : ResourcesAction
    data object OnActionSheetDismiss : ResourcesAction
    data class OnCopyLink(val id: String) : ResourcesAction
    data class OnRemove(val id: String) : ResourcesAction
    data class OnUndoRemove(val id: String) : ResourcesAction
    data class OnRemovalCommitted(val id: String) : ResourcesAction
    data object OnDismissTip : ResourcesAction

    data class OnUrlChange(val value: String) : ResourcesAction
    data class OnTitleChange(val value: String) : ResourcesAction
    data class OnDescriptionChange(val value: String) : ResourcesAction
    data class OnSourceChange(val value: String) : ResourcesAction
    data class OnTypeChange(val value: ResourceType) : ResourcesAction
    data class OnLevelChange(val value: String) : ResourcesAction
    data object OnSaveClick : ResourcesAction
    data object OnEditorDismiss : ResourcesAction
}
