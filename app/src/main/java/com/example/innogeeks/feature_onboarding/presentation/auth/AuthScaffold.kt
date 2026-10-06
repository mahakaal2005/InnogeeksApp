package com.example.innogeeks.feature_onboarding.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import edu.kiet.innogeeks.R
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.core.presentation.components.AuthGlowBackground
import com.example.innogeeks.core.presentation.components.liquidGlass
import dev.chrisbanes.haze.HazeState
import androidx.compose.ui.res.stringResource

// The shell all four auth screens share: glow backdrop, logo, heading, glass card.
// Extracted so the four screens differ only in their fields, not their chrome.
@Composable
fun AuthScaffold(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    footer: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val hazeState = remember { HazeState() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AuthGlowBackground(hazeState = hazeState)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.app_logo),
                contentDescription = stringResource(R.string.common_innogeeks_logo),
                modifier = Modifier.size(72.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(hazeState)
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                content = content
            )

            if (footer != null) {
                Spacer(Modifier.height(16.dp))
                footer()
            }
        }
    }
}

// The one primary CTA shape. enabled is driven by isSubmitting so §9's "disable repeated
// submit taps while a request is in flight" is handled in a single place.
@Composable
fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    isSubmitting: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isSubmitting,
        shape = RoundedCornerShape(percent = 50),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = Color.Black
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        if (isSubmitting) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = Color.Black
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Form-level errors (access denied, wrong code, unsupported version) that belong to the
// whole request rather than one field.
@Composable
fun AuthFormError(error: UiText?, modifier: Modifier = Modifier) {
    if (error == null) return
    Text(
        text = error.asString(),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth()
    )
}
