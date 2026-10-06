package com.example.innogeeks.feature_onboarding.domain.use_case

import com.example.innogeeks.core.domain.util.EmptyResult
import com.example.innogeeks.feature_onboarding.domain.auth.AuthError
import com.example.innogeeks.feature_onboarding.domain.auth.AuthFlowRepository

class SendVerificationCodeUseCase(
    private val authFlowRepository: AuthFlowRepository
) {
    suspend operator fun invoke(collegeEmail: String): EmptyResult<AuthError> =
        authFlowRepository.requestVerificationCode(collegeEmail)
}
