package com.example.innogeeks.feature_resources.domain.use_case

import com.example.innogeeks.feature_resources.domain.model.ResourceDraft

// Same rule the server and the list mapper apply: a title, and an http(s) link.
internal fun ResourceDraft.isValid(): Boolean =
    title.isNotBlank() && (url.startsWith("https://") || url.startsWith("http://"))

internal fun ResourceDraft.trimmed(): ResourceDraft = copy(
    title = title.trim(),
    description = description.trim(),
    author = author.trim(),
    url = url.trim()
)
