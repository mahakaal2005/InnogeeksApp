package com.example.innogeeks.feature_resources.data.remote

import com.example.innogeeks.core.data.networking.deleteEnveloped
import com.example.innogeeks.core.data.networking.get
import com.example.innogeeks.core.data.networking.patchEnveloped
import com.example.innogeeks.core.data.networking.postEnveloped
import com.example.innogeeks.core.domain.error.ApiFailure
import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.core.domain.util.mapData
import com.example.innogeeks.feature_resources.data.dto.DeletedDto
import com.example.innogeeks.feature_resources.data.dto.ResourceBodyDto
import com.example.innogeeks.feature_resources.data.dto.ResourceDto
import com.example.innogeeks.feature_resources.data.dto.ResourceListDto
import com.example.innogeeks.feature_resources.data.dto.ResourceListResponseDto
import io.ktor.client.HttpClient

// Not bound yet: the resources endpoints don't exist on the backend (docs/BACKEND_NEEDS_2026-10-09_resources.md).
class KtorResourcesRemoteDataSource(
    private val httpClient: HttpClient
) : ResourcesRemoteDataSource {

    override suspend fun getResources(): Result<ResourceListDto, DataError.Network> =
        httpClient.get<ResourceListResponseDto>(route = "/api/v1/app/resources").mapData { it.data }

    override suspend fun createResource(body: ResourceBodyDto): Result<ResourceDto, ApiFailure> =
        httpClient.postEnveloped<ResourceBodyDto, ResourceDto>(route = "/api/v1/app/resources", body = body)

    override suspend fun updateResource(id: String, body: ResourceBodyDto): Result<ResourceDto, ApiFailure> =
        httpClient.patchEnveloped<ResourceBodyDto, ResourceDto>(route = "/api/v1/app/resources/$id", body = body)

    override suspend fun deleteResource(id: String): Result<DeletedDto, ApiFailure> =
        httpClient.deleteEnveloped<DeletedDto>(route = "/api/v1/app/resources/$id")
}
