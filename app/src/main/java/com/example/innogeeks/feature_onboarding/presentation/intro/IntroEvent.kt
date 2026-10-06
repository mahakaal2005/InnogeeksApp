package com.example.innogeeks.feature_onboarding.presentation.intro

// One-shot intro events: ScrollToPage after Next, and NavigateToHome on Skip or Get Started, which also marks the intro as seen.
sealed interface IntroEvent {
    data class ScrollToPage(val page: Int) : IntroEvent
    data object NavigateToHome : IntroEvent
}
