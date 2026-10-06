package com.example.innogeeks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.innogeeks.core.navigation.MainRoute
import com.example.innogeeks.shell.shellGraph
import com.example.innogeeks.feature_onboarding.presentation.navigation.AuthGraphRoute
import com.example.innogeeks.feature_onboarding.presentation.navigation.OnboardingGraphRoute
import com.example.innogeeks.feature_onboarding.presentation.navigation.authGraph
import com.example.innogeeks.feature_onboarding.presentation.navigation.onboardingGraph
import com.example.innogeeks.ui.theme.InnogeeksTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Force status & navigation bars to dark transparent mode regardless of system settings.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        setContent {
            InnogeeksTheme {
                // rememberNavController keeps the controller across recompositions.
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = OnboardingGraphRoute
                ) {
                    onboardingGraph(
                        navController = navController,
                        onNavigateToHome = {
                            // Clear onboarding from back stack; system back on Home will exit app.
                            navController.navigate(MainRoute) {
                                popUpTo(OnboardingGraphRoute) { inclusive = true }
                            }
                        }
                    )
                    // Registers MainRoute and MainScaffold
                    shellGraph(
                        onNavigateToAuth = { navController.navigate(AuthGraphRoute) }
                    )

                    authGraph(
                        navController = navController,
                        // Pop the whole auth graph so Back never re-enters a finished login.
                        onAuthenticated = {
                            navController.popBackStack(AuthGraphRoute, inclusive = true)
                        },
                        onDismiss = {
                            navController.popBackStack(AuthGraphRoute, inclusive = true)
                        }
                    )
                }
            }
        }
    }
}
