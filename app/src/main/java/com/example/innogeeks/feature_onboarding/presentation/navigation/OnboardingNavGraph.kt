package com.example.innogeeks.feature_onboarding.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.innogeeks.feature_onboarding.presentation.intro.IntroRoot
import com.example.innogeeks.feature_onboarding.presentation.login.LoginRoot
import com.example.innogeeks.feature_onboarding.presentation.signup.SignUpRoot
import com.example.innogeeks.feature_onboarding.presentation.splash.SplashRoot

// The onboarding feature's nav graph. An extension on NavGraphBuilder so :app can plug it
// into the NavHost. navController = for moving BETWEEN onboarding screens; onNavigateToHome
// = a callback for leaving to another feature (onboarding never imports Home's route).
fun NavGraphBuilder.onboardingGraph(
    navController: NavController,
    onNavigateToHome : () -> Unit
){
    // Nested sub-graph; the flow starts at Splash, which decides Intro vs Login.
    navigation<OnboardingGraphRoute>(startDestination = SplashRoute){

        composable<SplashRoute> {
            SplashRoot(
                // popUpTo(SplashRoute){inclusive} removes Splash from the back stack, so back
                // from Intro/Login never returns to the splash.
                onNavigateToIntro = {
                    navController.navigate(IntroRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(LoginRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<IntroRoute> {
            IntroRoot(
                // Clear Intro from the back stack too — after Get Started, back shouldn't
                // return to the onboarding slides.
                onNavigateToLogin = {
                    navController.navigate(LoginRoute) {
                        popUpTo(IntroRoute) { inclusive = true }
                    }
                }
            )
        }

        composable <LoginRoute>{
            LoginRoot(
                onNavigateToHome = onNavigateToHome,                       // cross-feature: callback
                onNavigateToSignUp = {
                    navController.navigate(SignUpRoute){
                        popUpTo(LoginRoute) { inclusive=true }
                    }
                } // within-feature: direct
            )
        }

        composable<SignUpRoute>{
            SignUpRoot(
                onNavigateToLogin = {
                    navController.navigate(LoginRoute){
                        popUpTo(SignUpRoute){ inclusive=true }
                    }
                }
            )
        }
    }
}