package com.example.innogeeks.feature_onboarding.domain.use_case

import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_onboarding.domain.auth.AuthError
import com.example.innogeeks.feature_onboarding.domain.auth.AuthFlowRepository
import com.example.innogeeks.feature_onboarding.domain.auth.NextStep

class GetNextAuthStepUseCase(
    private val authFlowRepository: AuthFlowRepository
) {
    suspend operator fun invoke(collegeEmail: String): Result<NextStep, AuthError> =
        authFlowRepository.checkEmail(collegeEmail)
}
