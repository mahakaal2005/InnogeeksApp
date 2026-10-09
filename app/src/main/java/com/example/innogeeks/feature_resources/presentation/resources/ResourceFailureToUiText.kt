package com.example.innogeeks.feature_resources.presentation.resources

import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.feature_resources.domain.model.ResourceError
import com.example.innogeeks.feature_resources.domain.model.ResourceFailure
import edu.kiet.innogeeks.R

fun ResourceFailure.toUiText(): UiText = UiText.StringResource(
    when (this) {
        is ResourceFailure.Rejected -> when (error) {
            ResourceError.FORBIDDEN_ROLE, ResourceError.NO_DOMAIN -> R.string.resources_error_forbidden
            ResourceError.NOT_IN_DOMAIN -> R.string.resources_error_not_in_domain
            ResourceError.NOT_FOUND -> R.string.resources_error_not_found
            ResourceError.VALIDATION -> R.string.resources_error_title
            ResourceError.UNSUPPORTED -> R.string.resources_error_generic
        }
        is ResourceFailure.Transport -> R.string.resources_error_generic
    }
)
