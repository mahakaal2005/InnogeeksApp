package com.example.innogeeks.feature_onboarding.presentation.login

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.innogeeks.core.presentation.ObserveAsEvents
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials
import org.koin.androidx.compose.koinViewModel


// Root = the SMART half: knows the ViewModel, subscribes to state, handles one-shot events,
// owns navigation. It hands the dumb LoginScreen only state + onAction.
@Composable
fun LoginRoot(
    onNavigateToHome: () -> Unit,        // nav callbacks passed IN — the screen doesn't own navigation
    onNavigateToSignUp: () -> Unit,
    viewModel: LoginViewModel = koinViewModel()  // Koin builds it + injects LoginUseCase
) {
    // Subscribe to state; `by` makes `state` read as the current LoginState. Redraws on change.
    val state by viewModel.state.collectAsStateWithLifecycle()

    // One-shot events -> fire the matching navigation callback exactly once.
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is LoginEvent.NavigateToHome -> onNavigateToHome()
            is LoginEvent.NavigateToSignUp -> onNavigateToSignUp()
        }
    }

    // Hand the dumb screen what to draw (state) + how to talk back (onAction).
    // viewModel::onAction is a function REFERENCE — the function as a value, not a call.
    LoginScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun LoginScreen(
    state: LoginState,
    onAction: (LoginAction) -> Unit,
    modifier: Modifier = Modifier
) {
    // Haze needs a shared state object linking the blurred SOURCE (background) to the
    // glass EFFECT (card). remember so it survives recomposition.
    val hazeState = remember { HazeState() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Layer 1: the glowing brand-blue blobs. hazeSource marks this as the content the
        // glass card will sample + blur.
        Box(
            modifier = Modifier
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

        // Layer 2: the frosted-glass card holding the form.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(28.dp))
                .hazeEffect(state = hazeState, style = HazeMaterials.thin())
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            // INNO (white) + GEEKS (brand) wordmark, matching the site/mockups.
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                        append("INNO")
                    }
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)) {
                        append("GEEKS")
                    }
                },
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Welcome back",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(28.dp))

            // Email field
            OutlinedTextField(
                value = state.email,
                onValueChange = { onAction(LoginAction.OnEmailChange(it)) },
                label = { Text("Email") },
                singleLine = true,
                isError = state.emailError != null,
                supportingText = { state.emailError?.let { Text(it.asString()) } },
                colors = glassFieldColors(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Password field with the show/hide eye toggle
            OutlinedTextField(
                value = state.password,
                onValueChange = { onAction(LoginAction.OnPasswordChange(it)) },
                label = { Text("Password") },
                singleLine = true,
                isError = state.passwordError != null,
                supportingText = { state.passwordError?.let { Text(it.asString()) } },
                visualTransformation = if (state.isPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { onAction(LoginAction.OnTogglePasswordVisibility) }) {
                        Icon(
                            imageVector = if (state.isPasswordVisible) {
                                Icons.Filled.VisibilityOff
                            } else {
                                Icons.Filled.Visibility
                            },
                            contentDescription = if (state.isPasswordVisible) {
                                "Hide password"
                            } else {
                                "Show password"
                            },
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = glassFieldColors(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(28.dp))

            // Solid cyan pill CTA — glass buttons fail contrast, so the primary action is bold.
            Button(
                onClick = { onAction(LoginAction.OnLoginClick) },
                enabled = !state.isLoading,
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = Color.Black
                    )
                } else {
                    Text(
                        text = "Log in",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = { onAction(LoginAction.OnSignUpClick) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Don't have an account? Sign up",
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// A soft, heavily-blurred circle of brand color — the "stage lighting" the glass blurs.
// BlurredEdgeTreatment.Unbounded lets the blur bleed PAST the box bounds so it reads as a
// diffuse round glow, not a hard-edged square (the default clamps blur to the bounds).
@Composable
private fun GlowBlob(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .blur(radius = 90.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
            .background(color.copy(alpha = 0.55f), CircleShape)
    )
}

// Transparent field colors so the frosted glass shows through, with light borders/text.
@Composable
private fun glassFieldColors() = OutlinedTextFieldDefaults.colors(
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

// --- Previews (dark, matching our dark-first design). Haze blur won't render in preview —
// verify the glass on an emulator; the layout/colors preview fine. ---

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoginScreenPreview() {
    InnogeeksTheme {
        LoginScreen(state = LoginState(), onAction = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoginScreenErrorPreview() {
    InnogeeksTheme {
        LoginScreen(
            state = LoginState(
                email = "not-an-email",
                password = "123",
                emailError = com.example.innogeeks.core.presentation.UiText.DynamicString("That doesn't look like a valid email."),
                passwordError = com.example.innogeeks.core.presentation.UiText.DynamicString("Password must be at least 8 characters.")
            ),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoginScreenLoadingPreview() {
    InnogeeksTheme {
        LoginScreen(
            state = LoginState(email = "user@innogeeks.in", password = "password123", isLoading = true),
            onAction = {}
        )
    }
}
