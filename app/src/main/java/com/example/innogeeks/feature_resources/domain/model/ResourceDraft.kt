package com.example.innogeeks.feature_resources.domain.model

// What a coordinator types; the server fills in id, domain and date.
data class ResourceDraft(
    val type: ResourceType,
    val title: String,
    val description: String,
    val author: String,
    val level: String,
    val url: String
)

val resourceLevels = listOf("Beginner", "Intermediate", "Advanced")
