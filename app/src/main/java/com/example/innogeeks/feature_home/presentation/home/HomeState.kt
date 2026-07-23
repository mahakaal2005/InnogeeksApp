package com.example.innogeeks.feature_home.presentation.home

import com.example.innogeeks.feature_home.domain.model.ClubStats
import com.example.innogeeks.feature_home.domain.model.DomainPreview
import com.example.innogeeks.feature_home.domain.model.EventPreview

data class HomeState(
    val isLoading: Boolean = true,
    val stats: ClubStats? = null,
    val domains: List<DomainPreview> = emptyList(),
    val events: List<EventPreview> = emptyList(),
    val error: String? = null
)
