package com.example.innogeeks.feature_attendance.domain.use_case

import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_attendance.domain.model.AttendanceError
import com.example.innogeeks.feature_attendance.domain.model.AttendanceFailure
import com.example.innogeeks.feature_attendance.domain.model.AttendanceSession
import com.example.innogeeks.feature_attendance.domain.repository.AttendanceRepository
import kotlinx.datetime.LocalDate

// A blank title is rejected here so it never costs a network round trip.
class CreateSessionUseCase(
    private val attendanceRepository: AttendanceRepository
) {
    suspend operator fun invoke(title: String, date: LocalDate): Result<AttendanceSession, AttendanceFailure> {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return Result.Error(AttendanceFailure.Rejected(AttendanceError.VALIDATION))
        return attendanceRepository.createSession(trimmed, date)
    }
}
