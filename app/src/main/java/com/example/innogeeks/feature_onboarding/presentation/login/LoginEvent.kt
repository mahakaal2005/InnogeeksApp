package com.example.innogeeks.feature_onboarding.presentation.login

sealed interface LoginEvent {
    data object NavigateToHome : LoginEvent
    data object NavigateToSignUp : LoginEvent
}