package com.example.innogeeks.feature_onboarding.data.di

import com.example.innogeeks.feature_onboarding.data.auth.DefaultAuthFlowRepository
import com.example.innogeeks.feature_onboarding.data.auth.KtorAuthRemoteDataSource
import com.example.innogeeks.feature_onboarding.domain.auth.AuthFlowRepository
import com.example.innogeeks.feature_onboarding.domain.auth.AuthRemoteDataSource
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val onboardingDataModule = module {
    singleOf(::KtorAuthRemoteDataSource) { bind<AuthRemoteDataSource>() }
    singleOf(::DefaultAuthFlowRepository) { bind<AuthFlowRepository>() }
}
