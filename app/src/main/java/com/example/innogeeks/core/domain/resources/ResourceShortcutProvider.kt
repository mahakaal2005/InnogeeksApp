package com.example.innogeeks.core.domain.resources

// Lets Home show a domain's resources without importing feature_resources, which implements it.
interface ResourceShortcutProvider {
    // contentDomainId is UserDomain.contentDomainId; returns empty shortcuts when nothing can be read.
    suspend fun getShortcuts(contentDomainId: String, limit: Int): ResourceShortcuts
}

enum class ResourceKind { LINK, PDF, VIDEO, NOTES, GITHUB }

data class ResourceShortcut(
    val id: String,
    val title: String,
    val author: String,
    val kind: ResourceKind,
    val url: String
)

data class ResourceShortcuts(
    val items: List<ResourceShortcut>,
    val total: Int // every resource in the domain, for the "All N" card
)
