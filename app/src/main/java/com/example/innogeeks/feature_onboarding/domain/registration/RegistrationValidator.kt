package com.example.innogeeks.feature_onboarding.domain.registration

import com.example.innogeeks.core.domain.util.EmptyResult
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_onboarding.domain.model.RegistrationForm

// Validates the whole form as one unit, returning the FIRST problem it finds (Result holds
// a single error). Order matters: check blank BEFORE format (a blank phone should report
// EMPTY_PHONE, not INVALID_PHONE). Distinctness/validity of the 4 domains is enforced by the
// UI picker, not here — a ranked picker won't let you choose the same domain twice.
class RegistrationValidator {

    fun validateForm(form: RegistrationForm): EmptyResult<RegistrationValidationError> {

        if (form.fullName.isBlank()) return Result.Error(RegistrationValidationError.EMPTY_NAME)

        if (form.phone.isBlank()) return Result.Error(RegistrationValidationError.EMPTY_PHONE)

        if (form.phone.length != PHONE_LENGTH) return Result.Error(RegistrationValidationError.INVALID_PHONE)

        if (form.domainPreferences.size != REQUIRED_DOMAIN_COUNT) {
            return Result.Error(RegistrationValidationError.WRONG_DOMAIN_COUNT)
        }

        return Result.Success(Unit)
    }

    companion object {
        private const val PHONE_LENGTH = 10
        private const val REQUIRED_DOMAIN_COUNT = 4
    }
}
