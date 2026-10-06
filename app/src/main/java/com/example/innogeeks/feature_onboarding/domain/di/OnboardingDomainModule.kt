package com.example.innogeeks.feature_onboarding.domain.di

import com.example.innogeeks.feature_onboarding.domain.auth.AuthValidator
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val onboardingDomainModule = module {
    factoryOf(::AuthValidator)
}
