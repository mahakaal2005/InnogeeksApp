package com.example.innogeeks.feature_recruitment.presentation.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innogeeks.core.domain.session.Session
import com.example.innogeeks.core.domain.session.SessionRepository
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.core.presentation.mapper.toUiText
import com.example.innogeeks.feature_recruitment.domain.use_case.GetRecruitmentStatusUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrackerViewModel(
    private val sessionRepository: SessionRepository,
    private val getRecruitmentStatusUseCase: GetRecruitmentStatusUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TrackerState())
    val state = _state.asStateFlow()

    private val _events = Channel<TrackerEvent>()
    val events = _events.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            sessionRepository.session
                .map { (it as? Session.Authenticated)?.collegeEmail }
                .distinctUntilChanged()
                .collect { email ->
                    // A different user (or none) must never see the previous user's cached status.
                    loadJob?.cancel()
                    _state.value = TrackerState()
                    if (email != null) loadRecruitmentStatus()
                }
        }
    }

    fun onAction(action: TrackerAction) {
        when (action) {
            TrackerAction.OnRetryClick -> loadRecruitmentStatus()
            TrackerAction.OnSlotsChanged -> {
                _state.update { it.copy(slotPickerKind = null) }
                loadRecruitmentStatus(showLoading = false)
            }
            is TrackerAction.OnPickSlotClick -> _state.update { it.copy(slotPickerKind = action.kind) }
            TrackerAction.OnSlotPickerDismissed -> _state.update { it.copy(slotPickerKind = null) }
            TrackerAction.OnBrowseResourcesClick -> {
                viewModelScope.launch {
                    _events.send(TrackerEvent.NavigateToResources)
                }
            }
        }
    }

    private fun loadRecruitmentStatus(showLoading: Boolean = true) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (showLoading) _state.update { it.copy(isLoading = true, error = null) }
            when (val result = getRecruitmentStatusUseCase()) {
                is Result.Success -> _state.update {
                    it.copy(isLoading = false, error = null, recruitmentStatus = result.data)
                }
                is Result.Error -> if (showLoading) {
                    _state.update { it.copy(isLoading = false, error = result.error.toUiText()) }
                }
            }
        }
    }
}
