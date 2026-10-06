package com.example.innogeeks.feature_onboarding.presentation.splash

// The splash shows while the ViewModel decides where to route; isLoading is the only field since there is no user input.
data class SplashState(
    val isLoading: Boolean = true
)
