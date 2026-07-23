package com.example.innogeeks.feature_onboarding.presentation.signup

// Every user action on the SignUp screen. Same shape as LoginAction, plus a confirm-password
// field and an OnLoginClick ("already have an account?") link.
sealed interface SignUpAction {

    data class OnNameChange(val name : String) : SignUpAction
    data class OnEmailChange(val email :String) : SignUpAction
    data class OnPasswordChange(val password : String) : SignUpAction
    data class OnConfirmPasswordChange(val confirmPassword : String) : SignUpAction

    data object OnTogglePasswordVisibility : SignUpAction
    data object OnSignUpClick : SignUpAction
    data object OnLoginClick : SignUpAction
}