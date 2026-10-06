package com.example.innogeeks.feature_home.presentation.home.components

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.innogeeks.ui.theme.InnogeeksTheme

private const val TICKER_SPEED_DP_PER_SEC = 26f

// Mutable per-chip state driven by the frame loop. Not a data class — it is mutated in place.
private class ChipState(val text: String) {
    var x by mutableStateOf(0f)
    var width by mutableStateOf(0f)
    var ticked by mutableStateOf(false)
    // Last frame's past-center reading, so tick only flips on an actual crossing — never mid-wrap.
    var pastCenter by mutableStateOf(false)
    var wrapCount by mutableStateOf(0)
}

@Composable
fun KeywordTicker(
    rows: List<List<String>>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        rows.forEach { keywords ->
            TickerRow(keywords = keywords)
        }
    }
}

@Composable
private fun TickerRow(keywords: List<String>) {
    val density = LocalDensity.current
    var stripWidth by remember { mutableStateOf(0f) }

    val chips = remember(keywords) {
        mutableStateListOf<ChipState>().apply { addAll(keywords.map { ChipState(it) }) }
    }

    // Every chip must be measured before any can be positioned, since each sits after the last.
    val allMeasured = chips.isNotEmpty() && chips.all { it.width > 0f }
    var laidOut by remember(chips) { mutableStateOf(false) }
    // Full loop length (all chips + gaps). Wrapping shifts by this instead of an absolute
    // reset, so relative spacing between chips never drifts and they can't overlap.
    var contentWidth by remember(chips) { mutableStateOf(0f) }

    LaunchedEffect(chips, stripWidth, allMeasured) {
        if (stripWidth <= 0f || !allMeasured) return@LaunchedEffect

        if (!laidOut) {
            val gap = with(density) { 12.dp.toPx() }
            val start = with(density) { 14.dp.toPx() }
            var cursor = start
            chips.forEach { chip ->
                chip.x = cursor
                cursor += chip.width + gap
                // Seed real state at layout time — a chip can legitimately start past centre.
                val isPastCenter = (chip.x + chip.width / 2f) >= stripWidth / 2f
                chip.pastCenter = isPastCenter
                chip.ticked = isPastCenter
            }
            // Floor the wrap period so a wrapped chip reappears fully off-screen left (contentWidth >= x + chip.width).
            val maxChipWidth = chips.maxOfOrNull { it.width } ?: 0f
            contentWidth = maxOf(cursor - start, stripWidth + maxChipWidth + 25f)
            laidOut = true
        }

        val speed = with(density) { TICKER_SPEED_DP_PER_SEC.dp.toPx() }
        var lastFrame = 0L
        while (true) {
            withFrameNanos { now ->
                if (lastFrame == 0L) lastFrame = now
                val dt = (now - lastFrame) / 1_000_000_000f
                lastFrame = now

                chips.forEach { chip ->
                    chip.x += speed * dt
                    // Shift back by the full loop length so every chip keeps its original
                    // spacing relative to the others — never an absolute jump.
                    while (chip.x > stripWidth + 10f) {
                        chip.x -= contentWidth
                        chip.wrapCount++
                    }
                    // Tick on crossing the centre line and stay ticked until the chip wraps back before it, driven by position rather than the wrap event.
                    val centre = chip.x + chip.width / 2f
                    val isPastCenter = centre >= stripWidth / 2f
                    if (isPastCenter != chip.pastCenter) {
                        chip.ticked = isPastCenter
                        chip.pastCenter = isPastCenter
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(percent = 50)
            )
            .onSizeChanged { stripWidth = it.width.toFloat() }
    ) {
        // Faint centre marker the chips tick as they cross.
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(1.dp)
                .height(28.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
        )

        chips.forEach { chip ->
            androidx.compose.runtime.key(chip, chip.wrapCount) {
                TagChip(
                    text = chip.text,
                    ticked = chip.ticked,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        // Chips start off-screen left until the row has been laid out.
                        .offset { IntOffset(if (laidOut) chip.x.toInt() else -9999, 0) }
                        .onSizeChanged { chip.width = it.width.toFloat() }
                )
            }
        }
    }
}

@Composable
private fun TagChip(
    text: String,
    ticked: Boolean,
    modifier: Modifier = Modifier
) {
    val tickScale by animateFloatAsState(
        targetValue = if (ticked) 1f else 0.4f,
        animationSpec = tween(240),
        label = "tickScale"
    )
    val tickAlpha by animateFloatAsState(
        targetValue = if (ticked) 1f else 0f,
        animationSpec = tween(240),
        label = "tickAlpha"
    )
    val borderColor = if (ticked) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(1.dp, borderColor, RoundedCornerShape(percent = 50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurface
        )
        Box(
            modifier = Modifier
                .size(12.dp)
                .graphicsLayer {
                    scaleX = tickScale
                    scaleY = tickScale
                    alpha = tickAlpha
                }
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✓",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun KeywordTickerPreview() {
    InnogeeksTheme {
        KeywordTicker(
            rows = listOf(
                listOf("Technology", "Design", "Robotics"),
                listOf("Innovation", "Community", "Mentorship"),
                listOf("Hackathon", "Code", "Workshops")
            ),
            modifier = Modifier.padding(vertical = 12.dp)
        )
    }
}
