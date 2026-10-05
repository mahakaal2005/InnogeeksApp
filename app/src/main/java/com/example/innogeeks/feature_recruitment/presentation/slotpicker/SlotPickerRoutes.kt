package com.example.innogeeks.feature_recruitment.presentation.slotpicker

import com.example.innogeeks.feature_recruitment.domain.model.SlotKind
import kotlinx.serialization.Serializable

// Local nav graph scoped to the Tracker tab's content area.
@Serializable
internal data object TrackerHomeRoute

@Serializable
internal data class SlotPickerRoute(val kind: SlotKind)
