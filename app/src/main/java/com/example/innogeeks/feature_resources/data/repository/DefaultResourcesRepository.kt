package com.example.innogeeks.feature_resources.data.repository

import com.example.innogeeks.core.domain.error.ApiFailure
import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.core.domain.util.asEmptyResult
import com.example.innogeeks.core.domain.util.mapData
import com.example.innogeeks.core.domain.util.mapError
import com.example.innogeeks.feature_resources.data.dto.ResourceDto
import com.example.innogeeks.feature_resources.data.mapper.toBodyDto
import com.example.innogeeks.feature_resources.data.mapper.toResourceItem
import com.example.innogeeks.feature_resources.data.remote.ResourcesRemoteDataSource
import com.example.innogeeks.feature_resources.domain.ResourcesRepository
import com.example.innogeeks.feature_resources.domain.model.ResourceDraft
import com.example.innogeeks.feature_resources.domain.model.ResourceError
import com.example.innogeeks.feature_resources.domain.model.ResourceFailure
import com.example.innogeeks.feature_resources.domain.model.ResourceItem

class DefaultResourcesRepository(
    private val remoteDataSource: ResourcesRemoteDataSource
) : ResourcesRepository {

    override suspend fun getResources(): Result<List<ResourceItem>, DataError.Network> =
        remoteDataSource.getResources().mapData { list -> list.resources.mapNotNull { it.toResourceItem() } }

    override suspend fun createResource(draft: ResourceDraft): Result<ResourceItem, ResourceFailure> =
        remoteDataSource.createResource(draft.toBodyDto()).mapError { it.toFailure() }.toItemResult()

    override suspend fun updateResource(id: String, draft: ResourceDraft): Result<ResourceItem, ResourceFailure> =
        remoteDataSource.updateResource(id, draft.toBodyDto()).mapError { it.toFailure() }.toItemResult()

    override suspend fun deleteResource(id: String): Result<Unit, ResourceFailure> =
        remoteDataSource.deleteResource(id).mapError { it.toFailure() }.asEmptyResult()

    private fun ApiFailure.toFailure(): ResourceFailure = when (this) {
        is ApiFailure.Api -> ResourceFailure.Rejected(ResourceError.fromCode(code))
        is ApiFailure.Transport -> ResourceFailure.Transport(error)
    }

    // A reply the mapper rejects is a parse problem, not a business error.
    private fun Result<ResourceDto, ResourceFailure>.toItemResult(): Result<ResourceItem, ResourceFailure> = when (this) {
        is Result.Success -> data.toResourceItem()?.let { Result.Success(it) }
            ?: Result.Error(ResourceFailure.Transport(DataError.Network.SERIALIZATION))
        is Result.Error -> Result.Error(error)
    }
}
