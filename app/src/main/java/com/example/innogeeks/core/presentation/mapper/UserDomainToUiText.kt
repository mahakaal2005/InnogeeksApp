package com.example.innogeeks.core.presentation.mapper

import com.example.innogeeks.core.domain.model.UserDomain
import com.example.innogeeks.core.presentation.UiText
import edu.kiet.innogeeks.R

// Display name of a member's domain; it lives in presentation because it touches R.string.
fun UserDomain.toUiText(): UiText = UiText.StringResource(
    when (this) {
        UserDomain.ANDROID -> R.string.domain_app_dev
        UserDomain.WEB -> R.string.domain_web_dev
        UserDomain.ML -> R.string.domain_machine_learning
        UserDomain.IOT -> R.string.domain_iot
        UserDomain.AR_VR -> R.string.domain_ar_vr
    }
)
