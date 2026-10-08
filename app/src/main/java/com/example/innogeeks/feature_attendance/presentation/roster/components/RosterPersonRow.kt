package com.example.innogeeks.feature_attendance.presentation.roster.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.innogeeks.feature_attendance.domain.model.AttendanceStatus
import com.example.innogeeks.feature_attendance.presentation.roster.RosterRowUi
import com.example.innogeeks.ui.theme.InnogeeksTheme
import edu.kiet.innogeeks.R

// Two explicit buttons per person (48x44dp), because a coordinator taps these standing in a room.
@Composable
fun RosterPersonRow(
    row: RosterRowUi,
    status: AttendanceStatus?,
    onMarkClick: (AttendanceStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(scheme.surfaceContainerHigh, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = row.initials,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurfaceVariant
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = row.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                RateLine(row)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MarkButton(
                    icon = Icons.Filled.Check,
                    label = stringResource(R.string.attendance_mark_present_cd, row.name),
                    isOn = status == AttendanceStatus.PRESENT,
                    onColor = scheme.primary,
                    onContentColor = scheme.onPrimary,
                    onBorder = scheme.primary,
                    onClick = { onMarkClick(AttendanceStatus.PRESENT) }
                )
                MarkButton(
                    icon = Icons.Filled.Close,
                    label = stringResource(R.string.attendance_mark_absent_cd, row.name),
                    isOn = status == AttendanceStatus.ABSENT,
                    onColor = scheme.error.copy(alpha = 0.16f),
                    onContentColor = scheme.error,
                    onBorder = scheme.error,
                    onClick = { onMarkClick(AttendanceStatus.ABSENT) }
                )
            }
        }
        HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.5f))
    }
}

@Composable
private fun RateLine(row: RosterRowUi) {
    val scheme = MaterialTheme.colorScheme
    val rate = row.attendancePercent
    val text = buildString {
        if (row.isCoordinator) append(stringResource(R.string.attendance_coordinator_role)).append(" · ")
        append(if (rate == null) stringResource(R.string.attendance_no_rate_yet) else stringResource(R.string.attendance_person_rate, rate))
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = if (row.isLowAttendance) scheme.error else scheme.outline
    )
}

@Composable
private fun MarkButton(
    icon: ImageVector,
    label: String,
    isOn: Boolean,
    onColor: Color,
    onContentColor: Color,
    onBorder: Color,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isOn) onColor else Color.Transparent,
        border = BorderStroke(1.5.dp, if (isOn) onBorder else scheme.outlineVariant),
        modifier = Modifier
            .size(width = 48.dp, height = 44.dp)
            .toggleable(value = isOn, role = Role.Checkbox, onValueChange = { onClick() })
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isOn) onContentColor else scheme.outline,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun previewRow(name: String, percent: Int?, coordinator: Boolean = false) =
    RosterRowUi("id-$name", name, name.split(' ').joinToString("") { it.first().toString() }, coordinator, percent)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RosterPersonRowPreview() {
    InnogeeksTheme {
        Column(modifier = Modifier.padding(16.dp).width(360.dp)) {
            RosterPersonRow(previewRow("Asha Verma", 100), status = null, onMarkClick = {})
            RosterPersonRow(previewRow("Neha Singh", 91), status = AttendanceStatus.PRESENT, onMarkClick = {})
            RosterPersonRow(previewRow("Rohan Gupta", 45), status = AttendanceStatus.ABSENT, onMarkClick = {})
            RosterPersonRow(previewRow("Priya Nair", 100, coordinator = true), status = null, onMarkClick = {})
            RosterPersonRow(previewRow("Maya Iyer", null), status = null, onMarkClick = {})
        }
    }
}
