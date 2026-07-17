package com.example.innogeeks.feature_onboarding.domain.auth

import com.example.innogeeks.core.domain.util.Error

enum class AuthValidationError : Error{
    EMPTY_EMAIL,
    INVALID_EMAIL,
    EMPTY_PASSWORD,
    PASSWORD_TOO_SHORT
}