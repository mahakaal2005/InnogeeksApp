package com.example.innogeeks.feature_onboarding.presentation

import com.example.innogeeks.feature_onboarding.presentation.login.LoginViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val onboardingPresentationModule = module {
    viewModelOf(::LoginViewModel)
}