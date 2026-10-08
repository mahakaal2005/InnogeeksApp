package com.example.innogeeks.feature_attendance.presentation.myattendance

import com.example.innogeeks.feature_attendance.domain.model.AttendanceRecord
import com.example.innogeeks.feature_attendance.domain.model.AttendanceStatus
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char

data class AttendanceRecordUi(
    val sessionId: String,
    val title: String,
    val isPresent: Boolean,
    val dayOfMonth: String, // "7"
    val weekday: String, // "Wed"
    val shortDate: String, // "7 Oct"
    val monthLabel: String // "October 2026"
)

fun AttendanceRecord.toAttendanceRecordUi(): AttendanceRecordUi = AttendanceRecordUi(
    sessionId = sessionId,
    title = title,
    isPresent = status == AttendanceStatus.PRESENT,
    dayOfMonth = date.dayOfMonth.toString(),
    weekday = weekdayFormat.format(date),
    shortDate = shortDateFormat.format(date),
    monthLabel = monthFormat.format(date)
)

private val weekdayFormat = LocalDate.Format { dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED) }

private val shortDateFormat = LocalDate.Format {
    dayOfMonth()
    char(' ')
    monthName(MonthNames.ENGLISH_ABBREVIATED)
}

private val monthFormat = LocalDate.Format {
    monthName(MonthNames.ENGLISH_FULL)
    char(' ')
    year()
}
