package com.example.innogeeks.core.presentation.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.innogeeks.ui.theme.InnogeeksTheme

val PassCardShape = RoundedCornerShape(26.dp)

// A primary-tinted corner fading into the theme's own surface and background.
@Composable
fun passCardGradient(): List<Color> {
    val scheme = MaterialTheme.colorScheme
    return listOf(scheme.primary.copy(alpha = 0.16f).compositeOver(scheme.surface), scheme.surfaceContainerLow, scheme.background)
}

// Shared surface of the club ID cards on Home and Profile.
@Composable
fun Modifier.passCardSurface(shape: Shape = PassCardShape): Modifier {
    val primary = MaterialTheme.colorScheme.primary
    return this
        .clip(shape)
        .background(Brush.linearGradient(passCardGradient()))
        .border(1.dp, primary.copy(alpha = 0.35f), shape)
}

@Composable
fun DashedDivider(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .drawBehind {
                drawLine(
                    color = color,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                )
            }
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PassCardSurfacePreview() {
    InnogeeksTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.fillMaxWidth().passCardSurface().padding(20.dp)) {
                Text(text = "Pass card", color = MaterialTheme.colorScheme.onSurface)
                DashedDivider(modifier = Modifier.padding(vertical = 12.dp))
                Text(text = "Below the divider", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
