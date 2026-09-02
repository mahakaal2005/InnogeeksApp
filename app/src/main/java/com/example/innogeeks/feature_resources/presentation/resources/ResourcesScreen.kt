package com.example.innogeeks.feature_resources.presentation.resources

import android.content.res.Configuration
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.innogeeks.core.presentation.components.StatTile
import com.example.innogeeks.core.presentation.components.liquidGlass
import com.example.innogeeks.feature_domains.domain.model.Domain
import com.example.innogeeks.feature_resources.domain.model.ResourceItem
import com.example.innogeeks.feature_resources.domain.model.ResourceType
import com.example.innogeeks.feature_resources.presentation.resources.components.domainAccent
import com.example.innogeeks.feature_resources.presentation.resources.components.domainIconRes
import com.example.innogeeks.ui.theme.InnogeeksTheme
import com.example.innogeeks.ui.theme.displayFontFamily
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

// Nested NavHost scoped to this tab: domain picker -> per-domain feed -> resource detail.
// MainScaffold's bottom nav lives outside this Box, so it stays put across all three screens.
@Composable
fun ResourcesRoot(
    hazeState: HazeState,
    viewModel: ResourcesViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ResourcesEvent.OpenUrl -> uriHandler.openUri(event.url)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = ResourcesListRoute,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
    ) {
        composable<ResourcesListRoute> {
            ResourcesScreen(
                state = state,
                hazeState = hazeState,
                onAction = viewModel::onAction,
                onDomainClick = { domainId -> navController.navigate(ResourceBrowserRoute(domainId)) }
            )
        }
        composable<ResourceBrowserRoute> { backStackEntry ->
            val route: ResourceBrowserRoute = backStackEntry.toRoute()
            val domain = state.domains.find { it.id == route.domainId }
            if (domain != null) {
                ResourceBrowserScreen(
                    domain = domain,
                    resources = state.resources.filter { it.domainId == domain.id },
                    hazeState = hazeState,
                    onBack = { navController.popBackStack() },
                    onResourceClick = { resourceId -> navController.navigate(ResourceDetailRoute(resourceId)) }
                )
            }
        }
        composable<ResourceDetailRoute> { backStackEntry ->
            val route: ResourceDetailRoute = backStackEntry.toRoute()
            val resource = state.resources.find { it.id == route.resourceId }
            if (resource != null) {
                ResourceDetailScreen(
                    resource = resource,
                    hazeState = hazeState,
                    onBack = { navController.popBackStack() },
                    onOpenResource = { url -> viewModel.onAction(ResourcesAction.OnResourceItemClicked(url)) }
                )
            }
        }
    }
}

@Composable
fun ResourcesScreen(
    state: ResourcesState,
    hazeState: HazeState,
    onAction: (ResourcesAction) -> Unit,
    onDomainClick: (String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Box
        }

        if (state.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.error, color = scheme.error)
            }
            return@Box
        }

        val typeCount = state.resources.map { it.type }.distinct().size

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .hazeSource(hazeState)
                .padding(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 110.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Resources",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface
                )
                Text(
                    text = "Pick a domain — explore, learn, grow.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatTile(value = state.domains.size, caption = "Domains", hazeState = hazeState, modifier = Modifier.weight(1f))
                StatTile(value = state.resources.size, caption = "Resources", hazeState = hazeState, modifier = Modifier.weight(1f))
                StatTile(value = typeCount, caption = "Types", hazeState = hazeState, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.padding(top = 8.dp))

            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                state.domains.chunked(2).forEach { rowDomains ->
                    Row(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        rowDomains.forEach { domain ->
                            ResourceDomainCard(
                                domain = domain,
                                resourceCount = state.resources.count { it.domainId == domain.id },
                                hazeState = hazeState,
                                onClick = { onDomainClick(domain.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                        if (rowDomains.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResourceDomainCard(
    domain: Domain,
    resourceCount: Int,
    hazeState: HazeState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val accent = domainAccent(domain.accentIndex)

    Box(
        modifier = modifier
            .liquidGlass(hazeState = hazeState, cornerRadius = 22.dp)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 14.dp, top = 16.dp, bottom = 14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 2.dp)
                .align(Alignment.CenterStart)
                .width(3.dp)
                .background(accent.copy(alpha = 0.7f), shape = RoundedCornerShape(3.dp))
        )

        Column(modifier = Modifier.fillMaxSize().padding(start = 8.dp)) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = 0.14f))
                    .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = domainIconRes(domain.id)),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = domain.name,
                fontFamily = displayFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                lineHeight = 17.sp,
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = domain.tagline,
                fontSize = 9.5.sp,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$resourceCount resource${if (resourceCount != 1) "s" else ""}",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = accent
                )
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.16f))
                        .border(1.dp, accent.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }
    }
}

private val previewResources = listOf(
    ResourceItem("w1", "webd", ResourceType.LINK, "🌐", "The Odin Project", "Full-stack curriculum.", "Ritesh", "Aug 2026", "Beginner", "#"),
    ResourceItem("w2", "webd", ResourceType.PDF, "📄", "CSS Cheatsheet", "Layout reference.", "Neha", "Jul 2026", "Beginner", "#"),
    ResourceItem("a1", "appd", ResourceType.LINK, "🔗", "Compose Docs", "Official docs.", "Faiq", "Aug 2026", "Beginner", "#")
)

private val previewDomains = listOf(
    Domain("webd", "Web Dev", "React, Node & everything between", "Web Dev builds and maintains all of Innogeeks' web-facing tools.", 0, 18, emptyList(), emptyList()),
    Domain("appd", "App Dev", "Native & cross-platform builders", "App Dev designs and ships the club's native and cross-platform mobile apps.", 1, 14, emptyList(), emptyList())
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun ResourcesScreenPreview() {
    InnogeeksTheme {
        ResourcesScreen(
            state = ResourcesState(isLoading = false, domains = previewDomains, resources = previewResources),
            hazeState = HazeState(),
            onAction = {},
            onDomainClick = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun ResourcesScreenLoadingPreview() {
    InnogeeksTheme {
        ResourcesScreen(state = ResourcesState(), hazeState = HazeState(), onAction = {}, onDomainClick = {})
    }
}
