package com.example.innogeeks.feature_onboarding.presentation

import com.example.innogeeks.feature_onboarding.presentation.auth.emailgate.EmailGateViewModel
import com.example.innogeeks.feature_onboarding.presentation.auth.passwordlogin.PasswordLoginViewModel
import com.example.innogeeks.feature_onboarding.presentation.auth.passwordreset.PasswordResetCompleteViewModel
import com.example.innogeeks.feature_onboarding.presentation.auth.passwordreset.PasswordResetRequestViewModel
import com.example.innogeeks.feature_onboarding.presentation.auth.passwordreset.PasswordResetVerifyCodeViewModel
import com.example.innogeeks.feature_onboarding.presentation.auth.setpassword.SetPasswordViewModel
import com.example.innogeeks.feature_onboarding.presentation.auth.verifycode.VerifyCodeViewModel
import com.example.innogeeks.feature_onboarding.presentation.splash.SplashViewModel
import com.example.innogeeks.feature_onboarding.presentation.intro.IntroViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val onboardingPresentationModule = module {
    viewModelOf(::SplashViewModel)
    viewModelOf(::IntroViewModel)

    // Auth flow. The three later screens carry the college email (and setup token) forward,
    // so they take those as runtime parameters instead of resolving them from the graph.
    viewModelOf(::EmailGateViewModel)
    viewModel { params ->
        VerifyCodeViewModel(
            collegeEmail = params.get(),
            sendVerificationCodeUseCase = get(),
            submitVerificationCodeUseCase = get()
        )
    }
    viewModel { params ->
        SetPasswordViewModel(
            collegeEmail = params.get(),
            passwordSetupToken = params.get(),
            submitPasswordSetupUseCase = get(),
            authValidator = get()
        )
    }
    viewModel { params ->
        PasswordLoginViewModel(collegeEmail = params.get(), submitLoginUseCase = get())
    }

    // Password reset flow. Mirrors the first-login verification flow structure.
    viewModelOf(::PasswordResetRequestViewModel)
    viewModel { params ->
        PasswordResetVerifyCodeViewModel(
            collegeEmail = params.get(),
            sendPasswordResetCodeUseCase = get(),
            submitPasswordResetCodeUseCase = get()
        )
    }
    viewModel { params ->
        PasswordResetCompleteViewModel(
            collegeEmail = params.get(),
            passwordResetToken = params.get(),
            submitPasswordResetUseCase = get(),
            authValidator = get()
        )
    }
}
