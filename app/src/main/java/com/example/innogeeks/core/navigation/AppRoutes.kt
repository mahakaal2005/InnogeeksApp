package com.example.innogeeks.core.navigation

import kotlinx.serialization.Serializable

// Shared top-level routes (not owned by any single feature).
// Only add here if 2+ features need to reference the same route.

// The main app shell (Scaffold + bottom nav). Navigated to after onboarding completes.
// Not a screen itself — homeGraph uses it as the container for MainScaffoldRoute.
@Serializable
data object MainRoute
