package com.example.innogeeks.feature_home.presentation.memberhome

sealed interface MemberHomeEvent {
    data object OpenAttendance : MemberHomeEvent
    data object OpenResources : MemberHomeEvent
    data object OpenProfile : MemberHomeEvent
    data class OpenUrl(val url: String) : MemberHomeEvent
}
