package com.example.innogeeks.feature_onboarding.data.di

import com.example.innogeeks.feature_onboarding.data.auth.DefaultAuthFlowRepository
import com.example.innogeeks.feature_onboarding.data.auth.KtorAuthDataSource
import com.example.innogeeks.feature_onboarding.domain.auth.AuthFlowRepository
import com.example.innogeeks.feature_onboarding.domain.auth.AuthRemoteDataSource
import com.example.innogeeks.feature_onboarding.domain.auth.AuthValidator
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

// Koin wiring for the onboarding data graph; assembled into startKoin in InnogeeksApp.
val onboardingDataModule = module {
    single<AuthRemoteDataSource> { KtorAuthDataSource(get()) }
    singleOf(::DefaultAuthFlowRepository) bind AuthFlowRepository::class
    factoryOf(::AuthValidator)
}
