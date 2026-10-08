package com.example.innogeeks.feature_attendance.presentation.myattendance.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.innogeeks.core.presentation.components.liquidGlass
import com.example.innogeeks.feature_attendance.domain.model.AttendanceSummary
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState
import edu.kiet.innogeeks.R

// The one glass card: the score, how many were attended, and a tappable missed count.
@Composable
fun AttendanceSummaryCard(
    summary: AttendanceSummary,
    missedCount: Int,
    unmarkedCount: Int,
    hazeState: HazeState,
    onMissedClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val nothingMarked = summary.total == 0

    Row(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(hazeState = hazeState, cornerRadius = 20.dp)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (nothingMarked) "—" else "${summary.percent}%",
            style = MaterialTheme.typography.displayMedium,
            color = scheme.primary
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = if (nothingMarked) stringResource(R.string.attendance_nothing_marked)
                else stringResource(R.string.attendance_attended, summary.present, summary.total),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurface
            )
            if (missedCount > 0) {
                Row(
                    modifier = Modifier.clickable(onClick = onMissedClick),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.attendance_missed_count, missedCount),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = scheme.error
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = scheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            if (unmarkedCount > 0) {
                Text(
                    text = stringResource(R.string.attendance_pending_count, unmarkedCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.outline
                )
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AttendanceSummaryCardPreview() {
    InnogeeksTheme {
        AttendanceSummaryCard(
            summary = AttendanceSummary(total = 14, present = 9, percent = 64),
            missedCount = 5,
            unmarkedCount = 1,
            hazeState = HazeState(),
            onMissedClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AttendanceSummaryCardPerfectPreview() {
    InnogeeksTheme {
        AttendanceSummaryCard(
            summary = AttendanceSummary(total = 8, present = 8, percent = 100),
            missedCount = 0,
            unmarkedCount = 0,
            hazeState = HazeState(),
            onMissedClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AttendanceSummaryCardNothingMarkedPreview() {
    InnogeeksTheme {
        AttendanceSummaryCard(
            summary = AttendanceSummary(total = 0, present = 0, percent = 0),
            missedCount = 0,
            unmarkedCount = 2,
            hazeState = HazeState(),
            onMissedClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
