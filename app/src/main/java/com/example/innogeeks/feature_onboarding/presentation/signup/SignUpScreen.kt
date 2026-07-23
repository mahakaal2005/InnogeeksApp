package com.example.innogeeks.feature_onboarding.presentation.signup

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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
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
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.feature_onboarding.presentation.components.AuthGlowBackground
import com.example.innogeeks.feature_onboarding.presentation.components.glassFieldColors
import com.example.innogeeks.feature_onboarding.presentation.components.liquidGlass
import com.example.innogeeks.ui.theme.InnogeeksTheme
import dev.chrisbanes.haze.HazeState

import kotlinx.coroutines.launch
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SignUpScreen(
    state: SignUpState,
    onAction: (SignUpAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val hazeState = remember { HazeState() }

    // Bring-into-view plumbing: each password field gets a requester; on focus it asks the
    // scrollable parent to scroll it above the keyboard. Needed because Arrangement.Center
    // on a scrollable Column fights the built-in focus auto-scroll for the bottom fields.
    val scope = rememberCoroutineScope()
    val passwordRequester = remember { BringIntoViewRequester() }
    val confirmRequester = remember { BringIntoViewRequester() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Shared dark background + brand-blue glow blobs (the Haze source).
        AuthGlowBackground(hazeState = hazeState)

        // Whole page column — logo/heading, then the card, then the link. Scrollable because
        // sign-up has four fields plus header/footer outside the card.
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
                text = "Join Innogeeks",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Create your account and start building the future.",
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
                // Full name — feeds state.name, which the slice threads all the way to the fake repo.
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { onAction(SignUpAction.OnNameChange(it)) },
                    label = { Text("Full name") },
                    singleLine = true,
                    isError = state.nameError != null,
                    supportingText = { state.nameError?.let { Text(it.asString()) } },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    },
                    colors = glassFieldColors(),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // Email
                OutlinedTextField(
                    value = state.email,
                    onValueChange = { onAction(SignUpAction.OnEmailChange(it)) },
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
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f)
                        )
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(passwordRequester)
                        .onFocusEvent { if (it.isFocused) scope.launch { passwordRequester.bringIntoView() } }
                )

                Spacer(Modifier.height(16.dp))

                // Confirm password — same Lock icon + same visibility toggle as the password above.
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
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    },
                    colors = glassFieldColors(),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(confirmRequester)
                        .onFocusEvent { if (it.isFocused) scope.launch { confirmRequester.bringIntoView() } }
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
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- Footer (OUTSIDE the card): switch to login ---
            TextButton(onClick = { onAction(SignUpAction.OnLoginClick) }) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.White.copy(alpha = 0.7f))) {
                            append("Already have an account? ")
                        }
                        withStyle(
                            SpanStyle(
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("Log In")
                        }
                    }
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
                name = "",
                email = "bad-email",
                password = "12345678",
                confirmPassword = "87654321",
                nameError = UiText.DynamicString("Please enter your name."),
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
                name = "Ada Lovelace",
                email = "user@innogeeks.in",
                password = "password123",
                confirmPassword = "password123",
                isLoading = true
            ),
            onAction = {}
        )
    }
}
