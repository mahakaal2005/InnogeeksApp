package com.example.innogeeks.feature_attendance.presentation.myattendance

import com.example.innogeeks.core.domain.model.UserDomain
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.feature_attendance.domain.model.AttendanceSummary

data class MyAttendanceState(
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val summary: AttendanceSummary? = null, // non-null once loaded
    val records: List<AttendanceRecordUi> = emptyList(), // newest first
    val domain: UserDomain? = null
)
