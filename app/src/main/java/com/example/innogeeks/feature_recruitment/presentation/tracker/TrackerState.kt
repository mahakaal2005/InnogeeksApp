package com.example.innogeeks.feature_recruitment.presentation.tracker

import com.example.innogeeks.core.presentation.UiText
import com.example.innogeeks.feature_recruitment.domain.model.SlotKind
import com.example.innogeeks.feature_recruitment.domain.model.RecruitmentStatus

data class TrackerState(
    val isLoading: Boolean = false,
    val recruitmentStatus: RecruitmentStatus? = null,
    val error: UiText? = null,
    val slotPickerKind: SlotKind? = null // non-null while the slot sheet is open
)
