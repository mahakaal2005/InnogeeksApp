package com.example.innogeeks.feature_onboarding.domain.registration

import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.EmptyResult
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_onboarding.domain.model.AuthUser
import com.example.innogeeks.feature_onboarding.domain.model.RegistrationForm

// Contract for the club-registration concern (distinct from auth/identity). Two separate
// steps because they happen at different times: submit the form now, redeem the payment
// code later (maybe days later, from the email). Gateway auto-verify deferred — see
// docs/11_REVISED_ONBOARDING_FLOW_2026-07-17.md "Payment: two paths".
interface RegistrationRepository {

    // Send the club form. EmptyResult — user still has to pay, so nothing to hand back yet.
    suspend fun submitRegistration(form : RegistrationForm) : EmptyResult<DataError>

    // Redeem the emailed code → backend flips user to paid and returns the updated AuthUser
    // (isPaid = true) so the UI can immediately swap to the paid experience.
    suspend fun redeemPaymentCode(code : String ) : Result<AuthUser, DataError>
}