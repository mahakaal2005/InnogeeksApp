package com.example.innogeeks.feature_onboarding.domain.registration.usecase

import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.core.domain.util.mapError
import com.example.innogeeks.feature_onboarding.domain.model.AuthUser
import com.example.innogeeks.feature_onboarding.domain.registration.RegistrationRepository
import com.example.innogeeks.feature_onboarding.domain.registration.RegistrationValidationError

// "Redeem the emailed payment code". No validation (the backend judges the code) => only
// ONE failure kind (remote), so no wrapper error and no mapError — the repository's
// Result<AuthUser, DataError> passes straight through. Kept as a use case (not a direct
// repo call from the ViewModel) for uniformity + a home for future logic (e.g. trimming).
class RedeemCodeUseCase(
    private val registrationRepository: RegistrationRepository
) {

    suspend operator fun invoke(
        code : String
    ) : Result<AuthUser, DataError>{

        return registrationRepository.redeemPaymentCode(code)
    }
}