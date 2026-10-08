package com.example.innogeeks.feature_attendance.data.remote

import com.example.innogeeks.core.data.networking.get
import com.example.innogeeks.core.data.networking.postEnveloped
import com.example.innogeeks.core.data.networking.putEnveloped
import com.example.innogeeks.core.domain.error.ApiFailure
import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.core.domain.util.mapData
import com.example.innogeeks.feature_attendance.data.remote.dto.AttendanceSessionDto
import com.example.innogeeks.feature_attendance.data.remote.dto.CreateSessionRequestDto
import com.example.innogeeks.feature_attendance.data.remote.dto.MyAttendanceDto
import com.example.innogeeks.feature_attendance.data.remote.dto.MyAttendanceResponseDto
import com.example.innogeeks.feature_attendance.data.remote.dto.SessionListDto
import com.example.innogeeks.feature_attendance.data.remote.dto.SessionListResponseDto
import com.example.innogeeks.feature_attendance.data.remote.dto.SessionRosterDto
import com.example.innogeeks.feature_attendance.data.remote.dto.SessionRosterResponseDto
import com.example.innogeeks.feature_attendance.data.remote.dto.SubmitMarksRequestDto
import io.ktor.client.HttpClient

// Not bound yet: the attendance endpoints don't exist on the backend (docs/BACKEND_NEEDS_2026-10-08_attendance.md).
class KtorAttendanceRemoteDataSource(
    private val httpClient: HttpClient
) : AttendanceRemoteDataSource {

    override suspend fun getMyAttendance(): Result<MyAttendanceDto, DataError.Network> {
        return httpClient.get<MyAttendanceResponseDto>(route = "/api/v1/app/attendance/me")
            .mapData { it.data }
    }

    override suspend fun getDomainSessions(): Result<SessionListDto, DataError.Network> {
        return httpClient.get<SessionListResponseDto>(route = "/api/v1/app/attendance/sessions")
            .mapData { it.data }
    }

    override suspend fun getSessionRoster(sessionId: String): Result<SessionRosterDto, DataError.Network> {
        return httpClient.get<SessionRosterResponseDto>(route = "/api/v1/app/attendance/sessions/$sessionId")
            .mapData { it.data }
    }

    override suspend fun createSession(body: CreateSessionRequestDto): Result<AttendanceSessionDto, ApiFailure> {
        return httpClient.postEnveloped<CreateSessionRequestDto, AttendanceSessionDto>(
            route = "/api/v1/app/attendance/sessions",
            body = body
        )
    }

    override suspend fun submitMarks(sessionId: String, body: SubmitMarksRequestDto): Result<SessionRosterDto, ApiFailure> {
        return httpClient.putEnveloped<SubmitMarksRequestDto, SessionRosterDto>(
            route = "/api/v1/app/attendance/sessions/$sessionId/marks",
            body = body
        )
    }
}
