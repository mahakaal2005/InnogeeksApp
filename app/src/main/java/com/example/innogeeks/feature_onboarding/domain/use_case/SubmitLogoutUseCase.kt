package com.example.innogeeks.feature_onboarding.domain.use_case

import com.example.innogeeks.core.domain.util.EmptyResult
import com.example.innogeeks.feature_onboarding.domain.auth.AuthError
import com.example.innogeeks.feature_onboarding.domain.auth.AuthFlowRepository

class SubmitLogoutUseCase(
    private val authFlowRepository: AuthFlowRepository
) {
    suspend operator fun invoke(): EmptyResult<AuthError> = authFlowRepository.logout()
}
