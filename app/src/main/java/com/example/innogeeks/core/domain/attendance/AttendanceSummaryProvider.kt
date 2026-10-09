package com.example.innogeeks.core.domain.attendance

import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.Result

// Lets Home read attendance without importing feature_attendance, which implements it.
interface AttendanceSummaryProvider {
    suspend fun getOverview(): Result<AttendanceOverview, DataError.Network>

    // Coordinator/Admin only: sessions of their domain that still have someone unmarked.
    suspend fun getSessionsToMarkCount(): Result<Int, DataError.Network>
}
