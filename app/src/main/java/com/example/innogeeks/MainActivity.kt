package com.example.innogeeks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.innogeeks.feature_onboarding.presentation.navigation.OnboardingGraphRoute
import com.example.innogeeks.feature_onboarding.presentation.navigation.onboardingGraph
import com.example.innogeeks.ui.theme.InnogeeksTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InnogeeksTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = OnboardingGraphRoute
                ) {
                    this.onboardingGraph(
                        navController = navController,
                        onNavigateToHome = {
                            // TODO: no Home screen yet — wire when the home feature exists.
                        }
                    )
                }
            }
        }
    }
}

