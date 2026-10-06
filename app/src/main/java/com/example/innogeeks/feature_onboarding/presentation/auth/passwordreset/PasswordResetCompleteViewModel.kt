package com.example.innogeeks.feature_onboarding.presentation.auth.passwordreset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.kiet.innogeeks.R
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.feature_onboarding.domain.use_case.SubmitPasswordResetUseCase
import com.example.innogeeks.feature_onboarding.domain.auth.AuthValidator
import com.example.innogeeks.feature_onboarding.presentation.mapper.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PasswordResetCompleteViewModel(
    private val collegeEmail: String,
    private val passwordResetToken: String,
    private val submitPasswordResetUseCase: SubmitPasswordResetUseCase,
    private val authValidator: AuthValidator
) : ViewModel() {

    private val _state = MutableStateFlow(
        PasswordResetCompleteState(
            collegeEmail = collegeEmail,
            passwordResetToken = passwordResetToken
        )
    )
    val state = _state.asStateFlow()

    private val _events = Channel<PasswordResetCompleteEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: PasswordResetCompleteAction) {
        when (action) {
            is PasswordResetCompleteAction.OnPasswordChange -> _state.update {
                it.copy(password = action.password, passwordError = null, formError = null)
            }

            is PasswordResetCompleteAction.OnConfirmPasswordChange -> _state.update {
                it.copy(
                    confirmPassword = action.confirmPassword,
                    confirmPasswordError = null,
                    formError = null
                )
            }

            is PasswordResetCompleteAction.OnTogglePasswordVisibility -> _state.update {
                it.copy(isPasswordVisible = !it.isPasswordVisible)
            }

            is PasswordResetCompleteAction.OnToggleConfirmPasswordVisibility -> _state.update {
                it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible)
            }

            is PasswordResetCompleteAction.OnResetPasswordClick -> submit()
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

        if (password.length > MAX_PASSWORD_LENGTH) {
            _state.update {
                it.copy(passwordError = UiText.StringResource(R.string.error_password_too_long))
            }
            return
        }

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
            val result = submitPasswordResetUseCase(
                collegeEmail = collegeEmail,
                passwordResetToken = passwordResetToken,
                password = password
            )
            _state.update { it.copy(isSubmitting = false) }

            when (result) {
                is Result.Success -> _events.send(PasswordResetCompleteEvent.NavigateToHome)
                is Result.Error -> _state.update { it.copy(formError = result.error.toUiText()) }
            }
        }
    }

    private companion object {
        const val MAX_PASSWORD_LENGTH = 128
    }
}
