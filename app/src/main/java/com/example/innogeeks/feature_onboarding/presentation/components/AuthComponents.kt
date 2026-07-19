package com.example.innogeeks.feature_onboarding.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

// Shared onboarding UI building blocks, so Login/SignUp (and future auth screens) don't
// duplicate the glassmorphism styling. Extracted from LoginScreen once a 2nd screen needed it.

// A soft, heavily-blurred circle of brand color — the "stage lighting" the glass blurs.
// BlurredEdgeTreatment.Unbounded lets the blur bleed PAST the box bounds so it reads as a
// diffuse round glow, not a hard-edged square (the default clamps blur to the bounds).
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
