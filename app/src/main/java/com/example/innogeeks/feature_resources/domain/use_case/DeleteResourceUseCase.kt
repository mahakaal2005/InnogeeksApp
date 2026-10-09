package com.example.innogeeks.feature_resources.domain.use_case

import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_resources.domain.ResourcesRepository
import com.example.innogeeks.feature_resources.domain.model.ResourceFailure

class DeleteResourceUseCase(private val repository: ResourcesRepository) {
    suspend operator fun invoke(id: String): Result<Unit, ResourceFailure> = repository.deleteResource(id)
}
