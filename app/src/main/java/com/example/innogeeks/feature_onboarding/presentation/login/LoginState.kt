package com.example.innogeeks.feature_onboarding.presentation.login

import com.example.innogeeks.core.presentation.UiText

// The single source of truth for everything the Login screen shows. The screen is a pure
// mirror of this object — only the ViewModel ever writes to it. Every field has a default,
// so LoginState() alone is a valid empty starting screen.
data class LoginState (
    val email : String = "",
    val password : String = "",
    val isLoading : Boolean = false,        // request in flight -> spinner + disabled button
    val isPasswordVisible : Boolean = false,// eye-toggle; false = masked. ViewModel flips it, not the screen
    val emailError : UiText? = null,         // null = no error. UiText (not String) so it can localize
    val passwordError : UiText? = null
)