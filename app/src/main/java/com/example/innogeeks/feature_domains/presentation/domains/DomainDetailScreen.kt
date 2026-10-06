package com.example.innogeeks.feature_domains.presentation.domains

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.innogeeks.core.presentation.components.GlowBlob
import com.example.innogeeks.core.presentation.components.liquidGlass
import com.example.innogeeks.core.domain.model.Domain
import com.example.innogeeks.feature_domains.presentation.domains.components.DomainDetail
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

// Full-page version of what used to be the in-place expanded row — its own glow-blob
// backdrop (same recipe as AuthGlowBackground) so it reads as glass, not a flat black sheet.
@Composable
fun DomainDetailScreen(
    domain: Domain,
    hazeState: HazeState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val accent = scheme.secondary

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
            .hazeSource(hazeState)
    ) {
        GlowBlob(
            color = scheme.secondary,
            modifier = Modifier
                .size(380.dp)
                .offset(x = (-120).dp, y = (-40).dp)
        )
        GlowBlob(
            color = scheme.primary,
            modifier = Modifier
                .size(420.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 120.dp, y = 140.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .liquidGlass(hazeState = hazeState, cornerRadius = 19.dp)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = scheme.onSurface
                    )
                }
                Column {
                    Text(
                        text = domain.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    )
                    Text(
                        text = domain.tagline,
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant
                    )
                }
            }

            DomainDetail(
                domain = domain,
                accent = accent,
                hazeState = hazeState,
                modifier = Modifier.padding(bottom = 140.dp)
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun DomainDetailScreenPreview() {
    InnogeeksTheme {
        DomainDetailScreen(
            domain = previewDomainList.first(),
            hazeState = remember { HazeState() },
            onBack = {}
        )
    }
}
