package com.example.innogeeks.feature_attendance.presentation.roster

import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.feature_attendance.domain.model.AttendanceStatus

enum class RosterFilter { ALL, UNMARKED, ABSENT }

data class SessionRosterState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val error: UiText? = null,
    val title: String = "",
    val dateLabel: String = "",
    val rows: List<RosterRowUi> = emptyList(), // sorted once on load, never re-sorted while marking
    val marks: Map<String, AttendanceStatus?> = emptyMap(),
    val originalMarks: Map<String, AttendanceStatus?> = emptyMap(),
    val query: String = "",
    val filter: RosterFilter = RosterFilter.ALL,
    val showDiscardDialog: Boolean = false
) {
    val markedCount: Int get() = marks.values.count { it != null }
    val absentCount: Int get() = marks.values.count { it == AttendanceStatus.ABSENT }
    val unmarkedCount: Int get() = rows.size - markedCount
    val changedCount: Int get() = rows.count { marks[it.accountId] != originalMarks[it.accountId] }

    // Some marks already saved means this is an edit, so the bar says "Save changes".
    val isEditing: Boolean get() = originalMarks.values.any { it != null }

    val visibleRows: List<RosterRowUi>
        get() = rows.filter { row ->
            val mark = marks[row.accountId]
            val matchesFilter = when (filter) {
                RosterFilter.ALL -> true
                RosterFilter.UNMARKED -> mark == null
                RosterFilter.ABSENT -> mark == AttendanceStatus.ABSENT
            }
            matchesFilter && row.name.contains(query.trim(), ignoreCase = true)
        }
}
