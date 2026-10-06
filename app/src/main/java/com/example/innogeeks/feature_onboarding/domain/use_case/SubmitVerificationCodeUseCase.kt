package com.example.innogeeks.feature_onboarding.domain.use_case

import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_onboarding.domain.auth.AuthError
import com.example.innogeeks.feature_onboarding.domain.auth.AuthFlowRepository

class SubmitVerificationCodeUseCase(
    private val authFlowRepository: AuthFlowRepository
) {
    suspend operator fun invoke(collegeEmail: String, code: String): Result<String, AuthError> =
        authFlowRepository.verifyCode(collegeEmail, code)
}
