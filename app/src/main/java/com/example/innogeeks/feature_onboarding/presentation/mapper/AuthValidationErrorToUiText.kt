package com.example.innogeeks.feature_onboarding.presentation.mapper

import com.example.innogeeks.R
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.feature_onboarding.domain.auth.AuthValidationError

// Dresses an onboarding validation error for display (R.string). Lives in feature
// presentation, not core: AuthValidationError is onboarding-only, so its mapper stays here.
// enum => match by VALUE (no `is`), and all 4 cases are covered so no `else` needed.
fun AuthValidationError.toUiText() : UiText{
    return when(this){
        AuthValidationError.EMPTY_NAME -> UiText.StringResource(R.string.error_empty_name)
        AuthValidationError.EMPTY_EMAIL -> UiText.StringResource(R.string.error_empty_email)
        AuthValidationError.INVALID_EMAIL -> UiText.StringResource(R.string.error_invalid_email)
        AuthValidationError.EMPTY_PASSWORD -> UiText.StringResource(R.string.error_empty_password)
        AuthValidationError.PASSWORD_TOO_SHORT -> UiText.StringResource(R.string.error_password_too_short)
    }
}