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
import com.example.innogeeks.core.presentation.UiText
import edu.kiet.innogeeks.R

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

            // All five sources are independent, so fetch them concurrently.
            val statsDeferred = async { repository.getClubStats() }
            val domainsDeferred = async { repository.getDomains() }
            val achievementsDeferred = async { repository.getAchievements() }
            val tickerDeferred = async { repository.getTickerKeywords() }
            val cultureDeferred = async { repository.getCultureMoments() }

            val statsResult = statsDeferred.await()
            val domainsResult = domainsDeferred.await()
            val achievementsResult = achievementsDeferred.await()
            val tickerResult = tickerDeferred.await()
            val cultureResult = cultureDeferred.await()

            val allSucceeded = statsResult.isSuccess &&
                    domainsResult.isSuccess &&
                    achievementsResult.isSuccess &&
                    tickerResult.isSuccess &&
                    cultureResult.isSuccess

            if (allSucceeded) {
                val domains = domainsResult.getOrDefault(emptyList())
                _state.update {
                    it.copy(
                        isLoading = false,
                        stats = statsResult.getOrNull(),
                        domains = domains,
                        achievements = achievementsResult.getOrDefault(emptyList()),
                        tickerRows = tickerResult.getOrDefault(emptyList()),
                        cultureMoments = cultureResult.getOrDefault(emptyList()),
                        selectedDomainId = domains.firstOrNull()?.id
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = UiText.StringResource(R.string.home_load_error)
                    )
                }
            }
        }
    }

    fun onAction(action: HomeAction) {
        when (action) {
            // Selection is pure state — no navigation, the wheel just snaps.
            is HomeAction.OnDomainSelected -> {
                _state.update { it.copy(selectedDomainId = action.domainId) }
            }

            HomeAction.OnProfileClick -> {
                viewModelScope.launch { _events.send(HomeEvent.NavigateToProfile) }
            }

            is HomeAction.OnClassCultureClick -> {
                viewModelScope.launch { _events.send(HomeEvent.NavigateToEvents(action.eventId)) }
            }
        }
    }
}
