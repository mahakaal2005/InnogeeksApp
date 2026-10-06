package com.example.innogeeks.feature_onboarding.presentation.auth.passwordreset

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
import com.example.innogeeks.feature_onboarding.presentation.auth.verifycode.CodeBoxes
import com.example.innogeeks.ui.theme.InnogeeksTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import androidx.compose.ui.res.stringResource

@Composable
fun PasswordResetVerifyCodeRoot(
    collegeEmail: String,
    onNavigateToCompleteReset: (String, String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: PasswordResetVerifyCodeViewModel = koinViewModel { parametersOf(collegeEmail) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is PasswordResetVerifyCodeEvent.NavigateToCompleteReset ->
                onNavigateToCompleteReset(event.collegeEmail, event.passwordResetToken)

            is PasswordResetVerifyCodeEvent.NavigateBack -> onNavigateBack()
        }
    }

    PasswordResetVerifyCodeScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun PasswordResetVerifyCodeScreen(
    state: PasswordResetVerifyCodeState,
    onAction: (PasswordResetVerifyCodeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    AuthScaffold(
        title = stringResource(R.string.auth_check_your_email),
        subtitle = stringResource(R.string.auth_code_sent_subtitle, state.collegeEmail),
        modifier = modifier,
        footer = {
            TextButton(onClick = { onAction(PasswordResetVerifyCodeAction.OnBackClick) }) {
                Text(
                    text = stringResource(R.string.auth_back_to_login),
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    ) {
        CodeBoxes(
            code = state.code,
            onCodeChange = { onAction(PasswordResetVerifyCodeAction.OnCodeChange(it)) },
            isError = state.codeError != null,
            enabled = !state.isSubmitting
        )

        Spacer(Modifier.height(12.dp))

        AuthFormError(state.codeError ?: state.formError)

        Spacer(Modifier.height(16.dp))

        AuthPrimaryButton(
            text = stringResource(R.string.auth_verify),
            onClick = { onAction(PasswordResetVerifyCodeAction.OnVerifyClick) },
            isSubmitting = state.isSubmitting,
            enabled = state.code.length == 6
        )

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = { onAction(PasswordResetVerifyCodeAction.OnResendClick) },
            enabled = state.canResend && state.resendCountdown == 0
        ) {
            Text(
                text = when {
                    !state.canResend -> stringResource(R.string.auth_sending)
                    state.resendCountdown > 0 -> stringResource(R.string.auth_resend_code_in, state.resendCountdown)
                    else -> stringResource(R.string.auth_resend_code)
                },
                color = if (state.canResend && state.resendCountdown == 0) {
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
private fun PasswordResetVerifyCodeIdlePreview() {
    InnogeeksTheme {
        PasswordResetVerifyCodeScreen(
            state = PasswordResetVerifyCodeState(collegeEmail = "student@kiet.edu"),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PasswordResetVerifyCodeCountdownPreview() {
    InnogeeksTheme {
        PasswordResetVerifyCodeScreen(
            state = PasswordResetVerifyCodeState(
                collegeEmail = "student@kiet.edu",
                code = "123",
                resendCountdown = 45
            ),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PasswordResetVerifyCodeSubmittingPreview() {
    InnogeeksTheme {
        PasswordResetVerifyCodeScreen(
            state = PasswordResetVerifyCodeState(
                collegeEmail = "student@kiet.edu",
                code = "123456",
                isSubmitting = true
            ),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PasswordResetVerifyCodeInvalidPreview() {
    InnogeeksTheme {
        PasswordResetVerifyCodeScreen(
            state = PasswordResetVerifyCodeState(
                collegeEmail = "student@kiet.edu",
                codeError = UiText.StringResource(R.string.error_password_reset_code_invalid)
            ),
            onAction = {}
        )
    }
}
