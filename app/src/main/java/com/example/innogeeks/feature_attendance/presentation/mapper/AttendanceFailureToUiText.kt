package com.example.innogeeks.feature_attendance.presentation.mapper

import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.core.presentation.mapper.toUiText
import com.example.innogeeks.feature_attendance.domain.model.AttendanceError
import com.example.innogeeks.feature_attendance.domain.model.AttendanceFailure
import edu.kiet.innogeeks.R

fun AttendanceFailure.toUiText(): UiText = when (this) {
    is AttendanceFailure.Transport -> error.toUiText()
    is AttendanceFailure.Rejected -> UiText.StringResource(
        when (error) {
            AttendanceError.FORBIDDEN_ROLE -> R.string.attendance_error_forbidden
            AttendanceError.NO_DOMAIN -> R.string.attendance_error_no_domain
            AttendanceError.SESSION_NOT_FOUND -> R.string.attendance_error_session_not_found
            AttendanceError.NOT_IN_DOMAIN -> R.string.attendance_error_not_in_domain
            AttendanceError.VALIDATION -> R.string.attendance_error_validation
            AttendanceError.UNSUPPORTED -> R.string.error_unsupported_version
        }
    )
}
