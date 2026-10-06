package com.example.innogeeks.feature_onboarding.presentation.mapper

import edu.kiet.innogeeks.R
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.feature_onboarding.domain.auth.AuthValidationError

// Maps an onboarding validation error to display text; it stays in this feature because the error is onboarding-only.
fun AuthValidationError.toUiText() : UiText{
    return when(this){
        AuthValidationError.EMPTY_NAME -> UiText.StringResource(R.string.error_empty_name)
        AuthValidationError.EMPTY_EMAIL -> UiText.StringResource(R.string.error_empty_email)
        AuthValidationError.INVALID_EMAIL -> UiText.StringResource(R.string.error_invalid_email)
        AuthValidationError.EMPTY_PASSWORD -> UiText.StringResource(R.string.error_empty_password)
        AuthValidationError.PASSWORD_TOO_SHORT -> UiText.StringResource(R.string.error_password_too_short)
    }
}