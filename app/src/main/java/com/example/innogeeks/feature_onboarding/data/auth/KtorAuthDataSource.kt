package com.example.innogeeks.feature_onboarding.data.auth

import com.example.innogeeks.core.data.networking.postEnveloped
import com.example.innogeeks.core.domain.error.ApiFailure
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.core.domain.util.asEmptyResult
import com.example.innogeeks.core.domain.util.mapData
import com.example.innogeeks.core.domain.util.mapError
import com.example.innogeeks.feature_onboarding.domain.auth.AuthApiError
import com.example.innogeeks.feature_onboarding.domain.auth.AuthError
import com.example.innogeeks.feature_onboarding.domain.auth.AuthRemoteDataSource
import com.example.innogeeks.feature_onboarding.domain.auth.NextStep
import io.ktor.client.HttpClient

// The real contract implementation. Written now but NOT bound in Koin — no host exists yet.
// Swapping it in is one line in OnboardingDataModule.kt.
class KtorAuthDataSource(private val httpClient: HttpClient) : AuthRemoteDataSource {

    override suspend fun checkEmail(collegeEmail: String): Result<NextStep, AuthError> =
        httpClient.postEnveloped<EmailGateRequest, EmailGateResponse>(
            route = "/api/v1/app/auth/email-gate",
            body = EmailGateRequest(collegeEmail = collegeEmail)
        ).mapData { it.toNextStep() }.mapError { it.toAuthError() }

    override suspend fun requestVerificationCode(collegeEmail: String): Result<Unit, AuthError> =
        httpClient.postEnveloped<VerificationCodeRequest, VerificationCodeResponse>(
            route = "/api/v1/app/auth/verification-code",
            body = VerificationCodeRequest(collegeEmail = collegeEmail)
        ).asEmptyResult().mapError { it.toAuthError() }

    override suspend fun verifyCode(
        collegeEmail: String,
        code: String
    ): Result<String, AuthError> =
        httpClient.postEnveloped<VerifyCodeRequest, VerifyCodeResponse>(
            route = "/api/v1/app/auth/verify-code",
            body = VerifyCodeRequest(collegeEmail = collegeEmail, code = code)
        ).mapData { it.passwordSetupToken }.mapError { it.toAuthError() }

    override suspend fun setPassword(
        passwordSetupToken: String,
        password: String
    ): Result<String, AuthError> =
        httpClient.postEnveloped<SetPasswordRequest, SetPasswordResponse>(
            route = "/api/v1/app/auth/set-password",
            body = SetPasswordRequest(passwordSetupToken = passwordSetupToken, password = password)
        ).mapData { it.accessToken }.mapError { it.toAuthError() }

    override suspend fun login(
        collegeEmail: String,
        password: String
    ): Result<String, AuthError> =
        httpClient.postEnveloped<LoginRequest, LoginResponse>(
            route = "/api/v1/app/auth/login",
            body = LoginRequest(collegeEmail = collegeEmail, password = password)
        ).mapData { it.accessToken }.mapError { it.toAuthError() }

    override suspend fun requestPasswordResetCode(collegeEmail: String): Result<Unit, AuthError> =
        httpClient.postEnveloped<PasswordResetRequestRequest, PasswordResetRequestResponse>(
            route = "/api/v1/app/auth/password-reset/request",
            body = PasswordResetRequestRequest(collegeEmail = collegeEmail)
        ).asEmptyResult().mapError { it.toAuthError() }

    override suspend fun verifyResetCode(
        collegeEmail: String,
        code: String
    ): Result<String, AuthError> =
        httpClient.postEnveloped<PasswordResetVerifyRequest, PasswordResetVerifyResponse>(
            route = "/api/v1/app/auth/password-reset/verify",
            body = PasswordResetVerifyRequest(collegeEmail = collegeEmail, code = code)
        ).mapData { it.passwordResetToken }.mapError { it.toAuthError() }

    override suspend fun completePasswordReset(
        passwordResetToken: String,
        password: String
    ): Result<String, AuthError> =
        httpClient.postEnveloped<PasswordResetCompleteRequest, PasswordResetCompleteResponse>(
            route = "/api/v1/app/auth/password-reset/complete",
            body = PasswordResetCompleteRequest(passwordResetToken = passwordResetToken, password = password)
        ).mapData { it.accessToken }.mapError { it.toAuthError() }

    override suspend fun logout(): Result<Unit, AuthError> =
        httpClient.postEnveloped<Unit, LogoutResponse>(
            route = "/api/v1/app/auth/logout",
            body = Unit
        ).asEmptyResult().mapError { it.toAuthError() }
}

// An unrecognised nextStep is a version mismatch, not a default — §9.
private fun EmailGateResponse.toNextStep(): NextStep =
    NextStep.entries.firstOrNull { it.name == nextStep } ?: NextStep.UNSUPPORTED

private fun ApiFailure.toAuthError(): AuthError = when (this) {
    is ApiFailure.Api -> AuthError.Api(AuthApiError.fromCode(code))
    is ApiFailure.Transport -> AuthError.Transport(error)
}
