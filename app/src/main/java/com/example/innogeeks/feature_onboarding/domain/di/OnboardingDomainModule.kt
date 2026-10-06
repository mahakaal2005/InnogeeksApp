package com.example.innogeeks.feature_onboarding.domain.di

import com.example.innogeeks.feature_onboarding.domain.auth.AuthValidator
import com.example.innogeeks.feature_onboarding.domain.use_case.GetNextAuthStepUseCase
import com.example.innogeeks.feature_onboarding.domain.use_case.SendPasswordResetCodeUseCase
import com.example.innogeeks.feature_onboarding.domain.use_case.SendVerificationCodeUseCase
import com.example.innogeeks.feature_onboarding.domain.use_case.SubmitLoginUseCase
import com.example.innogeeks.feature_onboarding.domain.use_case.SubmitPasswordResetCodeUseCase
import com.example.innogeeks.feature_onboarding.domain.use_case.SubmitPasswordResetUseCase
import com.example.innogeeks.feature_onboarding.domain.use_case.SubmitPasswordSetupUseCase
import com.example.innogeeks.feature_onboarding.domain.use_case.SubmitVerificationCodeUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val onboardingDomainModule = module {
    factoryOf(::AuthValidator)
    factoryOf(::GetNextAuthStepUseCase)
    factoryOf(::SendVerificationCodeUseCase)
    factoryOf(::SubmitVerificationCodeUseCase)
    factoryOf(::SubmitPasswordSetupUseCase)
    factoryOf(::SubmitLoginUseCase)
    factoryOf(::SendPasswordResetCodeUseCase)
    factoryOf(::SubmitPasswordResetCodeUseCase)
    factoryOf(::SubmitPasswordResetUseCase)
}
