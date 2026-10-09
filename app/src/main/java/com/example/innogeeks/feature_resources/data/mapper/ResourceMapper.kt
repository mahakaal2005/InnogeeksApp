package com.example.innogeeks.feature_resources.data.mapper

import com.example.innogeeks.feature_resources.data.dto.ResourceBodyDto
import com.example.innogeeks.feature_resources.data.dto.ResourceDto
import com.example.innogeeks.feature_resources.domain.model.ResourceDraft
import com.example.innogeeks.feature_resources.domain.model.ResourceItem
import com.example.innogeeks.feature_resources.domain.model.ResourceType

// Returns null for unknown types or non-http(s) urls so one bad row never breaks the whole list.
fun ResourceDto.toResourceItem(): ResourceItem? {
    val resourceType = ResourceType.entries.firstOrNull { it.name == type } ?: return null
    if (!url.startsWith("https://") && !url.startsWith("http://")) return null
    return ResourceItem(
        id = id,
        domainId = domainId,
        type = resourceType,
        title = title,
        description = description,
        author = author,
        date = date,
        level = level,
        url = url
    )
}

fun ResourceDraft.toBodyDto(): ResourceBodyDto = ResourceBodyDto(
    type = type.name,
    title = title,
    description = description,
    author = author,
    level = level,
    url = url
)
