package com.example.innogeeks.feature_onboarding.domain.registration

import com.example.innogeeks.core.domain.util.Error

// One label per way the club form can be invalid. WRONG_DOMAIN_COUNT covers "not exactly 4"
// (too few or too many) — the UI will phrase it as "rank exactly 4 domains".
enum class RegistrationValidationError : Error {
    EMPTY_NAME,
    EMPTY_PHONE,
    INVALID_PHONE,
    WRONG_DOMAIN_COUNT
}
