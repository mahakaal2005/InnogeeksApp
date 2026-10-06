package com.example.innogeeks.shell

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.innogeeks.core.domain.session.SessionRepository
import com.example.innogeeks.core.navigation.MainRoute
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

// Extension on NavGraphBuilder — plugs the app shell into the NavHost the same way onboardingGraph does.
fun NavGraphBuilder.shellGraph(
    onNavigateToAuth: () -> Unit
) {
    navigation<MainRoute>(startDestination = MainScaffoldRoute) {
        // The Scaffold is the only destination. Tab switching happens inside it, not via nav.
        composable<MainScaffoldRoute> {
            val sessionRepository = koinInject<SessionRepository>()
            // No default here — a hardcoded Guest initialValue flashes wrong tabs for a frame
            // whenever the real session is Registered. Null just means "not read yet."
            val session by sessionRepository.session
                .collectAsStateWithLifecycle(initialValue = null)

            // Skips one frame instead of guessing; Splash already warmed this same DataStore
            // read, so in practice this resolves before the frame is ever shown.
            session?.let {
                MainScaffold(
                    session = it,
                    onNavigateToAuth = onNavigateToAuth
                )
            }
        }
    }
}

// Private: nothing outside navigates here directly. MainRoute is the only entry point.
@Serializable
private data object MainScaffoldRoute
