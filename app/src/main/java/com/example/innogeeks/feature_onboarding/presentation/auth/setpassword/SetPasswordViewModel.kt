package com.example.innogeeks.feature_onboarding.presentation.auth.setpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.kiet.innogeeks.R
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.feature_onboarding.domain.auth.AuthApiError
import com.example.innogeeks.feature_onboarding.domain.auth.AuthError
import com.example.innogeeks.feature_onboarding.domain.use_case.SubmitPasswordSetupUseCase
import com.example.innogeeks.feature_onboarding.domain.auth.AuthValidator
import com.example.innogeeks.feature_onboarding.presentation.mapper.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SetPasswordViewModel(
    private val collegeEmail: String,
    private val passwordSetupToken: String,
    private val submitPasswordSetupUseCase: SubmitPasswordSetupUseCase,
    private val authValidator: AuthValidator
) : ViewModel() {

    private val _state = MutableStateFlow(SetPasswordState(collegeEmail = collegeEmail))
    val state = _state.asStateFlow()

    private val _events = Channel<SetPasswordEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: SetPasswordAction) {
        when (action) {
            is SetPasswordAction.OnPasswordChange -> _state.update {
                it.copy(password = action.password, passwordError = null, formError = null)
            }

            is SetPasswordAction.OnConfirmPasswordChange -> _state.update {
                it.copy(
                    confirmPassword = action.confirmPassword,
                    confirmPasswordError = null,
                    formError = null
                )
            }

            is SetPasswordAction.OnTogglePasswordVisibility -> _state.update {
                it.copy(isPasswordVisible = !it.isPasswordVisible)
            }

            is SetPasswordAction.OnSubmitClick -> submit()
        }
    }

    private fun submit() {
        if (state.value.isSubmitting) return

        val password = state.value.password
        val validation = authValidator.validatePassword(password)
        if (validation is Result.Error) {
            _state.update { it.copy(passwordError = validation.error.toUiText()) }
            return
        }
        // The contract caps passwords at 128, which AuthValidator doesn't check.
        if (password.length > MAX_PASSWORD_LENGTH) {
            _state.update {
                it.copy(passwordError = UiText.StringResource(R.string.error_password_too_long))
            }
            return
        }
        // Confirm-match is UI-only; the backend never sees this field.
        if (password != state.value.confirmPassword) {
            _state.update {
                it.copy(
                    confirmPasswordError =
                        UiText.StringResource(R.string.error_passwords_do_not_match)
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, formError = null) }
            val result = submitPasswordSetupUseCase(
                collegeEmail = collegeEmail,
                passwordSetupToken = passwordSetupToken,
                password = password
            )
            _state.update { it.copy(isSubmitting = false) }

            when (result) {
                is Result.Success -> _events.send(SetPasswordEvent.NavigateToHome)
                is Result.Error -> handleError(result.error)
            }
        }
    }

    private suspend fun handleError(error: AuthError) {
        // A dead token can't be retried on this screen, so send the user back to the start.
        if ((error as? AuthError.Api)?.code == AuthApiError.PASSWORD_SETUP_TOKEN_INVALID) {
            _events.send(SetPasswordEvent.RestartVerification)
            return
        }
        _state.update { it.copy(formError = error.toUiText()) }
    }

    private companion object {
        const val MAX_PASSWORD_LENGTH = 128
    }
}
