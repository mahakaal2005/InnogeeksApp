package com.example.innogeeks.feature_onboarding.presentation.auth.passwordlogin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.kiet.innogeeks.R
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.feature_onboarding.domain.auth.AuthApiError
import com.example.innogeeks.feature_onboarding.domain.auth.AuthError
import com.example.innogeeks.feature_onboarding.domain.use_case.SubmitLoginUseCase
import com.example.innogeeks.feature_onboarding.presentation.mapper.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PasswordLoginViewModel(
    private val collegeEmail: String,
    private val submitLoginUseCase: SubmitLoginUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PasswordLoginState(collegeEmail = collegeEmail))
    val state = _state.asStateFlow()

    private val _events = Channel<PasswordLoginEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: PasswordLoginAction) {
        when (action) {
            is PasswordLoginAction.OnPasswordChange -> _state.update {
                it.copy(password = action.password, passwordError = null, formError = null)
            }

            is PasswordLoginAction.OnTogglePasswordVisibility -> _state.update {
                it.copy(isPasswordVisible = !it.isPasswordVisible)
            }

            is PasswordLoginAction.OnLoginClick -> login()

            is PasswordLoginAction.OnForgotPasswordClick -> viewModelScope.launch {
                _events.send(PasswordLoginEvent.NavigateToPasswordReset)
            }

            is PasswordLoginAction.OnBackClick -> viewModelScope.launch {
                _events.send(PasswordLoginEvent.NavigateBack)
            }
        }
    }

    private fun login() {
        if (state.value.isSubmitting) return
        // No length check here: an existing password may predate any client-side rule.
        if (state.value.password.isBlank()) {
            _state.update { it.copy(passwordError = emptyPasswordText()) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, formError = null) }
            val result = submitLoginUseCase(collegeEmail, state.value.password)
            _state.update { it.copy(isSubmitting = false) }

            when (result) {
                is Result.Success -> _events.send(PasswordLoginEvent.NavigateToHome)
                is Result.Error -> handleError(result.error)
            }
        }
    }

    private fun handleError(error: AuthError) {
        // Bad credentials belong on the password field; everything else is form-level.
        if ((error as? AuthError.Api)?.code == AuthApiError.INVALID_CREDENTIALS) {
            _state.update { it.copy(passwordError = error.toUiText(), password = "") }
            return
        }
        _state.update { it.copy(formError = error.toUiText()) }
    }

    private fun emptyPasswordText() = UiText.StringResource(R.string.error_empty_password)
}
