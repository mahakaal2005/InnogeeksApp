package com.example.innogeeks.feature_recruitment.presentation.slotpicker

import androidx.compose.runtime.Stable
import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.feature_recruitment.domain.model.SlotKind

@Stable
data class SlotPickerState(
    val kind: SlotKind = SlotKind.TEST,
    val isLoading: Boolean = true,
    val slots: List<SlotUi> = emptyList(),
    val selectedSlotId: String? = null,
    val currentSlotId: String? = null,
    val switchingEnabled: Boolean = true,
    val isBooking: Boolean = false,
    val error: UiText? = null
) {
    // A student with no slot yet can always book; switching needs the admin switch on.
    val canChange: Boolean get() = currentSlotId == null || switchingEnabled
    val canConfirm: Boolean
        get() = canChange && !isBooking && selectedSlotId != null && selectedSlotId != currentSlotId
}
