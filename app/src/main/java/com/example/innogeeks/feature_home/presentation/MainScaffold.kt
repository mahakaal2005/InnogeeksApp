package com.example.innogeeks.feature_home.presentation

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.model.UserRole
import com.example.innogeeks.core.domain.session.Session
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.core.presentation.components.AuthGlowBackground
import com.example.innogeeks.core.presentation.components.liquidGlass
import com.example.innogeeks.feature_domains.presentation.domains.DomainsRoot
import com.example.innogeeks.feature_events.presentation.events.EventsRoot
import com.example.innogeeks.feature_profile.presentation.profile.ProfileRoot
import com.example.innogeeks.feature_recruitment.domain.model.Decision
import com.example.innogeeks.feature_recruitment.domain.repository.RecruitmentRepository
import com.example.innogeeks.feature_recruitment.domain.use_case.GetRecruitmentStatusUseCase
import com.example.innogeeks.feature_recruitment.presentation.tracker.TrackerRoot
import com.example.innogeeks.feature_resources.presentation.resources.ResourcesRoot
import com.example.innogeeks.feature_home.domain.model.ClubStats
import com.example.innogeeks.feature_home.presentation.home.HomeRoot
import com.example.innogeeks.feature_home.presentation.home.HomeScreen
import com.example.innogeeks.feature_home.presentation.home.HomeState
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.koin.compose.koinInject

private data class BottomNavTab(
    val label: String,
    val icon: ImageVector,
)

// Guest: 4 tabs (Home/Domains/Events/Profile)
private val guestTabs = listOf(
    BottomNavTab("Home", Icons.Filled.Home),
    BottomNavTab("Domains", Icons.Filled.Category),
    BottomNavTab("Events", Icons.Filled.CalendarMonth),
    BottomNavTab("Profile", Icons.Filled.Person),
)

// Registered (first-year): 5 tabs (Tracker/Domains/Resources/Events/Profile)
private val registeredTabs = listOf(
    BottomNavTab("Tracker", Icons.Filled.Timeline),
    BottomNavTab("Domains", Icons.Filled.Category),
    BottomNavTab("Resources", Icons.Filled.FolderOpen),
    BottomNavTab("Events", Icons.Filled.CalendarMonth),
    BottomNavTab("Profile", Icons.Filled.Person),
)

// REGISTERED + REJECTED: Tracker has nothing left to show; Resources becomes tab 0 as the one
// thing the app still offers after a terminal decision (the fee was already paid).
private val rejectedTabs = listOf(
    BottomNavTab("Resources", Icons.Filled.FolderOpen),
    BottomNavTab("Domains", Icons.Filled.Category),
    BottomNavTab("Events", Icons.Filled.CalendarMonth),
    BottomNavTab("Profile", Icons.Filled.Person),
)

// Single place role -> tab-set is decided. Phase 4's actual Member/Coordinator/Admin nav isn't
// designed yet, so they reuse registeredTabs for now — update just this function when it is.
private fun UserRole.tabs(): List<BottomNavTab> = when (this) {
    UserRole.REGISTERED, UserRole.MEMBER, UserRole.COORDINATOR, UserRole.ADMIN -> registeredTabs
}

// Guest / normal Authenticated / REJECTED all have different tab counts+content, and this
// composable survives login/logout/decision changes in place (session just recomposes, nothing
// navigates) — this key drives the reset-to-0 effect below.
private enum class TabMode { GUEST, AUTHENTICATED, REJECTED }

@Composable
fun MainScaffold(
    session: Session = Session.Guest,
    onNavigateToAuth: () -> Unit = {},
    homeContent: (@Composable (HazeState) -> Unit)? = null,
    getRecruitmentStatusUseCase: GetRecruitmentStatusUseCase = koinInject()
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val hazeState = remember { HazeState() }
    var showBottomBar by remember { mutableStateOf(true) }
    LaunchedEffect(selectedTab) { showBottomBar = true }

    // Only REGISTERED users can ever be rejected — Member/Coordinator/Admin already passed
    // recruitment, so skip the fetch entirely for them (no network call, no stale-value risk).
    var decision by remember { mutableStateOf<Decision?>(null) }
    LaunchedEffect(session) {
        decision = if (session is Session.Authenticated && session.role == UserRole.REGISTERED) {
            when (val result = getRecruitmentStatusUseCase()) {
                is Result.Success -> result.data.decision
                is Result.Error -> null // fetch failed: fall back to the normal Tracker layout
            }
        } else {
            null
        }
    }

    val tabMode = when {
        session is Session.Guest -> TabMode.GUEST
        session is Session.Authenticated && session.role == UserRole.REGISTERED &&
            decision == Decision.REJECTED -> TabMode.REJECTED
        else -> TabMode.AUTHENTICATED
    }

    // Without this, selectedTab keeps its old index into the NEW tab list on a mode change,
    // landing on the wrong tab or, if the new list is shorter, on an index the `when` below
    // doesn't handle at all (blank screen).
    var previousTabMode by rememberSaveable { mutableStateOf(tabMode) }
    LaunchedEffect(tabMode) {
        if (previousTabMode != tabMode) {
            selectedTab = 0
        }
        previousTabMode = tabMode
    }

    // Determine tab layout based on session + recruitment decision
    val tabs = when (tabMode) {
        TabMode.GUEST -> guestTabs
        TabMode.REJECTED -> rejectedTabs
        TabMode.AUTHENTICATED -> (session as Session.Authenticated).role.tabs()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AuthGlowBackground(hazeState = hazeState)

        Box(modifier = Modifier.fillMaxSize()) {
            when (session) {
                Session.Guest -> {
                    when (selectedTab) {
                        0 -> homeContent?.invoke(hazeState) ?: HomeRoot(
                            hazeState = hazeState,
                            session = session,
                            onNavigateToProfile = { selectedTab = 3 },
                            onNavigateToAuth = onNavigateToAuth
                        )
                        1 -> DomainsRoot(hazeState = hazeState, onBottomBarVisibilityChanged = { showBottomBar = it })
                        2 -> EventsRoot(hazeState = hazeState, onBottomBarVisibilityChanged = { showBottomBar = it })
                        3 -> ProfileRoot(hazeState = hazeState, onNavigateToAuth = onNavigateToAuth)
                    }
                }
                is Session.Authenticated -> {
                    if (tabMode == TabMode.REJECTED) {
                        when (selectedTab) {
                            0 -> ResourcesRoot(hazeState = hazeState)
                            1 -> DomainsRoot(hazeState = hazeState, onBottomBarVisibilityChanged = { showBottomBar = it })
                            2 -> EventsRoot(hazeState = hazeState, onBottomBarVisibilityChanged = { showBottomBar = it })
                            3 -> ProfileRoot(hazeState = hazeState, onNavigateToAuth = onNavigateToAuth)
                        }
                    } else {
                        when (selectedTab) {
                            0 -> TrackerRoot(
                                hazeState = hazeState,
                                onNavigateToResources = { selectedTab = 2 }
                            )
                            1 -> DomainsRoot(hazeState = hazeState, onBottomBarVisibilityChanged = { showBottomBar = it })
                            2 -> ResourcesRoot(hazeState = hazeState)
                            3 -> EventsRoot(hazeState = hazeState, onBottomBarVisibilityChanged = { showBottomBar = it })
                            4 -> ProfileRoot(hazeState = hazeState, onNavigateToAuth = onNavigateToAuth)
                        }
                    }
                }
            }
        }

        // Floating Glassmorphic Bottom Navigation Bar — hidden while a detail page is open.
        AnimatedVisibility(
            visible = showBottomBar,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            InnogeeksBottomNav(
                tabs = tabs,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                hazeState = hazeState
            )
        }
    }
}

// Floating Glassmorphic Bottom Nav Bar with Liquid Stretch + Expanding Text
@Composable
private fun InnogeeksBottomNav(
    tabs: List<BottomNavTab>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .liquidGlass(hazeState = hazeState, cornerRadius = 32.dp)
            .padding(6.dp)
    ) {
        val tabBounds = remember { androidx.compose.runtime.mutableStateMapOf<Int, androidx.compose.ui.geometry.Rect>() }

        Box(modifier = Modifier.fillMaxWidth()) {
            val targetRect = tabBounds[selectedTab] ?: androidx.compose.ui.geometry.Rect.Zero

            var previousTab by remember { mutableIntStateOf(selectedTab) }
            LaunchedEffect(selectedTab) { previousTab = selectedTab }
            val isMovingRight = selectedTab > previousTab

            val leftStiffness = if (isMovingRight) 40f else 350f
            val rightStiffness = if (isMovingRight) 350f else 40f

            val density = androidx.compose.ui.platform.LocalDensity.current

            val animatedLeft by animateDpAsState(
                targetValue = with(density) { targetRect.left.toDp() },
                animationSpec = spring(dampingRatio = 0.65f, stiffness = leftStiffness),
                label = "leftEdge"
            )
            val animatedRight by animateDpAsState(
                targetValue = with(density) { targetRect.right.toDp() },
                animationSpec = spring(dampingRatio = 0.65f, stiffness = rightStiffness),
                label = "rightEdge"
            )

            // The stretching Liquid Pill background (only renders if we have bounds)
            if (targetRect != androidx.compose.ui.geometry.Rect.Zero) {
                Box(
                    modifier = Modifier
                        .offset(x = animatedLeft)
                        .width(animatedRight - animatedLeft)
                        .height(48.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                            RoundedCornerShape(22.dp)
                        )
                )
            }

            // The Icons and Text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.1f else 1.0f,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "iconScale"
                    )
                    val iconColor by androidx.compose.animation.animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(
                            alpha = 0.6f
                        ),
                        animationSpec = androidx.compose.animation.core.tween(300),
                        label = "iconColor"
                    )

                    Box(
                        modifier = Modifier
                            .height(48.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onTabSelected(index) }
                            )
                            .onGloballyPositioned { coordinates ->
                                // Convert bounds to parent BoxWithConstraints coordinates
                                val boundsInParent = coordinates.boundsInParent()
                                tabBounds[index] = boundsInParent
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = iconColor,
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer(scaleX = iconScale, scaleY = iconScale)
                            )

                            androidx.compose.animation.AnimatedVisibility(
                                visible = isSelected,
                                enter = androidx.compose.animation.fadeIn(
                                    androidx.compose.animation.core.tween(
                                        300
                                    )
                                ) + androidx.compose.animation.expandHorizontally(spring(stiffness = Spring.StiffnessMediumLow)),
                                exit = androidx.compose.animation.fadeOut(
                                    androidx.compose.animation.core.tween(
                                        200
                                    )
                                ) + androidx.compose.animation.shrinkHorizontally(spring(stiffness = Spring.StiffnessMediumLow))
                            ) {
                                Text(
                                    text = tab.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    modifier = Modifier.padding(start = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Placeholder for Phase 4 screens (Attendance) — Member/Coordinator tabs aren't reachable yet.
@Composable
private fun PlaceholderScreen(
    title: String,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .hazeSource(hazeState)
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Coming in Phase 2",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// No Koin context exists in previews — koinInject() would throw, so previews pass a stub
// use case directly. Session defaults to Guest here anyway, so it's never actually invoked.
private val previewRecruitmentUseCase = GetRecruitmentStatusUseCase(
    recruitmentRepository = object : RecruitmentRepository {
        override suspend fun getRecruitmentStatus() = Result.Error(DataError.Network.UNKNOWN)
    }
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MainScaffoldPreview() {
    InnogeeksTheme {
        MainScaffold(
            getRecruitmentStatusUseCase = previewRecruitmentUseCase,
            homeContent = { hazeState ->
                HomeScreen(
                    state = HomeState(
                        isLoading = false,
                        stats = ClubStats(150, 45, 6, 24),
                        domains = emptyList(),
                        tickerRows = listOf(listOf("Technology", "Design")),
                        cultureMoments = listOf("📡", "🤖", "🏆")
                    ),
                    hazeState = hazeState,
                    onAction = {}
                )
            }
        )
    }
}
