package com.example.innogeeks.feature_recruitment.presentation.di

import com.example.innogeeks.feature_recruitment.domain.model.SlotKind
import com.example.innogeeks.feature_recruitment.presentation.slotpicker.SlotPickerViewModel
import com.example.innogeeks.feature_recruitment.presentation.tracker.TrackerViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val recruitmentPresentationModule = module {
    viewModelOf(::TrackerViewModel)
    viewModel { (kind: SlotKind) -> SlotPickerViewModel(kind, get(), get()) }
}
