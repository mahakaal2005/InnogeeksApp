package com.example.innogeeks.feature_onboarding.domain.model

data class RegistrationForm (
    val fullName: String,
    val email: String,
    val phone: String,
    val yearOrBatch: String,
    val domainsOfInterest: List<String>,
    val whyJoin: String
)