package com.example.innogeeks.feature_attendance.domain.use_case

import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_attendance.domain.model.SessionRoster
import com.example.innogeeks.feature_attendance.domain.repository.AttendanceRepository

class GetSessionRosterUseCase(
    private val attendanceRepository: AttendanceRepository
) {
    suspend operator fun invoke(sessionId: String): Result<SessionRoster, DataError.Network> =
        attendanceRepository.getSessionRoster(sessionId)
}
