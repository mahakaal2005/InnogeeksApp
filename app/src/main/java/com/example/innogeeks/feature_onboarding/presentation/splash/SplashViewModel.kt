package com.example.innogeeks.feature_onboarding.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innogeeks.core.domain.model.UserDomain
import com.example.innogeeks.core.domain.model.UserRole
import com.example.innogeeks.core.domain.session.Session
import com.example.innogeeks.core.domain.session.SessionRepository
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

// Injects SessionRepository DIRECTLY (not via a use case): reading hasSeenIntro() is a trivial
// pass-through with no logic/validation, so a use case would be empty ceremony.
class SplashViewModel(
    private val sessionRepository: SessionRepository,
    private val profileRepository: ProfileRepository
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

            // A signed-in user skips the intro even on a fresh install of the same account.
            val session = sessionRepository.session.first()
            val isAuthenticated = session is Session.Authenticated
            if (isAuthenticated) {
                // Role/domain are never in the JWT — resync from the profile on every cold start
                // so a promotion made while the app was closed is picked up on next launch.
                refreshRoleAndDomain()
            }
            val seenIntro = sessionRepository.hasSeenIntro()
            _state.update { it.copy(isLoading = false) }

            if (isAuthenticated || seenIntro) {
                _events.send(SplashEvent.NavigateToHome)
            } else {
                _events.send(SplashEvent.NavigateToIntro)
            }
        }
    }

    private suspend fun refreshRoleAndDomain() {
        val profile = profileRepository.getProfile()
        if (profile is Result.Success) {
            val role = runCatching { UserRole.valueOf(profile.data.role) }.getOrDefault(UserRole.REGISTERED)
            val domain = profile.data.domain?.let { runCatching { UserDomain.valueOf(it) }.getOrNull() }
            sessionRepository.updateRoleAndDomain(role, domain)
        }
        // A failed fetch just leaves the previously-stored role/domain in place.
    }
}
