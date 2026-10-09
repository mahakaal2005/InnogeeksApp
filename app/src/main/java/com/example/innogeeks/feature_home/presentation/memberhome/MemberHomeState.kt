package com.example.innogeeks.feature_home.presentation.memberhome

import androidx.compose.runtime.Stable
import com.example.innogeeks.core.domain.model.UserDomain
import com.example.innogeeks.core.domain.model.UserRole
import com.example.innogeeks.core.domain.resources.ResourceKind
import com.example.innogeeks.core.presentation.UiText

enum class DayPart { MORNING, AFTERNOON, EVENING }

enum class MarkStatus { PRESENT, MISSED, UNMARKED }

data class HomeSessionUi(
    val id: String,
    val title: String,
    val dateLabel: String, // "7 Oct"
    val status: MarkStatus
)

@Stable
data class AttendanceOverviewUi(
    val percent: Int,
    val present: Int,
    val marked: Int,
    val sessions: List<HomeSessionUi> // newest first
) {
    val waiting: Int get() = sessions.count { it.status == MarkStatus.UNMARKED }
    val recent: List<HomeSessionUi> get() = sessions.take(3)

    // Oldest to newest, so the strip reads left to right like time.
    val strip: List<HomeSessionUi> get() = sessions.take(6).reversed()
}

data class ShortcutUi(
    val id: String,
    val title: String,
    val author: String,
    val kind: ResourceKind,
    val url: String
)

@Stable
data class MemberHomeState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val dayPart: DayPart = DayPart.MORNING,
    val name: String = "",
    val initials: String = "",
    val role: UserRole? = null,
    val domain: UserDomain? = null,
    val overview: AttendanceOverviewUi? = null,
    val attendanceError: UiText? = null,
    val toMarkCount: Int? = null, // null for members, which hides the coordinator bar
    val shortcuts: List<ShortcutUi> = emptyList(),
    val totalResources: Int = 0
)
