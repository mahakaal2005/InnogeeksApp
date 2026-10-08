package com.example.innogeeks.feature_attendance.presentation.navigation

import kotlinx.serialization.Serializable

// Local nav graph scoped to the Attendance tab's content area.
@Serializable
internal data object AttendanceHomeRoute

@Serializable
internal data class SessionRosterRoute(val sessionId: String)
