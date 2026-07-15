package com.example.innogeeks.core.presentation.mapper

import com.example.innogeeks.R
import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.presentation.UiText

// Maps a DataError to a user-facing UiText ticket. Lives in presentation (not domain)
// because it touches R.string — turning an error into DISPLAY text is a presentation
// concern; the DataError type itself stays pure in domain.
fun DataError.toUiText() : UiText {
    return when(this){
        DataError.Network.NO_INTERNET -> UiText.StringResource(R.string.error_no_internet)
        DataError.Network.REQUEST_TIMEOUT -> UiText.StringResource(R.string.error_request_timeout)
        DataError.Network.TOO_MANY_REQUESTS -> UiText.StringResource(R.string.error_too_many_requests)
        DataError.Network.SERVER_ERROR -> UiText.StringResource(R.string.error_server)
        DataError.Network.SERVICE_UNAVAILABLE -> UiText.StringResource(R.string.error_service_unavailable)
        DataError.Network.SERIALIZATION -> UiText.StringResource(R.string.error_serialization)
        DataError.Network.UNAUTHORIZED -> UiText.StringResource(R.string.error_unauthorized)
        DataError.Local.DISK_FULL -> UiText.StringResource(R.string.error_disk_full)
        // Only the cases worth distinguishing to a user get their own message; the
        // rest (BAD_REQUEST, FORBIDDEN, CONFLICT, Local.NOT_FOUND, UNKNOWN...) funnel
        // into a friendly generic. Tradeoff: `else` means adding a new DataError won't
        // trigger a compiler warning here — it silently becomes "unknown".
        else -> UiText.StringResource(R.string.error_unknown)
    }
}
