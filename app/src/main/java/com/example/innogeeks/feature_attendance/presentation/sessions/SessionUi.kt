package com.example.innogeeks.feature_attendance.presentation.sessions

import com.example.innogeeks.feature_attendance.domain.model.AttendanceSession
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

data class SessionUi(
    val id: String,
    val title: String,
    val dateLabel: String, // "Wed 7 Oct"
    val markedCount: Int,
    val memberCount: Int
) {
    val needsMarking: Boolean get() = markedCount < memberCount
    val progress: Float get() = if (memberCount == 0) 0f else markedCount.toFloat() / memberCount
}

fun AttendanceSession.toSessionUi(): SessionUi = SessionUi(
    id = id,
    title = title,
    dateLabel = formatSessionDate(date),
    markedCount = markedCount,
    memberCount = memberCount
)

fun formatSessionDate(date: LocalDate): String = sessionDateFormat.format(date)

private val sessionDateFormat = LocalDate.Format {
    dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED)
    char(' ')
    dayOfMonth(Padding.NONE)
    char(' ')
    monthName(MonthNames.ENGLISH_ABBREVIATED)
}
