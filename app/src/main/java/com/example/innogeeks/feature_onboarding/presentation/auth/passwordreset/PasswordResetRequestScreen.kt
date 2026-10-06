package com.example.innogeeks.feature_onboarding.presentation.auth.passwordreset

import android.content.res.Configuration
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.kiet.innogeeks.R
import com.example.innogeeks.core.presentation.ObserveAsEvents
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.feature_onboarding.presentation.auth.AuthFormError
import com.example.innogeeks.feature_onboarding.presentation.auth.AuthPrimaryButton
import com.example.innogeeks.feature_onboarding.presentation.auth.AuthScaffold
import com.example.innogeeks.ui.theme.InnogeeksTheme
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.res.stringResource

@Composable
fun PasswordResetRequestRoot(
    onNavigateToVerifyResetCode: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: PasswordResetRequestViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is PasswordResetRequestEvent.NavigateToVerifyResetCode ->
                onNavigateToVerifyResetCode(event.collegeEmail)
            is PasswordResetRequestEvent.NavigateBack -> onNavigateBack()
        }
    }

    PasswordResetRequestScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun PasswordResetRequestScreen(
    state: PasswordResetRequestState,
    onAction: (PasswordResetRequestAction) -> Unit,
    modifier: Modifier = Modifier
) {
    AuthScaffold(
        title = stringResource(R.string.auth_reset_password),
        subtitle = stringResource(R.string.auth_reset_request_enter_your_college_email_we),
        modifier = modifier,
        footer = {
            TextButton(onClick = { onAction(PasswordResetRequestAction.OnBackClick) }) {
                Text(
                    text = stringResource(R.string.auth_back_to_login),
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    ) {
        OutlinedTextField(
            value = state.email,
            onValueChange = { onAction(PasswordResetRequestAction.OnEmailChange(it)) },
            label = { Text(stringResource(R.string.auth_college_email)) },
            singleLine = true,
            enabled = !state.isSubmitting,
            isError = state.emailError != null,
            supportingText = { state.emailError?.let { Text(it.asString()) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f)
                )
            },
            colors = com.example.innogeeks.core.presentation.components.glassFieldColors(),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        AuthFormError(state.formError)

        Spacer(Modifier.height(16.dp))

        AuthPrimaryButton(
            text = stringResource(R.string.auth_reset_request_send_verification_code),
            onClick = { onAction(PasswordResetRequestAction.OnSendCodeClick) },
            isSubmitting = state.isSubmitting
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PasswordResetRequestIdlePreview() {
    InnogeeksTheme {
        PasswordResetRequestScreen(state = PasswordResetRequestState(), onAction = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PasswordResetRequestSubmittingPreview() {
    InnogeeksTheme {
        PasswordResetRequestScreen(
            state = PasswordResetRequestState(
                email = "student@kiet.edu",
                isSubmitting = true
            ),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PasswordResetRequestEmailErrorPreview() {
    InnogeeksTheme {
        PasswordResetRequestScreen(
            state = PasswordResetRequestState(
                email = "invalid-email",
                emailError = UiText.StringResource(R.string.error_invalid_email)
            ),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PasswordResetRequestPasswordNotSetPreview() {
    InnogeeksTheme {
        PasswordResetRequestScreen(
            state = PasswordResetRequestState(
                email = "newuser@kiet.edu",
                formError = UiText.StringResource(R.string.error_password_not_set)
            ),
            onAction = {}
        )
    }
}
