package com.example.innogeeks.feature_onboarding.presentation.auth.emailgate

import android.content.res.Configuration
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.foundation.text.KeyboardOptions
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
fun EmailGateRoot(
    onNavigateToVerifyCode: (String) -> Unit,
    onNavigateToPasswordLogin: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: EmailGateViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is EmailGateEvent.NavigateToVerifyCode -> onNavigateToVerifyCode(event.collegeEmail)
            is EmailGateEvent.NavigateToPasswordLogin -> onNavigateToPasswordLogin(event.collegeEmail)
            is EmailGateEvent.NavigateBack -> onNavigateBack()
        }
    }

    EmailGateScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun EmailGateScreen(
    state: EmailGateState,
    onAction: (EmailGateAction) -> Unit,
    modifier: Modifier = Modifier
) {
    AuthScaffold(
        title = stringResource(R.string.common_log_in),
        subtitle = stringResource(R.string.auth_email_gate_use_the_college_email_you),
        modifier = modifier,
        footer = {
            TextButton(onClick = { onAction(EmailGateAction.OnBackClick) }) {
                Text(
                    text = stringResource(R.string.auth_email_gate_continue_browsing_as_guest),
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    ) {
        OutlinedTextField(
            value = state.email,
            onValueChange = { onAction(EmailGateAction.OnEmailChange(it)) },
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
            text = stringResource(R.string.auth_email_gate_continue),
            onClick = { onAction(EmailGateAction.OnContinueClick) },
            isSubmitting = state.isSubmitting
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmailGateIdlePreview() {
    InnogeeksTheme {
        EmailGateScreen(state = EmailGateState(), onAction = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmailGateSubmittingPreview() {
    InnogeeksTheme {
        EmailGateScreen(
            state = EmailGateState(email = "student@kiet.edu", isSubmitting = true),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmailGateFieldErrorPreview() {
    InnogeeksTheme {
        EmailGateScreen(
            state = EmailGateState(
                email = "not-an-email",
                emailError = UiText.StringResource(R.string.error_invalid_email)
            ),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmailGateAccessDeniedPreview() {
    InnogeeksTheme {
        EmailGateScreen(
            state = EmailGateState(
                email = "outsider@kiet.edu",
                formError = UiText.StringResource(R.string.error_app_access_denied)
            ),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmailGateUnsupportedVersionPreview() {
    InnogeeksTheme {
        EmailGateScreen(
            state = EmailGateState(
                email = "student@kiet.edu",
                formError = UiText.StringResource(R.string.error_unsupported_version)
            ),
            onAction = {}
        )
    }
}
