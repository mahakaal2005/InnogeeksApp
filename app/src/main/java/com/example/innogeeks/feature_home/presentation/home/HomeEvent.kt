package com.example.innogeeks.feature_home.presentation.home

sealed interface HomeEvent {
    data class NavigateToDomain(val domainId: String) : HomeEvent
    data class NavigateToEvent(val eventId: String) : HomeEvent
    data object NavigateToJoin : HomeEvent
}
