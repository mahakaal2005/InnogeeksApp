package com.example.innogeeks.feature_resources.data

import com.example.innogeeks.core.domain.resources.ResourceKind
import com.example.innogeeks.core.domain.resources.ResourceShortcut
import com.example.innogeeks.core.domain.resources.ResourceShortcutProvider
import com.example.innogeeks.core.domain.resources.ResourceShortcuts
import com.example.innogeeks.feature_resources.domain.ResourcesRepository
import com.example.innogeeks.feature_resources.domain.model.ResourceType

class AssetResourceShortcutProvider(
    private val repository: ResourcesRepository
) : ResourceShortcutProvider {

    override suspend fun getShortcuts(contentDomainId: String, limit: Int): ResourceShortcuts {
        val inDomain = repository.getResources().getOrDefault(emptyList()).filter { it.domainId == contentDomainId }
        // The asset has no real dates, so one of each type gives a more useful mix than the first few.
        val picks = inDomain.distinctBy { it.type }.take(limit)
        return ResourceShortcuts(
            items = picks.map { item ->
                ResourceShortcut(
                    id = item.id,
                    title = item.title,
                    author = item.author,
                    kind = when (item.type) {
                        ResourceType.LINK -> ResourceKind.LINK
                        ResourceType.PDF -> ResourceKind.PDF
                        ResourceType.VIDEO -> ResourceKind.VIDEO
                        ResourceType.NOTES -> ResourceKind.NOTES
                        ResourceType.GITHUB -> ResourceKind.GITHUB
                    },
                    url = item.url
                )
            },
            total = inDomain.size
        )
    }
}
