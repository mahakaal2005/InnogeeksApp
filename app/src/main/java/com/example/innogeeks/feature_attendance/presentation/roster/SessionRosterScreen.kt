package com.example.innogeeks.feature_attendance.presentation.roster

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.innogeeks.core.presentation.ObserveAsEvents
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.core.presentation.components.liquidGlass
import com.example.innogeeks.feature_attendance.domain.model.AttendanceStatus
import com.example.innogeeks.feature_attendance.presentation.roster.components.RosterPersonRow
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch
import edu.kiet.innogeeks.R
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SessionRosterRoot(
    sessionId: String,
    hazeState: HazeState,
    onClose: () -> Unit,
    onSaved: (present: Int, absent: Int) -> Unit,
    viewModel: SessionRosterViewModel = koinViewModel(key = "roster-$sessionId", parameters = { parametersOf(sessionId) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is SessionRosterEvent.Saved -> onSaved(event.present, event.absent)
            SessionRosterEvent.Close -> onClose()
            is SessionRosterEvent.ShowMessage -> scope.launch {
                snackbarHostState.showSnackbar(event.message.asString(context))
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SessionRosterScreen(state = state, hazeState = hazeState, onAction = viewModel::onAction)
        // Sits above the sticky submit bar so a failed save is never hidden behind it.
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 96.dp, start = 16.dp, end = 16.dp)
        )
    }
}

@Composable
fun SessionRosterScreen(
    state: SessionRosterState,
    hazeState: HazeState,
    onAction: (SessionRosterAction) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    // Back with unsaved marks must ask first, so it goes through the ViewModel.
    BackHandler { onAction(SessionRosterAction.OnBackClick) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .hazeSource(hazeState),
            // Bottom padding keeps the last row clear of the sticky bar.
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 130.dp)
        ) {
            item {
                BackRow(onClick = { onAction(SessionRosterAction.OnBackClick) })
                Text(
                    text = state.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface
                )
                Text(
                    text = state.dateLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            when {
                state.isLoading -> item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 64.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                state.error != null -> item {
                    RosterError(message = state.error, onRetryClick = { onAction(SessionRosterAction.OnRetryClick) })
                }

                else -> {
                    item { RosterTools(state = state, onAction = onAction) }
                    val visible = state.visibleRows
                    if (visible.isEmpty()) {
                        item { NoResults(state) }
                    } else {
                        items(visible, key = { it.accountId }) { row ->
                            RosterPersonRow(
                                row = row,
                                status = state.marks[row.accountId],
                                onMarkClick = { onAction(SessionRosterAction.OnMarkClick(row.accountId, it)) }
                            )
                        }
                    }
                }
            }
        }

        if (!state.isLoading && state.error == null) {
            SubmitBar(
                state = state,
                hazeState = hazeState,
                onSubmitClick = { onAction(SessionRosterAction.OnSubmitClick) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    if (state.showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { onAction(SessionRosterAction.OnDiscardDismiss) },
            title = { Text(stringResource(R.string.attendance_discard_title)) },
            text = { Text(stringResource(R.string.attendance_discard_body)) },
            confirmButton = {
                TextButton(onClick = { onAction(SessionRosterAction.OnDiscardConfirm) }) {
                    Text(stringResource(R.string.attendance_discard), color = scheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(SessionRosterAction.OnDiscardDismiss) }) {
                    Text(stringResource(R.string.attendance_keep_editing))
                }
            }
        )
    }
}

@Composable
private fun BackRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Text(
            text = stringResource(R.string.attendance_sessions_back),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RosterTools(state: SessionRosterState, onAction: (SessionRosterAction) -> Unit) {
    val scheme = MaterialTheme.colorScheme

    Column {
        RosterSearchField(
            query = state.query,
            onQueryChange = { onAction(SessionRosterAction.OnQueryChange(it)) },
            onClear = { onAction(SessionRosterAction.OnClearQueryClick) },
            modifier = Modifier.padding(top = 14.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = state.filter == RosterFilter.ALL,
                    onClick = { onAction(SessionRosterAction.OnFilterChange(RosterFilter.ALL)) },
                    label = { Text(stringResource(R.string.attendance_filter_all)) }
                )
                FilterChip(
                    selected = state.filter == RosterFilter.UNMARKED,
                    onClick = { onAction(SessionRosterAction.OnFilterChange(RosterFilter.UNMARKED)) },
                    label = { Text(stringResource(R.string.attendance_filter_unmarked, state.unmarkedCount)) }
                )
                FilterChip(
                    selected = state.filter == RosterFilter.ABSENT,
                    onClick = { onAction(SessionRosterAction.OnFilterChange(RosterFilter.ABSENT)) },
                    label = { Text(stringResource(R.string.attendance_filter_absent, state.absentCount)) }
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.attendance_sorted_note),
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant
            )
            // Hidden while searching, so one tap on a single name can never mark the whole domain.
            if (state.query.isBlank()) {
                TextButton(onClick = { onAction(SessionRosterAction.OnMarkAllPresentClick) }) {
                    Text(
                        text = stringResource(R.string.attendance_mark_all_present),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun RosterSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(scheme.surfaceContainer)
            .padding(start = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = scheme.outline, modifier = Modifier.size(18.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = stringResource(R.string.attendance_search_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = scheme.onSurface),
                cursorBrush = SolidColor(scheme.primary),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (query.isNotEmpty()) {
            IconButton(onClick = onClear) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(R.string.attendance_clear_search),
                    tint = scheme.outline,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun NoResults(state: SessionRosterState) {
    val text = when {
        state.query.isNotBlank() -> stringResource(R.string.attendance_no_match, state.query.trim())
        state.filter == RosterFilter.UNMARKED -> stringResource(R.string.attendance_everyone_marked)
        else -> stringResource(R.string.attendance_nobody_absent)
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp)
    )
}

@Composable
private fun RosterError(message: UiText, onRetryClick: () -> Unit) {
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

// Sticky glass bar: progress on the left, one primary action on the right.
@Composable
private fun SubmitBar(
    state: SessionRosterState,
    hazeState: HazeState,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val total = state.rows.size
    val progress = if (total == 0) 0f else state.markedCount.toFloat() / total

    Row(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .fillMaxWidth()
            .liquidGlass(hazeState = hazeState, cornerRadius = 20.dp)
            .padding(start = 18.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (state.isEditing && state.changedCount > 0) {
                    stringResource(R.string.attendance_roster_progress_changed, state.markedCount, total, state.changedCount)
                } else {
                    stringResource(R.string.attendance_roster_progress, state.markedCount, total)
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface
            )
            Box(
                modifier = Modifier
                    .padding(top = 7.dp)
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(scheme.outlineVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .height(5.dp)
                        .background(scheme.primary)
                )
            }
        }
        Button(
            onClick = onSubmitClick,
            enabled = state.changedCount > 0 && !state.isSubmitting,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.height(46.dp)
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text(
                    text = stringResource(if (state.isEditing) R.string.attendance_save_changes else R.string.attendance_submit),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun row(name: String, percent: Int?, coordinator: Boolean = false) = RosterRowUi(
    accountId = name,
    name = name,
    initials = name.split(' ').joinToString("") { it.first().toString() },
    isCoordinator = coordinator,
    attendancePercent = percent
)

private val previewRows = listOf(
    row("Asha Verma", 100), row("Priya Nair", 100, coordinator = true), row("Neha Singh", 91),
    row("Karan Mehta", 82), row("Isha Rao", 73), row("Aman Joshi", 64), row("Rohan Gupta", 45), row("Dev Patel", 30)
)

private fun marks(vararg pairs: Pair<String, AttendanceStatus>): Map<String, AttendanceStatus?> =
    previewRows.associate { it.accountId to null } + pairs

private val previewNew = SessionRosterState(
    isLoading = false,
    title = "Hack night",
    dateLabel = "Wed 7 Oct",
    rows = previewRows,
    marks = marks(),
    originalMarks = marks()
)

private val saved = marks(*previewRows.map { it.accountId to AttendanceStatus.PRESENT }.toTypedArray())

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun SessionRosterFirstMarkingPreview() {
    InnogeeksTheme {
        SessionRosterScreen(
            state = previewNew.copy(
                marks = marks("Asha Verma" to AttendanceStatus.PRESENT, "Priya Nair" to AttendanceStatus.PRESENT, "Neha Singh" to AttendanceStatus.PRESENT)
            ),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun SessionRosterEditingPreview() {
    InnogeeksTheme {
        SessionRosterScreen(
            state = previewNew.copy(
                title = "Weekly sync",
                dateLabel = "Wed 16 Sep",
                originalMarks = saved,
                marks = saved + mapOf("Rohan Gupta" to AttendanceStatus.ABSENT, "Dev Patel" to AttendanceStatus.ABSENT)
            ),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun SessionRosterSearchPreview() {
    InnogeeksTheme {
        SessionRosterScreen(state = previewNew.copy(query = "ro"), hazeState = HazeState(), onAction = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun SessionRosterNoMatchPreview() {
    InnogeeksTheme {
        SessionRosterScreen(state = previewNew.copy(query = "zed"), hazeState = HazeState(), onAction = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun SessionRosterEveryoneMarkedPreview() {
    InnogeeksTheme {
        SessionRosterScreen(
            state = previewNew.copy(filter = RosterFilter.UNMARKED, marks = saved, originalMarks = saved),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun SessionRosterDiscardPreview() {
    InnogeeksTheme {
        SessionRosterScreen(
            state = previewNew.copy(marks = marks("Asha Verma" to AttendanceStatus.PRESENT), showDiscardDialog = true),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun SessionRosterLoadingPreview() {
    InnogeeksTheme {
        SessionRosterScreen(state = SessionRosterState(isLoading = true), hazeState = HazeState(), onAction = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun SessionRosterErrorPreview() {
    InnogeeksTheme {
        SessionRosterScreen(
            state = SessionRosterState(isLoading = false, error = UiText.StringResource(R.string.error_no_internet)),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}
