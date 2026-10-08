package com.example.innogeeks.feature_attendance.domain.model

import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.Error

// Server business codes for attendance writes; unknown codes map to UNSUPPORTED.
enum class AttendanceError {
    FORBIDDEN_ROLE,
    NO_DOMAIN,
    SESSION_NOT_FOUND,
    NOT_IN_DOMAIN,
    VALIDATION,
    UNSUPPORTED;

    companion object {
        fun fromCode(code: String): AttendanceError = when (code) {
            "ATTENDANCE_FORBIDDEN_ROLE" -> FORBIDDEN_ROLE
            "ATTENDANCE_NO_DOMAIN" -> NO_DOMAIN
            "ATTENDANCE_SESSION_NOT_FOUND" -> SESSION_NOT_FOUND
            "ATTENDANCE_NOT_IN_DOMAIN" -> NOT_IN_DOMAIN
            "VALIDATION_ERROR" -> VALIDATION
            else -> UNSUPPORTED
        }
    }
}

sealed interface AttendanceFailure : Error {
    data class Rejected(val error: AttendanceError) : AttendanceFailure
    data class Transport(val error: DataError.Network) : AttendanceFailure
}
