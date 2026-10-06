package com.example.innogeeks.feature_recruitment.presentation.tracker

import com.example.innogeeks.feature_recruitment.domain.model.SlotKind

sealed interface TrackerEvent {
    data object NavigateToResources : TrackerEvent
    data class ShowSlotPicker(val kind: SlotKind) : TrackerEvent
}
