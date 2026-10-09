package com.example.innogeeks.feature_resources.domain.model

import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.Error

// Server business codes for resource writes; unknown codes map to UNSUPPORTED.
enum class ResourceError {
    FORBIDDEN_ROLE,
    NO_DOMAIN,
    NOT_FOUND,
    NOT_IN_DOMAIN,
    VALIDATION,
    UNSUPPORTED;

    companion object {
        fun fromCode(code: String): ResourceError = when (code) {
            "RESOURCE_FORBIDDEN_ROLE" -> FORBIDDEN_ROLE
            "RESOURCE_NO_DOMAIN" -> NO_DOMAIN
            "RESOURCE_NOT_FOUND" -> NOT_FOUND
            "RESOURCE_NOT_IN_DOMAIN" -> NOT_IN_DOMAIN
            "VALIDATION_ERROR" -> VALIDATION
            else -> UNSUPPORTED
        }
    }
}

sealed interface ResourceFailure : Error {
    data class Rejected(val error: ResourceError) : ResourceFailure
    data class Transport(val error: DataError.Network) : ResourceFailure
}
