package com.example.innogeeks.feature_attendance.presentation.sessions

sealed interface DomainSessionsEvent {
    data class OpenRoster(val sessionId: String) : DomainSessionsEvent
}
