package com.example.innogeeks.feature_onboarding.presentation

import com.example.innogeeks.feature_onboarding.presentation.login.LoginViewModel
import com.example.innogeeks.feature_onboarding.presentation.signup.SignUpViewModel
import com.example.innogeeks.feature_onboarding.presentation.splash.SplashViewModel
import com.example.innogeeks.feature_onboarding.presentation.intro.IntroViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val onboardingPresentationModule = module {
    viewModelOf(::SplashViewModel)
    viewModelOf(::IntroViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::SignUpViewModel)
}