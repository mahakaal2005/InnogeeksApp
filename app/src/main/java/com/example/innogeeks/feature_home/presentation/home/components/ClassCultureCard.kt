package com.example.innogeeks.feature_home.presentation.home.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.innogeeks.feature_home.domain.model.CultureMoment
import com.example.innogeeks.ui.theme.InnogeeksTheme
import com.example.innogeeks.ui.theme.bodyFontFamily
import com.example.innogeeks.ui.theme.displayFontFamily
import edu.kiet.innogeeks.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val AUTO_ADVANCE_MS = 4200L
// One easing for every photo move — slow and settled, no springy overshoot.
private val PHOTO_MOTION = tween<Float>(650)
private val PHOTO_MOTION_DP = tween<Dp>(650)
// How far a swipe must travel before it counts as a page change, vs. springing back.
private val SWIPE_COMMIT_THRESHOLD = 56.dp
// How far the whole card is allowed to tug with a finger before it feels loose.
private val SWIPE_DRAG_LIMIT = 28.dp

// Depth -1 is the photo flying off the front, 0 is the front, 3 is a hidden photo waiting at the back.
private data class StackPose(val rotation: Float, val scale: Float, val offsetX: Dp, val alpha: Float)

private fun poseFor(depth: Int): StackPose = when (depth) {
    -1 -> StackPose(rotation = -14f, scale = 1.02f, offsetX = (-64).dp, alpha = 0f)
    0 -> StackPose(rotation = 0f, scale = 1f, offsetX = 0.dp, alpha = 1f)
    1 -> StackPose(rotation = -8f, scale = 0.92f, offsetX = (-16).dp, alpha = 0.8f)
    2 -> StackPose(rotation = 7f, scale = 0.85f, offsetX = 16.dp, alpha = 0.6f)
    else -> StackPose(rotation = 0f, scale = 0.78f, offsetX = 0.dp, alpha = 0f)
}

@Composable
fun ClassCultureCard(
    moments: List<CultureMoment>,
    modifier: Modifier = Modifier,
    onMomentClick: (CultureMoment) -> Unit = {}
) {
    if (moments.isEmpty()) return

    var activeIndex by remember { mutableIntStateOf(0) }
    var isPressed by remember { mutableStateOf(false) }
    val dragOffset = remember { Animatable(0f) }
    // Animatable's suspend functions can't be called directly from the gesture-detector's
    // restricted scope, so drag updates are launched on this instead.
    val dragScope = rememberCoroutineScope()

    // Keying on activeIndex too means a manual swipe restarts the same wait, so auto-advance
    // never fires right on top of it.
    LaunchedEffect(isPressed, activeIndex, moments.size) {
        if (isPressed) return@LaunchedEffect
        while (true) {
            delay(AUTO_ADVANCE_MS)
            activeIndex = (activeIndex + 1) % moments.size
        }
    }

    ClassCultureCardContent(
        moments = moments,
        activeIndex = activeIndex,
        dragOffsetPx = dragOffset.value,
        modifier = modifier
            .pointerInput(moments.size) {
                val commitThresholdPx = SWIPE_COMMIT_THRESHOLD.toPx()
                val dragLimitPx = SWIPE_DRAG_LIMIT.toPx()
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    var totalDrag = 0f
                    var isDragging = false
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break
                        val delta = change.positionChange().x
                        if (!isDragging && kotlin.math.abs(totalDrag + delta) > viewConfiguration.touchSlop) {
                            isDragging = true
                        }
                        if (isDragging) {
                            totalDrag += delta
                            change.consume()
                            val target = totalDrag.coerceIn(-dragLimitPx, dragLimitPx)
                            dragScope.launch { dragOffset.snapTo(target) }
                        }
                    }
                    isPressed = false
                    if (isDragging) {
                        when {
                            totalDrag <= -commitThresholdPx -> activeIndex = (activeIndex + 1).mod(moments.size)
                            totalDrag >= commitThresholdPx -> activeIndex = (activeIndex - 1).mod(moments.size)
                        }
                        dragScope.launch { dragOffset.animateTo(0f, tween(300)) }
                    } else {
                        onMomentClick(moments[activeIndex])
                    }
                }
            }
    )
}

@Composable
private fun ClassCultureCardContent(
    moments: List<CultureMoment>,
    activeIndex: Int,
    dragOffsetPx: Float = 0f,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val active = moments[activeIndex]

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .height(196.dp)
            // The little rubber-band tug while swiping — photos and text move together.
            .graphicsLayer { translationX = dragOffsetPx }
            .clip(RoundedCornerShape(20.dp))
            .background(scheme.surfaceContainerLowest)
            .border(1.dp, scheme.outlineVariant, RoundedCornerShape(20.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(0.46f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            // Soft glow behind the stack — echoes the accent glow used elsewhere on Home.
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(scheme.primary.copy(alpha = 0.14f), Color.Transparent)
                        )
                    )
            )
            PhotoStack(moments = moments, activeIndex = activeIndex)
        }

        Column(
            modifier = Modifier
                .weight(0.54f)
                .fillMaxHeight()
                .padding(end = 18.dp, top = 18.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "SESSIONS & MOMENTS",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.sp,
                color = scheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(6.dp))
            AnimatedContent(
                targetState = active,
                transitionSpec = { fadeIn(tween(400, delayMillis = 150)) togetherWith fadeOut(tween(200)) },
                label = "cultureCaption"
            ) { moment ->
                Column {
                    Text(
                        text = moment.title,
                        fontFamily = displayFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        lineHeight = 21.sp,
                        color = scheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = moment.caption,
                        fontFamily = bodyFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = scheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = moment.description,
                        fontFamily = bodyFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = scheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoStack(
    moments: List<CultureMoment>,
    activeIndex: Int,
    modifier: Modifier = Modifier
) {
    // Only render as many depths as there are distinct moments, so keys never collide.
    val backDepth = minOf(3, moments.size - 1)
    val showExit = moments.size >= 5
    // Back to front draw order; the exiting photo is drawn last so it flies over the stack.
    val depths = (backDepth downTo 0).toList() + if (showExit) listOf(-1) else emptyList()

    Box(modifier = modifier.size(150.dp), contentAlignment = Alignment.Center) {
        depths.forEach { depth ->
            val moment = moments[(activeIndex + depth).mod(moments.size)]
            // Keyed by id so each photo keeps its identity and animates between depths instead of swapping in place.
            key(moment.id) {
                MomentPhoto(moment = moment, depth = depth)
            }
        }
    }
}

@Composable
private fun MomentPhoto(
    moment: CultureMoment,
    depth: Int,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val pose = poseFor(depth)

    val rotation by animateFloatAsState(pose.rotation, PHOTO_MOTION, label = "photoRotation")
    val scale by animateFloatAsState(pose.scale, PHOTO_MOTION, label = "photoScale")
    val alpha by animateFloatAsState(pose.alpha, tween(500), label = "photoAlpha")
    val offsetX by animateDpAsState(pose.offsetX, PHOTO_MOTION_DP, label = "photoOffsetX")

    Box(
        modifier = modifier
            .size(112.dp)
            .graphicsLayer {
                translationX = offsetX.toPx()
                rotationZ = rotation
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .shadow(8.dp, RoundedCornerShape(8.dp))
            // Polaroid-style frame: a light border with extra weight at the bottom.
            .background(scheme.inverseSurface, RoundedCornerShape(8.dp))
            .padding(start = 5.dp, top = 5.dp, end = 5.dp, bottom = 12.dp)
            .clip(RoundedCornerShape(4.dp))
    ) {
        Image(
            painter = painterResource(id = moment.imageRes),
            contentDescription = if (depth == 0) moment.title else null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

private val previewMoments = listOf(
    CultureMoment(
        "cm1", "NASA Space Apps Challenge", "Ghaziabad Edition · Sep 2025",
        "150+ innovators, 35+ teams, and a ₹75,000 prize pool at KIET's own edition of NASA's global hackathon.",
        R.drawable.event_nasa_a, "e27"
    ),
    CultureMoment(
        "cm2", "InnoForge", "Jun 2025",
        "A hands-on build sprint where teams shipped working prototypes in a single day.",
        R.drawable.event_innoforge_a, "e26"
    ),
    CultureMoment(
        "cm3", "Winter of Code 3.0", "2024–25",
        "Innogeeks' flagship open-source program — students land their first real contributions.",
        R.drawable.event_iwoc3, "e23"
    ),
    CultureMoment(
        "cm4", "InnoHacks 2.0", "Hack & Innovate · Apr 2023",
        "Teams built and pitched full products over one high-energy hackathon weekend.",
        R.drawable.event_innohacks2, "e12"
    ),
    CultureMoment(
        "cm5", "CoderSpree 3.0", "Competitive coding · Oct 2023",
        "A campus-wide competitive programming arena open to every language and every year.",
        R.drawable.event_coderspree3, "e15"
    )
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ClassCultureCardPreview() {
    InnogeeksTheme {
        ClassCultureCardContent(
            moments = previewMoments,
            activeIndex = 0,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ClassCultureCardMidCyclePreview() {
    InnogeeksTheme {
        ClassCultureCardContent(
            moments = previewMoments,
            activeIndex = 2,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ClassCultureCardSingleMomentPreview() {
    InnogeeksTheme {
        ClassCultureCardContent(
            moments = listOf(previewMoments.first()),
            activeIndex = 0,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ClassCultureCardLongTitlePreview() {
    InnogeeksTheme {
        ClassCultureCardContent(
            moments = listOf(
                CultureMoment(
                    "cmLong",
                    "Innogeeks Winter of Code — Grand Finale Showcase",
                    "IWOC 2024–25 · Auditorium",
                    "A packed auditorium watched finalists demo six months of open-source work.",
                    R.drawable.event_iwoc3,
                    "e23"
                )
            ) + previewMoments.drop(1),
            activeIndex = 0,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}
