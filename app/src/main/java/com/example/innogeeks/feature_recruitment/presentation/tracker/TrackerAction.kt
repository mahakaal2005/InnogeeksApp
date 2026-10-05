package com.example.innogeeks.feature_recruitment.presentation.tracker

import com.example.innogeeks.feature_recruitment.domain.model.SlotKind

sealed interface TrackerAction {
    data object OnRetryClick : TrackerAction
    data object OnBrowseResourcesClick : TrackerAction
    data class OnPickSlotClick(val kind: SlotKind) : TrackerAction

    // Fired when the slot picker saved a booking, so the journey refreshes without a spinner.
    data object OnSlotsChanged : TrackerAction
}
