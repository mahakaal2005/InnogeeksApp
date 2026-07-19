package com.example.innogeeks.feature_onboarding.presentation.signup

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innogeeks.core.domain.util.onFailure
import com.example.innogeeks.core.domain.util.onSuccess
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.feature_onboarding.domain.auth.AuthValidationError
import com.example.innogeeks.feature_onboarding.domain.auth.usecase.SignUpError
import com.example.innogeeks.feature_onboarding.domain.auth.usecase.SignUpUseCase
import com.example.innogeeks.feature_onboarding.presentation.mapper.toUiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SignUpState())
    val state = _state.asStateFlow()

    private val _events = Channel<SignUpEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: SignUpAction) {
        when (action) {
            is SignUpAction.OnEmailChange -> {
                _state.update { it.copy(email = action.email, emailError = null) }
            }
            is SignUpAction.OnPasswordChange -> {
                _state.update { it.copy(password = action.password, passwordError = null) }
            }
            is SignUpAction.OnConfirmPasswordChange -> {
                _state.update { it.copy(confirmPassword = action.confirmPassword, confirmPasswordError = null) }
            }
            is SignUpAction.OnTogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            is SignUpAction.OnSignUpClick -> signUp()
            is SignUpAction.OnLoginClick -> {
                viewModelScope.launch {
                    _events.send(SignUpEvent.NavigateToLoginScreen)
                }
            }
        }
    }

    private fun signUp() {
        // UI-ONLY guard: confirm must equal password. The domain never sees confirmPassword
        // (backend only gets one password), so this check lives here, not in AuthValidator.
        // Bail early — no coroutine, no spinner, no network for a mismatch we know locally.
        if (state.value.password != state.value.confirmPassword) {
            _state.update {
                it.copy(confirmPasswordError = UiText.DynamicString("Passwords do not match."))
            }
            return
        }
        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true, emailError = null, passwordError = null, confirmPasswordError = null)
            }

            val result = signUpUseCase(state.value.email, state.value.password)

            _state.update { it.copy(isLoading = false) }

            result
                .onSuccess {
                    _events.send(SignUpEvent.NavigateToLoginScreen)
                }
                .onFailure { signUpError ->
                    when (signUpError) {
                        is SignUpError.Validation -> {
                            val message = signUpError.error.toUiText()
                            when (signUpError.error) {
                                AuthValidationError.EMPTY_EMAIL,
                                AuthValidationError.INVALID_EMAIL ->
                                    _state.update { it.copy(emailError = message) }

                                AuthValidationError.EMPTY_PASSWORD,
                                AuthValidationError.PASSWORD_TOO_SHORT ->
                                    _state.update { it.copy(passwordError = message) }
                            }
                        }
                        is SignUpError.Remote -> Unit  // TODO snackbar (deferred, same as Login)
                    }
                }
        }
    }
}