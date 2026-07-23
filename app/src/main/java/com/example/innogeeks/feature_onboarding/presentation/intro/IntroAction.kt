package com.example.innogeeks.feature_onboarding.presentation.intro

// User actions on the intro carousel. Unlike the splash (no interaction), the intro DOES
// respond to the user: swiping pages, and tapping Next / Skip / Get Started.
sealed interface IntroAction {
    // Fired when the pager settles on a new page (swipe) — keeps state.currentPage in sync.
    data class OnPageChanged(val page: Int) : IntroAction

    data object OnNextClick : IntroAction
    data object OnSkipClick : IntroAction
    data object OnGetStartedClick : IntroAction
}
