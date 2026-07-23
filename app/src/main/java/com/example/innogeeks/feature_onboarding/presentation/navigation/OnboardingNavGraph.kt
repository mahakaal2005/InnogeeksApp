package com.example.innogeeks.feature_onboarding.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.innogeeks.feature_onboarding.presentation.intro.IntroRoot
import com.example.innogeeks.feature_onboarding.presentation.login.LoginRoot
import com.example.innogeeks.feature_onboarding.presentation.signup.SignUpRoot
import com.example.innogeeks.feature_onboarding.presentation.splash.SplashRoot

// Extension on NavGraphBuilder to register onboarding screens.
fun NavGraphBuilder.onboardingGraph(
    navController: NavController,
    onNavigateToHome: () -> Unit
) {
    navigation<OnboardingGraphRoute>(startDestination = SplashRoute) {
        composable<SplashRoute> {
            SplashRoot(
                onNavigateToIntro = {
                    navController.navigate(IntroRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                },
                // Skip intro goes to Home, not Login.
                onNavigateToLogin = { onNavigateToHome() }
            )
        }

        composable<IntroRoute> {
            IntroRoot(
                // Finishing intro goes to Home.
                onNavigateToLogin = { onNavigateToHome() }
            )
        }

        composable<LoginRoute> {
            LoginRoot(
                onNavigateToHome = onNavigateToHome,
                // Go to signup, remove login from back stack.
                onNavigateToSignUp = {
                    navController.navigate(SignUpRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<SignUpRoute> {
            SignUpRoot(
                onNavigateToLogin = {
                    navController.navigate(LoginRoute) {
                        popUpTo(SignUpRoute) { inclusive = true }
                    }
                }
            )
        }
    }
}