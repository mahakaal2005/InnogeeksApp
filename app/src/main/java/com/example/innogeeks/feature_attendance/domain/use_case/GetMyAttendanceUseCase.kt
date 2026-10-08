package com.example.innogeeks.feature_attendance.domain.use_case

import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_attendance.domain.model.MyAttendance
import com.example.innogeeks.feature_attendance.domain.repository.AttendanceRepository

class GetMyAttendanceUseCase(
    private val attendanceRepository: AttendanceRepository
) {
    suspend operator fun invoke(): Result<MyAttendance, DataError.Network> =
        attendanceRepository.getMyAttendance()
}
