package com.example.innogeeks.feature_onboarding.presentation.splash

// The splash shows briefly while the ViewModel DECIDES where to route (Intro vs Login).
// isLoading = "still deciding" — drives a fade/spinner. No user input on a splash, so this
// is the only field, and there is no SplashAction type at all.
data class SplashState(
    val isLoading: Boolean = true
)
