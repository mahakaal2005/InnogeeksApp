package com.example.innogeeks.feature_events.presentation.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innogeeks.feature_events.domain.EventsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.innogeeks.core.presentation.UiText
import edu.kiet.innogeeks.R

class EventsViewModel(
    private val repository: EventsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EventsState())
    val state = _state.asStateFlow()

    init {
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            repository.getEvents()
                .onSuccess { events ->
                    _state.update { it.copy(isLoading = false, events = events) }
                }
                .onFailure {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = UiText.StringResource(R.string.events_load_error)
                        )
                    }
                }
        }
    }

    fun onAction(action: EventsAction) {
        when (action) {
            EventsAction.OnRetry -> loadEvents()
        }
    }
}
