package com.example.innogeeks.feature_onboarding.data

import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.model.UserRole
import com.example.innogeeks.core.domain.util.EmptyResult
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_onboarding.domain.auth.AuthRepository
import com.example.innogeeks.feature_onboarding.domain.model.AuthUser
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

// Stand-in for the real backend until it exists. Implements the SAME AuthRepository
// interface the ViewModel depends on, so the real KtorAuthRepository later swaps in via
// one Koin line — domain & presentation don't change. delay(...) fakes network latency
// so loading spinners actually have something to spin for.
class InMemoryAuthRepository: AuthRepository {

    // Fake "backend DB": email -> Account(password, name). Lives in memory; gone on app restart.
    private data class Account(val password: String, val name: String)
    private val accounts = mutableMapOf<String, Account>()

    private var IntroSeen = false

    override suspend fun signUp(
        name : String,
        email: String,
        password: String
    ): EmptyResult<DataError> {
        delay(1000.milliseconds)
        // Simulate a real 409 Conflict if the email is already registered.
        if(accounts.containsKey(email)){
            return Result.Error(DataError.Network.CONFLICT)
        }
        accounts[email] = Account(password = password, name = name)
        return Result.Success(Unit)
    }

    override suspend fun login(
        email: String,
        password: String
    ): Result<AuthUser, DataError> {
        delay(1000.milliseconds)
        // Unknown email OR wrong password -> 401 Unauthorized, like a real backend.
        if(!accounts.containsKey(email) || accounts[email]?.password != password){
            return Result.Error(DataError.Network.UNAUTHORIZED)
        }

        // Canned fresh user: not paid, REGISTERED -> drives the not-paid UI.
        return Result.Success(
            AuthUser(
                id = "Fake-1",
                email = email,
                fullName = accounts[email]?.name,
                isPaid = false,
                role = UserRole.REGISTERED
            )
        )
    }

    override suspend fun logout() {
        // Nothing to clear in the fake yet — real DataStore token-clear comes later.
    }

    override suspend fun hasSeenIntro(): Boolean {
        return IntroSeen
    }

    override suspend fun markIntroSeen() {
        IntroSeen = true
    }

}
