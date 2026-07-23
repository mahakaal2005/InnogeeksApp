package com.example.innogeeks.feature_onboarding.presentation.splash

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.innogeeks.R
import com.example.innogeeks.core.presentation.ObserveAsEvents
import com.example.innogeeks.feature_onboarding.presentation.components.AuthGlowBackground
import com.example.innogeeks.feature_onboarding.presentation.components.liquidGlass
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState

import org.koin.androidx.compose.koinViewModel


// Root = smart half: pulls the ViewModel from Koin, observes the one-shot routing event.
@Composable
fun SplashRoot(
    onNavigateToIntro: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SplashViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is SplashEvent.NavigateToIntro -> onNavigateToIntro()
            is SplashEvent.NavigateToLogin -> onNavigateToLogin()
        }
    }

    SplashScreen(state = state)
}


@Composable
fun SplashScreen(
    state: SplashState,
    modifier: Modifier = Modifier
) {
    val hazeState = remember { HazeState() }

    // One trigger flips true right after the screen appears; every timed element animates off it,
    // each with its own delay, so the splash reveals as a sequence (spin-up -> pulse -> identity).
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    // ---- Stage 1: SPIN-UP. The logo rotates in (-180 -> 0), scales up (0 -> 1) and settles. ----
    val logoScale by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "logoScale"
    )
    val logoSpin by animateFloatAsState(
        targetValue = if (started) 0f else -180f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "logoSpin"
    )

    // ---- Stage 2: PULSE. A cyan ring expands + fades outward once, right after the logo lands. ----
    val pulseProgress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 1200, delayMillis = 900, easing = LinearOutSlowInEasing),
        label = "pulseProgress"
    )

    // ---- AMBIENT: continuous, subtle life once everything has arrived. ----
    val ambient = rememberInfiniteTransition(label = "ambient")
    // Logo "breathes": a gentle float up/down.
    val breathe by ambient.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathe"
    )
    // An accent dot orbits the logo (0 -> 360 forever).
    val orbit by ambient.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing), RepeatMode.Restart),
        label = "orbit"
    )
    // The backdrop glow softly pulses in intensity.
    val glowPulse by ambient.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glowPulse"
    )

    val ringColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Same shared brand-blue glow backdrop; it's what the glass refracts. The gentle scale
        // makes the whole glow "breathe" in and out continuously.
        AuthGlowBackground(
            hazeState = hazeState,
            modifier = Modifier.graphicsLayer {
                scaleX = glowPulse
                scaleY = glowPulse
            }
        )

        // The centered liquid-glass badge (shared recipe, larger radius for the splash).
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .liquidGlass(hazeState, cornerRadius = 40.dp)
                .padding(horizontal = 40.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo zone: a Box so the pulse ring + orbiting dot can be layered over the logo.
            Box(contentAlignment = Alignment.Center) {

                // Stage 2 ring: drawn behind the logo, expands from the centre and fades to nothing.
                Canvas(modifier = Modifier.size(140.dp)) {
                    if (pulseProgress > 0f && pulseProgress < 1f) {
                        val maxR = size.minDimension / 2f
                        drawCircle(
                            color = ringColor,
                            radius = maxR * pulseProgress,
                            alpha = (1f - pulseProgress) * 0.6f,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                        )
                    }
                }

                // The logo itself: spins + scales in (stage 1), then breathes (ambient).
                Image(
                    painter = painterResource(R.drawable.app_logo),
                    contentDescription = "Innogeeks logo",
                    modifier = Modifier
                        .size(96.dp)
                        .graphicsLayer {
                            scaleX = logoScale
                            scaleY = logoScale
                            rotationZ = logoSpin
                            // breathe = 0..1 -> float up to 6px and back
                            translationY = -6f * breathe
                        }
                )

                // Ambient orbiting accent dot: a small cyan dot circling the logo.
                Canvas(modifier = Modifier.size(120.dp)) {
                    val r = size.minDimension / 2f
                    val angleRad = Math.toRadians(orbit.toDouble())
                    val cx = center.x + r * kotlin.math.cos(angleRad).toFloat()
                    val cy = center.y + r * kotlin.math.sin(angleRad).toFloat()
                    drawCircle(
                        color = ringColor,
                        radius = 5f,
                        center = Offset(cx, cy),
                        alpha = logoScale // only appears once the logo has arrived
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Stage 3 — Identity: wordmark rises + fades in.
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                        append("INNO")
                    }
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)) {
                        append("GEEKS")
                    }
                },
                style = MaterialTheme.typography.headlineLarge,
                letterSpacing = 8.sp,
                modifier = Modifier.revealFrom(started, delayMillis = 1900)
            )

            Spacer(Modifier.height(10.dp))

            // Stage 3 — Identity: motto last, the phrase the club is known by.
            Text(
                text = "We Learn, We Teach, We Conquer",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.revealFrom(started, delayMillis = 2300)
            )
        }

        // Stage 4 — Anchor: footer caption settles in last, pinned to the screen bottom.
        Text(
            text = "KIET Deemed To Be University",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .revealFrom(started, delayMillis = 2900)
        )
    }
}

// Shared reveal recipe: fade 0 -> 1 and slide up 24px -> 0, after a per-element delay.
// graphicsLayer keeps alpha/translation off the recomposition path (compose-ui skill).
private fun Modifier.revealFrom(started: Boolean, delayMillis: Int): Modifier = composed {
    val alpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 600, delayMillis = delayMillis, easing = LinearOutSlowInEasing),
        label = "revealAlpha"
    )
    val translateY by animateFloatAsState(
        targetValue = if (started) 0f else 24f,
        animationSpec = tween(durationMillis = 600, delayMillis = delayMillis, easing = LinearOutSlowInEasing),
        label = "revealTranslate"
    )
    graphicsLayer {
        this.alpha = alpha
        this.translationY = translateY
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SplashScreenPreview() {
    InnogeeksTheme {
        SplashScreen(state = SplashState())
    }
}
