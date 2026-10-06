package com.example.innogeeks.feature_domains.presentation.domains

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.kiet.innogeeks.R
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.toRoute
import com.example.innogeeks.core.domain.model.Domain
import com.example.innogeeks.core.domain.model.DomainMember
import com.example.innogeeks.core.domain.model.DomainMemberRole
import com.example.innogeeks.core.presentation.components.liquidGlass
import com.example.innogeeks.ui.theme.InnogeeksTheme
import com.example.innogeeks.ui.theme.displayFontFamily
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.koin.androidx.compose.koinViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import com.example.innogeeks.core.presentation.UiText
import androidx.compose.ui.res.stringResource

// Nested NavHost scoped to this tab; the bottom bar visibility is reported up via onBottomBarVisibilityChanged.
@Composable
fun DomainsRoot(
    hazeState: HazeState,
    onBottomBarVisibilityChanged: (Boolean) -> Unit = {},
    viewModel: DomainsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(currentBackStackEntry) {
        onBottomBarVisibilityChanged(currentBackStackEntry?.destination?.hasRoute<DomainDetailRoute>() != true)
    }

    NavHost(
        navController = navController,
        startDestination = DomainsListRoute,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
    ) {
        composable<DomainsListRoute> {
            DomainsScreen(
                state = state,
                hazeState = hazeState,
                onDomainClick = { domainId -> navController.navigate(DomainDetailRoute(domainId)) }
            )
        }
        composable<DomainDetailRoute> { backStackEntry ->
            val route: DomainDetailRoute = backStackEntry.toRoute()
            val domain = state.domains.find { it.id == route.domainId }
            if (domain != null) {
                DomainDetailScreen(
                    domain = domain,
                    hazeState = hazeState,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun DomainsScreen(
    state: DomainsState,
    hazeState: HazeState,
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
                Text(text = state.error.asString(), color = scheme.error)
            }
            return@Box
        }

        // A fixed non-scrolling grid whose rows share the leftover height, in a 2/1/2 pattern.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .hazeSource(hazeState)
                .padding(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 110.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.common_domains),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface
                )
                Text(
                    text = stringResource(R.string.domains_tap_any_domain_to_see),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val columns = 2
                state.domains.chunkedByRowPattern(rowPattern = listOf(2, 1, 2)).forEach { rowDomains ->
                    Row(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Centers a shorter row by splitting the leftover columns evenly on both sides,
                        // instead of dumping it all as trailing space.
                        val sideSpace = (columns - rowDomains.size) / 2f
                        if (sideSpace > 0f) Spacer(modifier = Modifier.weight(sideSpace))
                        rowDomains.forEach { domain ->
                            DomainSquareCard(
                                domain = domain,
                                hazeState = hazeState,
                                onClick = { onDomainClick(domain.id) },
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )
                        }
                        if (sideSpace > 0f) Spacer(modifier = Modifier.weight(sideSpace))
                    }
                }
            }
        }
    }
}

// Splits into rows of the given sizes, cycling the pattern if more items remain than it covers.
private fun <T> List<T>.chunkedByRowPattern(rowPattern: List<Int>): List<List<T>> {
    val rows = mutableListOf<List<T>>()
    var index = 0
    var patternIndex = 0
    while (index < size) {
        val rowSize = rowPattern[patternIndex % rowPattern.size]
        rows.add(subList(index, minOf(index + rowSize, size)))
        index += rowSize
        patternIndex++
    }
    return rows
}

// Collapsed grid cell — mirrors .ev-card/.ev-tile/.ev-disc/.ev-card-label from the events
// screen section of Inno_guest.html (24px tile radius, 63% centered disc, 12.5px/10.5px label).
@Composable
private fun DomainSquareCard(
    domain: Domain,
    hazeState: HazeState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .liquidGlass(hazeState = hazeState, cornerRadius = 24.dp)
                // clickable goes after liquidGlass (which already clips to this rounded shape) so
                // the tap feedback is confined to the card, not a full rectangle over the label too.
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            BlobBackground(seed = domain.id.hashCode(), modifier = Modifier.fillMaxSize())
            Image(
                painter = painterResource(id = domainIconRes(domain.id)),
                contentDescription = null,
                modifier = Modifier.size(75.dp)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 4.dp, end = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = domain.name,
                fontFamily = displayFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                lineHeight = 17.sp,
                color = scheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = domain.tagline,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

// Maps each domain to its 3D icon; falls back to the IoT glyph for any unknown id.
private fun domainIconRes(domainId: String): Int = when (domainId) {
    "webd" -> R.drawable.ic_domain_webd
    "appd" -> R.drawable.ic_domain_appd
    "ml" -> R.drawable.ic_domain_ml
    "arvr" -> R.drawable.ic_domain_arvr
    else -> R.drawable.ic_domain_iot
}

// Ambient gradient behind the domain icon, using normal alpha blending so overlaps deepen instead of blowing out to white.
private data class BlobSpec(
    val color: Color,
    val peakAlpha: Float,
    val radiusScale: Float,
    val orbitReachScale: Float,
    val orbitCenterOffset: Offset, // fraction of container size, relative to center
    val periodMs: Int
)

private val blobSpecs = listOf(
    BlobSpec(Color(0xFF0B4C63), 0.50f, 0.78f, 0.14f, Offset(0f, 0f), 13000),
    BlobSpec(Color(0xFF1AA6C9), 0.38f, 0.58f, 0.26f, Offset(0.10f, -0.06f), 10000),
    BlobSpec(Color(0xFF7FE3FF), 0.32f, 0.34f, 0.34f, Offset(-0.12f, 0.10f), 8000)
)

private const val TWO_PI = (2 * PI).toFloat()

// A blob's live position, driven by BlobBackground's per-frame bounce loop below —
// var pos is a mutableStateOf so writing it invalidates only the Canvas draw, not recomposition.
private class BouncingBlob(pos: Offset, var vel: Offset, val rng: Random) {
    var pos by mutableStateOf(pos)
}

// Each blob drifts in a straight line and bounces off the walls with a small random turn, so the motion never restarts.
@Composable
private fun BlobBackground(seed: Int, modifier: Modifier = Modifier) {
    var containerSize by remember(seed) { mutableStateOf(IntSize.Zero) }
    val blobs = remember(seed) {
        blobSpecs.indices.map { i -> BouncingBlob(pos = Offset.Zero, vel = Offset.Zero, rng = Random(seed + i * 97)) }
    }

    LaunchedEffect(seed, containerSize) {
        val size = containerSize
        if (size.width == 0 || size.height == 0) return@LaunchedEffect
        val minDim = minOf(size.width, size.height).toFloat()

        fun centerPxFor(spec: BlobSpec) = Offset(
            size.width / 2f + spec.orbitCenterOffset.x * size.width,
            size.height / 2f + spec.orbitCenterOffset.y * size.height
        )

        blobSpecs.forEachIndexed { i, spec ->
            val blob = blobs[i]
            val reachPx = minDim * spec.orbitReachScale
            val centerPx = centerPxFor(spec)
            val startAngle = blob.rng.nextFloat() * TWO_PI
            val startDist = blob.rng.nextFloat() * reachPx
            val speed = (2f * reachPx) / (spec.periodMs / 1000f)
            val velAngle = blob.rng.nextFloat() * TWO_PI
            blob.pos = centerPx + Offset(cos(startAngle), sin(startAngle)) * startDist
            blob.vel = Offset(cos(velAngle), sin(velAngle)) * speed
        }

        var lastFrameNanos = withFrameNanos { it }
        while (true) {
            val nowNanos = withFrameNanos { it }
            val dt = ((nowNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0f, 0.1f)
            lastFrameNanos = nowNanos

            blobSpecs.forEachIndexed { i, spec ->
                val blob = blobs[i]
                val reachPx = minDim * spec.orbitReachScale
                val centerPx = centerPxFor(spec)
                val minX = centerPx.x - reachPx
                val maxX = centerPx.x + reachPx
                val minY = centerPx.y - reachPx
                val maxY = centerPx.y + reachPx

                var x = blob.pos.x + blob.vel.x * dt
                var y = blob.pos.y + blob.vel.y * dt
                var vx = blob.vel.x
                var vy = blob.vel.y
                var bounced = false

                if (x < minX || x > maxX) {
                    x = x.coerceIn(minX, maxX)
                    vx = -vx
                    bounced = true
                }
                if (y < minY || y > maxY) {
                    y = y.coerceIn(minY, maxY)
                    vy = -vy
                    bounced = true
                }

                if (bounced) {
                    val jitter = (blob.rng.nextFloat() - 0.5f) * (PI.toFloat() / 8f)
                    val cosJ = cos(jitter)
                    val sinJ = sin(jitter)
                    val rotatedVx = vx * cosJ - vy * sinJ
                    val rotatedVy = vx * sinJ + vy * cosJ
                    vx = rotatedVx
                    vy = rotatedVy
                }

                blob.vel = Offset(vx, vy)
                blob.pos = Offset(x, y)
            }
        }
    }

    Canvas(
        modifier = modifier
            .blur(20.dp)
            .onSizeChanged { containerSize = it }
    ) {
        blobSpecs.forEachIndexed { i, spec ->
            val radius = size.minDimension * spec.radiusScale
            val center = blobs[i].pos
            drawCircle(
                brush = Brush.radialGradient(
                    0f to spec.color.copy(alpha = spec.peakAlpha),
                    0.45f to spec.color.copy(alpha = spec.peakAlpha * 0.55f),
                    1f to spec.color.copy(alpha = 0f),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }
    }
}

internal val previewDomainList = listOf(
    // App Dev listed first to match the real repository's order.
    Domain(
        id = "appd",
        name = "App Dev",
        tagline = "Native & cross-platform builders",
        description = "App Dev designs and ships the club's native and cross-platform mobile apps, end to end.",
        accentIndex = 1,
        memberCount = 14,
        techStack = listOf("Kotlin", "Flutter", "Firebase", "Jetpack Compose"),
        members = listOf(
            DomainMember("Rohan Verma", "RV", DomainMemberRole.COORDINATOR),
            DomainMember("Kavya Reddy", "KR", DomainMemberRole.COORDINATOR),
            DomainMember("Arjun Nanda", "AN", DomainMemberRole.TEAM)
        )
    ),
    Domain(
        id = "webd",
        name = "Web Dev",
        tagline = "React, Node & everything between",
        description = "Web Dev builds and maintains all of Innogeeks' web-facing tools, from the club site to event portals.",
        accentIndex = 0,
        memberCount = 18,
        techStack = listOf("React", "Node.js", "Tailwind", "MongoDB", "TypeScript"),
        members = listOf(
            DomainMember("Priya Sharma", "PS", DomainMemberRole.COORDINATOR),
            DomainMember("Rahul Deshmukh", "RD", DomainMemberRole.COORDINATOR),
            DomainMember("Ananya Iyer", "AI", DomainMemberRole.TEAM)
        )
    ),
    Domain(
        id = "ml",
        name = "Machine Learning",
        tagline = "Models, data & leaderboard chasing",
        description = "Machine Learning explores applied ML and data science, from model training to real-world deployment.",
        accentIndex = 2,
        memberCount = 11,
        techStack = listOf("Python", "TensorFlow", "PyTorch", "Scikit-learn", "Pandas"),
        members = listOf(
            DomainMember("Ananya Gupta", "AG", DomainMemberRole.COORDINATOR),
            DomainMember("Vikram Rao", "VR", DomainMemberRole.COORDINATOR),
            DomainMember("Nisha Bhat", "NB", DomainMemberRole.TEAM)
        )
    )
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun DomainsScreenCollapsedPreview() {
    InnogeeksTheme {
        DomainsScreen(
            state = DomainsState(isLoading = false, domains = previewDomainList),
            hazeState = HazeState(),
            onDomainClick = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DomainsScreenLoadingPreview() {
    InnogeeksTheme {
        DomainsScreen(state = DomainsState(), hazeState = HazeState(), onDomainClick = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DomainsScreenErrorPreview() {
    InnogeeksTheme {
        DomainsScreen(
            state = DomainsState(isLoading = false, error = UiText.DynamicString("Failed to load domains.")),
            hazeState = HazeState(),
            onDomainClick = {}
        )
    }
}
