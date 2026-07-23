package com.example.innogeeks.feature_onboarding.presentation.signup

import com.example.innogeeks.core.presentation.UiText

data class SignUpState(
    val name : String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val nameError : UiText?= null,
    val emailError: UiText? = null,
    val passwordError: UiText? = null,
    val confirmPasswordError: UiText? = null
)