package com.example.innogeeks.feature_resources.data.dto

import kotlinx.serialization.Serializable

// Mirrors one entry of assets/resources.json — the shape the backend endpoints return.
@Serializable
data class ResourceDto(
    val id: String,
    val domainId: String,
    val type: String,
    val title: String,
    val description: String,
    val author: String,
    val date: String,
    val level: String,
    val url: String
)

// GET /resources
@Serializable
data class ResourceListResponseDto(val data: ResourceListDto)

@Serializable
data class ResourceListDto(val resources: List<ResourceDto> = emptyList())

// POST /resources and PATCH /resources/:id body; the app never sends a domain.
@Serializable
data class ResourceBodyDto(
    val type: String,
    val title: String,
    val description: String,
    val author: String,
    val level: String,
    val url: String
)

// DELETE /resources/:id reply
@Serializable
data class DeletedDto(val deleted: Boolean = true)
