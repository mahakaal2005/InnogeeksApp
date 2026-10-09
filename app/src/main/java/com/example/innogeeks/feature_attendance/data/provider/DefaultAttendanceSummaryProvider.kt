package com.example.innogeeks.feature_attendance.data.provider

import com.example.innogeeks.core.domain.attendance.AttendanceOverview
import com.example.innogeeks.core.domain.attendance.AttendanceSummaryProvider
import com.example.innogeeks.core.domain.attendance.OverviewSession
import com.example.innogeeks.core.domain.attendance.SessionMark
import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.core.domain.util.mapData
import com.example.innogeeks.feature_attendance.domain.model.AttendanceStatus
import com.example.innogeeks.feature_attendance.domain.repository.AttendanceRepository

class DefaultAttendanceSummaryProvider(
    private val repository: AttendanceRepository
) : AttendanceSummaryProvider {

    override suspend fun getOverview(): Result<AttendanceOverview, DataError.Network> =
        repository.getMyAttendance().mapData { mine ->
            AttendanceOverview(
                percent = mine.summary.percent,
                present = mine.summary.present,
                marked = mine.summary.total,
                sessions = mine.records.map { record ->
                    OverviewSession(
                        id = record.sessionId,
                        title = record.title,
                        date = record.date,
                        mark = when (record.status) {
                            AttendanceStatus.PRESENT -> SessionMark.PRESENT
                            AttendanceStatus.ABSENT -> SessionMark.ABSENT
                            null -> null
                        }
                    )
                }
            )
        }

    // Same rule as the coordinator's "To mark" list.
    override suspend fun getSessionsToMarkCount(): Result<Int, DataError.Network> =
        repository.getDomainSessions().mapData { sessions -> sessions.count { it.markedCount < it.memberCount } }
}
