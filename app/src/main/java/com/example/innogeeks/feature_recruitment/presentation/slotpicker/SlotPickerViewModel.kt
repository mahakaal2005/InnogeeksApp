package com.example.innogeeks.feature_recruitment.presentation.slotpicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.core.presentation.mapper.toUiText
import com.example.innogeeks.feature_recruitment.domain.model.SlotBookingFailure
import com.example.innogeeks.feature_recruitment.domain.model.SlotKind
import com.example.innogeeks.feature_recruitment.domain.use_case.GetSlotsUseCase
import com.example.innogeeks.feature_recruitment.domain.use_case.SubmitSlotBookingUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SlotPickerViewModel(
    private val kind: SlotKind,
    private val getSlotsUseCase: GetSlotsUseCase,
    private val submitSlotBookingUseCase: SubmitSlotBookingUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SlotPickerState(kind = kind))
    val state = _state.asStateFlow()

    private val _events = Channel<SlotPickerEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: SlotPickerAction) {
        when (action) {
            is SlotPickerAction.OnSlotClick -> selectSlot(action.slotId)
            SlotPickerAction.OnConfirmClick -> bookSelectedSlot()
            SlotPickerAction.OnRetryClick -> loadSlots(showLoading = true)
            SlotPickerAction.OnSheetShown -> {
                _state.update { it.copy(selectedSlotId = null, isBooking = false) }
                loadSlots(showLoading = true)
            }
        }
    }

    private fun selectSlot(slotId: String) {
        val current = _state.value
        val slot = current.slots.firstOrNull { it.id == slotId } ?: return
        if (!current.canChange || current.isBooking || slot.isFull && !slot.isMine) return
        // Tapping the already-selected slot clears the selection.
        _state.update { it.copy(selectedSlotId = if (it.selectedSlotId == slotId) null else slotId) }
    }

    private fun loadSlots(showLoading: Boolean) {
        viewModelScope.launch {
            if (showLoading) _state.update { it.copy(isLoading = true, error = null) }
            when (val result = getSlotsUseCase(kind)) {
                is Result.Success -> _state.update { current ->
                    val slots = result.data.slots.map { it.toSlotUi() }
                    // Drop the selection if the slot filled up while the list was open.
                    val stillPickable = slots.any { it.id == current.selectedSlotId && !it.isFull }
                    current.copy(
                        isLoading = false,
                        error = null,
                        slots = slots,
                        currentSlotId = slots.firstOrNull { it.isMine }?.id,
                        switchingEnabled = result.data.switchingEnabled,
                        selectedSlotId = current.selectedSlotId.takeIf { stillPickable }
                    )
                }
                is Result.Error -> _state.update {
                    it.copy(isLoading = false, error = result.error.toUiText())
                }
            }
        }
    }

    private fun bookSelectedSlot() {
        val current = _state.value
        val slotId = current.selectedSlotId ?: return
        if (!current.canConfirm) return

        viewModelScope.launch {
            _state.update { it.copy(isBooking = true) }
            when (val result = submitSlotBookingUseCase(kind, slotId)) {
                is Result.Success -> {
                    _state.update { it.copy(isBooking = false) }
                    _events.send(SlotPickerEvent.BookingConfirmed)
                }
                is Result.Error -> {
                    _state.update { it.copy(isBooking = false) }
                    _events.send(SlotPickerEvent.ShowMessage(result.error.toUiText()))
                    // The server said no for a business reason, so our list is stale: refresh it.
                    if (result.error is SlotBookingFailure.Rejected) loadSlots(showLoading = false)
                }
            }
        }
    }
}
