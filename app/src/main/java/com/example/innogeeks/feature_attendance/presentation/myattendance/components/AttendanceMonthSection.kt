package com.example.innogeeks.feature_attendance.presentation.myattendance.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.innogeeks.feature_attendance.presentation.myattendance.MonthGroupUi
import com.example.innogeeks.feature_attendance.presentation.myattendance.RecordStatus
import com.example.innogeeks.ui.theme.InnogeeksTheme
import edu.kiet.innogeeks.R

// A collapsed month still shows how it went through its dots; the header keeps one shape open or closed.
@Composable
fun AttendanceMonthSection(
    month: MonthGroupUi,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val chevronAngle by animateFloatAsState(if (month.isExpanded) 180f else 0f, label = "chevron")
    val toggleLabel = stringResource(
        if (month.isExpanded) R.string.attendance_collapse_month else R.string.attendance_expand_month,
        month.label
    )

    Column(modifier = modifier.fillMaxWidth().animateContentSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clickable(onClickLabel = toggleLabel, role = Role.Button, onClick = onToggle),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = month.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            // Oldest to newest, left to right, so the dots read like the timeline.
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                month.records.reversed().forEach { record -> StatusDot(record.status) }
            }
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = scheme.outline,
                modifier = Modifier.size(20.dp).rotate(chevronAngle)
            )
        }
        AnimatedVisibility(visible = month.isExpanded) {
            Column(modifier = Modifier.padding(bottom = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                month.records.forEach { record -> AttendanceRecordRow(record) }
            }
        }
        HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.6f))
    }
}

@Composable
private fun StatusDot(status: RecordStatus) {
    val scheme = MaterialTheme.colorScheme
    val base = Modifier.size(10.dp)
    Box(
        modifier = when (status) {
            RecordStatus.PRESENT -> base.background(scheme.primary, CircleShape)
            RecordStatus.MISSED -> base.border(1.5.dp, scheme.error, CircleShape)
            RecordStatus.UNMARKED -> base.border(1.dp, scheme.outlineVariant, CircleShape)
        }
    )
}

private val previewMonth = MonthGroupUi(
    label = "September 2026",
    records = listOf(
        previewRecord("Design teardown", RecordStatus.PRESENT, "30 Sep", "September 2026"),
        previewRecord("Play Store prep", RecordStatus.MISSED, "23 Sep", "September 2026"),
        previewRecord("Weekly sync", RecordStatus.PRESENT, "16 Sep", "September 2026"),
        previewRecord("Kickoff", RecordStatus.PRESENT, "9 Sep", "September 2026")
    ),
    isExpanded = false
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AttendanceMonthSectionCollapsedPreview() {
    InnogeeksTheme {
        AttendanceMonthSection(month = previewMonth, onToggle = {}, modifier = Modifier.padding(16.dp))
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AttendanceMonthSectionExpandedPreview() {
    InnogeeksTheme {
        AttendanceMonthSection(month = previewMonth.copy(isExpanded = true), onToggle = {}, modifier = Modifier.padding(16.dp))
    }
}
