package com.example.innogeeks.feature_resources.data.di

import com.example.innogeeks.core.domain.resources.ResourceShortcutProvider
import com.example.innogeeks.feature_resources.data.AssetResourceShortcutProvider
import com.example.innogeeks.feature_resources.data.AssetResourcesRepository
import com.example.innogeeks.feature_resources.domain.ResourcesRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.core.module.dsl.bind as bindTo
import org.koin.dsl.module

val resourcesDataModule = module {
    single { AssetResourcesRepository(androidContext()) } bind ResourcesRepository::class
    singleOf(::AssetResourceShortcutProvider) { bindTo<ResourceShortcutProvider>() }
}
