package com.example.innogeeks.feature_onboarding.presentation.auth.setpassword

import android.content.res.Configuration
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.kiet.innogeeks.R
import com.example.innogeeks.core.presentation.ObserveAsEvents
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.feature_onboarding.presentation.auth.AuthFormError
import com.example.innogeeks.feature_onboarding.presentation.auth.AuthPasswordField
import com.example.innogeeks.feature_onboarding.presentation.auth.AuthPrimaryButton
import com.example.innogeeks.feature_onboarding.presentation.auth.AuthScaffold
import com.example.innogeeks.ui.theme.InnogeeksTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import androidx.compose.ui.res.stringResource

@Composable
fun SetPasswordRoot(
    collegeEmail: String,
    passwordSetupToken: String,
    onNavigateToHome: () -> Unit,
    onRestartVerification: () -> Unit,
    viewModel: SetPasswordViewModel = koinViewModel {
        parametersOf(collegeEmail, passwordSetupToken)
    }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is SetPasswordEvent.NavigateToHome -> onNavigateToHome()
            is SetPasswordEvent.RestartVerification -> onRestartVerification()
        }
    }

    SetPasswordScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun SetPasswordScreen(
    state: SetPasswordState,
    onAction: (SetPasswordAction) -> Unit,
    modifier: Modifier = Modifier
) {
    AuthScaffold(
        title = stringResource(R.string.auth_set_password_create_your_password),
        subtitle = stringResource(R.string.auth_set_password_subtitle, state.collegeEmail),
        modifier = modifier
    ) {
        AuthPasswordField(
            value = state.password,
            onValueChange = { onAction(SetPasswordAction.OnPasswordChange(it)) },
            label = stringResource(R.string.auth_new_password),
            isVisible = state.isPasswordVisible,
            onToggleVisibility = { onAction(SetPasswordAction.OnTogglePasswordVisibility) },
            error = state.passwordError,
            enabled = !state.isSubmitting
        )

        Spacer(Modifier.height(16.dp))

        AuthPasswordField(
            value = state.confirmPassword,
            onValueChange = { onAction(SetPasswordAction.OnConfirmPasswordChange(it)) },
            label = stringResource(R.string.auth_confirm_password),
            isVisible = state.isPasswordVisible,
            onToggleVisibility = { onAction(SetPasswordAction.OnTogglePasswordVisibility) },
            error = state.confirmPasswordError,
            enabled = !state.isSubmitting
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.auth_password_length_hint),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.55f),
            textAlign = TextAlign.Start
        )

        Spacer(Modifier.height(12.dp))

        AuthFormError(state.formError)

        Spacer(Modifier.height(16.dp))

        AuthPrimaryButton(
            text = stringResource(R.string.auth_set_password_set_password),
            onClick = { onAction(SetPasswordAction.OnSubmitClick) },
            isSubmitting = state.isSubmitting
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SetPasswordIdlePreview() {
    InnogeeksTheme {
        SetPasswordScreen(
            state = SetPasswordState(collegeEmail = "student@kiet.edu"),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SetPasswordTooShortPreview() {
    InnogeeksTheme {
        SetPasswordScreen(
            state = SetPasswordState(
                collegeEmail = "student@kiet.edu",
                password = "abc",
                passwordError = UiText.StringResource(R.string.error_password_too_short)
            ),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SetPasswordMismatchPreview() {
    InnogeeksTheme {
        SetPasswordScreen(
            state = SetPasswordState(
                collegeEmail = "student@kiet.edu",
                password = "innogeeks123",
                confirmPassword = "innogeeks124",
                confirmPasswordError =
                    UiText.StringResource(R.string.error_passwords_do_not_match)
            ),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SetPasswordSubmittingPreview() {
    InnogeeksTheme {
        SetPasswordScreen(
            state = SetPasswordState(
                collegeEmail = "student@kiet.edu",
                password = "innogeeks123",
                confirmPassword = "innogeeks123",
                isSubmitting = true
            ),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SetPasswordNotAllowedPreview() {
    InnogeeksTheme {
        SetPasswordScreen(
            state = SetPasswordState(
                collegeEmail = "student@kiet.edu",
                formError = UiText.StringResource(R.string.error_password_setup_not_allowed)
            ),
            onAction = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SetPasswordVisiblePreview() {
    InnogeeksTheme {
        SetPasswordScreen(
            state = SetPasswordState(
                collegeEmail = "student@kiet.edu",
                password = "innogeeks123",
                confirmPassword = "innogeeks123",
                isPasswordVisible = true
            ),
            onAction = {}
        )
    }
}
