package com.example.innogeeks.feature_attendance.domain.use_case

import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_attendance.domain.model.AttendanceSession
import com.example.innogeeks.feature_attendance.domain.repository.AttendanceRepository

class GetDomainSessionsUseCase(
    private val attendanceRepository: AttendanceRepository
) {
    suspend operator fun invoke(): Result<List<AttendanceSession>, DataError.Network> =
        attendanceRepository.getDomainSessions()
}
