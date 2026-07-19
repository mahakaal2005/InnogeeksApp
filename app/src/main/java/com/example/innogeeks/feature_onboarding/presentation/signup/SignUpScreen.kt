package com.example.innogeeks.feature_onboarding.presentation.signup

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.feature_onboarding.presentation.components.AuthGlowBackground
import com.example.innogeeks.feature_onboarding.presentation.components.glassFieldColors
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import org.koin.androidx.compose.koinViewModel


// Root = smart half: pulls the ViewModel from Koin, observes events, owns navigation.
@Composable
fun SignUpRoot(
    onNavigateToLogin: () -> Unit,
    viewModel: SignUpViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Both signup-success AND the "already have an account? Log in" link -> go to Login.
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is SignUpEvent.NavigateToLoginScreen -> onNavigateToLogin()
        }
    }

    SignUpScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun SignUpScreen(
    state: SignUpState,
    onAction: (SignUpAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val hazeState = remember { HazeState() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Shared dark background + brand-blue glow blobs (the Haze source).
        AuthGlowBackground(hazeState = hazeState)

        // The frosted-glass card holding the form.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(28.dp))
                .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin())
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
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
                text = "Create your account",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(28.dp))

            // Email
            OutlinedTextField(
                value = state.email,
                onValueChange = { onAction(SignUpAction.OnEmailChange(it)) },
                label = { Text("Email") },
                singleLine = true,
                isError = state.emailError != null,
                supportingText = { state.emailError?.let { Text(it.asString()) } },
                colors = glassFieldColors(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Password (the eye toggle reveals both password fields together)
            OutlinedTextField(
                value = state.password,
                onValueChange = { onAction(SignUpAction.OnPasswordChange(it)) },
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
                    IconButton(onClick = { onAction(SignUpAction.OnTogglePasswordVisibility) }) {
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

            Spacer(Modifier.height(16.dp))

            // Confirm password — same visibility toggle as the password above.
            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = { onAction(SignUpAction.OnConfirmPasswordChange(it)) },
                label = { Text("Confirm password") },
                singleLine = true,
                isError = state.confirmPasswordError != null,
                supportingText = { state.confirmPasswordError?.let { Text(it.asString()) } },
                visualTransformation = if (state.isPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                colors = glassFieldColors(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(28.dp))

            // Solid cyan pill CTA — swaps to a spinner while the signup call is in flight.
            Button(
                onClick = { onAction(SignUpAction.OnSignUpClick) },
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
                        text = "Create account",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = { onAction(SignUpAction.OnLoginClick) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Already have an account? Log in",
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// --- Previews (dark). Haze blur only renders on a device; layout/colors preview fine. ---

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SignUpScreenPreview() {
    InnogeeksTheme {
        SignUpScreen(state = SignUpState(), onAction = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SignUpScreenErrorPreview() {
    InnogeeksTheme {
        SignUpScreen(
            state = SignUpState(
                email = "bad-email",
                password = "12345678",
                confirmPassword = "87654321",
                confirmPasswordError = UiText.DynamicString("Passwords do not match.")
            ),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SignUpScreenLoadingPreview() {
    InnogeeksTheme {
        SignUpScreen(
            state = SignUpState(
                email = "user@innogeeks.in",
                password = "password123",
                confirmPassword = "password123",
                isLoading = true
            ),
            onAction = {}
        )
    }
}
