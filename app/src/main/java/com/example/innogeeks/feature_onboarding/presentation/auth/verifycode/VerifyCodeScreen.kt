package com.example.innogeeks.feature_onboarding.presentation.auth.verifycode

import android.content.res.Configuration
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import org.koin.core.parameter.parametersOf
import androidx.compose.ui.res.stringResource

@Composable
fun VerifyCodeRoot(
    collegeEmail: String,
    onNavigateToSetPassword: (String, String) -> Unit,
    onNavigateToPasswordLogin: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: VerifyCodeViewModel = koinViewModel { parametersOf(collegeEmail) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is VerifyCodeEvent.NavigateToSetPassword ->
                onNavigateToSetPassword(event.collegeEmail, event.passwordSetupToken)

            is VerifyCodeEvent.NavigateToPasswordLogin ->
                onNavigateToPasswordLogin(event.collegeEmail)

            is VerifyCodeEvent.NavigateBack -> onNavigateBack()
        }
    }

    VerifyCodeScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun VerifyCodeScreen(
    state: VerifyCodeState,
    onAction: (VerifyCodeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    AuthScaffold(
        title = stringResource(R.string.auth_check_your_email),
        subtitle = stringResource(R.string.auth_code_sent_subtitle, state.collegeEmail),
        modifier = modifier,
        footer = {
            TextButton(onClick = { onAction(VerifyCodeAction.OnBackClick) }) {
                Text(
                    text = stringResource(R.string.auth_use_a_different_email),
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    ) {
        CodeBoxes(
            code = state.code,
            onCodeChange = { onAction(VerifyCodeAction.OnCodeChange(it)) },
            isError = state.codeError != null,
            enabled = !state.isSubmitting
        )

        Spacer(Modifier.height(12.dp))

        AuthFormError(state.codeError ?: state.formError)

        Spacer(Modifier.height(16.dp))

        AuthPrimaryButton(
            text = stringResource(R.string.auth_verify),
            onClick = { onAction(VerifyCodeAction.OnVerifyClick) },
            isSubmitting = state.isSubmitting,
            enabled = state.isCodeComplete
        )

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = { onAction(VerifyCodeAction.OnResendClick) },
            enabled = state.canResend
        ) {
            Text(
                text = when {
                    state.isRequestingCode -> stringResource(R.string.auth_sending)
                    state.resendSecondsLeft > 0 -> stringResource(R.string.auth_resend_code_in, state.resendSecondsLeft)
                    else -> stringResource(R.string.auth_resend_code)
                },
                color = if (state.canResend) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    Color.White.copy(alpha = 0.5f)
                }
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun VerifyCodeSendingPreview() {
    InnogeeksTheme {
        VerifyCodeScreen(
            state = VerifyCodeState(collegeEmail = "student@kiet.edu", isRequestingCode = true),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun VerifyCodeCountingDownPreview() {
    InnogeeksTheme {
        VerifyCodeScreen(
            state = VerifyCodeState(
                collegeEmail = "student@kiet.edu",
                code = "012",
                resendSecondsLeft = 47
            ),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun VerifyCodeSubmittingPreview() {
    InnogeeksTheme {
        VerifyCodeScreen(
            state = VerifyCodeState(
                collegeEmail = "student@kiet.edu",
                code = "123456",
                resendSecondsLeft = 12,
                isSubmitting = true
            ),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun VerifyCodeInvalidPreview() {
    InnogeeksTheme {
        VerifyCodeScreen(
            state = VerifyCodeState(
                collegeEmail = "student@kiet.edu",
                codeError = UiText.StringResource(R.string.error_verification_code_invalid)
            ),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun VerifyCodeCooldownPreview() {
    InnogeeksTheme {
        VerifyCodeScreen(
            state = VerifyCodeState(
                collegeEmail = "student@kiet.edu",
                resendSecondsLeft = 58,
                formError = UiText.StringResource(R.string.error_verification_code_cooldown)
            ),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun VerifyCodeReadyPreview() {
    InnogeeksTheme {
        VerifyCodeScreen(
            state = VerifyCodeState(collegeEmail = "student@kiet.edu", code = "123456"),
            onAction = {}
        )
    }
}
