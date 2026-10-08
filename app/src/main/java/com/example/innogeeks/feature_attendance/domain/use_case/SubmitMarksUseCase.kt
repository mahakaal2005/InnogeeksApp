package com.example.innogeeks.feature_attendance.domain.use_case

import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_attendance.domain.model.AttendanceFailure
import com.example.innogeeks.feature_attendance.domain.model.AttendanceMark
import com.example.innogeeks.feature_attendance.domain.model.SessionRoster
import com.example.innogeeks.feature_attendance.domain.repository.AttendanceRepository

// Same call for a first submit and a later edit; the server upserts each mark.
class SubmitMarksUseCase(
    private val attendanceRepository: AttendanceRepository
) {
    suspend operator fun invoke(sessionId: String, marks: List<AttendanceMark>): Result<SessionRoster, AttendanceFailure> =
        attendanceRepository.submitMarks(sessionId, marks)
}
