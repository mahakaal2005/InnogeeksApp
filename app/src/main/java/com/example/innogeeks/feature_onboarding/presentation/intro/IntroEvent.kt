package com.example.innogeeks.feature_onboarding.presentation.intro

// One-shot events out of the intro. Two kinds:
//  - ScrollToPage: tell the pager (Compose-owned state) to animate to a page after a Next tap.
//  - NavigateToLogin: leave the intro (Skip or Get Started) -> the ViewModel also marks the
//    intro as seen so returning users skip it next launch.
sealed interface IntroEvent {
    data class ScrollToPage(val page: Int) : IntroEvent
    data object NavigateToLogin : IntroEvent
}
