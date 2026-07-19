package com.example.innogeeks.feature_onboarding.presentation.signup

// One-shot events out of SignUp. Only navigation: after signup succeeds you go to LOGIN
// (not Home) — signup creates the account but doesn't log you in. Same event fires for the
// "already have an account? Log in" link.
sealed interface SignUpEvent {
    data object NavigateToLoginScreen : SignUpEvent
}