package com.example.innogeeks.feature_attendance.presentation.roster

import com.example.innogeeks.core.presentation.UiText

sealed interface SessionRosterEvent {
    data class Saved(val present: Int, val absent: Int) : SessionRosterEvent
    data object Close : SessionRosterEvent
    data class ShowMessage(val message: UiText) : SessionRosterEvent
}
