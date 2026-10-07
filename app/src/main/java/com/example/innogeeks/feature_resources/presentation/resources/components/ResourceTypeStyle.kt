package com.example.innogeeks.feature_resources.presentation.resources.components

import androidx.compose.ui.graphics.Color
import edu.kiet.innogeeks.R
import com.example.innogeeks.feature_resources.domain.model.ResourceType
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

// One accent + label per resource type, shared by the browser feed, filter bar and detail screen.
@Composable
internal fun ResourceType.label(): String = stringResource(
    when (this) {
        ResourceType.LINK -> R.string.resource_type_link
        ResourceType.PDF -> R.string.resource_type_pdf
        ResourceType.VIDEO -> R.string.resource_type_video
        ResourceType.NOTES -> R.string.resource_type_notes
        ResourceType.GITHUB -> R.string.resource_type_github
    }
)

@Composable
internal fun ResourceType.groupLabel(): String = stringResource(
    when (this) {
        ResourceType.LINK -> R.string.resource_group_link
        ResourceType.PDF -> R.string.resource_group_pdf
        ResourceType.VIDEO -> R.string.resource_group_video
        ResourceType.NOTES -> R.string.resource_group_notes
        ResourceType.GITHUB -> R.string.resource_group_github
    }
)

internal fun ResourceType.accentColor(): Color = when (this) {
    ResourceType.LINK -> Color(0xFF6CD4F3)
    ResourceType.PDF -> Color(0xFFFF8A78)
    ResourceType.VIDEO -> Color(0xFFBFA0FF)
    ResourceType.NOTES -> Color(0xFF9DE6B8)
    ResourceType.GITHUB -> Color(0xFFC8C8C8)
}

// Per-domain accent for the picker cards, indexed by Domain.accentIndex (reserved for this).
internal val domainAccentPalette = listOf(
    Color(0xFF6CD4F3),
    Color(0xFF82B4FF),
    Color(0xFFA8C7FF),
    Color(0xFFD0BCFF),
    Color(0xFFF9C94C),
    Color(0xFF9DE6B8)
)

internal fun domainAccent(accentIndex: Int): Color =
    domainAccentPalette[accentIndex.mod(domainAccentPalette.size)]

// Maps each domain to its 3D icon; falls back to the IoT glyph for any unknown id.
internal fun domainIconRes(domainId: String): Int = when (domainId) {
    "webd" -> R.drawable.ic_domain_webd
    "appd" -> R.drawable.ic_domain_appd
    "ml" -> R.drawable.ic_domain_ml
    "arvr" -> R.drawable.ic_domain_arvr
    else -> R.drawable.ic_domain_iot
}
