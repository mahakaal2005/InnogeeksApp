package com.example.innogeeks.feature_resources.domain.model

data class DetectedLink(val type: ResourceType, val source: String)

// Guesses type and source from a pasted link so the coordinator rarely has to pick them.
fun detectLink(rawUrl: String): DetectedLink? {
    val url = rawUrl.trim()
    if (!url.startsWith("http://") && !url.startsWith("https://")) return null
    val host = url.substringAfter("://").substringBefore('/').substringBefore('?').lowercase().removePrefix("www.")
    if (host.isBlank() || !host.contains('.')) return null
    val path = url.substringAfter("://").substringAfter('/', "").substringBefore('?').lowercase()

    val type = when {
        host == "youtu.be" || host.endsWith("youtube.com") || host.endsWith("vimeo.com") -> ResourceType.VIDEO
        host == "github.com" || host.endsWith(".github.com") || host.endsWith("gitlab.com") -> ResourceType.GITHUB
        path.endsWith(".pdf") -> ResourceType.PDF
        else -> ResourceType.LINK
    }
    val source = when {
        host == "youtu.be" || host.endsWith("youtube.com") -> "YouTube"
        host.endsWith("github.com") -> "GitHub"
        else -> host.split('.').let { parts -> parts[(parts.size - 2).coerceAtLeast(0)] }.replaceFirstChar { it.uppercaseChar() }
    }
    return DetectedLink(type, source)
}
