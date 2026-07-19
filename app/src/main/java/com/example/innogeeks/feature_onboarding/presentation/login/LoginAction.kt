package com.example.innogeeks.feature_onboarding.presentation.login

// Every action a user can take on the Login screen. The screen sends these UP to the
// ViewModel; it never acts on its own. sealed => the ViewModel's when(action) is exhaustive.
sealed interface LoginAction {
    // data class = carries data (the newly typed text)
    data class OnEmailChange(val email : String) : LoginAction
    data class OnPasswordChange(val password : String): LoginAction

    // data object = pure signal, no data — just "this happened"
    data object OnLoginClick : LoginAction
    data object OnTogglePasswordVisibility : LoginAction
    data object OnSignUpClick : LoginAction
}