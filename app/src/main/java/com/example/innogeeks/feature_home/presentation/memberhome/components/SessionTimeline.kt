package com.example.innogeeks.feature_home.presentation.memberhome.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.innogeeks.feature_home.presentation.memberhome.HomeSessionUi
import com.example.innogeeks.feature_home.presentation.memberhome.MarkStatus
import com.example.innogeeks.ui.theme.InnogeeksTheme
import edu.kiet.innogeeks.R

// A vertical line with one node per session; the node style repeats the mark style on the pass card.
@Composable
fun SessionTimeline(sessions: List<HomeSessionUi>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        sessions.forEachIndexed { index, session ->
            TimelineRow(session = session, isFirst = index == 0, isLast = index == sessions.lastIndex)
        }
    }
}

@Composable
private fun TimelineRow(session: HomeSessionUi, isFirst: Boolean, isLast: Boolean) {
    val line = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    val primary = MaterialTheme.colorScheme.primary
    val error = MaterialTheme.colorScheme.error
    val muted = MaterialTheme.colorScheme.outline
    val background = MaterialTheme.colorScheme.background

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(22.dp)
                .fillMaxHeight()
                .drawBehind {
                    val x = size.width / 2
                    val centerY = size.height / 2
                    if (!isFirst) drawLine(line, Offset(x, 0f), Offset(x, centerY), strokeWidth = 2.dp.toPx())
                    if (!isLast) drawLine(line, Offset(x, centerY), Offset(x, size.height), strokeWidth = 2.dp.toPx())
                    // A background-coloured disc hides the line behind the node.
                    drawCircle(background, radius = 9.dp.toPx(), center = Offset(x, centerY))
                    when (session.status) {
                        MarkStatus.PRESENT -> drawCircle(primary, radius = 6.dp.toPx(), center = Offset(x, centerY))
                        MarkStatus.MISSED -> drawCircle(error, radius = 5.dp.toPx(), center = Offset(x, centerY), style = Stroke(2.dp.toPx()))
                        MarkStatus.UNMARKED -> drawCircle(
                            muted, radius = 5.dp.toPx(), center = Offset(x, centerY),
                            style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f)))
                        )
                    }
                }
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = session.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (session.status == MarkStatus.UNMARKED) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = sessionMeta(session),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

// "30 Sep · Missed", with the status styled by state.
@Composable
private fun sessionMeta(session: HomeSessionUi) = buildAnnotatedString {
    val muted = MaterialTheme.colorScheme.outline
    withStyle(SpanStyle(color = muted)) { append(session.dateLabel + " · ") }
    when (session.status) {
        MarkStatus.PRESENT -> withStyle(SpanStyle(color = muted, fontWeight = FontWeight.Medium)) {
            append(stringResource(R.string.member_home_status_present))
        }
        MarkStatus.MISSED -> withStyle(SpanStyle(color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)) {
            append(stringResource(R.string.member_home_status_missed))
        }
        MarkStatus.UNMARKED -> withStyle(SpanStyle(color = muted, fontStyle = FontStyle.Italic)) {
            append(stringResource(R.string.member_home_status_unmarked))
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SessionTimelinePreview() {
    InnogeeksTheme { SessionTimeline(sessions = previewSessions.take(3), modifier = Modifier.padding(20.dp)) }
}
