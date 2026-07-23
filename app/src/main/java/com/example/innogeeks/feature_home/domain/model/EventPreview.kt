package com.example.innogeeks.feature_home.domain.model

data class EventPreview(
    val id: String,
    val title: String,
    val dateDisplay: String, // e.g. "12 Oct 2026, 5:00 PM"
    val location: String,
    val imageUrl: String? = null
)
