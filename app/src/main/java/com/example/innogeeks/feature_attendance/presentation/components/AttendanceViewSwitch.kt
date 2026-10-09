package com.example.innogeeks.feature_attendance.presentation.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.innogeeks.core.presentation.components.liquidGlass
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState
import edu.kiet.innogeeks.R

enum class AttendanceView { MINE, DOMAIN }

// Coordinators and admins flip between their own attendance and the domain sessions they manage.
@Composable
fun AttendanceViewSwitch(
    selected: AttendanceView,
    hazeState: HazeState,
    onSelect: (AttendanceView) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .liquidGlass(hazeState = hazeState, cornerRadius = 14.dp)
            .padding(4.dp)
    ) {
        SwitchOption(
            text = stringResource(R.string.attendance_view_mine),
            isSelected = selected == AttendanceView.MINE,
            onClick = { onSelect(AttendanceView.MINE) },
            modifier = Modifier.weight(1f)
        )
        SwitchOption(
            text = stringResource(R.string.attendance_view_domain),
            isSelected = selected == AttendanceView.DOMAIN,
            onClick = { onSelect(AttendanceView.DOMAIN) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SwitchOption(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(10.dp))
            // Same selected pill as the glass nav bar.
            .background(if (isSelected) scheme.primary.copy(alpha = 0.20f) else Color.Transparent)
            .selectable(selected = isSelected, role = Role.Tab, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) scheme.primary else scheme.onSurfaceVariant
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AttendanceViewSwitchPreview() {
    InnogeeksTheme {
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
            AttendanceViewSwitch(selected = AttendanceView.MINE, hazeState = HazeState(), onSelect = {})
            AttendanceViewSwitch(selected = AttendanceView.DOMAIN, hazeState = HazeState(), onSelect = {})
        }
    }
}
