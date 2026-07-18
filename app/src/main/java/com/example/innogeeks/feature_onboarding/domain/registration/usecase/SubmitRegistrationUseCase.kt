package com.example.innogeeks.feature_onboarding.domain.registration.usecase

import com.example.innogeeks.core.domain.util.EmptyResult
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.core.domain.util.mapError
import com.example.innogeeks.core.domain.util.onFailure
import com.example.innogeeks.feature_onboarding.domain.model.RegistrationForm
import com.example.innogeeks.feature_onboarding.domain.registration.RegistrationRepository
import com.example.innogeeks.feature_onboarding.domain.registration.RegistrationValidator

// "Submit the club form": validate first (bail early with Validation), then send to the
// backend, converting its DataError into the unified SubmitRegistrationError via mapError.
// Same shape as SignUpUseCase — two failure kinds => wrapper error type.
class SubmitRegistrationUseCase(
    private val registrationValidator: RegistrationValidator,
    private val registrationRepository: RegistrationRepository
) {

    suspend operator fun invoke(
        form: RegistrationForm
    ) : EmptyResult<SubmitRegistrationError>{

        registrationValidator.validateForm(form)
            .onFailure { return Result.Error(SubmitRegistrationError.Validation(it)) }

        return registrationRepository.submitRegistration(form)
            .mapError { SubmitRegistrationError.Remote(it) }
    }
}