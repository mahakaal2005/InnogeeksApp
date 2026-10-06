package com.example.innogeeks.feature_onboarding.presentation.auth.verifycode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.feature_onboarding.domain.auth.AuthApiError
import com.example.innogeeks.feature_onboarding.domain.auth.AuthError
import com.example.innogeeks.feature_onboarding.domain.use_case.SendVerificationCodeUseCase
import com.example.innogeeks.feature_onboarding.domain.use_case.SubmitVerificationCodeUseCase
import com.example.innogeeks.feature_onboarding.presentation.mapper.toUiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import edu.kiet.innogeeks.R

class VerifyCodeViewModel(
    private val collegeEmail: String,
    private val sendVerificationCodeUseCase: SendVerificationCodeUseCase,
    private val submitVerificationCodeUseCase: SubmitVerificationCodeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(VerifyCodeState(collegeEmail = collegeEmail))
    val state = _state.asStateFlow()

    private val _events = Channel<VerifyCodeEvent>()
    val events = _events.receiveAsFlow()

    private var countdownJob: Job? = null

    // The gate sends the user here without having asked for a code yet, so ask on arrival.
    init {
        requestCode()
    }

    fun onAction(action: VerifyCodeAction) {
        when (action) {
            is VerifyCodeAction.OnCodeChange -> {
                // Digits only, capped at six — the boxes render one character each.
                val digits = action.code.filter { it.isDigit() }.take(VerifyCodeState.CODE_LENGTH)
                _state.update { it.copy(code = digits, codeError = null, formError = null) }
            }

            is VerifyCodeAction.OnVerifyClick -> verify()
            is VerifyCodeAction.OnResendClick -> requestCode()
            is VerifyCodeAction.OnBackClick -> viewModelScope.launch {
                _events.send(VerifyCodeEvent.NavigateBack)
            }
        }
    }

    private fun requestCode() {
        if (state.value.isRequestingCode || state.value.resendSecondsLeft > 0) return

        viewModelScope.launch {
            _state.update { it.copy(isRequestingCode = true, formError = null, codeError = null) }
            val result = sendVerificationCodeUseCase(collegeEmail)
            _state.update { it.copy(isRequestingCode = false) }

            when (result) {
                // Countdown starts only on success — §5 says only after a 202.
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
        if (!state.value.isCodeComplete) {
            _state.update { it.copy(codeError = enterFullCodeText()) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, formError = null) }
            val result = submitVerificationCodeUseCase(collegeEmail, state.value.code)
            _state.update { it.copy(isSubmitting = false) }

            when (result) {
                is Result.Success -> _events.send(
                    VerifyCodeEvent.NavigateToSetPassword(
                        collegeEmail = collegeEmail,
                        passwordSetupToken = result.data
                    )
                )

                is Result.Error -> handleError(result.error)
            }
        }
    }

    private suspend fun handleError(error: AuthError) {
        val apiCode = (error as? AuthError.Api)?.code
        when (apiCode) {
            // Not a first login after all — the contract says route to the password screen.
            AuthApiError.PASSWORD_ALREADY_SET ->
                _events.send(VerifyCodeEvent.NavigateToPasswordLogin(collegeEmail))

            // A cooldown means the server still has a live timer, so keep resend disabled.
            AuthApiError.VERIFICATION_CODE_COOLDOWN -> {
                if (state.value.resendSecondsLeft == 0) startCountdown()
                _state.update { it.copy(formError = error.toUiText()) }
            }

            AuthApiError.VERIFICATION_CODE_INVALID ->
                _state.update { it.copy(codeError = error.toUiText(), code = "") }

            else -> _state.update { it.copy(formError = error.toUiText()) }
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            _state.update { it.copy(resendSecondsLeft = RESEND_SECONDS) }
            while (state.value.resendSecondsLeft > 0) {
                delay(1000)
                _state.update { it.copy(resendSecondsLeft = it.resendSecondsLeft - 1) }
            }
        }
    }

    private fun enterFullCodeText() =
        UiText.StringResource(R.string.auth_enter_all_digits, arrayOf(VerifyCodeState.CODE_LENGTH))

    private companion object {
        const val RESEND_SECONDS = 60
    }
}
