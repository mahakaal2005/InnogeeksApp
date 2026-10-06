package com.example.innogeeks.feature_onboarding.presentation.auth.passwordreset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.feature_onboarding.domain.auth.AuthApiError
import com.example.innogeeks.feature_onboarding.domain.auth.AuthError
import com.example.innogeeks.feature_onboarding.domain.use_case.SendPasswordResetCodeUseCase
import com.example.innogeeks.feature_onboarding.domain.use_case.SubmitPasswordResetCodeUseCase
import com.example.innogeeks.feature_onboarding.presentation.mapper.toUiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PasswordResetVerifyCodeViewModel(
    private val collegeEmail: String,
    private val sendPasswordResetCodeUseCase: SendPasswordResetCodeUseCase,
    private val submitPasswordResetCodeUseCase: SubmitPasswordResetCodeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PasswordResetVerifyCodeState(collegeEmail = collegeEmail))
    val state = _state.asStateFlow()

    private val _events = Channel<PasswordResetVerifyCodeEvent>()
    val events = _events.receiveAsFlow()

    private var countdownJob: Job? = null

    fun onAction(action: PasswordResetVerifyCodeAction) {
        when (action) {
            is PasswordResetVerifyCodeAction.OnCodeChange -> {
                val digits = action.code.filter { it.isDigit() }.take(CODE_LENGTH)
                _state.update { it.copy(code = digits, codeError = null, formError = null) }
            }

            is PasswordResetVerifyCodeAction.OnVerifyClick -> verify()
            is PasswordResetVerifyCodeAction.OnResendClick -> requestCode()
            is PasswordResetVerifyCodeAction.OnBackClick -> viewModelScope.launch {
                _events.send(PasswordResetVerifyCodeEvent.NavigateBack)
            }
        }
    }

    private fun requestCode() {
        if (!state.value.canResend || state.value.resendCountdown > 0) return

        viewModelScope.launch {
            _state.update { it.copy(canResend = false, formError = null, codeError = null) }
            val result = sendPasswordResetCodeUseCase(collegeEmail)
            _state.update { it.copy(canResend = true) }

            when (result) {
                is Result.Success -> {
                    _state.update { it.copy(code = "") }
                    startCountdown()
                }

                is Result.Error -> handleError(result.error)
            }
        }
    }

    private fun verify() {
        if (state.value.isSubmitting) return
        if (state.value.code.length < CODE_LENGTH) {
            _state.update { it.copy(codeError = enterFullCodeText()) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, formError = null) }
            val result = submitPasswordResetCodeUseCase(collegeEmail, state.value.code)
            _state.update { it.copy(isSubmitting = false) }

            when (result) {
                is Result.Success -> _events.send(
                    PasswordResetVerifyCodeEvent.NavigateToCompleteReset(
                        collegeEmail = collegeEmail,
                        passwordResetToken = result.data
                    )
                )

                is Result.Error -> handleError(result.error)
            }
        }
    }

    private suspend fun handleError(error: AuthError) {
        val apiCode = (error as? AuthError.Api)?.code
        when (apiCode) {
            AuthApiError.PASSWORD_RESET_COOLDOWN -> {
                if (state.value.resendCountdown == 0) startCountdown()
                _state.update { it.copy(formError = error.toUiText()) }
            }

            AuthApiError.PASSWORD_RESET_CODE_INVALID ->
                _state.update { it.copy(codeError = error.toUiText(), code = "") }

            else -> _state.update { it.copy(formError = error.toUiText()) }
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            _state.update { it.copy(resendCountdown = RESEND_SECONDS) }
            while (state.value.resendCountdown > 0) {
                delay(1000)
                _state.update { it.copy(resendCountdown = it.resendCountdown - 1) }
            }
        }
    }

    private fun enterFullCodeText() = UiText.DynamicString("Enter all $CODE_LENGTH digits.")

    private companion object {
        const val CODE_LENGTH = 6
        const val RESEND_SECONDS = 60
    }
}
