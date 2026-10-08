package com.example.innogeeks.feature_attendance.data.remote.dto

import kotlinx.serialization.Serializable

// GET /attendance/me
@Serializable
data class MyAttendanceResponseDto(val data: MyAttendanceDto)

@Serializable
data class MyAttendanceDto(
    val summary: AttendanceSummaryDto,
    val records: List<AttendanceRecordDto> = emptyList()
)

@Serializable
data class AttendanceSummaryDto(
    val total: Int,
    val present: Int,
    val percent: Int
)

@Serializable
data class AttendanceRecordDto(
    val sessionId: String,
    val title: String,
    val date: String, // ISO local date, e.g. 2026-10-07
    val status: String? = null // null while the session is unmarked
)

// GET /attendance/sessions
@Serializable
data class SessionListResponseDto(val data: SessionListDto)

@Serializable
data class SessionListDto(val sessions: List<AttendanceSessionDto> = emptyList())

// Also the POST /attendance/sessions reply.
@Serializable
data class AttendanceSessionDto(
    val id: String,
    val title: String,
    val date: String,
    val markedCount: Int = 0,
    val memberCount: Int = 0
)

// GET /attendance/sessions/:id, and the PUT .../marks reply
@Serializable
data class SessionRosterResponseDto(val data: SessionRosterDto)

@Serializable
data class SessionRosterDto(
    val session: SessionHeaderDto,
    val roster: List<RosterEntryDto> = emptyList()
)

@Serializable
data class SessionHeaderDto(
    val id: String,
    val title: String,
    val date: String
)

@Serializable
data class RosterEntryDto(
    val accountId: String,
    val fullName: String,
    val role: String,
    val status: String? = null
)

// POST /attendance/sessions body
@Serializable
data class CreateSessionRequestDto(val title: String, val date: String)

// PUT /attendance/sessions/:id/marks body
@Serializable
data class SubmitMarksRequestDto(val marks: List<MarkDto>)

@Serializable
data class MarkDto(val accountId: String, val status: String)
