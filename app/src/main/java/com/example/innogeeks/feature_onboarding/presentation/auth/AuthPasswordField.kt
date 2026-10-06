package com.example.innogeeks.feature_onboarding.presentation.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.core.presentation.components.glassFieldColors
import androidx.compose.ui.res.stringResource
import edu.kiet.innogeeks.R

// Shared by set-password and password-login so the eye toggle and glass styling live once.
@Composable
fun AuthPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier,
    error: UiText? = null,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        enabled = enabled,
        isError = error != null,
        supportingText = { error?.let { Text(it.asString()) } },
        visualTransformation = if (isVisible) {
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
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = if (isVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = stringResource(if (isVisible) R.string.auth_hide_password else R.string.auth_show_password),
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
        },
        colors = glassFieldColors(),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    )
}
