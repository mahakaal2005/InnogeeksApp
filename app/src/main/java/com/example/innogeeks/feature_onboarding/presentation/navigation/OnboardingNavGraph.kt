package com.example.innogeeks.feature_onboarding.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.innogeeks.feature_onboarding.presentation.login.LoginRoot
import com.example.innogeeks.feature_onboarding.presentation.signup.SignUpRoot

// The onboarding feature's nav graph. An extension on NavGraphBuilder so :app can plug it
// into the NavHost. navController = for moving BETWEEN onboarding screens; onNavigateToHome
// = a callback for leaving to another feature (onboarding never imports Home's route).
fun NavGraphBuilder.onboardingGraph(
    navController: NavController,
    onNavigateToHome : () -> Unit
){
    // Nested sub-graph named OnboardingGraphRoute; entering it shows LoginRoute first.
    navigation<OnboardingGraphRoute>(startDestination = LoginRoute){

        composable <LoginRoute>{
            LoginRoot(
                onNavigateToHome = onNavigateToHome,                       // cross-feature: callback
                onNavigateToSignUp = {navController.navigate(SignUpRoute)} // within-feature: direct
            )
        }

        composable<SignUpRoute>{
            SignUpRoot(
                onNavigateToLogin = {navController.navigate(LoginRoute)}
            )
        }
    }
}