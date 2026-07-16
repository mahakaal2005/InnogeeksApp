package com.example.innogeeks.feature_onboarding.domain.model

data class ClubEvent(
    val id: String,
    val title: String,
    val description: String,
    val dateEpochMillis: Long,
    val venue: String
)