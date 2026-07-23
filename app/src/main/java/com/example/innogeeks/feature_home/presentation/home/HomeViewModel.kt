package com.example.innogeeks.feature_home.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innogeeks.feature_home.domain.HomeRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: HomeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    private val _events = Channel<HomeEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            // Concurrently fetch all 3 data sources
            val statsDeferred = async { repository.getClubStats() }
            val domainsDeferred = async { repository.getDomains() }
            val eventsDeferred = async { repository.getUpcomingEvents() }
            
            val statsResult = statsDeferred.await()
            val domainsResult = domainsDeferred.await()
            val eventsResult = eventsDeferred.await()
            
            if (statsResult.isSuccess && domainsResult.isSuccess && eventsResult.isSuccess) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        stats = statsResult.getOrNull(),
                        domains = domainsResult.getOrDefault(emptyList()),
                        events = eventsResult.getOrDefault(emptyList())
                    )
                }
            } else {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load home data. Please try again."
                    )
                }
            }
        }
    }

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.OnDomainClick -> {
                viewModelScope.launch { _events.send(HomeEvent.NavigateToDomain(action.domainId)) }
            }
            is HomeAction.OnEventClick -> {
                viewModelScope.launch { _events.send(HomeEvent.NavigateToEvent(action.eventId)) }
            }
            HomeAction.OnJoinClick -> {
                viewModelScope.launch { _events.send(HomeEvent.NavigateToJoin) }
            }
        }
    }
}
