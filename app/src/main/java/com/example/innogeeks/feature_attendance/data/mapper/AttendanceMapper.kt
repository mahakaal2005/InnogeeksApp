package com.example.innogeeks.feature_attendance.data.mapper

import com.example.innogeeks.core.domain.model.UserRole
import com.example.innogeeks.feature_attendance.data.remote.dto.AttendanceRecordDto
import com.example.innogeeks.feature_attendance.data.remote.dto.AttendanceSessionDto
import com.example.innogeeks.feature_attendance.data.remote.dto.MarkDto
import com.example.innogeeks.feature_attendance.data.remote.dto.MyAttendanceDto
import com.example.innogeeks.feature_attendance.data.remote.dto.RosterEntryDto
import com.example.innogeeks.feature_attendance.data.remote.dto.SessionRosterDto
import com.example.innogeeks.feature_attendance.domain.model.AttendanceMark
import com.example.innogeeks.feature_attendance.domain.model.AttendanceRecord
import com.example.innogeeks.feature_attendance.domain.model.AttendanceSession
import com.example.innogeeks.feature_attendance.domain.model.AttendanceStatus
import com.example.innogeeks.feature_attendance.domain.model.AttendanceSummary
import com.example.innogeeks.feature_attendance.domain.model.MyAttendance
import com.example.innogeeks.feature_attendance.domain.model.RosterEntry
import com.example.innogeeks.feature_attendance.domain.model.SessionRoster
import kotlinx.datetime.LocalDate

fun MyAttendanceDto.toMyAttendance(): MyAttendance = MyAttendance(
    summary = AttendanceSummary(summary.total, summary.present, summary.percent),
    records = records.mapNotNull { it.toAttendanceRecord() }
)

// Null for an unknown status or a bad date, so one odd row can't break the whole list.
fun AttendanceRecordDto.toAttendanceRecord(): AttendanceRecord? {
    val parsedDate = date.toLocalDateOrNull() ?: return null
    // A missing status is an unmarked session; only a non-null unknown string drops the row.
    val parsedStatus = if (status == null) null else status.toAttendanceStatus() ?: return null
    return AttendanceRecord(sessionId, title, parsedDate, parsedStatus)
}

fun AttendanceSessionDto.toAttendanceSession(): AttendanceSession? {
    val parsedDate = date.toLocalDateOrNull() ?: return null
    return AttendanceSession(id, title, parsedDate, markedCount, memberCount)
}

fun SessionRosterDto.toSessionRoster(): SessionRoster? {
    val parsedDate = session.date.toLocalDateOrNull() ?: return null
    return SessionRoster(
        sessionId = session.id,
        title = session.title,
        date = parsedDate,
        entries = roster.map { it.toRosterEntry() }
    )
}

// An unknown status reads as unmarked rather than dropping the member from the roster.
fun RosterEntryDto.toRosterEntry(): RosterEntry = RosterEntry(
    accountId = accountId,
    fullName = fullName,
    role = UserRole.entries.firstOrNull { it.name == role } ?: UserRole.MEMBER,
    status = status?.toAttendanceStatus()
)

fun AttendanceMark.toMarkDto(): MarkDto = MarkDto(accountId, status.name)

fun String.toAttendanceStatus(): AttendanceStatus? =
    AttendanceStatus.entries.firstOrNull { it.name == this }

private fun String.toLocalDateOrNull(): LocalDate? = runCatching { LocalDate.parse(this) }.getOrNull()
