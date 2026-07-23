package com.example.innogeeks.feature_onboarding.presentation.intro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innogeeks.feature_onboarding.domain.auth.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Injects AuthRepository directly to call markIntroSeen() (trivial pass-through, no use case).
class IntroViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(IntroState())
    val state = _state.asStateFlow()

    private val _events = Channel<IntroEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: IntroAction) {
        when (action) {
            // Pager settled on a new page (swipe) -> record it so isLastPage stays correct.
            is IntroAction.OnPageChanged -> {
                _state.update { it.copy(currentPage = action.page) }
            }
            // Next -> ask the pager to animate to the next page (via an event, since the
            // pager's scroll position is Compose-owned state the ViewModel can't touch directly).
            is IntroAction.OnNextClick -> {
                val next = (state.value.currentPage + 1).coerceAtMost(state.value.pages.lastIndex)
                viewModelScope.launch { _events.send(IntroEvent.ScrollToPage(next)) }
            }
            // Skip and Get Started both finish the intro: mark it seen, then go to login.
            is IntroAction.OnSkipClick -> finishIntro()
            is IntroAction.OnGetStartedClick -> finishIntro()
        }
    }

    private fun finishIntro() {
        viewModelScope.launch {
            authRepository.markIntroSeen()   // returning users skip the intro next launch
            _events.send(IntroEvent.NavigateToLogin)
        }
    }
}
