package com.example.innogeeks.feature_resources.presentation.resources

import com.example.innogeeks.core.domain.model.Domain
import com.example.innogeeks.feature_resources.domain.model.ResourceItem
import com.example.innogeeks.core.presentation.UiText

data class ResourcesState(
    val isLoading: Boolean = true,
    val domains: List<Domain> = emptyList(),
    val resources: List<ResourceItem> = emptyList(),
    val error: UiText? = null
)
