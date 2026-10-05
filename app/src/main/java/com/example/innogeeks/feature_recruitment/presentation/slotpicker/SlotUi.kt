package com.example.innogeeks.feature_recruitment.presentation.slotpicker

import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.feature_recruitment.domain.model.SlotBookingError
import com.example.innogeeks.feature_recruitment.domain.model.SlotBookingFailure
import com.example.innogeeks.feature_recruitment.domain.model.SlotOption
import com.example.innogeeks.core.presentation.mapper.toUiText
import edu.kiet.innogeeks.R
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime

data class SlotUi(
    val id: String,
    val dateLabel: String,
    val timeLabel: String,
    val location: String?,
    val remaining: Int,
    val capacity: Int,
    val isFull: Boolean,
    val isMine: Boolean
)

fun SlotOption.toSlotUi(): SlotUi = SlotUi(
    id = id,
    dateLabel = formatDate(startTime),
    timeLabel = "${formatTime(startTime)} – ${formatTime(endTime)}",
    location = location,
    remaining = remaining,
    capacity = capacity,
    isFull = isFull,
    isMine = isMine
)

fun SlotBookingFailure.toUiText(): UiText = when (this) {
    is SlotBookingFailure.Transport -> error.toUiText()
    is SlotBookingFailure.Rejected -> UiText.StringResource(
        when (error) {
            SlotBookingError.FULL -> R.string.slot_error_full
            SlotBookingError.SWITCHING_DISABLED -> R.string.slot_error_switching_disabled
            SlotBookingError.LOCKED -> R.string.slot_error_locked
            SlotBookingError.CLOSED -> R.string.slot_error_closed
            SlotBookingError.NOT_FOUND -> R.string.slot_error_not_found
            SlotBookingError.ALREADY_BOOKED -> R.string.slot_error_already_booked
            SlotBookingError.NOT_OPEN -> R.string.slot_error_not_open
            SlotBookingError.ALREADY_DECIDED -> R.string.slot_error_already_decided
            SlotBookingError.UNSUPPORTED -> R.string.error_unsupported_version
        }
    )
}

private val dateFormat = LocalDateTime.Format {
    dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED)
    chars(", ")
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    dayOfMonth()
}

private val timeFormat = LocalDateTime.Format {
    amPmHour()
    char(':')
    minute()
    char(' ')
    amPmMarker("AM", "PM")
}

private fun formatDate(iso: String): String = formatOrRaw(iso) { dateFormat.format(it) }

private fun formatTime(iso: String): String = formatOrRaw(iso) { timeFormat.format(it) }

private fun formatOrRaw(iso: String, format: (LocalDateTime) -> String): String = try {
    format(Instant.parse(iso).toLocalDateTime(TimeZone.currentSystemDefault()))
} catch (_: Exception) {
    iso // fallback to the raw string if parsing fails
}
