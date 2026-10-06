package com.example.innogeeks.feature_onboarding.domain.auth

import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.Error

// Three failure kinds, each presented differently: Api (contract message), Transport (check connection), Validation (inline field error).
sealed interface AuthError : Error {
    data class Api(val code: AuthApiError) : AuthError
    data class Transport(val error: DataError.Network) : AuthError
    data class Validation(val error: AuthValidationError) : AuthError
}
