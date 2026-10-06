package com.example.innogeeks.feature_home.presentation.home

import com.example.innogeeks.feature_home.domain.model.Achievement
import com.example.innogeeks.feature_home.domain.model.ClubStats
import com.example.innogeeks.feature_home.domain.model.CultureMoment
import com.example.innogeeks.feature_home.domain.model.DomainPreview
import com.example.innogeeks.core.presentation.UiText

data class HomeState(
    val isLoading: Boolean = true,
    val stats: ClubStats? = null,
    val domains: List<DomainPreview> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val tickerRows: List<List<String>> = emptyList(),
    val cultureMoments: List<CultureMoment> = emptyList(),
    val selectedDomainId: String? = null,
    val error: UiText? = null
) {
    // Falls back to the first domain so the wheel's info panel is never empty.
    val selectedDomain: DomainPreview?
        get() = domains.firstOrNull { it.id == selectedDomainId } ?: domains.firstOrNull()
}
