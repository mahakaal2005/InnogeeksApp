package com.example.innogeeks.feature_onboarding.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innogeeks.core.domain.util.onFailure
import com.example.innogeeks.core.domain.util.onSuccess
import com.example.innogeeks.feature_onboarding.domain.auth.AuthValidationError
import com.example.innogeeks.feature_onboarding.domain.auth.usecase.LoginError
import com.example.innogeeks.feature_onboarding.domain.auth.usecase.LoginUseCase
import com.example.innogeeks.feature_onboarding.presentation.mapper.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// The "kitchen": owns all screen state, handles every action, emits one-shot events.
// Extends ViewModel so it SURVIVES config changes (rotation) — state isn't lost.
class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    // _state is the private, writable truth (only the ViewModel mutates it).
    // state is the read-only view the screen observes. StateFlow holds + replays its
    // current value, so the screen always redraws from the latest LoginState.
    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    // Events use a Channel, NOT a StateFlow: a Channel delivers each item exactly ONCE.
    // A navigation event must fire once and never replay (a StateFlow would re-serve its
    // last value on every re-subscribe -> endless re-navigation).
    private val _events = Channel<LoginEvent>()
    val events = _events.receiveAsFlow()

    // The single front door: the screen calls ONLY this. when() over a sealed action ->
    // exhaustive, so a forgotten action won't compile.
    fun onAction(action: LoginAction) {
        when (action) {
            // update = read current state, return a modified copy (never mutate in place).
            // Clear the field's error as they type: an old error describes the OLD text, so
            // it's stale the moment the text changes — clearing keeps errors honest + un-naggy.
            is LoginAction.OnEmailChange -> {
                _state.update { it.copy(email = action.email, emailError = null) }
            }

            is LoginAction.OnPasswordChange -> {
                _state.update { it.copy(password = action.password, passwordError = null) }
            }
            // Flip the boolean; the ViewModel owns this state change, not the screen.
            is LoginAction.OnTogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }

            is LoginAction.OnLoginClick -> login()
            is LoginAction.OnSignUpClick -> {
                viewModelScope.launch {
                    _events.send(LoginEvent.NavigateToSignUp)
                }
            }
        }
    }

    // Runs in viewModelScope because loginUseCase suspends (fake repo has delay(1000)).
    // The coroutine auto-cancels if the ViewModel dies -> no leaks.
    private fun login() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, emailError = null, passwordError = null) }


            val result = loginUseCase(state.value.email, state.value.password)

            // Spinner off once the call returns, regardless of outcome — one place, not two.
            _state.update { it.copy(isLoading = false) }

            result.onSuccess {
                // `it` here is the AuthUser (unused for now — we only need to leave the screen).
                _events.send(LoginEvent.NavigateToHome)
            }.onFailure { loginError ->
                when (loginError) {
                    // Route each validation error to the RIGHT field (email vs password),
                    // since AuthValidationError alone doesn't say which field it's about.
                    is LoginError.Validation -> {
                        val message = loginError.error.toUiText()
                        when (loginError.error) {
                            AuthValidationError.EMPTY_EMAIL,
                            AuthValidationError.INVALID_EMAIL ->
                                _state.update { it.copy(emailError = message) }

                            AuthValidationError.EMPTY_PASSWORD,
                            AuthValidationError.PASSWORD_TOO_SHORT ->
                                _state.update { it.copy(passwordError = message) }
                        }
                    }

                    is LoginError.Remote -> Unit  // TODO: one-shot snackbar (deferred, see spec §7)
                }
            }
        }
    }
}