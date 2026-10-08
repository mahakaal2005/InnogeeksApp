package com.example.innogeeks.feature_attendance.presentation.roster

import com.example.innogeeks.feature_attendance.domain.model.AttendanceStatus

sealed interface SessionRosterAction {
    data object OnRetryClick : SessionRosterAction
    data class OnMarkClick(val accountId: String, val status: AttendanceStatus) : SessionRosterAction
    data object OnMarkAllPresentClick : SessionRosterAction
    data class OnQueryChange(val query: String) : SessionRosterAction
    data object OnClearQueryClick : SessionRosterAction
    data class OnFilterChange(val filter: RosterFilter) : SessionRosterAction
    data object OnSubmitClick : SessionRosterAction
    data object OnBackClick : SessionRosterAction
    data object OnDiscardConfirm : SessionRosterAction
    data object OnDiscardDismiss : SessionRosterAction
}
