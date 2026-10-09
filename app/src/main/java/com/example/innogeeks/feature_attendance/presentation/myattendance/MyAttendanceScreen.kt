package com.example.innogeeks.feature_attendance.presentation.myattendance

import android.content.res.Configuration
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.innogeeks.core.domain.model.UserDomain
import com.example.innogeeks.core.presentation.ObserveOnResume
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.core.presentation.components.SectionLabel
import com.example.innogeeks.core.presentation.mapper.toUiText
import com.example.innogeeks.feature_attendance.domain.model.AttendanceSummary
import com.example.innogeeks.feature_attendance.presentation.components.AttendanceHeader
import com.example.innogeeks.feature_attendance.presentation.myattendance.components.AttendanceMonthSection
import com.example.innogeeks.feature_attendance.presentation.myattendance.components.AttendanceSummaryCard
import com.example.innogeeks.feature_attendance.presentation.myattendance.components.AttendanceRecordRow
import com.example.innogeeks.feature_attendance.presentation.myattendance.components.previewRecord
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import edu.kiet.innogeeks.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun MyAttendanceRoot(
    hazeState: HazeState,
    topContent: @Composable () -> Unit = {},
    viewModel: MyAttendanceViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // The ViewModel outlives the tab, so coming back needs its own reload.
    ObserveOnResume { viewModel.onAction(MyAttendanceAction.OnScreenShown) }
    MyAttendanceScreen(state = state, hazeState = hazeState, onAction = viewModel::onAction, topContent = topContent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAttendanceScreen(
    state: MyAttendanceState,
    hazeState: HazeState,
    onAction: (MyAttendanceAction) -> Unit,
    topContent: @Composable () -> Unit = {} // e.g. the coordinator's view switch
) {
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { onAction(MyAttendanceAction.OnRefresh) },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .hazeSource(hazeState),
            // Bottom padding keeps the last row clear of the floating glass nav.
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 110.dp)
        ) {
            item { AttendanceHeader(domain = state.domain) }
            item { topContent() }

            when {
                state.isLoading -> item { AttendanceSkeleton() }

                state.error != null -> item {
                    AttendanceError(message = state.error, onRetryClick = { onAction(MyAttendanceAction.OnRetryClick) })
                }

                state.summary != null && state.records.isEmpty() -> item { AttendanceEmpty() }

                state.summary != null -> {
                    item {
                        AttendanceSummaryCard(
                            summary = state.summary,
                            missedCount = state.missedCount,
                            unmarkedCount = state.unmarkedCount,
                            hazeState = hazeState,
                            onMissedClick = { onAction(MyAttendanceAction.OnMissedClick) },
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                    if (state.showMissedOnly) {
                        missedSection(state, onAction)
                    } else {
                        historySection(state, onAction)
                    }
                }
            }
        }
    }
}

private fun LazyListScope.missedSection(state: MyAttendanceState, onAction: (MyAttendanceAction) -> Unit) {
    item(key = "missed-header") {
        SectionHeader(
            title = stringResource(R.string.attendance_missed_sessions),
            actionLabel = stringResource(R.string.attendance_show_all),
            onActionClick = { onAction(MyAttendanceAction.OnShowAllClick) }
        )
    }
    items(state.missedRecords, key = { it.sessionId }) { AttendanceRecordRow(it) }
}

private fun LazyListScope.historySection(state: MyAttendanceState, onAction: (MyAttendanceAction) -> Unit) {
    item(key = "recent-header") { SectionHeader(title = stringResource(R.string.attendance_recent)) }
    items(state.recent, key = { it.sessionId }) { AttendanceRecordRow(it) }

    val earlier = state.earlierMonths
    if (earlier.isNotEmpty()) {
        item(key = "earlier-header") { SectionHeader(title = stringResource(R.string.attendance_earlier)) }
        items(earlier, key = { it.label }) { month ->
            AttendanceMonthSection(month = month, onToggle = { onAction(MyAttendanceAction.OnMonthToggle(month.label)) })
        }
    }
    item(key = "footer") {
        Text(
            text = stringResource(R.string.attendance_wrong_mark),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 28.dp)
        )
    }
}

// SectionLabel plus an optional text action on the right.
@Composable
private fun SectionHeader(title: String, actionLabel: String? = null, onActionClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 26.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SectionLabel(text = title)
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
private fun AttendanceEmpty() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp, start = 12.dp, end = 12.dp),
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
            text = stringResource(R.string.attendance_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AttendanceError(message: UiText, onRetryClick: () -> Unit) {
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

// Same shape as the loaded screen so nothing jumps when data arrives.
@Composable
private fun AttendanceSkeleton() {
    val pulse = rememberInfiniteTransition(label = "attendanceSkeleton").animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse"
    )

    Column(modifier = Modifier.graphicsLayer { alpha = pulse.value }) {
        SkeletonBlock(modifier = Modifier.padding(top = 16.dp).fillMaxWidth().height(76.dp), radius = 20.dp)
        SkeletonBlock(modifier = Modifier.padding(top = 30.dp, bottom = 14.dp).width(70.dp).height(10.dp), radius = 5.dp)
        repeat(4) {
            Row(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SkeletonBlock(modifier = Modifier.fillMaxWidth(0.55f).height(14.dp), radius = 7.dp)
                SkeletonBlock(modifier = Modifier.width(40.dp).height(12.dp), radius = 6.dp)
            }
        }
    }
}

@Composable
private fun SkeletonBlock(modifier: Modifier, radius: Dp) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(radius))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    )
}

private val previewRecords = listOf(
    previewRecord("Hack night", RecordStatus.UNMARKED, "7 Oct"),
    previewRecord("Design teardown", RecordStatus.PRESENT, "30 Sep", "September 2026"),
    previewRecord("Play Store prep", RecordStatus.MISSED, "23 Sep", "September 2026"),
    previewRecord("Weekly sync", RecordStatus.PRESENT, "16 Sep", "September 2026"),
    previewRecord("Kickoff", RecordStatus.PRESENT, "9 Sep", "September 2026"),
    previewRecord("Compose layouts", RecordStatus.PRESENT, "2 Sep", "September 2026"),
    previewRecord("Kotlin basics", RecordStatus.MISSED, "26 Aug", "August 2026"),
    previewRecord("Setup day", RecordStatus.PRESENT, "19 Aug", "August 2026"),
    previewRecord("Welcome", RecordStatus.PRESENT, "12 Jul", "July 2026")
)

private val previewLoaded = MyAttendanceState(
    summary = AttendanceSummary(total = 8, present = 6, percent = 75),
    records = previewRecords,
    domain = UserDomain.ANDROID
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun MyAttendanceLoadedPreview() {
    InnogeeksTheme { MyAttendanceScreen(state = previewLoaded, hazeState = HazeState(), onAction = {}) }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 1100)
@Composable
private fun MyAttendanceMonthExpandedPreview() {
    InnogeeksTheme {
        MyAttendanceScreen(
            state = previewLoaded.copy(expandedMonths = setOf("September 2026")),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun MyAttendanceMissedOnlyPreview() {
    InnogeeksTheme {
        MyAttendanceScreen(state = previewLoaded.copy(showMissedOnly = true), hazeState = HazeState(), onAction = {})
    }
}

// Few sessions: no Earlier section at all.
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun MyAttendanceFewSessionsPreview() {
    InnogeeksTheme {
        MyAttendanceScreen(
            state = MyAttendanceState(
                summary = AttendanceSummary(total = 2, present = 2, percent = 100),
                records = previewRecords.take(3).drop(1).map { it.copy(status = RecordStatus.PRESENT) },
                domain = UserDomain.WEB
            ),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

// Sessions exist but none is marked yet, so there is no score to show.
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun MyAttendanceNothingMarkedPreview() {
    InnogeeksTheme {
        MyAttendanceScreen(
            state = MyAttendanceState(
                summary = AttendanceSummary(total = 0, present = 0, percent = 0),
                records = previewRecords.take(1),
                domain = UserDomain.ML
            ),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun MyAttendanceEmptyPreview() {
    InnogeeksTheme {
        MyAttendanceScreen(
            state = MyAttendanceState(summary = AttendanceSummary(0, 0, 0), domain = UserDomain.IOT),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun MyAttendanceLoadingPreview() {
    InnogeeksTheme {
        MyAttendanceScreen(state = MyAttendanceState(isLoading = true, domain = UserDomain.ANDROID), hazeState = HazeState(), onAction = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun MyAttendanceErrorPreview() {
    InnogeeksTheme {
        MyAttendanceScreen(
            state = MyAttendanceState(
                error = UiText.StringResource(R.string.error_no_internet),
                domain = UserDomain.AR_VR
            ),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}
