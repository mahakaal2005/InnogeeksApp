package com.example.innogeeks.feature_recruitment.presentation.slotpicker

import com.example.innogeeks.core.presentation.UiText

sealed interface SlotPickerEvent {
    data object BookingConfirmed : SlotPickerEvent
    data class ShowMessage(val message: UiText) : SlotPickerEvent
}
