package com.example.innogeeks.feature_recruitment.domain.model

// GET /test-slot-booking's own model, with ISO 8601 string times like the rest of this feature.
data class TestSlotBooking(
    val testSlotId: String,
    val startTime: String,
    val endTime: String,
    val bookedAt: String
)
