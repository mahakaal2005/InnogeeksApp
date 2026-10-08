package com.example.innogeeks.feature_attendance.presentation.myattendance.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.innogeeks.core.presentation.components.liquidGlass
import com.example.innogeeks.feature_attendance.domain.model.AttendanceSummary
import com.example.innogeeks.feature_attendance.presentation.myattendance.AttendanceRecordUi
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState
import edu.kiet.innogeeks.R

private const val STRIP_SIZE = 6

// The one glass card: big score, a strip of the last sessions, and a one-line note.
@Composable
fun AttendanceHero(
    summary: AttendanceSummary,
    records: List<AttendanceRecordUi>, // newest first
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val isEmpty = summary.total == 0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(hazeState = hazeState, cornerRadius = 24.dp)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = if (isEmpty) "—" else "${summary.percent}%",
                style = MaterialTheme.typography.displayLarge,
                color = scheme.primary
            )
            Text(
                text = if (isEmpty) stringResource(R.string.attendance_no_sessions_title)
                else stringResource(R.string.attendance_sessions_count, summary.present, summary.total),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }

        AttendanceStrip(records = records, modifier = Modifier.padding(top = 20.dp))

        if (!isEmpty && records.isNotEmpty()) {
            AttendanceNote(records = records, modifier = Modifier.padding(top = 16.dp))
        }
    }
}

// Up to the last 6 sessions, oldest to newest, so the newest sits at the right edge.
@Composable
private fun AttendanceStrip(records: List<AttendanceRecordUi>, modifier: Modifier = Modifier) {
    val recent = records.take(STRIP_SIZE).reversed()
    val presentLabel = stringResource(R.string.attendance_present)
    val missedLabel = stringResource(R.string.attendance_missed)

    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        // Empty history shows 6 outlined placeholders instead of nothing.
        if (recent.isEmpty()) {
            repeat(STRIP_SIZE) { StripBlock(record = null, label = "", modifier = Modifier.weight(1f)) }
        } else {
            recent.forEach { record ->
                val status = if (record.isPresent) presentLabel else missedLabel
                StripBlock(
                    record = record,
                    label = stringResource(R.string.attendance_strip_cd, record.shortDate, status),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StripBlock(record: AttendanceRecordUi?, label: String, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(10.dp)

    Column(
        modifier = modifier.clearAndSetSemantics { if (label.isNotEmpty()) contentDescription = label },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(shape)
                .then(
                    when {
                        record == null -> Modifier.border(1.dp, scheme.outlineVariant, shape)
                        record.isPresent -> Modifier.background(scheme.primary.copy(alpha = 0.85f))
                        else -> Modifier.border(1.5.dp, scheme.error, shape)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (record != null && !record.isPresent) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = null,
                    tint = scheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Text(
            text = record?.shortDate ?: " ",
            style = MaterialTheme.typography.labelSmall,
            color = scheme.outline,
            maxLines = 1,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun AttendanceNote(records: List<AttendanceRecordUi>, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val lastMissed = records.firstOrNull { !it.isPresent }
    val head = stringResource(
        if (records.first().isPresent) R.string.attendance_last_present else R.string.attendance_last_missed_session
    )
    val tail = if (lastMissed == null) {
        stringResource(R.string.attendance_no_misses)
    } else {
        stringResource(R.string.attendance_last_missed_on, lastMissed.shortDate)
    }

    Text(
        text = buildAnnotatedString {
            append("$head ")
            // The date is highlighted inside the sentence so a miss reads at a glance.
            val at = if (lastMissed == null) -1 else tail.indexOf(lastMissed.shortDate)
            if (at < 0) {
                append(tail)
            } else {
                append(tail.substring(0, at))
                withStyle(SpanStyle(color = scheme.error, fontWeight = FontWeight.Bold)) { append(lastMissed!!.shortDate) }
                append(tail.substring(at + lastMissed!!.shortDate.length))
            }
        },
        style = MaterialTheme.typography.bodyMedium,
        color = scheme.onSurfaceVariant,
        modifier = modifier
    )
}

internal fun previewRecords(vararg present: Boolean): List<AttendanceRecordUi> =
    present.mapIndexed { i, isPresent ->
        AttendanceRecordUi(
            sessionId = "s$i",
            title = "Weekly sync #${present.size - i}",
            isPresent = isPresent,
            dayOfMonth = "${7 - i}",
            weekday = "Wed",
            shortDate = "${7 - i} Oct",
            monthLabel = "October 2026"
        )
    }

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AttendanceHeroMixedPreview() {
    InnogeeksTheme {
        AttendanceHero(
            summary = AttendanceSummary(total = 6, present = 4, percent = 66),
            records = previewRecords(true, false, true, true, true, false),
            hazeState = HazeState(),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AttendanceHeroAllPresentPreview() {
    InnogeeksTheme {
        AttendanceHero(
            summary = AttendanceSummary(total = 6, present = 6, percent = 100),
            records = previewRecords(true, true, true, true, true, true),
            hazeState = HazeState(),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AttendanceHeroLastMissedPreview() {
    InnogeeksTheme {
        AttendanceHero(
            summary = AttendanceSummary(total = 3, present = 2, percent = 66),
            records = previewRecords(false, true, true),
            hazeState = HazeState(),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AttendanceHeroEmptyPreview() {
    InnogeeksTheme {
        AttendanceHero(
            summary = AttendanceSummary(total = 0, present = 0, percent = 0),
            records = emptyList(),
            hazeState = HazeState(),
            modifier = Modifier.padding(16.dp)
        )
    }
}
