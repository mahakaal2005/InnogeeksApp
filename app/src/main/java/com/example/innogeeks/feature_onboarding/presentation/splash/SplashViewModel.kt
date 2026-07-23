package com.example.innogeeks.feature_onboarding.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innogeeks.feature_onboarding.domain.auth.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

// Injects AuthRepository DIRECTLY (not via a use case): reading hasSeenIntro() is a trivial
// pass-through with no logic/validation, so a use case would be empty ceremony.
class SplashViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SplashState())
    val state = _state.asStateFlow()

    private val _events = Channel<SplashEvent>()
    val events = _events.receiveAsFlow()

    // No onAction(): a splash has no user interaction. The decision runs automatically once,
    // in init {}, the moment the ViewModel is created.
    init {
        decideStartDestination()
    }

    private fun decideStartDestination() {
        viewModelScope.launch {
            // Hold long enough for the full "Spiral Awakens" splash animation to play out
            // (spin-up -> pulse -> identity -> anchor -> a beat of ambient) before we route away.
            delay(3800.milliseconds)

            val seenIntro = authRepository.hasSeenIntro()
            _state.update { it.copy(isLoading = false) }

            // First launch -> intro slides; returning user -> straight to login.
            if (seenIntro) {
                _events.send(SplashEvent.NavigateToLogin)
            } else {
                _events.send(SplashEvent.NavigateToIntro)
            }
        }
    }
}
