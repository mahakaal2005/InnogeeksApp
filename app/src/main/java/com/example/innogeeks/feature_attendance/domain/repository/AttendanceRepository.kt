package com.example.innogeeks.feature_attendance.domain.repository

import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_attendance.domain.model.AttendanceFailure
import com.example.innogeeks.feature_attendance.domain.model.AttendanceMark
import com.example.innogeeks.feature_attendance.domain.model.AttendanceSession
import com.example.innogeeks.feature_attendance.domain.model.MyAttendance
import com.example.innogeeks.feature_attendance.domain.model.SessionRoster
import kotlinx.datetime.LocalDate

interface AttendanceRepository {
    suspend fun getMyAttendance(): Result<MyAttendance, DataError.Network>

    // Coordinator/Admin only; the server scopes everything to the caller's own domain.
    suspend fun getDomainSessions(): Result<List<AttendanceSession>, DataError.Network>
    suspend fun getSessionRoster(sessionId: String): Result<SessionRoster, DataError.Network>
    suspend fun createSession(title: String, date: LocalDate): Result<AttendanceSession, AttendanceFailure>
    suspend fun submitMarks(sessionId: String, marks: List<AttendanceMark>): Result<SessionRoster, AttendanceFailure>
}
