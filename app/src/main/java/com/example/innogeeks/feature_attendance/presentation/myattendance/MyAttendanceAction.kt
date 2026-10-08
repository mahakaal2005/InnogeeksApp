package com.example.innogeeks.feature_attendance.presentation.myattendance

sealed interface MyAttendanceAction {
    data object OnRetryClick : MyAttendanceAction
}
