package com.example.innogeeks.feature_recruitment.presentation.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.core.presentation.mapper.toUiText
import com.example.innogeeks.feature_recruitment.domain.use_case.GetRecruitmentStatusUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrackerViewModel(
    private val getRecruitmentStatusUseCase: GetRecruitmentStatusUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TrackerState())
    val state = _state.asStateFlow()

    private val _events = Channel<TrackerEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadRecruitmentStatus()
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
        viewModelScope.launch {
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
