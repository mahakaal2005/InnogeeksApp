package com.example.innogeeks.feature_events.presentation.events

import com.example.innogeeks.feature_events.domain.model.ClubEvent
import com.example.innogeeks.core.presentation.UiText

data class EventsState(
    val isLoading: Boolean = true,
    val events: List<ClubEvent> = emptyList(),
    val error: UiText? = null
) {
    // Soonest-future first, oldest-past last — one flat list, no upcoming/past split.
    val sortedEvents: List<ClubEvent>
        get() = events.sortedByDescending { it.date }
}
