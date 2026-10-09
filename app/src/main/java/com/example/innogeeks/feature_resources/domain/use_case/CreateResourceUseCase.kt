package com.example.innogeeks.feature_resources.domain.use_case

import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_resources.domain.ResourcesRepository
import com.example.innogeeks.feature_resources.domain.model.ResourceDraft
import com.example.innogeeks.feature_resources.domain.model.ResourceError
import com.example.innogeeks.feature_resources.domain.model.ResourceFailure
import com.example.innogeeks.feature_resources.domain.model.ResourceItem

// A bad draft is rejected here so it never costs a network round trip.
class CreateResourceUseCase(private val repository: ResourcesRepository) {
    suspend operator fun invoke(draft: ResourceDraft): Result<ResourceItem, ResourceFailure> {
        val clean = draft.trimmed()
        if (!clean.isValid()) return Result.Error(ResourceFailure.Rejected(ResourceError.VALIDATION))
        return repository.createResource(clean)
    }
}
