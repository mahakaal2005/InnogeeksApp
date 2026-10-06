package com.example.innogeeks.feature_resources.presentation.resources.components

import com.example.innogeeks.feature_resources.domain.model.ResourceItem
import kotlin.math.max

private const val SUGGESTION_THRESHOLD = 0.4

// Levenshtein distance, case-insensitive.
private fun levenshtein(a: String, b: String): Int {
    val rows = a.length + 1
    val cols = b.length + 1
    val dist = Array(rows) { IntArray(cols) }
    for (i in 0 until rows) dist[i][0] = i
    for (j in 0 until cols) dist[0][j] = j
    for (i in 1 until rows) {
        for (j in 1 until cols) {
            val cost = if (a[i - 1].equals(b[j - 1], ignoreCase = true)) 0 else 1
            dist[i][j] = minOf(
                dist[i - 1][j] + 1,
                dist[i][j - 1] + 1,
                dist[i - 1][j - 1] + cost
            )
        }
    }
    return dist[rows - 1][cols - 1]
}

// Normalized similarity in [0,1] — 1 means identical, 0 means completely different.
internal fun fuzzyScore(query: String, target: String): Double {
    if (query.isBlank() || target.isBlank()) return 0.0
    val distance = levenshtein(query, target)
    return 1.0 - distance.toDouble() / max(query.length, target.length)
}

// Word-by-word scoring finds a short typo query in a long title, which one whole-string comparison would bury.
private fun wordScore(query: String, target: String): Double {
    val queryWords = query.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    val targetWords = target.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    if (queryWords.isEmpty() || targetWords.isEmpty()) return 0.0
    return queryWords.map { queryWord ->
        targetWords.maxOf { targetWord -> fuzzyScore(queryWord, targetWord) }
    }.average()
}

private fun bestScore(query: String, target: String): Double =
    max(fuzzyScore(query, target), wordScore(query, target))

// Closest resources to a query that returned zero exact matches, ranked by title/author similarity.
internal fun suggestResources(query: String, pool: List<ResourceItem>, limit: Int = 4): List<ResourceItem> {
    return pool
        .map { it to max(bestScore(query, it.title), bestScore(query, it.author)) }
        .filter { (_, score) -> score >= SUGGESTION_THRESHOLD }
        .sortedByDescending { (_, score) -> score }
        .take(limit)
        .map { (resource, _) -> resource }
}
