package com.example.innogeeks.feature_domains.presentation.domains.components

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.innogeeks.core.presentation.components.GlassIntensity
import com.example.innogeeks.core.presentation.components.liquidGlass
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// Each domain gets its own animated glyph, drawn on a frosted-glass stage.
@Composable
fun DomainSignatureIcon(
    domainId: String,
    accent: Color,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    height: Dp = 68.dp
) {
    val transition = rememberInfiniteTransition(label = "signature")
    // Single 0..1 driver reused by every glyph so they all share one clock.
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "signatureProgress"
    )

    val muted = MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .liquidGlass(hazeState = hazeState, cornerRadius = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        when (domainId) {
            "webd" -> CodeEditorSimulation(
                accent = accent,
                hazeState = hazeState,
                modifier = Modifier.fillMaxSize().padding(10.dp)
            )
            "appd" -> ChatAppSimulation(
                accent = accent,
                hazeState = hazeState,
                modifier = Modifier.fillMaxSize().padding(10.dp)
            )
            "arvr" -> ArPhoneSimulation(
                accent = accent,
                hazeState = hazeState,
                modifier = Modifier.fillMaxSize().padding(10.dp)
            )
            "ml" -> Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                drawNeuralNet(progress, accent, muted)
            }
            else -> IotNetworkSimulation(
                accent = accent,
                muted = muted,
                modifier = Modifier.fillMaxSize().padding(10.dp)
            )
        }
    }
}

// One highlighted token in the simulated code — a run of characters sharing a color.
private data class CodeToken(val text: String, val color: Color)

// VS Code Dark+ palette, since a code editor only reads as "real" with its familiar colors.
private object EditorPalette {
    val background = Color(0xFF1E1E1E)
    val gutter = Color(0xFF858585)
    val plain = Color(0xFFD4D4D4)
    val keyword = Color(0xFFC586C0)
    val function = Color(0xFFDCDCAA)
    val number = Color(0xFFB5CEA8)
    val tag = Color(0xFF569CD6)
    val attribute = Color(0xFF9CDCFE)
    val punctuation = Color(0xFF808080)
}

// A tiny React counter component — familiar enough to read as "real" web-dev code at a glance.
private val simulatedCodeLines: List<List<CodeToken>> = with(EditorPalette) {
    listOf(
        listOf(CodeToken("function ", keyword), CodeToken("Web", function), CodeToken("() {", plain)),
        listOf(
            CodeToken("  const ", keyword),
            CodeToken("[n, setN]", plain),
            CodeToken(" = ", plain),
            CodeToken("useState", function),
            CodeToken("(", punctuation),
            CodeToken("0", number),
            CodeToken(")", punctuation)
        ),
        listOf(
            CodeToken("  return ", keyword),
            CodeToken("<", punctuation),
            CodeToken("button", tag),
            CodeToken(" onClick", attribute),
            CodeToken("={", punctuation),
            CodeToken("() => setN(n + 1)", plain),
            CodeToken("}", punctuation),
            CodeToken(">", punctuation)
        ),
        listOf(CodeToken("    {n} clicks", plain)),
        listOf(CodeToken("  </", punctuation), CodeToken("button", tag), CodeToken(">", punctuation)),
        listOf(CodeToken("}", plain))
    )
}

private const val TYPING_CHARS_PER_SEC = 16f
private const val ERASING_CHARS_PER_SEC = 42f
private const val HOLD_DURATION_MS = 1600f
private const val CURSOR_BLINK_PERIOD_MS = 500f

private enum class TypingMode { TYPING, HOLD, ERASING }

// A VS Code-style window: traffic lights, a filename tab, a line-number gutter, and
// syntax-highlighted code that types itself out, holds, backspaces, then repeats.
@Composable
private fun CodeEditorSimulation(accent: Color, hazeState: HazeState, modifier: Modifier = Modifier) {
    val totalChars = remember { simulatedCodeLines.sumOf { line -> line.sumOf { it.text.length } + 1 } - 1 }

    var revealedChars by remember { mutableIntStateOf(0) }
    var mode by remember { mutableStateOf(TypingMode.TYPING) }
    var cursorVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        var charProgress = 0f
        var holdElapsed = 0f
        var blinkElapsed = 0f
        var lastFrame = 0L
        while (true) {
            withFrameNanos { now ->
                if (lastFrame == 0L) lastFrame = now
                val dtMs = (now - lastFrame) / 1_000_000f
                val dt = dtMs / 1000f
                lastFrame = now

                blinkElapsed += dtMs
                cursorVisible = mode != TypingMode.HOLD || (blinkElapsed % CURSOR_BLINK_PERIOD_MS) < CURSOR_BLINK_PERIOD_MS / 2f

                when (mode) {
                    TypingMode.TYPING -> {
                        charProgress += TYPING_CHARS_PER_SEC * dt
                        revealedChars = charProgress.toInt().coerceAtMost(totalChars)
                        if (revealedChars >= totalChars) {
                            mode = TypingMode.HOLD
                            holdElapsed = 0f
                            blinkElapsed = 0f
                        }
                    }
                    TypingMode.HOLD -> {
                        holdElapsed += dtMs
                        if (holdElapsed >= HOLD_DURATION_MS) mode = TypingMode.ERASING
                    }
                    TypingMode.ERASING -> {
                        charProgress -= ERASING_CHARS_PER_SEC * dt
                        revealedChars = charProgress.toInt().coerceAtLeast(0)
                        if (revealedChars <= 0) {
                            mode = TypingMode.TYPING
                            charProgress = 0f
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .liquidGlass(hazeState = hazeState, cornerRadius = 10.dp)
            // Dark tint on top of the glass blur so the editor still reads dark, just translucent.
            .background(EditorPalette.background.copy(alpha = 0.45f))
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            listOf(Color(0xFFFF5F56), Color(0xFFFFBD2E), Color(0xFF27C93F)).forEach { dot ->
                Box(
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(dot)
                )
            }
            Text(
                text = "Web.tsx",
                fontFamily = FontFamily.Monospace,
                fontSize = 7.sp,
                color = EditorPalette.gutter,
                modifier = Modifier.padding(start = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Column {
                simulatedCodeLines.indices.forEach { lineIndex ->
                    Text(
                        text = (lineIndex + 1).toString(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        lineHeight = 13.sp,
                        color = EditorPalette.gutter.copy(alpha = 0.6f),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
            Text(
                text = buildRevealedCode(revealedChars, cursorVisible, accent),
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                lineHeight = 13.sp
            )
        }
    }
}

// Walks the token list, appending characters up to the reveal budget, then a blinking
// cursor glyph right where typing left off.
private fun buildRevealedCode(revealedChars: Int, cursorVisible: Boolean, cursorColor: Color) =
    buildAnnotatedString {
        var remaining = revealedChars
        simulatedCodeLines.forEachIndexed { lineIndex, tokens ->
            if (lineIndex > 0) {
                if (remaining <= 0) return@forEachIndexed
                append('\n')
                remaining--
            }
            for (token in tokens) {
                if (remaining <= 0) break
                val take = token.text.length.coerceAtMost(remaining)
                withStyle(SpanStyle(color = token.color)) {
                    append(token.text.substring(0, take))
                }
                remaining -= take
            }
        }
        withStyle(SpanStyle(color = cursorColor.copy(alpha = if (cursorVisible) 1f else 0f), fontWeight = FontWeight.Bold)) {
            append('▏')
        }
    }

// One scripted chat line, kept short because the phone is only about 70dp wide.
private data class ChatLine(val text: String, val isOutgoing: Boolean)

private val chatScript = listOf(
    ChatLine("Hey! 👋", isOutgoing = false),
    ChatLine("Ready?", isOutgoing = true),
    ChatLine("Always 🚀", isOutgoing = false),
    ChatLine("Shipped it", isOutgoing = true),
    ChatLine("Testing", isOutgoing = false),
    ChatLine("Bug 😅", isOutgoing = true),
    ChatLine("On it", isOutgoing = false),
    ChatLine("Fixed ✅", isOutgoing = true)
)

private const val CHAT_STEP_MS = 950L
private const val CHAT_TYPING_MS = 800L
private const val CHAT_HOLD_MS = 1300L
private const val CHAT_RESET_MS = 400L

// A small glassmorphic phone playing an endless two-person chat that clears and restarts when the log fills.
@Composable
private fun ChatAppSimulation(accent: Color, hazeState: HazeState, modifier: Modifier = Modifier) {
    val messages = remember { mutableStateListOf<ChatLine>() }
    var typingVisible by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            messages.clear()
            typingVisible = false
            visible = true
            chatScript.forEachIndexed { index, line ->
                if (!line.isOutgoing && index > 0) {
                    typingVisible = true
                    delay(CHAT_TYPING_MS)
                    typingVisible = false
                } else {
                    delay(CHAT_STEP_MS)
                }
                messages.add(line)
            }
            delay(CHAT_HOLD_MS)
            visible = false
            delay(CHAT_RESET_MS)
        }
    }

    val contentAlpha by animateFloatAsState(targetValue = if (visible) 1f else 0f, animationSpec = tween(300), label = "chatFade")

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(0.62f)
                .liquidGlass(hazeState = hazeState, cornerRadius = 16.dp, intensity = GlassIntensity.REDUCED)
                // Dark tint over the glass blur so the phone reads as its own surface, not just more card.
                .background(Color.Black.copy(alpha = 0.3f))
                .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
                .graphicsLayer { alpha = contentAlpha }
                .padding(horizontal = 6.dp, vertical = 5.dp)
        ) {
            // Camera cutout / notch.
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.22f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.6f))
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("9:41", fontSize = 6.sp, lineHeight = 7.sp, color = Color.White.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace)
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(3) {
                        Box(modifier = Modifier.size(2.5.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.4f)))
                    }
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(accent))
                Text(
                    text = " Team Chat",
                    fontSize = 7.sp,
                    lineHeight = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(3.dp))

            // Bottom-anchored so old messages scroll off the top like a real chat, with a small padding buffer under the newest bubble.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clipToBounds()
                    .padding(bottom = 2.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                messages.forEachIndexed { index, line ->
                    key(index) { ChatBubble(line = line, accent = accent) }
                }
                if (typingVisible) TypingDots(accent = accent)
            }

            Spacer(modifier = Modifier.height(3.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(13.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.08f))
                )
                Box(
                    modifier = Modifier.size(13.dp).clip(CircleShape).background(accent),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size(5.dp).background(Color.White, RoundedCornerShape(1.dp)))
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            // Home indicator.
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 24.dp, height = 2.5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.3f))
            )
        }
    }
}

// Pops in with a bouncy scale once composed — since it's only ever added to the message list
// after its line "arrives," entering composition already IS the reveal moment.
@Composable
private fun ChatBubble(line: ChatLine, accent: Color, modifier: Modifier = Modifier) {
    // Starts false so the first frame animates in, because animateFloatAsState does not animate a value already at its target.
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { revealed = true }
    val scale by animateFloatAsState(
        targetValue = if (revealed) 1f else 0.6f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "bubbleScale"
    )

    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 1.5.dp),
        horizontalArrangement = if (line.isOutgoing) Arrangement.End else Arrangement.Start
    ) {
        Text(
            text = line.text,
            fontSize = 7.sp,
            lineHeight = 8.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (line.isOutgoing) Color.White else Color.White.copy(alpha = 0.85f),
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale; scaleY = scale
                    // Anchor growth to the bubble's bottom edge so a bubble at the log's bottom only grows upward.
                    transformOrigin = TransformOrigin(if (line.isOutgoing) 1f else 0f, 1f)
                }
                .clip(RoundedCornerShape(topStart = 7.dp, topEnd = 7.dp, bottomStart = if (line.isOutgoing) 7.dp else 2.dp, bottomEnd = if (line.isOutgoing) 2.dp else 7.dp))
                .background(if (line.isOutgoing) accent else Color.White.copy(alpha = 0.12f))
                .padding(horizontal = 5.dp, vertical = 2.5.dp)
        )
    }
}

@Composable
private fun TypingDots(accent: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "typingDots")
    val bounce by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(600, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "typingBounce"
    )

    Row(
        modifier = modifier
            .padding(vertical = 1.5.dp)
            .clip(RoundedCornerShape(topStart = 7.dp, topEnd = 7.dp, bottomStart = 2.dp, bottomEnd = 7.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(horizontal = 5.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        repeat(3) { index ->
            val local = (bounce - index * 0.2f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .size(2.5.dp)
                    .graphicsLayer { translationY = -local * 2f }
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.5f + local * 0.5f))
            )
        }
    }
}

private const val SCAN_PASS_MS = 900f
private const val SCAN_PASSES = 2
private const val AR_PLACED_HOLD_MS = 2000f

private enum class ArMode { SCANNING, PLACED }

// A small glassmorphic phone (same shell as ChatAppSimulation) running a mock AR furniture-placement app, modeled on IKEA Place and Houzz.
@Composable
private fun ArPhoneSimulation(accent: Color, hazeState: HazeState, modifier: Modifier = Modifier) {
    var mode by remember { mutableStateOf(ArMode.SCANNING) }
    var scanElapsed by remember { mutableFloatStateOf(0f) }
    var placedElapsed by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var lastFrame = 0L
        while (true) {
            withFrameNanos { now ->
                if (lastFrame == 0L) lastFrame = now
                val dtMs = (now - lastFrame) / 1_000_000f
                lastFrame = now

                when (mode) {
                    ArMode.SCANNING -> {
                        scanElapsed += dtMs
                        if (scanElapsed >= SCAN_PASS_MS * SCAN_PASSES) {
                            mode = ArMode.PLACED
                            placedElapsed = 0f
                        }
                    }
                    ArMode.PLACED -> {
                        placedElapsed += dtMs
                        if (placedElapsed >= AR_PLACED_HOLD_MS) {
                            mode = ArMode.SCANNING
                            scanElapsed = 0f
                        }
                    }
                }
            }
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(0.62f)
                .liquidGlass(hazeState = hazeState, cornerRadius = 16.dp, intensity = GlassIntensity.REDUCED)
                // Dark tint over the glass blur so the phone reads as its own surface, not just more card.
                .background(Color.Black.copy(alpha = 0.3f))
                .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
                .padding(horizontal = 6.dp, vertical = 5.dp)
        ) {
            // Camera cutout / notch.
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.22f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.6f))
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("9:41", fontSize = 6.sp, lineHeight = 7.sp, color = Color.White.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace)
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(3) {
                        Box(modifier = Modifier.size(2.5.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.4f)))
                    }
                }
            }
            Spacer(modifier = Modifier.height(3.dp))

            // The "camera feed" — a near-black tint standing in for a live viewfinder, with the
            // scan brackets/dot-grid/placed object drawn on top.
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF120E0A))
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                    drawArScan(mode, scanElapsed, placedElapsed, accent)
                }
            }

            Spacer(modifier = Modifier.height(3.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    listOf(accent, Color.White.copy(alpha = 0.5f), Color.White.copy(alpha = 0.25f)).forEachIndexed { index, swatch ->
                        val selected = index == 0 && mode == ArMode.PLACED
                        Box(
                            modifier = Modifier
                                .size(if (selected) 8.dp else 6.5.dp)
                                .clip(CircleShape)
                                .background(swatch)
                                .then(
                                    if (selected) Modifier.border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                                    else Modifier
                                )
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(accent)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Place",
                        fontSize = 6.sp,
                        lineHeight = 7.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            // Home indicator.
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 24.dp, height = 2.5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.3f))
            )
        }
    }
}

private fun DrawScope.drawArScan(mode: ArMode, scanElapsed: Float, placedElapsed: Float, accent: Color) {
    val bracket = size.height * 0.12f
    val stroke = Stroke(width = 2f)
    listOf(0, 1, 2, 3).forEach { corner ->
        val x = if (corner % 2 == 0) 0f else size.width
        val y = if (corner < 2) 0f else size.height
        val dx = if (corner % 2 == 0) bracket else -bracket
        val dy = if (corner < 2) bracket else -bracket
        drawLine(accent.copy(alpha = 0.7f), Offset(x, y), Offset(x + dx, y), strokeWidth = 2f)
        drawLine(accent.copy(alpha = 0.7f), Offset(x, y), Offset(x, y + dy), strokeWidth = 2f)
    }

    val rows = 4
    val cols = 5
    val lineY = size.height * ((scanElapsed % SCAN_PASS_MS) / SCAN_PASS_MS)
    for (row in 0 until rows) {
        val t = row / (rows - 1f)
        val y = size.height * (0.3f + t * 0.65f)
        val spread = size.width * (0.15f + t * 0.35f)
        for (col in 0 until cols) {
            val cx = size.width / 2f + (col - (cols - 1) / 2f) * (spread / (cols - 1))
            val distToScan = kotlin.math.abs(y - lineY)
            val glow = (1f - (distToScan / (size.height * 0.15f)).coerceIn(0f, 1f))
            drawCircle(
                color = accent.copy(alpha = 0.15f + glow * 0.65f),
                radius = 1.5f + t * 1.5f,
                center = Offset(cx, y)
            )
        }
    }

    if (mode == ArMode.SCANNING) {
        drawLine(
            color = accent.copy(alpha = 0.5f),
            start = Offset(size.width * 0.1f, lineY),
            end = Offset(size.width * 0.9f, lineY),
            strokeWidth = 1.5f
        )
    }

    if (mode == ArMode.PLACED) {
        val fadeIn = (placedElapsed / 300f).coerceIn(0f, 1f)
        val fadeOut = ((AR_PLACED_HOLD_MS - placedElapsed) / 300f).coerceIn(0f, 1f)
        val alpha = minOf(fadeIn, fadeOut)
        val anchor = Offset(size.width / 2f, size.height * 0.72f)
        drawCircle(accent.copy(alpha = alpha * 0.4f), radius = 6f + fadeIn * 4f, center = anchor, style = stroke)
        val angle = placedElapsed / 1000f * 1.6f
        drawWireCube(center = anchor, side = size.height * 0.32f, angle = angle, alpha = alpha, accent = accent)
    }
}

// Same skewed-projection cube used for the old AR/VR glyph — the geometry read well, it just
// needed a real scanning context around it.
private fun DrawScope.drawWireCube(center: Offset, side: Float, angle: Float, alpha: Float, accent: Color) {
    if (alpha <= 0f) return
    val skew = cos(angle) * side * 0.28f
    val depth = side * 0.3f
    val backTopLeft = Offset(center.x - side / 2f + skew, center.y - side - depth * 0.4f)
    val frontTopLeft = Offset(center.x - side / 2f - skew, center.y - side + depth * 0.4f)

    drawRect(color = accent.copy(alpha = alpha * 0.4f), topLeft = backTopLeft, size = Size(side, side), style = Stroke(width = 1.5f))
    drawRect(color = accent.copy(alpha = alpha), topLeft = frontTopLeft, size = Size(side, side), style = Stroke(width = 2f))
    listOf(Offset(0f, 0f), Offset(side, 0f), Offset(0f, side), Offset(side, side)).forEach { corner ->
        drawLine(
            color = accent.copy(alpha = alpha * 0.5f),
            start = backTopLeft + corner,
            end = frontTopLeft + corner,
            strokeWidth = 1.2f
        )
    }
}

// Three-layer network whose edges pulse as the signal travels left to right.
private fun DrawScope.drawNeuralNet(progress: Float, accent: Color, muted: Color) {
    val layers = listOf(3, 4, 2)
    val columnGap = size.width / (layers.size + 1)
    val positions = layers.mapIndexed { layerIndex, count ->
        val x = columnGap * (layerIndex + 1)
        val rowGap = size.height / (count + 1)
        List(count) { Offset(x, rowGap * (it + 1)) }
    }

    positions.zipWithNext().forEachIndexed { edgeLayer, (from, to) ->
        val slotStart = edgeLayer / 2f
        val local = ((progress - slotStart) * 2f).coerceIn(0f, 1f)
        from.forEach { a ->
            to.forEach { b ->
                drawLine(muted, a, b, strokeWidth = 1f)
                drawLine(
                    color = accent,
                    start = a,
                    end = Offset(a.x + (b.x - a.x) * local, a.y + (b.y - a.y) * local),
                    strokeWidth = 1.6f
                )
            }
        }
    }

    positions.flatten().forEach { drawCircle(accent, radius = 3.5f, center = it) }
}

private data class IotDeviceSpec(val icon: ImageVector, val angleDeg: Float)

// Five devices evenly spaced around the hub, starting at the top and going clockwise.
private val iotDevices = listOf(
    IotDeviceSpec(Icons.Filled.Lightbulb, -90f),
    IotDeviceSpec(Icons.Filled.Thermostat, -18f),
    IotDeviceSpec(Icons.Filled.ElectricalServices, 54f),
    IotDeviceSpec(Icons.Filled.Videocam, 126f),
    IotDeviceSpec(Icons.Filled.Speaker, 198f)
)

private const val IOT_STAGGER_MS = 260f
private const val IOT_SPOKE_DRAW_MS = 350f
private const val IOT_PULSE_MS = 900f
private const val IOT_HOLD_MS = 1400f
private const val IOT_RESET_FADE_MS = 350f
private val IOT_HUB_ICON_SIZE = 22.dp
private val IOT_DEVICE_ICON_SIZE = 19.dp

// A central hub connecting to devices one at a time: each spoke draws in, then a pulse travels
// it and the device icon lights up — once every device is connected it holds, fades, and repeats.
@Composable
private fun IotNetworkSimulation(accent: Color, muted: Color, modifier: Modifier = Modifier) {
    var elapsedMs by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var lastFrame = 0L
        while (true) {
            withFrameNanos { now ->
                if (lastFrame == 0L) lastFrame = now
                elapsedMs += (now - lastFrame) / 1_000_000f
                lastFrame = now
            }
        }
    }

    val connectEnd = (iotDevices.size - 1) * IOT_STAGGER_MS + IOT_SPOKE_DRAW_MS
    val holdEnd = connectEnd + IOT_HOLD_MS
    val cycleTotal = holdEnd + IOT_RESET_FADE_MS
    val t = elapsedMs % cycleTotal
    // Symmetric fade: in at the very start of the cycle, out at the very end — without the
    // fade-in half, wrapping back to t=0 snapped the whole network to full opacity in one frame.
    val networkAlpha = when {
        t < IOT_RESET_FADE_MS -> (t / IOT_RESET_FADE_MS).coerceIn(0f, 1f)
        t > holdEnd -> (1f - (t - holdEnd) / IOT_RESET_FADE_MS).coerceIn(0f, 1f)
        else -> 1f
    }

    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        // Cap the spread so a device icon's outer edge never crosses the box bounds — the card is
        // much wider than tall, so the top/bottom spokes are the ones at risk of poking outside.
        val minDimension = minOf(maxWidth, maxHeight)
        val maxRadius = minDimension / 2f - IOT_DEVICE_ICON_SIZE / 2f - 3.dp
        val radiusFraction = (maxRadius / minDimension).coerceIn(0.3f, 0.56f)
        val radius = minDimension * radiusFraction

        Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = networkAlpha }) {
            drawIotNetwork(t, radiusFraction, accent, muted)
        }

        Icon(
            imageVector = Icons.Filled.Router,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(IOT_HUB_ICON_SIZE).graphicsLayer { alpha = networkAlpha }
        )

        iotDevices.forEachIndexed { index, device ->
            val angleRad = Math.toRadians(device.angleDeg.toDouble())
            val glow = ((t - index * IOT_STAGGER_MS) / IOT_SPOKE_DRAW_MS).coerceIn(0f, 1f)
            Icon(
                imageVector = device.icon,
                contentDescription = null,
                tint = lerp(muted, accent, glow),
                modifier = Modifier
                    .size(IOT_DEVICE_ICON_SIZE)
                    .offset(x = radius * cos(angleRad).toFloat(), y = radius * sin(angleRad).toFloat())
                    .graphicsLayer { alpha = networkAlpha }
            )
        }
    }
}

private fun DrawScope.drawIotNetwork(t: Float, radiusFraction: Float, accent: Color, muted: Color) {
    val hub = Offset(size.width / 2f, size.height / 2f)
    val radius = minOf(size.width, size.height) * radiusFraction

    iotDevices.forEachIndexed { index, device ->
        val angleRad = Math.toRadians(device.angleDeg.toDouble())
        val target = Offset(
            hub.x + (cos(angleRad) * radius).toFloat(),
            hub.y + (sin(angleRad) * radius).toFloat()
        )
        val startDelay = index * IOT_STAGGER_MS
        val drawProgress = ((t - startDelay) / IOT_SPOKE_DRAW_MS).coerceIn(0f, 1f)
        if (drawProgress > 0f) {
            drawLine(muted.copy(alpha = 0.35f), hub, target, strokeWidth = 1f)
            val end = Offset(hub.x + (target.x - hub.x) * drawProgress, hub.y + (target.y - hub.y) * drawProgress)
            drawLine(accent.copy(alpha = 0.3f + drawProgress * 0.5f), hub, end, strokeWidth = 1.6f)
        }

        val connectedAt = startDelay + IOT_SPOKE_DRAW_MS
        if (t >= connectedAt) {
            val pulseT = ((t - connectedAt) % IOT_PULSE_MS) / IOT_PULSE_MS
            val pos = 1f - abs(1f - 2f * pulseT)
            val pulseCenter = Offset(hub.x + (target.x - hub.x) * pos, hub.y + (target.y - hub.y) * pos)
            drawCircle(accent, radius = 2.2f, center = pulseCenter)
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DomainSignatureIconPreview() {
    InnogeeksTheme {
        val hazeState = remember { HazeState() }
        val scheme = MaterialTheme.colorScheme
        val ids = listOf("webd", "appd", "ml", "arvr", "iot")
        val accents = listOf(
            scheme.primary,
            scheme.secondary,
            scheme.tertiary,
            scheme.secondaryContainer,
            scheme.primaryContainer
        )
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ids.chunked(2).forEachIndexed { rowIndex, pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    pair.forEachIndexed { colIndex, id ->
                        DomainSignatureIcon(
                            domainId = id,
                            accent = accents[rowIndex * 2 + colIndex],
                            hazeState = hazeState,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
