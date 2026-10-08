package com.example.innogeeks.feature_attendance.presentation.sessions

import com.example.innogeeks.core.domain.model.UserDomain
import com.example.innogeeks.core.presentation.UiText
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class DomainSessionsState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: UiText? = null,
    val hasLoaded: Boolean = false,
    val sessions: List<SessionUi> = emptyList(), // newest first
    val domain: UserDomain? = null,
    val isCreateSheetOpen: Boolean = false,
    val createTitle: String = "",
    val createDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val isCreating: Boolean = false,
    val createError: UiText? = null
) {
    val toMark: List<SessionUi> get() = sessions.filter { it.needsMarking }
    val marked: List<SessionUi> get() = sessions.filterNot { it.needsMarking }
}
