package com.example.innogeeks.feature_onboarding.data.di

import com.example.innogeeks.feature_onboarding.data.InMemoryAuthRepository
import com.example.innogeeks.feature_onboarding.data.InMemoryRegistrationRepository
import com.example.innogeeks.feature_onboarding.domain.auth.AuthRepository
import com.example.innogeeks.feature_onboarding.domain.auth.AuthValidator
import com.example.innogeeks.feature_onboarding.domain.auth.usecase.LoginUseCase
import com.example.innogeeks.feature_onboarding.domain.auth.usecase.SignUpUseCase
import com.example.innogeeks.feature_onboarding.domain.registration.RegistrationRepository
import com.example.innogeeks.feature_onboarding.domain.registration.RegistrationValidator
import com.example.innogeeks.feature_onboarding.domain.registration.usecase.RedeemCodeUseCase
import com.example.innogeeks.feature_onboarding.domain.registration.usecase.SubmitRegistrationUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

// Koin wiring for the onboarding data/domain graph. A `module` is just a list of recipes:
// how to build each object + how they connect. Assembled into startKoin in InnogeeksApp.
val onboardingDataModule = module {
    // Repositories -> single (ONE shared instance): they hold the fake in-memory "DB", so
    // reuse is mandatory. bind registers each under its INTERFACE, so use cases asking for
    // AuthRepository / RegistrationRepository get the fake — swap to real Ktor here one day.
    singleOf(::InMemoryAuthRepository) bind AuthRepository::class

    singleOf(::InMemoryRegistrationRepository) bind RegistrationRepository::class

    // Validators -> factory (fresh each time): stateless pure logic, nothing to preserve.
    // Registered because the use-case constructors below need them as ingredients.
    factoryOf(::AuthValidator)
    factoryOf(::RegistrationValidator)

    // Use cases -> factory: stateless orchestrators. factoryOf reads each constructor and
    // auto-injects its validator + repository from the recipes above (no manual get()).
    // No consumer yet — the Lesson 10 ViewModels will ask for these; pre-stocked on the shelf.
    factoryOf(::LoginUseCase)
    factoryOf(::SignUpUseCase)
    factoryOf(::SubmitRegistrationUseCase)
    factoryOf(::RedeemCodeUseCase)
}