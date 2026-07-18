package com.example.innogeeks.feature_onboarding.data

import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.model.UserRole
import com.example.innogeeks.core.domain.util.EmptyResult
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_onboarding.domain.model.AuthUser
import com.example.innogeeks.feature_onboarding.domain.model.RegistrationForm
import com.example.innogeeks.feature_onboarding.domain.registration.RegistrationRepository
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

// Fake stand-in for the club-registration backend until it exists. Implements the SAME
// RegistrationRepository interface the ViewModel depends on, so the real Ktor+Room impl
// later swaps in via one Koin line — domain & presentation don't change. delay(...) fakes
// network latency so loading spinners actually have something to spin for.
class InMemoryRegistrationRepository : RegistrationRepository {

    // The one code our fake "backend" accepts as proof of payment. Real backend emails a
    // unique code per payment; here it's a single hard-coded value so you can test the
    // unpaid -> paid flip by hand.
    private val validCode = "INNO2026"

    override suspend fun submitRegistration(form: RegistrationForm): EmptyResult<DataError> {
        delay(1000.milliseconds)
        // Nothing to hand back — the user still has to pay. A real backend would persist
        // the form here; our fake just pretends it succeeded.
        return Result.Success(Unit)
    }

    override suspend fun redeemPaymentCode(code: String): Result<AuthUser, DataError> {
        // Wrong code -> 404 Not Found: a code is a lookup ("does this token exist?"), not a
        // login, so NOT_FOUND — not UNAUTHORIZED — is the honest error for the UI to map.
        delay(1000.milliseconds)
        if (code != validCode) {
            return Result.Error(DataError.Network.NOT_FOUND)
        }
        // Correct code -> canned user with isPaid = TRUE. This flip is the whole payoff:
        // the UI watches isPaid and immediately swaps to the paid experience.
        return Result.Success(
            AuthUser(
                id = "Fake-1",
                email = "fake@innogeeks.com",
                fullName = null,
                isPaid = true,
                role = UserRole.REGISTERED
            )
        )
    }
}