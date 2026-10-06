package com.example.innogeeks.feature_events.presentation.events.components

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import edu.kiet.innogeeks.R
import com.example.innogeeks.core.presentation.components.liquidGlass
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.delay
import androidx.compose.ui.res.stringResource

// Diagonal-hatch placeholder for the real event image/logo, which no content
// endpoint provides yet (display-only screen, see APP_API_GAPS_RESOLVED.md §6).
@Composable
fun EventImagePlaceholder(
    modifier: Modifier = Modifier,
    height: Dp = 120.dp
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(14.dp))
            .background(scheme.surfaceContainerHigh)
            .border(1.dp, scheme.outlineVariant, RoundedCornerShape(14.dp))
            .drawWithContent {
                drawContent()
                val stripeColor = scheme.outlineVariant.copy(alpha = 0.5f)
                val gap = 16.dp.toPx()
                var x = -size.height
                while (x < size.width) {
                    drawLine(
                        color = stripeColor,
                        start = Offset(x, size.height),
                        end = Offset(x + size.height, 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                    x += gap
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.events_event_image_logo),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

// Real event photo, same footprint as EventImagePlaceholder, for when one exists.
@Composable
fun EventImage(
    imageRes: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    height: Dp = 120.dp
) {
    Image(
        painter = painterResource(id = imageRes),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(14.dp))
    )
}

// Fullscreen pinch-to-zoom/pan viewer for a tapped event photo. Tapping the
// scrim (not the photo itself) or the close button dismisses it.
@Composable
fun ZoomableImageDialog(imageRes: Int, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        var scale by remember { mutableStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onDismiss() })
                }
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (scale * zoom).coerceIn(1f, 5f)
                            scale = newScale
                            offset = if (newScale <= 1f) Offset.Zero else offset + pan
                        }
                    }
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(R.string.events_close), tint = Color.White)
            }
        }
    }
}

// Static pill button, no toggle state — this screen is display-only.
@Composable
private fun LearnMoreButton(
    onClick: () -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(hazeState = hazeState, cornerRadius = 999.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.events_learn_more),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface
        )
    }
}

// Date badge (day + month) — every card gets one, past or future alike.
@Composable
private fun EventDateBadge(day: String, month: String, hazeState: HazeState, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .size(52.dp)
            .liquidGlass(hazeState = hazeState, cornerRadius = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = day, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = scheme.primary)
        Text(text = month, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
    }
}

// Small pill clarifying a recurring event's date is just its next occurrence, not a one-off.
@Composable
private fun CadenceChip(cadence: String, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Text(
        text = cadence,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = scheme.primary,
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(scheme.primary.copy(alpha = 0.14f))
            .border(1.dp, scheme.primary.copy(alpha = 0.4f), RoundedCornerShape(percent = 50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

// Date badge + title/blurb, an attendee count once known, and Learn more — same shape
// regardless of whether the event is past, upcoming, or recurring.
@Composable
fun EventCard(
    title: String,
    blurb: String,
    day: String,
    month: String,
    attendees: Int,
    cadence: String,
    cardImageRes: Int?,
    onClick: () -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(hazeState = hazeState, cornerRadius = 18.dp)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (cardImageRes != null) {
            EventImage(imageRes = cardImageRes, contentDescription = title)
        } else {
            EventImagePlaceholder()
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EventDateBadge(day = day, month = month, hazeState = hazeState)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface
                )
                if (cadence.isNotBlank()) {
                    CadenceChip(cadence = cadence)
                }
            }
        }

        Text(
            text = blurb,
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        if (attendees > 0) {
            Text(
                text = stringResource(R.string.events_attendees, attendees),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurfaceVariant
            )
        }

        LearnMoreButton(onClick = onClick, hazeState = hazeState)
    }
}

// Real event photos that pop in one after another on a past event's detail page.
@Composable
fun EventPhotoRow(
    images: List<Int>,
    modifier: Modifier = Modifier,
    onImageClick: (Int) -> Unit = {}
) {
    if (images.isEmpty()) return

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        images.forEachIndexed { index, imageRes ->
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(120L + index * 90L)
                visible = true
            }
            val scale by animateFloatAsState(
                targetValue = if (visible) 1f else 0.6f,
                animationSpec = tween(280),
                label = "photoScale"
            )
            val alpha by animateFloatAsState(
                targetValue = if (visible) 1f else 0f,
                animationSpec = tween(280),
                label = "photoAlpha"
            )

            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
                    .clip(RoundedCornerShape(10.dp))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { onImageClick(imageRes) }
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EventComponentsPreview() {
    InnogeeksTheme {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            EventCard(
                title = "Hack The Campus 3.0",
                blurb = "A 24-hour campus-wide hackathon open to all branches.",
                day = "14",
                month = "AUG",
                attendees = 210,
                cadence = "",
                cardImageRes = R.drawable.event_coderspree1,
                onClick = {},
                hazeState = remember { HazeState() }
            )
            EventPhotoRow(images = listOf(R.drawable.event_nasa_a, R.drawable.event_nasa_b, R.drawable.event_nasa_c))
        }
    }
}
