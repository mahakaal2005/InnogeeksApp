package com.example.innogeeks.feature_resources.presentation.di

import com.example.innogeeks.feature_resources.presentation.resources.ResourcesViewModel
import org.koin.core.qualifier.named
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val resourcesPresentationModule = module {
    viewModel {
        ResourcesViewModel(get(), get(), get(), get(), get(), get(), get(), appScope = get(named("app")))
    }
}
