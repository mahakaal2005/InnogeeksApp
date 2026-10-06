package com.example.innogeeks.feature_onboarding.data.auth

import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_onboarding.domain.auth.AuthApiError
import com.example.innogeeks.feature_onboarding.domain.auth.AuthError
import com.example.innogeeks.feature_onboarding.domain.auth.AuthRemoteDataSource
import com.example.innogeeks.feature_onboarding.domain.auth.NextStep
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

// Stands in for the backend until a host exists. Mirrors the contract's shapes and error
// codes exactly so swapping in KtorAuthRemoteDataSource changes nothing above the data layer.
class FakeAuthRemoteDataSource : AuthRemoteDataSource {

    private data class Account(
        val eligible: Boolean,
        var password: String? = null,
        var resetToken: String? = null
    )

    // Three accounts so every branch of the flow is reachable on a device.
    private val accounts = mutableMapOf(
        SETUP_EMAIL to Account(eligible = true),
        LOGIN_EMAIL to Account(eligible = true, password = "innogeeks123"),
        DENIED_EMAIL to Account(eligible = false)
    )

    private var failedAttempts = 0
    private var issuedSetupToken: String? = null

    override suspend fun checkEmail(collegeEmail: String): Result<NextStep, AuthError> {
        delay(NETWORK_DELAY.milliseconds)
        val account = accounts[collegeEmail.lowercase()]
        // An unknown email is indistinguishable from an ineligible one — §4 wants one message.
        if (account == null || !account.eligible) return denied()
        return Result.Success(
            if (account.password == null) NextStep.PASSWORD_SETUP else NextStep.PASSWORD_LOGIN
        )
    }

    override suspend fun requestVerificationCode(collegeEmail: String): Result<Unit, AuthError> {
        delay(NETWORK_DELAY.milliseconds)
        val account = accounts[collegeEmail.lowercase()] ?: return denied()
        if (!account.eligible) return denied()
        if (account.password != null) return apiError(AuthApiError.PASSWORD_ALREADY_SET)
        // A new code invalidates the old one, so the attempt counter resets with it.
        failedAttempts = 0
        return Result.Success(Unit)
    }

    override suspend fun verifyCode(
        collegeEmail: String,
        code: String
    ): Result<String, AuthError> {
        delay(NETWORK_DELAY.milliseconds)
        val account = accounts[collegeEmail.lowercase()] ?: return denied()
        if (!account.eligible) return denied()
        if (account.password != null) return apiError(AuthApiError.PASSWORD_ALREADY_SET)

        if (code != FIXED_CODE || failedAttempts >= MAX_ATTEMPTS) {
            failedAttempts++
            return apiError(AuthApiError.VERIFICATION_CODE_INVALID)
        }

        failedAttempts = 0
        // Contract requires at least 20 characters, so the real length constraint is exercised.
        return Result.Success("fake-setup-token-$collegeEmail".also { issuedSetupToken = it })
    }

    override suspend fun setPassword(
        passwordSetupToken: String,
        password: String
    ): Result<String, AuthError> {
        delay(NETWORK_DELAY)
        // Single-use: the token is cleared whether or not this call succeeds.
        val expected = issuedSetupToken
        issuedSetupToken = null
        if (expected == null || passwordSetupToken != expected) {
            return apiError(AuthApiError.PASSWORD_SETUP_TOKEN_INVALID)
        }
        if (password.length !in MIN_PASSWORD..MAX_PASSWORD) {
            return apiError(AuthApiError.VALIDATION_ERROR)
        }

        val email = expected.removePrefix("fake-setup-token-")
        val account = accounts[email] ?: return apiError(AuthApiError.PASSWORD_SETUP_NOT_ALLOWED)
        if (account.password != null) return apiError(AuthApiError.PASSWORD_SETUP_NOT_ALLOWED)

        account.password = password
        return Result.Success(fakeToken(email))
    }

    override suspend fun login(
        collegeEmail: String,
        password: String
    ): Result<String, AuthError> {
        delay(NETWORK_DELAY)
        val email = collegeEmail.lowercase()
        val account = accounts[email] ?: return apiError(AuthApiError.INVALID_CREDENTIALS)
        // Eligibility is rechecked on every login, per §8.
        if (!account.eligible) return denied()
        if (account.password == null || account.password != password) {
            return apiError(AuthApiError.INVALID_CREDENTIALS)
        }
        return Result.Success(fakeToken(email))
    }

    override suspend fun requestPasswordResetCode(collegeEmail: String): Result<Unit, AuthError> {
        delay(NETWORK_DELAY)
        val account = accounts[collegeEmail.lowercase()] ?: return denied()
        if (!account.eligible) return denied()
        if (account.password == null) return apiError(AuthApiError.PASSWORD_NOT_SET)
        // Reset code requested, reset attempt counter
        failedAttempts = 0
        return Result.Success(Unit)
    }

    override suspend fun verifyResetCode(
        collegeEmail: String,
        code: String
    ): Result<String, AuthError> {
        delay(NETWORK_DELAY)
        val account = accounts[collegeEmail.lowercase()] ?: return denied()
        if (!account.eligible) return denied()
        if (account.password == null) return apiError(AuthApiError.PASSWORD_NOT_SET)

        if (code != FIXED_CODE || failedAttempts >= MAX_ATTEMPTS) {
            failedAttempts++
            return apiError(AuthApiError.PASSWORD_RESET_CODE_INVALID)
        }

        failedAttempts = 0
        val token = "fake-reset-token-$collegeEmail"
        account.resetToken = token
        return Result.Success(token)
    }

    override suspend fun completePasswordReset(
        passwordResetToken: String,
        password: String
    ): Result<String, AuthError> {
        delay(NETWORK_DELAY)
        if (password.length !in MIN_PASSWORD..MAX_PASSWORD) {
            return apiError(AuthApiError.VALIDATION_ERROR)
        }

        // Find account by matching reset token
        val email = accounts.entries.firstOrNull { it.value.resetToken == passwordResetToken }?.key
            ?: return apiError(AuthApiError.PASSWORD_RESET_TOKEN_INVALID)

        val account = accounts[email]!!
        account.password = password
        account.resetToken = null // Single-use token consumed
        return Result.Success(fakeToken(email))
    }

    override suspend fun logout(): Result<Unit, AuthError> {
        delay(NETWORK_DELAY)
        // Fake implementation just returns success - token revocation happens in SessionRepository
        return Result.Success(Unit)
    }

    private fun denied() = apiError(AuthApiError.APP_ACCESS_DENIED)

    private fun apiError(code: AuthApiError) = Result.Error(AuthError.Api(code))

    private fun fakeToken(email: String) = "fake-access-token-$email"

    companion object {
        // Test accounts, kept public so the login screen can show them while the backend is absent.
        const val SETUP_EMAIL = "setup@kiet.edu"
        const val LOGIN_EMAIL = "login@kiet.edu"
        const val DENIED_EMAIL = "denied@kiet.edu"
        const val FIXED_CODE = "123456"

        private const val MAX_ATTEMPTS = 5
        private const val MIN_PASSWORD = 8
        private const val MAX_PASSWORD = 128
        private const val NETWORK_DELAY = 900L
    }
}
