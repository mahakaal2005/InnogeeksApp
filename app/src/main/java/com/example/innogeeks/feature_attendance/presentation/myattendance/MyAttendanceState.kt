package com.example.innogeeks.feature_attendance.presentation.myattendance

import com.example.innogeeks.core.domain.model.UserDomain
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.feature_attendance.domain.model.AttendanceSummary

// The last few sessions stay visible no matter where the month boundary falls.
const val RECENT_COUNT = 4

data class MonthGroupUi(
    val label: String,
    val records: List<AttendanceRecordUi>, // newest first
    val isExpanded: Boolean
)

data class MyAttendanceState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: UiText? = null,
    val summary: AttendanceSummary? = null, // non-null once loaded
    val records: List<AttendanceRecordUi> = emptyList(), // newest first
    val domain: UserDomain? = null,
    val showMissedOnly: Boolean = false,
    val expandedMonths: Set<String> = emptySet()
) {
    val missedCount: Int get() = records.count { it.status == RecordStatus.MISSED }
    val unmarkedCount: Int get() = records.count { it.status == RecordStatus.UNMARKED }
    val missedRecords: List<AttendanceRecordUi> get() = records.filter { it.status == RecordStatus.MISSED }
    val recent: List<AttendanceRecordUi> get() = records.take(RECENT_COUNT)

    // Everything past the recent list, grouped by month in the order it arrives.
    val earlierMonths: List<MonthGroupUi>
        get() = records.drop(RECENT_COUNT)
            .groupBy { it.monthLabel }
            .map { (label, group) -> MonthGroupUi(label, group, label in expandedMonths) }
}
