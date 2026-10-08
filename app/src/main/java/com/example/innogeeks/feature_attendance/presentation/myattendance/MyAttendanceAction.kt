package com.example.innogeeks.feature_attendance.presentation.myattendance

sealed interface MyAttendanceAction {
    data object OnRetryClick : MyAttendanceAction
    data object OnRefresh : MyAttendanceAction

    // Fired when the tab comes back on screen, so a mark made meanwhile shows up.
    data object OnScreenShown : MyAttendanceAction
    data object OnMissedClick : MyAttendanceAction
    data object OnShowAllClick : MyAttendanceAction
    data class OnMonthToggle(val label: String) : MyAttendanceAction
}
