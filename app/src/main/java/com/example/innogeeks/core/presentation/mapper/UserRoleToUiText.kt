package com.example.innogeeks.core.presentation.mapper

import com.example.innogeeks.core.domain.model.UserRole
import com.example.innogeeks.core.presentation.UiText
import edu.kiet.innogeeks.R

// Display name of a role; it lives in presentation because it touches R.string.
fun UserRole.toUiText(): UiText = UiText.StringResource(
    when (this) {
        UserRole.REGISTERED -> R.string.common_registered
        UserRole.MEMBER -> R.string.role_member
        UserRole.COORDINATOR -> R.string.role_coordinator
        UserRole.ADMIN -> R.string.role_admin
    }
)
