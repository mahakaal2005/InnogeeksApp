package com.example.innogeeks.feature_attendance.presentation.sessions

import kotlinx.datetime.LocalDate

sealed interface DomainSessionsAction {
    data object OnRetryClick : DomainSessionsAction
    data object OnRefresh : DomainSessionsAction

    // Fired when the tab or list comes back on screen, so counts from a roster save show up.
    data object OnScreenShown : DomainSessionsAction
    data class OnSessionClick(val sessionId: String) : DomainSessionsAction
    data object OnNewSessionClick : DomainSessionsAction
    data object OnSheetDismiss : DomainSessionsAction
    data class OnTitleChange(val title: String) : DomainSessionsAction
    data class OnDateChange(val date: LocalDate) : DomainSessionsAction
    data object OnCreateClick : DomainSessionsAction
}
