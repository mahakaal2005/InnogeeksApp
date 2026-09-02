package com.example.innogeeks.feature_recruitment.presentation.tracker

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.innogeeks.core.presentation.components.GlowBlob
import com.example.innogeeks.core.presentation.components.liquidGlass
import com.example.innogeeks.feature_recruitment.domain.model.Decision
import com.example.innogeeks.feature_recruitment.domain.model.Interview
import com.example.innogeeks.feature_recruitment.domain.model.RecruitmentStatus
import com.example.innogeeks.feature_recruitment.domain.model.TestSlot
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime

@Composable
fun TrackerRoot(
    hazeState: HazeState,
    onNavigateToResources: () -> Unit = {},
    viewModel: TrackerViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                TrackerEvent.NavigateToResources -> onNavigateToResources()
            }
        }
    }

    TrackerScreen(state = state, hazeState = hazeState, onAction = viewModel::onAction)
}

@Composable
fun TrackerScreen(
    state: TrackerState,
    hazeState: HazeState,
    onAction: (TrackerAction) -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    // A plain Column, not LazyColumn: this screen's content is a handful of fixed blocks, never
    // a long list, and the journey below needs Modifier.weight(1f) to fill the remaining screen
    // height (LazyColumn items can't do that — they size to content, leaving empty space below
    // on taller phones, which is what the original stacked-box layout also did unnoticed).
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .hazeSource(hazeState)
            .padding(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Recruitment Tracker",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface
        )

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.error != null -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = state.error.asString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.error,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = { onAction(TrackerAction.OnRetryClick) }) {
                        Text(text = "Retry")
                    }
                }
            }

            state.recruitmentStatus != null -> {
                val stages = state.recruitmentStatus.toJourneyStages()

                Text(
                    text = state.recruitmentStatus.statusLine(stages),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )

                JourneyStages(
                    stages = stages,
                    hazeState = hazeState,
                    modifier = Modifier.weight(1f)
                )

                if (state.recruitmentStatus.decisionNote != null) {
                    DecisionNoteCard(note = state.recruitmentStatus.decisionNote)
                }

                val decision = state.recruitmentStatus.decision
                if (decision == Decision.REJECTED || decision == Decision.WAITLISTED) {
                    NonSelectionCard(
                        decision = decision,
                        onBrowseResourcesClick = { onAction(TrackerAction.OnBrowseResourcesClick) }
                    )
                }
            }
        }
    }
}

// One node in the recruitment journey. `decision` is non-null only for the final Decision stage
// and drives its icon/color independent of the generic DONE/CURRENT/PENDING state below.
private enum class StageState { DONE, CURRENT, PENDING }

private data class JourneyStageUi(
    val title: String,
    val subtitle: String,
    val state: StageState,
    val icon: ImageVector,
    val decision: Decision? = null
)

private fun RecruitmentStatus.toJourneyStages(): List<JourneyStageUi> {
    val afterDecision = decision != Decision.PENDING

    // Registered and Fee Paid are never independently observable here: per
    // docs/APP_API_CONTRACT.md, a registration only becomes reachable by this app once it's
    // already PAID, so `paid` can never actually be false on a screen the app can render. Two
    // stages for one fact was redundant, so this is one merged stage, not two.
    val stages = mutableListOf(
        JourneyStageUi(
            title = "Registered",
            subtitle = "Application submitted & ₹50 fee verified",
            state = StageState.DONE,
            icon = Icons.Default.Description
        ),
        JourneyStageUi(
            title = "Aptitude Test",
            subtitle = when {
                afterDecision -> testSlot.startTime?.let { "Completed ${formatDateTime(it)}" } ?: "Completed"
                testSlot.booked -> "Slot booked: ${testSlot.startTime?.let { formatDateTime(it) } ?: "TBD"}"
                else -> "Test slot not booked yet"
            },
            state = if (afterDecision) StageState.DONE else StageState.PENDING,
            icon = Icons.Default.Schedule
        ),
        JourneyStageUi(
            title = "Interview",
            subtitle = when {
                afterDecision -> interview.startTime?.let { "Completed ${formatDateTime(it)}" } ?: "Completed"
                interview.assigned -> "Interview scheduled: ${interview.startTime?.let { formatDateTime(it) } ?: "TBD"}"
                else -> "Not scheduled yet"
            },
            state = if (afterDecision) StageState.DONE else StageState.PENDING,
            icon = Icons.Default.Groups
        )
    )

    val (decisionTitle, decisionSubtitle) = when (decision) {
        Decision.SELECTED -> "Selected" to "Congratulations! You're now a member."
        Decision.WAITLISTED -> "Waitlisted" to "You're on the waitlist. We'll notify you."
        Decision.REJECTED -> "Not selected" to "Thank you for applying."
        Decision.PENDING -> "Decision" to "Awaiting result"
    }
    stages += JourneyStageUi(
        title = decisionTitle,
        subtitle = decisionSubtitle,
        state = if (decision == Decision.SELECTED) StageState.DONE else StageState.PENDING,
        icon = Icons.Default.Flag,
        decision = decision
    )

    // The first unsettled stage in sequence is where the student's attention belongs right now.
    val currentIndex = stages.indexOfFirst { it.state == StageState.PENDING }
    return if (currentIndex >= 0) {
        stages.mapIndexed { index, stage ->
            if (index == currentIndex) stage.copy(state = StageState.CURRENT) else stage
        }
    } else {
        stages
    }
}

private fun RecruitmentStatus.statusLine(stages: List<JourneyStageUi>): String {
    val current = stages.firstOrNull { it.state == StageState.CURRENT }
    return when {
        decision == Decision.SELECTED -> "You're all set. Welcome to Innogeeks!"
        current != null -> "You're on track. Next up: ${current.title}."
        else -> "Here's where things stand."
    }
}

private val nodeAnchorSize = 24.dp

// A single vertical route through the recruitment stages. The connector line is drawn from each
// node's real measured position (onGloballyPositioned), never guessed coordinates, so it cannot
// misalign with the rows the way a hand-authored path could.
@Composable
private fun JourneyStages(
    stages: List<JourneyStageUi>,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val nodeCenters = remember { mutableStateMapOf<Int, Float>() }
    val currentIndex = stages.indexOfFirst { it.state == StageState.CURRENT }

    Box(modifier = modifier.fillMaxWidth()) {
        val lastSettledIndex = stages.indexOfLast { it.state != StageState.PENDING }
        val startY = nodeCenters[0]
        val doneY = nodeCenters[lastSettledIndex.coerceAtLeast(0)]

        // The primary segment draws itself in on entry rather than snapping in fully formed,
        // so the line reads as tracing the journey rather than just being there.
        val drawProgress = remember { Animatable(0f) }
        LaunchedEffect(startY, doneY) {
            if (startY != null && doneY != null) {
                drawProgress.snapTo(0f)
                drawProgress.animateTo(1f, animationSpec = tween(700))
            }
        }

        Canvas(modifier = Modifier.matchParentSize()) {
            val x = nodeAnchorSize.toPx() / 2
            val endY = nodeCenters[stages.lastIndex]
            if (startY != null && endY != null) {
                drawLine(
                    color = scheme.outlineVariant,
                    start = Offset(x, startY),
                    end = Offset(x, endY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            if (startY != null && doneY != null && doneY > startY) {
                val animatedDoneY = startY + (doneY - startY) * drawProgress.value
                drawLine(
                    color = scheme.primary,
                    start = Offset(x, startY),
                    end = Offset(x, animatedDoneY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // A soft light source that tracks whichever stage is current, distinct from the two
        // fixed app-wide glow blobs behind MainScaffold — this one is local to "you are here."
        val currentY = if (currentIndex >= 0) nodeCenters[currentIndex] else null
        if (currentY != null) {
            val density = LocalDensity.current
            GlowBlob(
                color = scheme.primary,
                modifier = Modifier
                    .size(160.dp)
                    .offset {
                        IntOffset(
                            x = with(density) { (-30).dp.roundToPx() },
                            y = (currentY - with(density) { 80.dp.toPx() }).toInt()
                        )
                    }
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            stages.forEachIndexed { index, stage ->
                JourneyStageRow(
                    stage = stage,
                    index = index,
                    hazeState = hazeState,
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        val bounds = coordinates.boundsInParent()
                        nodeCenters[index] = bounds.top + bounds.height / 2f
                    }
                )
            }
        }
    }
}

@Composable
private fun JourneyStageRow(
    stage: JourneyStageUi,
    index: Int,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val isCurrent = stage.state == StageState.CURRENT
    val isDone = stage.state == StageState.DONE

    val dotColor = when {
        stage.decision == Decision.REJECTED -> scheme.outline
        stage.decision == Decision.WAITLISTED -> scheme.secondary
        isDone || isCurrent -> scheme.primary
        else -> scheme.outlineVariant
    }

    // Rows stagger in on entry rather than appearing fully formed — same technique
    // DomainDetail's ProjectRow uses (slide in, staggered by index).
    var visible by remember(stage.title) { mutableStateOf(false) }
    LaunchedEffect(stage.title) {
        delay(80L * index)
        visible = true
    }
    val entranceOffset by animateFloatAsState(
        targetValue = if (visible) 0f else -24f,
        animationSpec = tween(300),
        label = "stageOffset"
    )
    val entranceAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300),
        label = "stageAlpha"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationX = entranceOffset
                alpha = entranceAlpha
            },
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(modifier = Modifier.size(nodeAnchorSize), contentAlignment = Alignment.Center) {
            // The pulsing ring signals "in progress" — suppressed for a terminal decision
            // (WAITLISTED/REJECTED can be "current" without anything actually being ongoing).
            if (isCurrent && stage.decision == null) {
                val transition = rememberInfiniteTransition(label = "currentRing")
                val ringScale by transition.animateFloat(
                    initialValue = 0.7f,
                    targetValue = 1.3f,
                    animationSpec = infiniteRepeatable(tween(2400, easing = LinearOutSlowInEasing)),
                    label = "ringScale"
                )
                val ringAlpha by transition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(tween(2400, easing = LinearOutSlowInEasing)),
                    label = "ringAlpha"
                )
                Box(
                    modifier = Modifier
                        .size(nodeAnchorSize)
                        .graphicsLayer(scaleX = ringScale, scaleY = ringScale, alpha = ringAlpha)
                        .border(1.5.dp, scheme.primary, CircleShape)
                )
            }
            Box(
                modifier = Modifier
                    .size(if (isCurrent) 22.dp else 18.dp)
                    .clip(CircleShape)
                    .background(dotColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = stage.icon,
                    contentDescription = null,
                    // dotColor is either a light/colored circle (primary/secondary/outline for a
                    // terminal decision) or, for a plain pending stage, the near-background
                    // outlineVariant — the icon needs the opposite contrast in each case.
                    tint = if (dotColor == scheme.outlineVariant) scheme.outline else scheme.background,
                    modifier = Modifier.size(if (isCurrent) 13.dp else 11.dp)
                )
            }
        }

        val textModifier = if (isCurrent) {
            Modifier
                .liquidGlass(hazeState = hazeState, cornerRadius = 16.dp)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        } else {
            Modifier.padding(top = 2.dp)
        }

        Column(modifier = textModifier) {
            Text(
                text = stage.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = when {
                    isCurrent -> scheme.primary
                    isDone -> scheme.onSurface
                    else -> scheme.onSurfaceVariant
                }
            )
            Text(
                text = stage.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun DecisionNoteCard(
    note: String,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(scheme.secondaryContainer)
            .border(1.dp, scheme.outlineVariant, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Note from Admin",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = scheme.onSecondaryContainer
        )
        Text(
            text = note,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSecondaryContainer
        )
    }
}

// "What happens now" card for a decision that isn't SELECTED — REJECTED/WAITLISTED users
// still see the progress stages above, but need a clear next step instead of a dead end.
@Composable
private fun NonSelectionCard(
    decision: Decision,
    onBrowseResourcesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    val icon: ImageVector
    val headline: String
    val message: String
    when (decision) {
        Decision.WAITLISTED -> {
            icon = Icons.Default.HourglassTop
            headline = "You're on the waitlist"
            message = "We'll notify you the moment a seat opens up — no action needed right now. " +
                "In the meantime, the Resources tab is open to you."
        }
        // REJECTED is terminal — MainScaffold routes those users straight to a Resources-first
        // tab layout and Tracker is never shown, so there's no card to render for it here.
        else -> return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(scheme.surfaceContainerHigh)
            .border(1.dp, scheme.outlineVariant, RoundedCornerShape(18.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = scheme.onSurfaceVariant,
            modifier = Modifier.size(40.dp)
        )
        Text(
            text = headline,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
            textAlign = TextAlign.Center
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        OutlinedButton(onClick = onBrowseResourcesClick) {
            Icon(
                imageVector = Icons.Default.FolderOpen,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(text = "Browse Resources", modifier = Modifier.padding(start = 8.dp))
        }
    }
}

private val trackerDateTimeFormat = LocalDateTime.Format {
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    dayOfMonth()
    chars(", ")
    amPmHour()
    char(':')
    minute()
    char(' ')
    amPmMarker("AM", "PM")
}

private fun formatDateTime(isoString: String): String {
    return try {
        val localDateTime = Instant.parse(isoString).toLocalDateTime(TimeZone.currentSystemDefault())
        trackerDateTimeFormat.format(localDateTime)
    } catch (_: Exception) {
        isoString // fallback to raw string if parsing fails
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun TrackerScreenLoadingPreview() {
    InnogeeksTheme {
        TrackerScreen(
            state = TrackerState(isLoading = true),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun TrackerScreenErrorPreview() {
    InnogeeksTheme {
        TrackerScreen(
            state = TrackerState(error = com.example.innogeeks.core.presentation.UiText.DynamicString("Network error")),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

// Login itself 403s unless the registration is already paid (docs/APP_API_CONTRACT.md), so
// `paid = false` can never actually reach this screen — every preview below is paid = true.
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun TrackerScreenPendingPreview() {
    InnogeeksTheme {
        TrackerScreen(
            state = TrackerState(
                recruitmentStatus = RecruitmentStatus(
                    paid = true,
                    decision = Decision.PENDING,
                    decisionNote = null,
                    testSlot = TestSlot(booked = false, startTime = null, endTime = null),
                    interview = Interview(assigned = false, startTime = null, endTime = null, location = null, meetingUrl = null)
                )
            ),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun TrackerScreenTestBookedPreview() {
    InnogeeksTheme {
        TrackerScreen(
            state = TrackerState(
                recruitmentStatus = RecruitmentStatus(
                    paid = true,
                    decision = Decision.PENDING,
                    decisionNote = null,
                    testSlot = TestSlot(
                        booked = true,
                        startTime = "2026-08-15T10:00:00Z",
                        endTime = "2026-08-15T11:00:00Z"
                    ),
                    interview = Interview(
                        assigned = true,
                        startTime = "2026-08-22T09:00:00Z",
                        endTime = "2026-08-22T09:30:00Z",
                        location = "Room 204, Innovation Block",
                        meetingUrl = null
                    )
                )
            ),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun TrackerScreenWaitlistedPreview() {
    InnogeeksTheme {
        TrackerScreen(
            state = TrackerState(
                recruitmentStatus = RecruitmentStatus(
                    paid = true,
                    decision = Decision.WAITLISTED,
                    decisionNote = null,
                    testSlot = TestSlot(
                        booked = true,
                        startTime = "2026-08-15T10:00:00Z",
                        endTime = "2026-08-15T11:00:00Z"
                    ),
                    interview = Interview(
                        assigned = true,
                        startTime = "2026-08-22T09:00:00Z",
                        endTime = "2026-08-22T09:30:00Z",
                        location = "Room 204, Innovation Block",
                        meetingUrl = null
                    )
                )
            ),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}

// No TrackerScreenRejectedPreview — REJECTED is a terminal decision MainScaffold now routes
// away from Tracker entirely (see MainScaffold.kt's TabMode.REJECTED), so this state is
// unreachable in the running app.

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 900)
@Composable
private fun TrackerScreenSelectedPreview() {
    InnogeeksTheme {
        TrackerScreen(
            state = TrackerState(
                recruitmentStatus = RecruitmentStatus(
                    paid = true,
                    decision = Decision.SELECTED,
                    decisionNote = "Great work on the test! Welcome to Innogeeks.",
                    testSlot = TestSlot(
                        booked = true,
                        startTime = "2026-08-15T10:00:00Z",
                        endTime = "2026-08-15T11:00:00Z"
                    ),
                    interview = Interview(
                        assigned = true,
                        startTime = "2026-08-22T09:00:00Z",
                        endTime = "2026-08-22T09:30:00Z",
                        location = "Room 204, Innovation Block",
                        meetingUrl = null
                    )
                )
            ),
            hazeState = HazeState(),
            onAction = {}
        )
    }
}
