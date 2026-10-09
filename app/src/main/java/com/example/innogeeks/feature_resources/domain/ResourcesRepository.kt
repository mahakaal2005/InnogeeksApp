package com.example.innogeeks.feature_resources.domain

import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_resources.domain.model.ResourceDraft
import com.example.innogeeks.feature_resources.domain.model.ResourceFailure
import com.example.innogeeks.feature_resources.domain.model.ResourceItem

interface ResourcesRepository {
    suspend fun getResources(): Result<List<ResourceItem>, DataError.Network>

    // Coordinator/Admin only, and only for their own domain; the server derives the domain.
    suspend fun createResource(draft: ResourceDraft): Result<ResourceItem, ResourceFailure>
    suspend fun updateResource(id: String, draft: ResourceDraft): Result<ResourceItem, ResourceFailure>
    suspend fun deleteResource(id: String): Result<Unit, ResourceFailure>
}
