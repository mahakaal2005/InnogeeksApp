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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.core.presentation.components.SectionLabel
import com.example.innogeeks.core.presentation.mapper.toUiText
import com.example.innogeeks.feature_attendance.domain.model.AttendanceSummary
import com.example.innogeeks.feature_attendance.presentation.myattendance.components.AttendanceHero
import com.example.innogeeks.feature_attendance.presentation.myattendance.components.AttendanceRecordRow
import com.example.innogeeks.feature_attendance.presentation.myattendance.components.AttendanceRowDivider
import com.example.innogeeks.feature_attendance.presentation.myattendance.components.previewRecords
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import edu.kiet.innogeeks.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun MyAttendanceRoot(
    hazeState: HazeState,
    viewModel: MyAttendanceViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MyAttendanceScreen(state = state, hazeState = hazeState, onAction = viewModel::onAction)
}

@Composable
fun MyAttendanceScreen(
    state: MyAttendanceState,
    hazeState: HazeState,
    onAction: (MyAttendanceAction) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .hazeSource(hazeState),
        // Bottom padding keeps the last row clear of the floating glass nav.
        contentPadding = PaddingValues(
            start = 24.dp, end = 24.dp, top = 8.dp, bottom = 110.dp
        )
    ) {
        item { AttendanceHeader(domain = state.domain) }

        when {
            state.isLoading -> item { AttendanceSkeleton() }

            state.error != null -> item {
                AttendanceError(message = state.error, onRetryClick = { onAction(MyAttendanceAction.OnRetryClick) })
            }

            state.summary != null -> {
                item {
                    AttendanceHero(
                        summary = state.summary,
                        records = state.records,
                        hazeState = hazeState,
                        modifier = Modifier.padding(top = 18.dp)
                    )
                }

                if (state.records.isEmpty()) {
                    item { AttendanceEmpty() }
                } else {
                    state.records.groupBy { it.monthLabel }.forEach { (month, records) ->
                        item(key = month) { SectionLabel(text = month, modifier = Modifier.padding(top = 26.dp, bottom = 6.dp)) }
                        items(records, key = { it.sessionId }) { record ->
                            Column {
                                AttendanceRecordRow(record)
                                if (record != records.last()) AttendanceRowDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceHeader(domain: UserDomain?) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.attendance_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface
        )
        if (domain != null) {
            Text(
                text = domain.toUiText().asString().uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.primary,
                modifier = Modifier
                    .border(1.dp, scheme.primary.copy(alpha = 0.4f), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
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
    val pulse by rememberInfiniteTransition(label = "attendanceSkeleton").animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse"
    )

    Column(modifier = Modifier.alpha(pulse)) {
        SkeletonBlock(modifier = Modifier.padding(top = 18.dp).fillMaxWidth().height(188.dp), radius = 24.dp)
        SkeletonBlock(modifier = Modifier.padding(top = 26.dp).width(90.dp).height(10.dp), radius = 5.dp)
        repeat(5) {
            Row(
                modifier = Modifier.fillMaxWidth().height(50.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SkeletonBlock(modifier = Modifier.size(width = 30.dp, height = 28.dp), radius = 8.dp)
                SkeletonBlock(modifier = Modifier.weight(1f).height(14.dp), radius = 7.dp)
                SkeletonBlock(modifier = Modifier.size(18.dp), radius = 9.dp)
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

private val previewMixed = MyAttendanceState(
    summary = AttendanceSummary(total = 6, present = 4, percent = 66),
    records = previewRecords(true, false, true, true, true, false),
    domain = UserDomain.ANDROID
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun MyAttendanceLoadedPreview() {
    InnogeeksTheme { MyAttendanceScreen(state = previewMixed, hazeState = HazeState(), onAction = {}) }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun MyAttendanceAllPresentPreview() {
    InnogeeksTheme {
        MyAttendanceScreen(
            state = MyAttendanceState(
                summary = AttendanceSummary(total = 6, present = 6, percent = 100),
                records = previewRecords(true, true, true, true, true, true),
                domain = UserDomain.WEB
            ),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

// 12 sessions over two months, to check the strip caps at 6 and the list groups by month.
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 1200)
@Composable
private fun MyAttendanceManySessionsPreview() {
    val october = previewRecords(true, true, false, true, true, true)
    val september = previewRecords(true, false, true, true, true, true).mapIndexed { i, record ->
        record.copy(
            sessionId = "sep$i",
            title = "Weekly sync #${6 - i}",
            dayOfMonth = "${30 - i * 7}",
            shortDate = "${30 - i * 7} Sep",
            monthLabel = "September 2026"
        )
    }
    InnogeeksTheme {
        MyAttendanceScreen(
            state = MyAttendanceState(
                summary = AttendanceSummary(total = 12, present = 10, percent = 83),
                records = october + september,
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
