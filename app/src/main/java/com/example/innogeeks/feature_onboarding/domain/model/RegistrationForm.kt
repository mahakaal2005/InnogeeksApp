package com.example.innogeeks.feature_onboarding.domain.model

// The club-registration form, filled in-app by an already-logged-in user. Email/year
// removed: email comes from the account (JWT source of truth); only first-years can
// register (backend enforces the batch). domainPreferences is ORDERED — index 0 = top
// priority — and must contain exactly 4 (see RegistrationValidator).
data class RegistrationForm (
    val fullName: String,
    val phone: String,
    val domainPreferences: List<String>
)