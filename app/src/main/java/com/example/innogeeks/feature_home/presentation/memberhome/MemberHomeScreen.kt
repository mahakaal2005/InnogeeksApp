package com.example.innogeeks.feature_home.presentation.memberhome

import android.content.res.Configuration
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.innogeeks.core.domain.model.UserDomain
import com.example.innogeeks.core.domain.model.UserRole
import com.example.innogeeks.core.presentation.ObserveAsEvents
import com.example.innogeeks.core.presentation.ObserveOnResume
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.core.presentation.mapper.toUiText
import com.example.innogeeks.feature_home.presentation.memberhome.components.MemberPassCard
import com.example.innogeeks.feature_home.presentation.memberhome.components.ResourceCarousel
import com.example.innogeeks.feature_home.presentation.memberhome.components.SessionTimeline
import com.example.innogeeks.feature_home.presentation.memberhome.components.ToMarkBar
import com.example.innogeeks.feature_home.presentation.memberhome.components.previewOverview
import com.example.innogeeks.feature_home.presentation.memberhome.components.previewShortcuts
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import edu.kiet.innogeeks.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun MemberHomeRoot(
    hazeState: HazeState,
    onOpenAttendance: () -> Unit,
    onOpenResources: () -> Unit,
    onOpenProfile: () -> Unit,
    viewModel: MemberHomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    // The ViewModel outlives the tab, so coming back needs its own quiet refresh.
    ObserveOnResume { viewModel.onAction(MemberHomeAction.OnScreenShown) }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            MemberHomeEvent.OpenAttendance -> onOpenAttendance()
            MemberHomeEvent.OpenResources -> onOpenResources()
            MemberHomeEvent.OpenProfile -> onOpenProfile()
            is MemberHomeEvent.OpenUrl -> runCatching { uriHandler.openUri(event.url) }
        }
    }

    MemberHomeScreen(state = state, hazeState = hazeState, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberHomeScreen(
    state: MemberHomeState,
    hazeState: HazeState,
    onAction: (MemberHomeAction) -> Unit
) {
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { onAction(MemberHomeAction.OnRefresh) },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .hazeSource(hazeState),
            // Bottom padding keeps the last card clear of the floating glass nav.
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            item(key = "top-bar") { TopBar(initials = state.initials, onProfileClick = { onAction(MemberHomeAction.OnProfileClick) }) }
            item(key = "greeting") { Greeting(state.dayPart, state.name) }

            if (state.isLoading) {
                item(key = "skeleton") { HomeSkeleton() }
                return@LazyColumn
            }

            val domain = state.domain
            val role = state.role

            when {
                state.overview != null && domain != null && role != null -> item(key = "pass") {
                    MemberPassCard(
                        domain = domain,
                        role = role,
                        overview = state.overview,
                        hazeState = hazeState,
                        onClick = { onAction(MemberHomeAction.OnAttendanceClick) },
                        // Room above the card for the mascot that breaks out of it.
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 58.dp)
                    )
                }

                state.attendanceError != null -> item(key = "error") {
                    AttendanceError(message = state.attendanceError, onRetryClick = { onAction(MemberHomeAction.OnRetryClick) })
                }
            }

            state.toMarkCount?.takeIf { it > 0 }?.let { count ->
                item(key = "to-mark") {
                    ToMarkBar(
                        count = count,
                        hazeState = hazeState,
                        onMarkNowClick = { onAction(MemberHomeAction.OnToMarkClick) },
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp)
                    )
                }
            }

            state.overview?.let { overview ->
                item(key = "sessions-header") {
                    SectionHeader(
                        title = stringResource(R.string.member_home_last_sessions),
                        actionLabel = stringResource(R.string.member_home_see_all).takeIf { overview.sessions.isNotEmpty() },
                        onActionClick = { onAction(MemberHomeAction.OnAttendanceClick) }
                    )
                }
                if (overview.sessions.isEmpty()) {
                    item(key = "sessions-empty") {
                        Text(
                            text = stringResource(R.string.member_home_no_sessions),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                } else {
                    item(key = "timeline") { SessionTimeline(sessions = overview.recent, modifier = Modifier.padding(horizontal = 20.dp)) }
                }
            }

            if (state.shortcuts.isNotEmpty() && domain != null) {
                item(key = "learn-header") {
                    SectionHeader(title = stringResource(R.string.member_home_learn, domain.toUiText().asString()))
                }
                item(key = "learn") {
                    ResourceCarousel(
                        shortcuts = state.shortcuts,
                        totalCount = state.totalResources,
                        hazeState = hazeState,
                        onResourceClick = { onAction(MemberHomeAction.OnResourceClick(it)) },
                        onAllClick = { onAction(MemberHomeAction.OnAllResourcesClick) }
                    )
                }
            }
        }
    }
}

// Brand on the left, your initials on the right as a shortcut to Profile.
@Composable
private fun TopBar(initials: String, onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Image(
                painter = painterResource(R.drawable.app_logo),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = stringResource(R.string.pass_card_club_label),
                style = MaterialTheme.typography.titleMedium,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClickLabel = stringResource(R.string.member_home_open_profile), onClick = onProfileClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun Greeting(dayPart: DayPart, name: String) {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp)) {
        Text(
            text = stringResource(
                when (dayPart) {
                    DayPart.MORNING -> R.string.member_home_greeting_morning
                    DayPart.AFTERNOON -> R.string.member_home_greeting_afternoon
                    DayPart.EVENING -> R.string.member_home_greeting_evening
                }
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = name,
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 32.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SectionHeader(title: String, actionLabel: String? = null, onActionClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 30.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
        if (actionLabel != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onActionClick)
            )
        }
    }
}

@Composable
private fun AttendanceError(message: UiText, onRetryClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = message.asString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Button(onClick = onRetryClick) { Text(text = stringResource(R.string.common_retry)) }
    }
}

// Same shape as the loaded screen so nothing jumps when data arrives.
@Composable
private fun HomeSkeleton() {
    val pulse = rememberInfiniteTransition(label = "homeSkeleton").animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse"
    )

    // The alpha is read inside graphicsLayer, so the pulse never recomposes the skeleton.
    Column(modifier = Modifier.graphicsLayer { alpha = pulse.value }.padding(horizontal = 20.dp)) {
        SkeletonBlock(Modifier.padding(top = 58.dp).fillMaxWidth().height(200.dp), 26.dp)
        SkeletonBlock(Modifier.padding(top = 34.dp, bottom = 14.dp).width(150.dp).height(18.dp), 9.dp)
        repeat(3) {
            Row(Modifier.fillMaxWidth().height(62.dp), verticalAlignment = Alignment.CenterVertically) {
                SkeletonBlock(Modifier.width(14.dp).height(14.dp), 7.dp)
                SkeletonBlock(Modifier.padding(start = 20.dp).fillMaxWidth(0.55f).height(14.dp), 7.dp)
            }
        }
    }
}

@Composable
private fun SkeletonBlock(modifier: Modifier, radius: Dp) {
    Box(modifier = modifier.clip(RoundedCornerShape(radius)).background(MaterialTheme.colorScheme.surfaceContainerHigh))
}

private val previewMember = MemberHomeState(
    isLoading = false,
    name = "Atul",
    initials = "AK",
    role = UserRole.MEMBER,
    domain = UserDomain.ANDROID,
    overview = previewOverview,
    shortcuts = previewShortcuts,
    totalResources = 50
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun MemberHomeMemberPreview() {
    InnogeeksTheme { MemberHomeScreen(state = previewMember, hazeState = HazeState(), onAction = {}) }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun MemberHomeCoordinatorPreview() {
    InnogeeksTheme {
        MemberHomeScreen(
            state = previewMember.copy(role = UserRole.COORDINATOR, domain = UserDomain.WEB, toMarkCount = 2, dayPart = DayPart.EVENING),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

// Sessions exist but none is marked, so there is no score yet.
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun MemberHomeNothingMarkedPreview() {
    InnogeeksTheme {
        MemberHomeScreen(
            state = previewMember.copy(overview = AttendanceOverviewUi(0, 0, 0, previewOverview.sessions.take(1)), domain = UserDomain.ML),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun MemberHomeNewMemberPreview() {
    InnogeeksTheme {
        MemberHomeScreen(
            state = previewMember.copy(overview = AttendanceOverviewUi(0, 0, 0, emptyList()), domain = UserDomain.IOT),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun MemberHomeLoadingPreview() {
    InnogeeksTheme {
        MemberHomeScreen(state = MemberHomeState(name = "Atul", role = UserRole.MEMBER, domain = UserDomain.ANDROID), hazeState = HazeState(), onAction = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun MemberHomeAttendanceErrorPreview() {
    InnogeeksTheme {
        MemberHomeScreen(
            state = previewMember.copy(overview = null, attendanceError = UiText.StringResource(R.string.error_no_internet)),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}
