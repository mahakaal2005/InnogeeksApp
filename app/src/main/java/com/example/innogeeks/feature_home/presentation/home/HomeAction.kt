package com.example.innogeeks.feature_home.presentation.home

sealed interface HomeAction {
    data class OnDomainClick(val domainId: String) : HomeAction
    data class OnEventClick(val eventId: String) : HomeAction
    data object OnJoinClick : HomeAction
}
