package com.example.innogeeks.feature_attendance.presentation.myattendance.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.innogeeks.feature_attendance.presentation.myattendance.AttendanceRecordUi
import com.example.innogeeks.feature_attendance.presentation.myattendance.RecordStatus
import com.example.innogeeks.ui.theme.InnogeeksTheme
import edu.kiet.innogeeks.R

// Designed for the exception: present rows stay plain, missed are tinted, unmarked are grey.
@Composable
fun AttendanceRecordRow(record: AttendanceRecordUi, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val isMissed = record.status == RecordStatus.MISSED
    val isUnmarked = record.status == RecordStatus.UNMARKED

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = if (isMissed) scheme.error.copy(alpha = 0.07f) else scheme.surface.copy(alpha = 0f),
                shape = RoundedCornerShape(14.dp)
            )
            .heightIn(min = 56.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = record.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (isUnmarked) scheme.onSurfaceVariant else scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            when (record.status) {
                RecordStatus.MISSED -> Text(
                    text = stringResource(R.string.attendance_missed),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.error
                )
                RecordStatus.UNMARKED -> Text(
                    text = stringResource(R.string.attendance_not_marked),
                    style = MaterialTheme.typography.labelMedium,
                    fontStyle = FontStyle.Italic,
                    color = scheme.outline
                )
                RecordStatus.PRESENT -> Unit
            }
        }
        Text(
            text = record.dateLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.outline
        )
    }
}

internal fun previewRecord(
    title: String,
    status: RecordStatus,
    dateLabel: String,
    monthLabel: String = "October 2026",
    id: String = title
) = AttendanceRecordUi(id, title, status, dateLabel, monthLabel)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AttendanceRecordRowPreview() {
    InnogeeksTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            AttendanceRecordRow(previewRecord("Hack night", RecordStatus.UNMARKED, "7 Oct"))
            AttendanceRecordRow(previewRecord("Design teardown", RecordStatus.PRESENT, "30 Sep"))
            AttendanceRecordRow(previewRecord("Play Store prep", RecordStatus.MISSED, "23 Sep"))
        }
    }
}
