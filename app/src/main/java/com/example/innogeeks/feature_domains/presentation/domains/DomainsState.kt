package com.example.innogeeks.feature_domains.presentation.domains

import com.example.innogeeks.core.domain.model.Domain
import com.example.innogeeks.core.presentation.UiText

data class DomainsState(
    val isLoading: Boolean = true,
    val domains: List<Domain> = emptyList(),
    val error: UiText? = null
)
