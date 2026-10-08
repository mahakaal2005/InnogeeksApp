package com.example.innogeeks.feature_attendance.presentation.myattendance

import com.example.innogeeks.feature_attendance.domain.model.AttendanceRecord
import com.example.innogeeks.feature_attendance.domain.model.AttendanceStatus
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

enum class RecordStatus { PRESENT, MISSED, UNMARKED }

data class AttendanceRecordUi(
    val sessionId: String,
    val title: String,
    val status: RecordStatus,
    val dateLabel: String, // "7 Oct"
    val monthLabel: String // "October 2026"
)

fun AttendanceRecord.toAttendanceRecordUi(): AttendanceRecordUi = AttendanceRecordUi(
    sessionId = sessionId,
    title = title,
    status = when (status) {
        AttendanceStatus.PRESENT -> RecordStatus.PRESENT
        AttendanceStatus.ABSENT -> RecordStatus.MISSED
        null -> RecordStatus.UNMARKED
    },
    dateLabel = shortDateFormat.format(date),
    monthLabel = monthFormat.format(date)
)

private val shortDateFormat = LocalDate.Format {
    dayOfMonth(Padding.NONE)
    char(' ')
    monthName(MonthNames.ENGLISH_ABBREVIATED)
}

private val monthFormat = LocalDate.Format {
    monthName(MonthNames.ENGLISH_FULL)
    char(' ')
    year()
}
