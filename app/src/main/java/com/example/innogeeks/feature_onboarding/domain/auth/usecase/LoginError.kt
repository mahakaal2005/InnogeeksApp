package com.example.innogeeks.feature_onboarding.domain.auth.usecase

import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.Error
import com.example.innogeeks.feature_onboarding.domain.auth.AuthValidationError

sealed interface LoginError : Error {
    data class Validation(val error : AuthValidationError) : LoginError

    data class Remote(val error : DataError) : LoginError
}