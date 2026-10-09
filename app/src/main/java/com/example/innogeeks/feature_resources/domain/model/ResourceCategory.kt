package com.example.innogeeks.feature_resources.domain.model

enum class ResourceType {
    LINK,
    PDF,
    VIDEO,
    NOTES,
    GITHUB
}

// Every resource belongs to exactly one domain — the Resources tab is a domain picker
// feeding into a per-domain, type-filterable feed of these.
data class ResourceItem(
    val id: String,
    val domainId: String,
    val type: ResourceType,
    val title: String,
    val description: String,
    val author: String,
    val date: String,
    val level: String,
    val url: String
)
