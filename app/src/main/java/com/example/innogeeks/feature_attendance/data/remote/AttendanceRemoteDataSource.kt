package com.example.innogeeks.feature_attendance.data.remote

import com.example.innogeeks.core.domain.error.ApiFailure
import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_attendance.data.remote.dto.AttendanceSessionDto
import com.example.innogeeks.feature_attendance.data.remote.dto.CreateSessionRequestDto
import com.example.innogeeks.feature_attendance.data.remote.dto.MyAttendanceDto
import com.example.innogeeks.feature_attendance.data.remote.dto.SessionListDto
import com.example.innogeeks.feature_attendance.data.remote.dto.SessionRosterDto
import com.example.innogeeks.feature_attendance.data.remote.dto.SubmitMarksRequestDto

interface AttendanceRemoteDataSource {
    suspend fun getMyAttendance(): Result<MyAttendanceDto, DataError.Network>
    suspend fun getDomainSessions(): Result<SessionListDto, DataError.Network>
    suspend fun getSessionRoster(sessionId: String): Result<SessionRosterDto, DataError.Network>

    // ApiFailure.Api carries the server's error.code (NOT_IN_DOMAIN, SESSION_NOT_FOUND, ...).
    suspend fun createSession(body: CreateSessionRequestDto): Result<AttendanceSessionDto, ApiFailure>
    suspend fun submitMarks(sessionId: String, body: SubmitMarksRequestDto): Result<SessionRosterDto, ApiFailure>
}
