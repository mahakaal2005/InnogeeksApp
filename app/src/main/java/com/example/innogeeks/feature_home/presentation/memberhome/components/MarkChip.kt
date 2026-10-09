package com.example.innogeeks.feature_home.presentation.memberhome.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.innogeeks.feature_home.presentation.memberhome.MarkStatus
import com.example.innogeeks.ui.theme.InnogeeksTheme

// One pill per session: filled when present, red outline when missed, dashed when not marked yet.
@Composable
fun MarkChip(status: MarkStatus, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(6.dp)
    val primary = MaterialTheme.colorScheme.primary
    val error = MaterialTheme.colorScheme.error
    val muted = MaterialTheme.colorScheme.outline
    val base = modifier.size(width = 16.dp, height = 30.dp)
    when (status) {
        MarkStatus.PRESENT -> Box(base.clip(shape).background(primary))
        MarkStatus.MISSED -> Box(base.border(2.dp, error, shape))
        MarkStatus.UNMARKED -> Box(
            base.drawBehind {
                drawRoundRect(
                    color = muted,
                    cornerRadius = CornerRadius(6.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 6f)))
                )
            }
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MarkChipPreview() {
    InnogeeksTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MarkStatus.entries.forEach { MarkChip(it) }
        }
    }
}
