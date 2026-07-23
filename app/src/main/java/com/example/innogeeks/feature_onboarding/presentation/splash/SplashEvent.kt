package com.example.innogeeks.feature_onboarding.presentation.splash

// One-shot routing events out of the splash. The splash's whole job is to decide where to
// go next: first-launch users see the intro; returning users skip straight to login.
sealed interface SplashEvent {
    data object NavigateToIntro : SplashEvent
    data object NavigateToLogin : SplashEvent
}
