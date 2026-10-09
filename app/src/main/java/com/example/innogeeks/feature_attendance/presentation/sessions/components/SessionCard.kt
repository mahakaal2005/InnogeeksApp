package com.example.innogeeks.feature_attendance.presentation.sessions.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.innogeeks.feature_attendance.presentation.sessions.SessionUi
import com.example.innogeeks.core.presentation.components.liquidGlass
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState
import edu.kiet.innogeeks.R

// A session that still needs marking is highlighted: it is the coordinator's to-do.
@Composable
fun SessionCard(session: SessionUi, hazeState: HazeState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val todo = session.needsMarking

    Column(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(hazeState = hazeState, cornerRadius = 16.dp)
            .then(if (todo) Modifier.background(scheme.primary.copy(alpha = 0.10f)) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Bottom) {
            Text(
                text = session.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(text = session.dateLabel, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
        }
        Row(
            modifier = Modifier.padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProgressBar(progress = session.progress, modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.attendance_marked_progress, session.markedCount, session.memberCount),
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant
            )
            Text(
                text = when {
                    session.markedCount == 0 -> stringResource(R.string.attendance_needs_marking)
                    todo -> stringResource(R.string.attendance_left_to_mark, session.memberCount - session.markedCount)
                    else -> stringResource(R.string.attendance_all_marked)
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (todo) FontWeight.Bold else FontWeight.Normal,
                color = if (todo) scheme.primary else scheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProgressBar(progress: Float, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .height(5.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(scheme.outlineVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .background(scheme.primary)
        )
    }
}

private fun previewSession(marked: Int) = SessionUi("s", "Weekly sync", "Wed 7 Oct", marked, 8)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SessionCardPreview() {
    InnogeeksTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SessionCard(previewSession(0), hazeState = HazeState(), onClick = {})
            SessionCard(previewSession(7), hazeState = HazeState(), onClick = {})
            SessionCard(previewSession(8), hazeState = HazeState(), onClick = {})
        }
    }
}
