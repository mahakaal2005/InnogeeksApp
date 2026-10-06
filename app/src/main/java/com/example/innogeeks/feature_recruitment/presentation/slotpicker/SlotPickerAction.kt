package com.example.innogeeks.feature_recruitment.presentation.slotpicker

sealed interface SlotPickerAction {
    data class OnSlotClick(val slotId: String) : SlotPickerAction
    data object OnConfirmClick : SlotPickerAction
    data object OnRetryClick : SlotPickerAction

    // Fired each time the sheet opens, so a reopened sheet never shows a stale list or selection.
    data object OnSheetShown : SlotPickerAction
}
