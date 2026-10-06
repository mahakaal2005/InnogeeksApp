package com.example.innogeeks.feature_onboarding.presentation.auth.emailgate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.kiet.innogeeks.R
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.feature_onboarding.domain.use_case.GetNextAuthStepUseCase
import com.example.innogeeks.feature_onboarding.domain.auth.AuthValidator
import com.example.innogeeks.feature_onboarding.domain.auth.NextStep
import com.example.innogeeks.feature_onboarding.presentation.mapper.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EmailGateViewModel(
    private val getNextAuthStepUseCase: GetNextAuthStepUseCase,
    private val authValidator: AuthValidator
) : ViewModel() {

    private val _state = MutableStateFlow(EmailGateState())
    val state = _state.asStateFlow()

    private val _events = Channel<EmailGateEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: EmailGateAction) {
        when (action) {
            is EmailGateAction.OnEmailChange -> _state.update {
                it.copy(email = action.email, emailError = null, formError = null)
            }

            is EmailGateAction.OnContinueClick -> submit()

            is EmailGateAction.OnBackClick -> viewModelScope.launch {
                _events.send(EmailGateEvent.NavigateBack)
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
            val result = getNextAuthStepUseCase(email)
            _state.update { it.copy(isSubmitting = false) }

            when (result) {
                is Result.Success -> when (result.data) {
                    NextStep.PASSWORD_SETUP ->
                        _events.send(EmailGateEvent.NavigateToVerifyCode(email))

                    NextStep.PASSWORD_LOGIN ->
                        _events.send(EmailGateEvent.NavigateToPasswordLogin(email))

                    // §9: never coerce an unknown step into a known one.
                    NextStep.UNSUPPORTED -> _state.update {
                        it.copy(formError = UiText.StringResource(R.string.error_unsupported_version))
                    }
                }

                is Result.Error -> _state.update { it.copy(formError = result.error.toUiText()) }
            }
        }
    }
}
