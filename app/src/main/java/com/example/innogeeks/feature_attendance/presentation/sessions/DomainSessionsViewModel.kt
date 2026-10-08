package com.example.innogeeks.feature_attendance.presentation.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innogeeks.core.domain.session.Session
import com.example.innogeeks.core.domain.session.SessionRepository
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.core.presentation.mapper.toUiText
import com.example.innogeeks.feature_attendance.domain.model.AttendanceError
import com.example.innogeeks.feature_attendance.domain.model.AttendanceFailure
import com.example.innogeeks.feature_attendance.domain.use_case.CreateSessionUseCase
import com.example.innogeeks.feature_attendance.domain.use_case.GetDomainSessionsUseCase
import com.example.innogeeks.feature_attendance.presentation.mapper.toUiText
import edu.kiet.innogeeks.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DomainSessionsViewModel(
    private val sessionRepository: SessionRepository,
    private val getDomainSessionsUseCase: GetDomainSessionsUseCase,
    private val createSessionUseCase: CreateSessionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DomainSessionsState())
    val state = _state.asStateFlow()

    private val _events = Channel<DomainSessionsEvent>()
    val events = _events.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            sessionRepository.session
                .map { it as? Session.Authenticated }
                .distinctUntilChanged()
                .collect { session ->
                    // A different user (or none) must never see the previous user's sessions.
                    loadJob?.cancel()
                    _state.value = DomainSessionsState(domain = session?.domain)
                    if (session != null) loadSessions(showLoading = true)
                }
        }
    }

    fun onAction(action: DomainSessionsAction) {
        when (action) {
            DomainSessionsAction.OnRetryClick -> loadSessions(showLoading = true)
            DomainSessionsAction.OnRefresh -> loadSessions(showLoading = false)
            // Skipped until the first load lands, since init already started it.
            DomainSessionsAction.OnScreenShown -> if (_state.value.hasLoaded) loadSessions(showLoading = false)
            is DomainSessionsAction.OnSessionClick -> openRoster(action.sessionId)
            DomainSessionsAction.OnNewSessionClick -> _state.update { it.copy(isCreateSheetOpen = true, createError = null) }
            DomainSessionsAction.OnSheetDismiss -> _state.update {
                it.copy(isCreateSheetOpen = false, createTitle = "", createError = null)
            }
            is DomainSessionsAction.OnTitleChange -> _state.update { it.copy(createTitle = action.title, createError = null) }
            is DomainSessionsAction.OnDateChange -> _state.update { it.copy(createDate = action.date) }
            DomainSessionsAction.OnCreateClick -> createSession()
        }
    }

    // A refresh keeps the current list on screen and shows only the pull indicator.
    private fun loadSessions(showLoading: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update {
                if (showLoading) it.copy(isLoading = true, error = null) else it.copy(isRefreshing = true)
            }
            when (val result = getDomainSessionsUseCase()) {
                is Result.Success -> _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = null,
                        hasLoaded = true,
                        sessions = result.data.map { session -> session.toSessionUi() }
                    )
                }
                // A failed refresh keeps the old list instead of replacing it with an error.
                is Result.Error -> _state.update {
                    if (showLoading) it.copy(isLoading = false, error = result.error.toUiText())
                    else it.copy(isRefreshing = false)
                }
            }
        }
    }

    private fun createSession() {
        val current = _state.value
        if (current.isCreating) return
        viewModelScope.launch {
            _state.update { it.copy(isCreating = true, createError = null) }
            when (val result = createSessionUseCase(current.createTitle, current.createDate)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(isCreating = false, isCreateSheetOpen = false, createTitle = "")
                    }
                    // Creating leads straight into marking the new session.
                    openRoster(result.data.id)
                }
                is Result.Error -> _state.update {
                    it.copy(isCreating = false, createError = result.error.toCreateErrorText())
                }
            }
        }
    }

    // The only validation a create can fail is a blank title, so say that instead of a generic line.
    private fun AttendanceFailure.toCreateErrorText(): UiText =
        if (this is AttendanceFailure.Rejected && error == AttendanceError.VALIDATION) {
            UiText.StringResource(R.string.attendance_title_required)
        } else {
            toUiText()
        }

    private fun openRoster(sessionId: String) {
        viewModelScope.launch { _events.send(DomainSessionsEvent.OpenRoster(sessionId)) }
    }
}
