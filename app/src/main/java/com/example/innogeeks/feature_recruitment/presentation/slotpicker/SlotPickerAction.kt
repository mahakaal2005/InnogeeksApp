package com.example.innogeeks.feature_recruitment.presentation.slotpicker

sealed interface SlotPickerAction {
    data class OnSlotClick(val slotId: String) : SlotPickerAction
    data object OnConfirmClick : SlotPickerAction
    data object OnRetryClick : SlotPickerAction
    data object OnBackClick : SlotPickerAction
}
