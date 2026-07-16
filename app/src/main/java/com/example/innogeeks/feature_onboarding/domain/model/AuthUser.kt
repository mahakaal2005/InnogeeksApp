package com.example.innogeeks.feature_onboarding.domain.model

import com.example.innogeeks.core.domain.model.UserRole

// Who the logged-in user is + the status that drives which UI they see.
data class AuthUser(
    val id: String,
    val fullName: String?,   // null until the club-registration form is filled (light signup)
    val email: String,
    val isPaid: Boolean,     // fee gate — kept separate from role (a REGISTERED user can be either)
    val role: UserRole
)