package com.example.innogeeks.feature_onboarding.presentation.auth.passwordreset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_onboarding.domain.use_case.SendPasswordResetCodeUseCase
import com.example.innogeeks.feature_onboarding.domain.auth.AuthValidator
import com.example.innogeeks.feature_onboarding.presentation.mapper.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PasswordResetRequestViewModel(
    private val sendPasswordResetCodeUseCase: SendPasswordResetCodeUseCase,
    private val authValidator: AuthValidator
) : ViewModel() {

    private val _state = MutableStateFlow(PasswordResetRequestState())
    val state = _state.asStateFlow()

    private val _events = Channel<PasswordResetRequestEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: PasswordResetRequestAction) {
        when (action) {
            is PasswordResetRequestAction.OnEmailChange -> _state.update {
                it.copy(email = action.email, emailError = null, formError = null)
            }

            is PasswordResetRequestAction.OnSendCodeClick -> submit()

            is PasswordResetRequestAction.OnBackClick -> viewModelScope.launch {
                _events.send(PasswordResetRequestEvent.NavigateBack)
            }
        }
    }

    private fun submit() {
        if (state.value.isSubmitting) return

        val email = state.value.email.trim()
        val validation = authValidator.validateEmail(email)
        if (validation is Result.Error) {
            _state.update { it.copy(emailError = validation.error.toUiText()) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, formError = null) }
            val result = sendPasswordResetCodeUseCase(email)
            _state.update { it.copy(isSubmitting = false) }

            when (result) {
                is Result.Success -> _events.send(
                    PasswordResetRequestEvent.NavigateToVerifyResetCode(email)
                )

                is Result.Error -> _state.update { it.copy(formError = result.error.toUiText()) }
            }
        }
    }
}
