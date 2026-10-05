package com.example.innogeeks.feature_recruitment.domain.model

import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.Error

enum class SlotKind { TEST, INTERVIEW }

data class SlotOption(
    val id: String,
    val startTime: String, // ISO 8601
    val endTime: String,
    val location: String?, // interview only
    val capacity: Int,
    val remaining: Int,
    val isMine: Boolean
) {
    val isFull: Boolean get() = remaining <= 0
}

data class SlotList(
    val slots: List<SlotOption>,
    val switchingEnabled: Boolean
)

// Server business codes for the book/switch calls (contract §14, §15); unknown codes map to UNSUPPORTED.
enum class SlotBookingError {
    FULL,
    SWITCHING_DISABLED,
    LOCKED,
    CLOSED,
    NOT_FOUND,
    ALREADY_BOOKED,
    NOT_OPEN,
    ALREADY_DECIDED,
    UNSUPPORTED;

    companion object {
        fun fromCode(code: String): SlotBookingError = when (code) {
            "TEST_SLOT_FULL", "INTERVIEW_SLOT_FULL" -> FULL
            "TEST_SLOT_SWITCHING_DISABLED", "INTERVIEW_SLOT_SWITCHING_DISABLED" -> SWITCHING_DISABLED
            "TEST_SLOT_LOCKED", "INTERVIEW_SLOT_LOCKED" -> LOCKED
            "TEST_SLOT_CLOSED", "INTERVIEW_SLOT_CLOSED" -> CLOSED
            "TEST_SLOT_NOT_FOUND", "INTERVIEW_SLOT_NOT_FOUND" -> NOT_FOUND
            "TEST_SLOT_ALREADY_BOOKED", "INTERVIEW_SLOT_ALREADY_BOOKED" -> ALREADY_BOOKED
            "INTERVIEW_BOOKING_NOT_OPEN" -> NOT_OPEN
            "RECRUITMENT_ALREADY_DECIDED" -> ALREADY_DECIDED
            else -> UNSUPPORTED
        }
    }
}

sealed interface SlotBookingFailure : Error {
    data class Rejected(val error: SlotBookingError) : SlotBookingFailure
    data class Transport(val error: DataError.Network) : SlotBookingFailure
}
