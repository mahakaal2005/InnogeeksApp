package com.example.innogeeks.feature_attendance.domain.model

import com.example.innogeeks.core.domain.model.UserRole
import kotlinx.datetime.LocalDate

enum class AttendanceStatus { PRESENT, ABSENT }

// One session the signed-in user was marked in.
data class AttendanceRecord(
    val sessionId: String,
    val title: String,
    val date: LocalDate,
    val status: AttendanceStatus
)

data class AttendanceSummary(
    val total: Int,
    val present: Int,
    val percent: Int // 0..100, computed by the server
)

data class MyAttendance(
    val summary: AttendanceSummary,
    val records: List<AttendanceRecord> // newest first
)

data class AttendanceSession(
    val id: String,
    val title: String,
    val date: LocalDate,
    val markedCount: Int,
    val memberCount: Int
)

// status is null until the Coordinator marks this member for the session.
data class RosterEntry(
    val accountId: String,
    val fullName: String,
    val role: UserRole,
    val status: AttendanceStatus?
)

data class SessionRoster(
    val sessionId: String,
    val title: String,
    val date: LocalDate,
    val entries: List<RosterEntry>
)

data class AttendanceMark(
    val accountId: String,
    val status: AttendanceStatus
)
