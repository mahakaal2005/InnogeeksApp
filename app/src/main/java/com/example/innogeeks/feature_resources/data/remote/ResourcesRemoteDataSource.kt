package com.example.innogeeks.feature_resources.data.remote

import com.example.innogeeks.core.domain.error.ApiFailure
import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_resources.data.dto.DeletedDto
import com.example.innogeeks.feature_resources.data.dto.ResourceBodyDto
import com.example.innogeeks.feature_resources.data.dto.ResourceDto
import com.example.innogeeks.feature_resources.data.dto.ResourceListDto

interface ResourcesRemoteDataSource {
    suspend fun getResources(): Result<ResourceListDto, DataError.Network>

    // ApiFailure.Api carries the server's error.code (RESOURCE_NOT_IN_DOMAIN, RESOURCE_NOT_FOUND, ...).
    suspend fun createResource(body: ResourceBodyDto): Result<ResourceDto, ApiFailure>
    suspend fun updateResource(id: String, body: ResourceBodyDto): Result<ResourceDto, ApiFailure>
    suspend fun deleteResource(id: String): Result<DeletedDto, ApiFailure>
}
