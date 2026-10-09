package com.example.innogeeks.feature_home.presentation.memberhome

import com.example.innogeeks.core.domain.attendance.AttendanceOverview
import com.example.innogeeks.core.domain.attendance.OverviewSession
import com.example.innogeeks.core.domain.attendance.SessionMark
import com.example.innogeeks.core.domain.resources.ResourceShortcut
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

fun AttendanceOverview.toAttendanceOverviewUi(): AttendanceOverviewUi = AttendanceOverviewUi(
    percent = percent,
    present = present,
    marked = marked,
    sessions = sessions.map { it.toHomeSessionUi() }
)

private fun OverviewSession.toHomeSessionUi(): HomeSessionUi = HomeSessionUi(
    id = id,
    title = title,
    dateLabel = shortDateFormat.format(date),
    status = when (mark) {
        SessionMark.PRESENT -> MarkStatus.PRESENT
        SessionMark.ABSENT -> MarkStatus.MISSED
        null -> MarkStatus.UNMARKED
    }
)

fun ResourceShortcut.toShortcutUi(): ShortcutUi = ShortcutUi(id, title, author, kind, url)

// "test.member@kiet.edu" becomes "Test"; the email is the only name source until a /me endpoint exists.
fun String.toGreetingName(): String =
    substringBefore('@')
        .split('.', '_', '-')
        .firstOrNull { it.isNotBlank() }
        ?.replaceFirstChar { it.uppercaseChar() }
        .orEmpty()

private val shortDateFormat = LocalDate.Format {
    dayOfMonth(Padding.NONE)
    char(' ')
    monthName(MonthNames.ENGLISH_ABBREVIATED)
}
