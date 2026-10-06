package com.example.innogeeks.feature_onboarding.presentation.navigation

import kotlinx.serialization.Serializable

// Type-safe nav routes; @Serializable lets the nav library store them in the back stack.
@Serializable
data object OnboardingGraphRoute

@Serializable
data object SplashRoute

@Serializable
data object IntroRoute
