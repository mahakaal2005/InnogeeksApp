package com.example.innogeeks.feature_onboarding.presentation.login

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.innogeeks.R
import com.example.innogeeks.core.presentation.ObserveAsEvents
import com.example.innogeeks.core.presentation.components.AuthGlowBackground
import com.example.innogeeks.core.presentation.components.glassFieldColors
import com.example.innogeeks.core.presentation.components.liquidGlass
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState

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
        // Layer 1: shared dark background + brand-blue glow blobs (the Haze source).
        AuthGlowBackground(hazeState = hazeState)

        // Layer 2: the whole page column — logo/heading, then the card, then the link.
        // Scrollable so nothing clips on short screens once content sits outside the card.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- Header (OUTSIDE the card): logo badge + heading + subtitle ---
            Image(
                painter = painterResource(R.drawable.app_logo),
                contentDescription = "Innogeeks logo",
                modifier = Modifier.size(72.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Sign in to continue your journey",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(24.dp))

            // --- The liquid-glass card: form fields + primary CTA only ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(hazeState)
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                // Email field
                OutlinedTextField(
                    value = state.email,
                    onValueChange = { onAction(LoginAction.OnEmailChange(it)) },
                    label = { Text("Email") },
                    singleLine = true,
                    isError = state.emailError != null,
                    supportingText = { state.emailError?.let { Text(it.asString()) } },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Email,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    },
                    colors = glassFieldColors(),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // Password field: Lock leading icon + the show/hide eye toggle trailing.
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
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f)
                        )
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
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- Footer (OUTSIDE the card): switch to sign up ---
            TextButton(onClick = { onAction(LoginAction.OnSignUpClick) }) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.White.copy(alpha = 0.7f))) {
                            append("Don't have an account? ")
                        }
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)) {
                            append("Sign Up")
                        }
                    }
                )
            }
        }
    }
}

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
