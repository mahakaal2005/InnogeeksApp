package com.example.innogeeks.feature_onboarding.presentation.navigation

import kotlinx.serialization.Serializable

// Type-safe nav routes. @Serializable lets the nav library store them in the back stack.
// data object = a screen with no arguments. OnboardingGraphRoute names the whole sub-graph;
// the rest are the individual screens inside it, in flow order.
@Serializable
data object OnboardingGraphRoute

@Serializable
data object SplashRoute

@Serializable
data object IntroRoute

@Serializable
data object LoginRoute


@Serializable
data object SignUpRoute