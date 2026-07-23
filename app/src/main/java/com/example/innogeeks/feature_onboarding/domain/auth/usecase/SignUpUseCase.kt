package com.example.innogeeks.feature_onboarding.domain.auth.usecase

import com.example.innogeeks.core.domain.util.EmptyResult
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.core.domain.util.mapError
import com.example.innogeeks.core.domain.util.onFailure
import com.example.innogeeks.feature_onboarding.domain.auth.AuthRepository
import com.example.innogeeks.feature_onboarding.domain.auth.AuthValidator

// One business action: "sign up a user". Depends on the AuthRepository INTERFACE (not
// a concrete impl) + the shared AuthValidator. Both injected via constructor (Koin later).
class SignUpUseCase(
    private val authRepository: AuthRepository,
    private val authValidator: AuthValidator
) {
    // operator fun invoke => call the object like a function: signUpUseCase(email, password).
    // A use case does exactly ONE thing, so its single method is just `invoke`.
    suspend operator fun invoke(
        name : String,
        email: String,
        password: String
    ) : EmptyResult<SignUpError>{

        // Validation gate: check input BEFORE any network call. onFailure's lambda
        // early-returns out of invoke with the error wrapped as SignUpError.Validation.

        authValidator.validateName(name)
            .onFailure { return Result.Error(SignUpError.Validation(it)) }

        authValidator.validateEmail(email)
            .onFailure { return Result.Error(SignUpError.Validation(it)) }

        authValidator.validatePassword(password)
            .onFailure { return Result.Error(SignUpError.Validation(it)) }

        // Both valid → hit the backend. signUp returns Result<Unit, DataError>; mapError
        // converts that DataError into SignUpError.Remote so the whole fn returns one
        // unified error type (SignUpError). Success passes through untouched.
        return authRepository.signUp(name,email,password)
            .mapError { SignUpError.Remote(it) }
    }
}