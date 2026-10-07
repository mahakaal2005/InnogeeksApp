package com.example.innogeeks.feature_resources.data.dto

import kotlinx.serialization.Serializable

// Mirrors one entry of assets/resources.json — the shape a future backend endpoint should return.
@Serializable
data class ResourceDto(
    val id: String,
    val domainId: String,
    val type: String,
    val emoji: String,
    val title: String,
    val description: String,
    val author: String,
    val date: String,
    val level: String,
    val url: String
)
