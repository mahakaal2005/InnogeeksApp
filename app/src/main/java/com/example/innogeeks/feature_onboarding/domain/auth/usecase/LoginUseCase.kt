package com.example.innogeeks.feature_onboarding.domain.auth.usecase

import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.core.domain.util.mapError
import com.example.innogeeks.core.domain.util.onFailure
import com.example.innogeeks.feature_onboarding.domain.auth.AuthRepository
import com.example.innogeeks.feature_onboarding.domain.auth.AuthValidator
import com.example.innogeeks.feature_onboarding.domain.model.AuthUser

// One business action: "log a user in". Same shape as SignUpUseCase, but returns the
// AuthUser on success (so the ViewModel can read isPaid/role and pick the right screen).
class LoginUseCase(
    val authValidator: AuthValidator,
    val authRepository: AuthRepository
) {

    suspend operator fun invoke(
         email : String,
         password: String
    ) : Result<AuthUser , LoginError>{

        // Validate before hitting the network (same gate as signup).
        authValidator.validateEmail(email)
            .onFailure { return Result.Error(LoginError.Validation(it)) }

        authValidator.validatePassword(password)
            .onFailure { return Result.Error(LoginError.Validation(it)) }

        // mapError only touches the error side — Success(AuthUser) passes through untouched.
        return authRepository.login(email , password)
            .mapError { LoginError.Remote(it) }
    }
}