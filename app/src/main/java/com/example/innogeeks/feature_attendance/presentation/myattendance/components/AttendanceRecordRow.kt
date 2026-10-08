package com.example.innogeeks.feature_attendance.presentation.myattendance.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.innogeeks.feature_attendance.presentation.myattendance.AttendanceRecordUi
import com.example.innogeeks.ui.theme.InnogeeksTheme
import edu.kiet.innogeeks.R

// Quiet by default: only a missed session is tinted.
@Composable
fun AttendanceRecordRow(record: AttendanceRecordUi, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val accent = if (record.isPresent) scheme.primary else scheme.error

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(modifier = Modifier.width(44.dp)) {
            Text(
                text = record.dayOfMonth,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface
            )
            Text(text = record.weekday, style = MaterialTheme.typography.labelSmall, color = scheme.outline)
        }
        Text(
            text = record.title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (record.isPresent) scheme.onSurface else scheme.error,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = if (record.isPresent) Icons.Filled.Check else Icons.Filled.Close,
            contentDescription = stringResource(
                if (record.isPresent) R.string.attendance_present else R.string.attendance_missed
            ),
            tint = accent,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun AttendanceRowDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AttendanceRecordRowPreview() {
    InnogeeksTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            val rows = previewRecords(true, false)
            AttendanceRecordRow(rows[0])
            AttendanceRowDivider()
            AttendanceRecordRow(rows[1])
        }
    }
}
