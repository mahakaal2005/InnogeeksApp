package com.example.innogeeks.feature_attendance.presentation.roster

import com.example.innogeeks.core.domain.model.UserRole
import com.example.innogeeks.feature_attendance.domain.model.RosterEntry

data class RosterRowUi(
    val accountId: String,
    val name: String,
    val initials: String,
    val isCoordinator: Boolean,
    val attendancePercent: Int? // null until the person has any marks
) {
    val isLowAttendance: Boolean get() = attendancePercent != null && attendancePercent < LOW_ATTENDANCE_PERCENT
}

private const val LOW_ATTENDANCE_PERCENT = 50

fun RosterEntry.toRosterRowUi(): RosterRowUi = RosterRowUi(
    accountId = accountId,
    name = fullName,
    initials = fullName.split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() },
    isCoordinator = role == UserRole.COORDINATOR,
    attendancePercent = attendancePercent
)

// Regulars first so the coordinator can tick them in one pass; people with no history go last.
fun List<RosterEntry>.sortedForMarking(): List<RosterEntry> =
    sortedWith(compareByDescending<RosterEntry> { it.attendancePercent ?: -1 }.thenBy { it.fullName })
