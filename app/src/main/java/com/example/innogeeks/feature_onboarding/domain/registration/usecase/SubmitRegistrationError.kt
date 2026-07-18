package com.example.innogeeks.feature_onboarding.domain.registration.usecase

import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.Error
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_onboarding.domain.registration.RegistrationValidationError

// Unified failure type for submitting the club form: it can fail as a Validation error
// (bad field, caught locally) OR a Remote error (backend/network). Sealed => the
// ViewModel `when`s over exactly these two, handling each differently.
sealed interface SubmitRegistrationError : Error{
    data class Validation(val error: RegistrationValidationError) : SubmitRegistrationError

    data class Remote(val error: DataError) : SubmitRegistrationError

}