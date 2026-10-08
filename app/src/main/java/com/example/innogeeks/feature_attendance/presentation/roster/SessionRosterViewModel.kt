package com.example.innogeeks.feature_attendance.presentation.roster

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.core.presentation.mapper.toUiText
import com.example.innogeeks.feature_attendance.domain.model.AttendanceMark
import com.example.innogeeks.feature_attendance.domain.model.AttendanceStatus
import com.example.innogeeks.feature_attendance.domain.use_case.GetSessionRosterUseCase
import com.example.innogeeks.feature_attendance.domain.use_case.SubmitMarksUseCase
import com.example.innogeeks.feature_attendance.presentation.mapper.toUiText
import com.example.innogeeks.feature_attendance.presentation.sessions.formatSessionDate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SessionRosterViewModel(
    private val sessionId: String,
    private val getSessionRosterUseCase: GetSessionRosterUseCase,
    private val submitMarksUseCase: SubmitMarksUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SessionRosterState())
    val state = _state.asStateFlow()

    private val _events = Channel<SessionRosterEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadRoster()
    }

    fun onAction(action: SessionRosterAction) {
        when (action) {
            SessionRosterAction.OnRetryClick -> loadRoster()
            is SessionRosterAction.OnMarkClick -> mark(action.accountId, action.status)
            SessionRosterAction.OnMarkAllPresentClick -> markAllPresent()
            is SessionRosterAction.OnQueryChange -> _state.update { it.copy(query = action.query) }
            SessionRosterAction.OnClearQueryClick -> _state.update { it.copy(query = "") }
            is SessionRosterAction.OnFilterChange -> _state.update { it.copy(filter = action.filter) }
            SessionRosterAction.OnSubmitClick -> submit()
            SessionRosterAction.OnBackClick -> {
                if (_state.value.changedCount > 0) _state.update { it.copy(showDiscardDialog = true) } else close()
            }
            SessionRosterAction.OnDiscardConfirm -> close()
            SessionRosterAction.OnDiscardDismiss -> _state.update { it.copy(showDiscardDialog = false) }
        }
    }

    private fun loadRoster() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = getSessionRosterUseCase(sessionId)) {
                is Result.Success -> {
                    val roster = result.data
                    val marks = roster.entries.associate { it.accountId to it.status }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            title = roster.title,
                            dateLabel = formatSessionDate(roster.date),
                            rows = roster.entries.sortedForMarking().map { entry -> entry.toRosterRowUi() },
                            marks = marks,
                            originalMarks = marks
                        )
                    }
                }
                is Result.Error -> _state.update { it.copy(isLoading = false, error = result.error.toUiText()) }
            }
        }
    }

    // Tapping the selected button goes back to the saved value, since the API has no way to unmark.
    private fun mark(accountId: String, status: AttendanceStatus) {
        _state.update { state ->
            val next = if (state.marks[accountId] == status) state.originalMarks[accountId] else status
            state.copy(marks = state.marks + (accountId to next))
        }
    }

    // Only the rows on screen that are still unmarked, so a search or filter never marks hidden people.
    private fun markAllPresent() {
        _state.update { state ->
            val toMark = state.visibleRows.filter { state.marks[it.accountId] == null }
            state.copy(marks = state.marks + toMark.associate { it.accountId to AttendanceStatus.PRESENT })
        }
    }

    private fun submit() {
        val current = _state.value
        if (current.isSubmitting || current.changedCount == 0) return
        // The API upserts, so only the changed marks go over the wire.
        val changes = current.rows.mapNotNull { row ->
            val status = current.marks[row.accountId]
            if (status != null && status != current.originalMarks[row.accountId]) AttendanceMark(row.accountId, status) else null
        }
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            when (val result = submitMarksUseCase(sessionId, changes)) {
                is Result.Success -> {
                    val entries = result.data.entries
                    _state.update { it.copy(isSubmitting = false) }
                    _events.send(
                        SessionRosterEvent.Saved(
                            present = entries.count { it.status == AttendanceStatus.PRESENT },
                            absent = entries.count { it.status == AttendanceStatus.ABSENT }
                        )
                    )
                }
                is Result.Error -> {
                    _state.update { it.copy(isSubmitting = false) }
                    _events.send(SessionRosterEvent.ShowMessage(result.error.toUiText()))
                }
            }
        }
    }

    private fun close() {
        viewModelScope.launch { _events.send(SessionRosterEvent.Close) }
    }
}
