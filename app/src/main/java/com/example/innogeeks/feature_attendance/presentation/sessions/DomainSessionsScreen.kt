package com.example.innogeeks.feature_attendance.presentation.sessions

import android.content.res.Configuration
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.innogeeks.core.domain.model.UserDomain
import com.example.innogeeks.core.presentation.ObserveAsEvents
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.core.presentation.components.SectionLabel
import com.example.innogeeks.feature_attendance.presentation.components.AttendanceHeader
import com.example.innogeeks.feature_attendance.presentation.sessions.components.CreateSessionSheet
import com.example.innogeeks.feature_attendance.presentation.sessions.components.SessionCard
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import edu.kiet.innogeeks.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun DomainSessionsRoot(
    hazeState: HazeState,
    onOpenRoster: (sessionId: String) -> Unit,
    topContent: @Composable () -> Unit = {},
    viewModel: DomainSessionsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Coming back from a roster must refresh the marked counts.
    LaunchedEffect(Unit) { viewModel.onAction(DomainSessionsAction.OnScreenShown) }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is DomainSessionsEvent.OpenRoster -> onOpenRoster(event.sessionId)
        }
    }

    DomainSessionsScreen(state = state, hazeState = hazeState, onAction = viewModel::onAction, topContent = topContent)

    if (state.isCreateSheetOpen) {
        CreateSessionSheet(
            title = state.createTitle,
            date = state.createDate,
            isCreating = state.isCreating,
            error = state.createError,
            onTitleChange = { viewModel.onAction(DomainSessionsAction.OnTitleChange(it)) },
            onDateChange = { viewModel.onAction(DomainSessionsAction.OnDateChange(it)) },
            onCreateClick = { viewModel.onAction(DomainSessionsAction.OnCreateClick) },
            onDismiss = { viewModel.onAction(DomainSessionsAction.OnSheetDismiss) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DomainSessionsScreen(
    state: DomainSessionsState,
    hazeState: HazeState,
    onAction: (DomainSessionsAction) -> Unit,
    topContent: @Composable () -> Unit = {}
) {
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { onAction(DomainSessionsAction.OnRefresh) },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .hazeSource(hazeState),
            // Bottom padding keeps the last card clear of the floating glass nav.
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 110.dp)
        ) {
            item { AttendanceHeader(domain = state.domain) }
            item { topContent() }

            when {
                state.isLoading -> item { SessionsSkeleton() }

                state.error != null -> item {
                    SessionsError(message = state.error, onRetryClick = { onAction(DomainSessionsAction.OnRetryClick) })
                }

                state.hasLoaded -> {
                    item { NewSessionButton(onClick = { onAction(DomainSessionsAction.OnNewSessionClick) }) }

                    if (state.sessions.isEmpty()) {
                        item { SessionsEmpty() }
                    } else {
                        val toMark = state.toMark
                        if (toMark.isNotEmpty()) {
                            item(key = "to-mark-header") { SectionHeader(stringResource(R.string.attendance_to_mark), toMark.size) }
                            items(toMark, key = { it.id }) { session ->
                                SessionCard(
                                    session = session,
                                    onClick = { onAction(DomainSessionsAction.OnSessionClick(session.id)) },
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }
                        val marked = state.marked
                        if (marked.isNotEmpty()) {
                            item(key = "marked-header") { SectionHeader(stringResource(R.string.attendance_marked_section), null) }
                            items(marked, key = { it.id }) { session ->
                                SessionCard(
                                    session = session,
                                    onClick = { onAction(DomainSessionsAction.OnSessionClick(session.id)) },
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewSessionButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .height(48.dp)
    ) {
        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Text(
            text = stringResource(R.string.attendance_new_session),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun SectionHeader(title: String, count: Int?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SectionLabel(text = title)
        if (count != null) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SessionsEmpty() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, start = 12.dp, end = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = stringResource(R.string.attendance_no_sessions_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.attendance_sessions_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SessionsError(message: UiText, onRetryClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
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

// Same shape as the loaded list so nothing jumps when data arrives.
@Composable
private fun SessionsSkeleton() {
    val pulse by rememberInfiniteTransition(label = "sessionsSkeleton").animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse"
    )

    Column(modifier = Modifier.alpha(pulse)) {
        SkeletonBlock(Modifier.padding(top = 16.dp).fillMaxWidth().height(48.dp), 14.dp)
        SkeletonBlock(Modifier.padding(top = 28.dp, bottom = 12.dp).width(70.dp).height(10.dp), 5.dp)
        repeat(3) {
            SkeletonBlock(Modifier.padding(bottom = 8.dp).fillMaxWidth().height(78.dp), 16.dp)
        }
    }
}

@Composable
private fun SkeletonBlock(modifier: Modifier, radius: Dp) {
    Box(modifier = modifier.clip(RoundedCornerShape(radius)).background(MaterialTheme.colorScheme.surfaceContainerHigh))
}

private val previewSessions = listOf(
    SessionUi("1", "Hack night", "Wed 7 Oct", 0, 8),
    SessionUi("4", "Weekly sync", "Wed 16 Sep", 7, 8),
    SessionUi("2", "Design teardown", "Wed 30 Sep", 8, 8),
    SessionUi("3", "Play Store prep", "Wed 23 Sep", 8, 8)
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun DomainSessionsLoadedPreview() {
    InnogeeksTheme {
        DomainSessionsScreen(
            state = DomainSessionsState(hasLoaded = true, sessions = previewSessions, domain = UserDomain.ANDROID),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun DomainSessionsAllMarkedPreview() {
    InnogeeksTheme {
        DomainSessionsScreen(
            state = DomainSessionsState(hasLoaded = true, sessions = previewSessions.drop(2), domain = UserDomain.WEB),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun DomainSessionsEmptyPreview() {
    InnogeeksTheme {
        DomainSessionsScreen(
            state = DomainSessionsState(hasLoaded = true, domain = UserDomain.ML),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun DomainSessionsLoadingPreview() {
    InnogeeksTheme {
        DomainSessionsScreen(
            state = DomainSessionsState(isLoading = true, domain = UserDomain.IOT),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun DomainSessionsErrorPreview() {
    InnogeeksTheme {
        DomainSessionsScreen(
            state = DomainSessionsState(error = UiText.StringResource(R.string.error_no_internet), domain = UserDomain.AR_VR),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}
