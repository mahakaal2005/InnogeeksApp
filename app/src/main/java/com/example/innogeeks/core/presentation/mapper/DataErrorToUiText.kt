package com.example.innogeeks.core.presentation.mapper

import edu.kiet.innogeeks.R
import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.presentation.UiText

// Maps a DataError to display text; it lives in presentation because it touches R.string.
fun DataError.toUiText() : UiText {
    return when(this){
        DataError.Network.NO_INTERNET -> UiText.StringResource(R.string.error_no_internet)
        DataError.Network.REQUEST_TIMEOUT -> UiText.StringResource(R.string.error_request_timeout)
        DataError.Network.TOO_MANY_REQUESTS -> UiText.StringResource(R.string.error_too_many_requests)
        DataError.Network.SERVER_ERROR -> UiText.StringResource(R.string.error_server)
        DataError.Network.SERVICE_UNAVAILABLE -> UiText.StringResource(R.string.error_service_unavailable)
        DataError.Network.SERIALIZATION -> UiText.StringResource(R.string.error_serialization)
        DataError.Network.UNAUTHORIZED -> UiText.StringResource(R.string.error_unauthorized)
        DataError.Network.FORBIDDEN -> UiText.StringResource(R.string.error_app_access_denied)
        DataError.Local.DISK_FULL -> UiText.StringResource(R.string.error_disk_full)
        // Only the cases worth distinguishing get their own message and the rest fall into a generic one, so a new DataError silently becomes unknown.
        else -> UiText.StringResource(R.string.error_unknown)
    }
}
