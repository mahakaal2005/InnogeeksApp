package com.example.innogeeks.feature_onboarding.domain.auth

import com.example.innogeeks.core.common.Constants.EMAIL_REGEX
import com.example.innogeeks.core.domain.util.EmptyResult
import com.example.innogeeks.core.domain.util.Result

class AuthValidator {

    fun validateEmail(email: String) : EmptyResult<AuthValidationError>{
        if(email.isBlank()) return Result.Error(AuthValidationError.EMPTY_EMAIL)
        if(!email.matches(EMAIL_REGEX)) return Result.Error(AuthValidationError.INVALID_EMAIL)
        return Result.Success(Unit)
    }

    fun validatePassword(password : String) : EmptyResult<AuthValidationError>{
        if(password.isBlank()) return Result.Error(AuthValidationError.EMPTY_PASSWORD)
        if(password.length < MIN_PASSWORD_LENGTH ) return Result.Error(AuthValidationError.PASSWORD_TOO_SHORT)
        return Result.Success(Unit)
    }
    companion object {
        private const val MIN_PASSWORD_LENGTH = 8
    }
}