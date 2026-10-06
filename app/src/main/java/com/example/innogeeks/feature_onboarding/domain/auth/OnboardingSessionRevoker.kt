package com.example.innogeeks.feature_onboarding.domain.auth

import com.example.innogeeks.core.domain.session.SessionRevoker
import com.example.innogeeks.feature_onboarding.domain.use_case.SubmitLogoutUseCase

class OnboardingSessionRevoker(
    private val submitLogoutUseCase: SubmitLogoutUseCase
) : SessionRevoker {

    // The result is dropped on purpose: a failed revoke must not block the local sign-out.
    override suspend fun revokeRemoteSession() {
        submitLogoutUseCase()
    }
}
