package com.example.innogeeks.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.blur.blurEffect
import dev.chrisbanes.haze.blur.materials.HazeMaterials

// Shared onboarding UI building blocks, so Login/SignUp (and future auth screens) don't
// duplicate the glassmorphism styling. Extracted from LoginScreen once a 2nd screen needed it.

// A soft blurred circle of brand color that lets the blur bleed past the box bounds as a diffuse glow.
@Composable
fun GlowBlob(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .blur(radius = 90.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
            .background(color.copy(alpha = 0.55f), CircleShape)
    )
}

// The full dark background + two brand-blue glow blobs, marked as the Haze source (the
// content the glass card samples + blurs). One call gives any auth screen the same backdrop.
@Composable
fun AuthGlowBackground(hazeState: HazeState, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .hazeSource(hazeState)
    ) {
        GlowBlob(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(360.dp)
                .offset(x = (-100).dp, y = (-60).dp)
        )
        GlowBlob(
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 60.dp)
        )
    }
}

// FULL is the original recipe everywhere; REDUCED is a quieter variant for guest profile.
enum class GlassIntensity { FULL, REDUCED }

// The single liquid-glass recipe; composable values are resolved here first because the Haze blur builder is not composable.
@Composable
fun Modifier.liquidGlass(
    hazeState: HazeState,
    cornerRadius: Dp = 28.dp,
    intensity: GlassIntensity = GlassIntensity.FULL
): Modifier {
    // Resolve all @Composable calls here, before entering any non-composable lambda.
    val glassStyle = when (intensity) {
        GlassIntensity.FULL -> HazeMaterials.ultraThin()
        GlassIntensity.REDUCED -> HazeMaterials.thin()
    }
    val borderAlphaScale = if (intensity == GlassIntensity.REDUCED) 0.5f else 1f
    val topColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f * borderAlphaScale)
    val bottomColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f * borderAlphaScale)
    val borderWidth = if (intensity == GlassIntensity.REDUCED) 1.dp else 1.5.dp

    return this
        .clip(RoundedCornerShape(cornerRadius))
        .hazeEffect(state = hazeState) {
            blurEffect {
                style = glassStyle   // plain value, no composable call inside lambda
            }
        }
        .border(
            width = borderWidth,
            brush = Brush.verticalGradient(listOf(topColor, bottomColor)),
            shape = RoundedCornerShape(cornerRadius)
        )
}

// Transparent field colors so the frosted glass shows through, with light borders/text.
@Composable
fun glassFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White.copy(alpha = 0.06f),
    unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
    focusedBorderColor = MaterialTheme.colorScheme.secondary,
    unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
    focusedLabelColor = MaterialTheme.colorScheme.secondary,
    unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = MaterialTheme.colorScheme.secondary
)
