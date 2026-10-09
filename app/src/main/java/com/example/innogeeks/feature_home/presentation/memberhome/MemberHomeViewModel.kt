package com.example.innogeeks.feature_home.presentation.memberhome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innogeeks.core.domain.attendance.AttendanceSummaryProvider
import com.example.innogeeks.core.domain.model.UserRole
import com.example.innogeeks.core.domain.resources.ResourceShortcutProvider
import com.example.innogeeks.core.domain.session.Session
import com.example.innogeeks.core.domain.session.SessionRepository
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.core.presentation.mapper.toUiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class MemberHomeViewModel(
    private val sessionRepository: SessionRepository,
    private val attendanceProvider: AttendanceSummaryProvider,
    private val resourceProvider: ResourceShortcutProvider
) : ViewModel() {

    private val _state = MutableStateFlow(MemberHomeState())
    val state = _state.asStateFlow()

    private val _events = Channel<MemberHomeEvent>()
    val events = _events.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            sessionRepository.session
                .map { it as? Session.Authenticated }
                .distinctUntilChanged()
                .collect { session ->
                    // A different user (or none) must never see the previous user's dashboard.
                    loadJob?.cancel()
                    _state.value = MemberHomeState(
                        dayPart = currentDayPart(),
                        name = session?.collegeEmail?.toGreetingName().orEmpty(),
                        initials = session?.collegeEmail?.toInitials().orEmpty(),
                        role = session?.role,
                        domain = session?.domain
                    )
                    if (session != null) load(showLoading = true)
                }
        }
    }

    fun onAction(action: MemberHomeAction) {
        when (action) {
            MemberHomeAction.OnRefresh, MemberHomeAction.OnRetryClick -> load(showLoading = action == MemberHomeAction.OnRetryClick)
            // Skipped until the first load lands, since init already started it.
            MemberHomeAction.OnScreenShown -> if (!_state.value.isLoading) load(showLoading = false)
            MemberHomeAction.OnAttendanceClick, MemberHomeAction.OnToMarkClick -> send(MemberHomeEvent.OpenAttendance)
            MemberHomeAction.OnAllResourcesClick -> send(MemberHomeEvent.OpenResources)
            MemberHomeAction.OnProfileClick -> send(MemberHomeEvent.OpenProfile)
            is MemberHomeAction.OnResourceClick -> send(MemberHomeEvent.OpenUrl(action.url))
        }
    }

    // A refresh keeps the current dashboard on screen and shows only the pull indicator.
    private fun load(showLoading: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val current = _state.value
            _state.update {
                if (showLoading) it.copy(isLoading = true, attendanceError = null) else it.copy(isRefreshing = true)
            }

            // The three sources are independent, so fetch them concurrently.
            val overviewDeferred = async { attendanceProvider.getOverview() }
            val toMarkDeferred = async {
                if (current.role == UserRole.COORDINATOR || current.role == UserRole.ADMIN) {
                    attendanceProvider.getSessionsToMarkCount()
                } else null
            }
            val shortcutsDeferred = async {
                current.domain?.let { resourceProvider.getShortcuts(it.contentDomainId, SHORTCUT_LIMIT) }
            }

            val overview = overviewDeferred.await()
            val toMark = toMarkDeferred.await()
            val shortcuts = shortcutsDeferred.await()

            _state.update { state ->
                state.copy(
                    isLoading = false,
                    isRefreshing = false,
                    dayPart = currentDayPart(),
                    overview = (overview as? Result.Success)?.data?.toAttendanceOverviewUi() ?: state.overview,
                    // A failed refresh keeps the old numbers instead of replacing them with an error.
                    attendanceError = (overview as? Result.Error)?.takeIf { state.overview == null }?.error?.toUiText(),
                    toMarkCount = (toMark as? Result.Success)?.data ?: state.toMarkCount,
                    shortcuts = shortcuts?.items?.map { it.toShortcutUi() } ?: state.shortcuts,
                    totalResources = shortcuts?.total ?: state.totalResources
                )
            }
        }
    }

    private fun send(event: MemberHomeEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    private fun currentDayPart(): DayPart {
        val hour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
        return when {
            hour < 12 -> DayPart.MORNING
            hour < 17 -> DayPart.AFTERNOON
            else -> DayPart.EVENING
        }
    }

    private companion object {
        const val SHORTCUT_LIMIT = 4
    }
}
