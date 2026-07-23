package com.example.innogeeks.feature_home.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.innogeeks.core.navigation.MainRoute
import com.example.innogeeks.feature_home.presentation.MainScaffold
import kotlinx.serialization.Serializable

// Extension on NavGraphBuilder — plugs home into the NavHost the same way onboardingGraph does.
fun NavGraphBuilder.homeGraph() {
    navigation<MainRoute>(startDestination = MainScaffoldRoute) {
        // The Scaffold is the only destination. Tab switching happens inside it, not via nav.
        composable<MainScaffoldRoute> {
            MainScaffold()
        }
    }
}

// Private: nothing outside navigates here directly. MainRoute is the only entry point.
@Serializable
private data object MainScaffoldRoute