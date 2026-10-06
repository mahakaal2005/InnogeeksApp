package com.example.innogeeks.feature_onboarding.data.auth

import com.example.innogeeks.core.domain.model.UserRole
import com.example.innogeeks.core.domain.session.SessionRefresher
import com.example.innogeeks.core.domain.session.SessionRepository
import com.example.innogeeks.core.domain.util.EmptyResult
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.core.domain.util.asEmptyResult
import com.example.innogeeks.feature_onboarding.domain.auth.AuthError
import com.example.innogeeks.feature_onboarding.domain.auth.AuthFlowRepository
import com.example.innogeeks.feature_onboarding.domain.auth.AuthRemoteDataSource
import com.example.innogeeks.feature_onboarding.domain.auth.NextStep

// Thin layer over the data source whose one real job is to trap the access token here
// and hand it to SessionRepository instead of returning it upward.
class DefaultAuthFlowRepository(
    private val remote: AuthRemoteDataSource,
    private val sessionRepository: SessionRepository,
    private val sessionRefresher: SessionRefresher
) : AuthFlowRepository {

    override suspend fun checkEmail(collegeEmail: String): Result<NextStep, AuthError> =
        remote.checkEmail(collegeEmail.trim())

    override suspend fun requestVerificationCode(collegeEmail: String): EmptyResult<AuthError> =
        remote.requestVerificationCode(collegeEmail.trim())

    override suspend fun verifyCode(
        collegeEmail: String,
        code: String
    ): Result<String, AuthError> = remote.verifyCode(collegeEmail.trim(), code)

    override suspend fun setPassword(
        collegeEmail: String,
        passwordSetupToken: String,
        password: String
    ): EmptyResult<AuthError> {
        val result = remote.setPassword(passwordSetupToken, password)
        if (result is Result.Success) {
            signIn(accessToken = result.data, collegeEmail = collegeEmail.trim())
        }
        return result.asEmptyResult()
    }

    override suspend fun login(collegeEmail: String, password: String): EmptyResult<AuthError> {
        val email = collegeEmail.trim()
        val result = remote.login(email, password)
        if (result is Result.Success) {
            signIn(accessToken = result.data, collegeEmail = email)
        }
        return result.asEmptyResult()
    }

    override suspend fun requestPasswordResetCode(collegeEmail: String): EmptyResult<AuthError> =
        remote.requestPasswordResetCode(collegeEmail.trim())

    override suspend fun verifyResetCode(
        collegeEmail: String,
        code: String
    ): Result<String, AuthError> = remote.verifyResetCode(collegeEmail.trim(), code)

    override suspend fun completePasswordReset(
        collegeEmail: String,
        passwordResetToken: String,
        password: String
    ): EmptyResult<AuthError> {
        val email = collegeEmail.trim()
        val result = remote.completePasswordReset(passwordResetToken, password)
        if (result is Result.Success) {
            signIn(accessToken = result.data, collegeEmail = email)
        }
        return result.asEmptyResult()
    }

    override suspend fun logout(): EmptyResult<AuthError> {
        val result = remote.logout()
        if (result is Result.Success) {
            sessionRepository.signOut()
        }
        return result
    }

    // Role/domain never come from the login response — only GET /me knows them, same as the
    // real backend contract. The token must be stored BEFORE calling getProfile(): Ktor's Auth
    // plugin loads the bearer token once via SessionRepository.currentAccessToken() and caches
    // it for the HttpClient's lifetime (no refreshTokens block is configured), so calling
    // getProfile() first — with no token yet in the session — makes that first authenticated
    // call go out with no Authorization header, and it never reloads on later calls either.
    // Falls back to REGISTERED/null if the profile fetch fails so a login still succeeds even
    // if the profile call has trouble.
    private suspend fun signIn(accessToken: String, collegeEmail: String) {
        sessionRepository.signIn(
            accessToken = accessToken,
            collegeEmail = collegeEmail,
            role = UserRole.REGISTERED,
            domain = null
        )
        sessionRefresher.refreshRoleAndDomain()
    }
}
