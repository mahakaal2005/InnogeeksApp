package com.example.innogeeks.feature_attendance.presentation.myattendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innogeeks.core.domain.session.Session
import com.example.innogeeks.core.domain.session.SessionRepository
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.core.presentation.mapper.toUiText
import com.example.innogeeks.feature_attendance.domain.use_case.GetMyAttendanceUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MyAttendanceViewModel(
    private val sessionRepository: SessionRepository,
    private val getMyAttendanceUseCase: GetMyAttendanceUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MyAttendanceState())
    val state = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            sessionRepository.session
                .map { it as? Session.Authenticated }
                .distinctUntilChanged()
                .collect { session ->
                    // A different user (or none) must never see the previous user's attendance.
                    loadJob?.cancel()
                    _state.value = MyAttendanceState(domain = session?.domain)
                    if (session != null) loadAttendance()
                }
        }
    }

    fun onAction(action: MyAttendanceAction) {
        when (action) {
            MyAttendanceAction.OnRetryClick -> loadAttendance()
        }
    }

    private fun loadAttendance() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = getMyAttendanceUseCase()) {
                is Result.Success -> _state.update {
                    it.copy(
                        isLoading = false,
                        summary = result.data.summary,
                        records = result.data.records.map { record -> record.toAttendanceRecordUi() }
                    )
                }
                is Result.Error -> _state.update {
                    it.copy(isLoading = false, error = result.error.toUiText())
                }
            }
        }
    }
}
