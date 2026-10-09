package com.example.innogeeks.feature_resources.presentation.resources

import com.example.innogeeks.core.domain.model.Domain
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.feature_resources.domain.model.ResourceItem

data class ResourcesState(
    val isLoading: Boolean = true,
    val domains: List<Domain> = emptyList(),
    val resources: List<ResourceItem> = emptyList(),
    val error: UiText? = null,
    val editableDomainId: String? = null, // the one domain this user may change; null for everyone else
    val editor: ResourceEditorState? = null,
    val actionSheetFor: String? = null,
    val pendingRemovalIds: Set<String> = emptySet(), // hidden at once; deleted for real when the undo window closes
    val newIds: Set<String> = emptySet(),
    val showTip: Boolean = true
) {
    val visibleResources: List<ResourceItem> get() = resources.filterNot { it.id in pendingRemovalIds }
}
