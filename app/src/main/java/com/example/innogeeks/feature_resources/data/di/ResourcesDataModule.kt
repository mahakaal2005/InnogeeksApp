package com.example.innogeeks.feature_resources.data.di

import com.example.innogeeks.core.domain.resources.ResourceShortcutProvider
import com.example.innogeeks.feature_resources.data.DefaultResourceShortcutProvider
import com.example.innogeeks.feature_resources.data.remote.FakeResourcesRemoteDataSource
import com.example.innogeeks.feature_resources.data.remote.ResourcesRemoteDataSource
import com.example.innogeeks.feature_resources.data.repository.DefaultResourcesRepository
import com.example.innogeeks.feature_resources.domain.ResourcesRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

// Fake until the backend ships the resources endpoints; swap to KtorResourcesRemoteDataSource then.
val resourcesDataModule = module {
    singleOf(::FakeResourcesRemoteDataSource) { bind<ResourcesRemoteDataSource>() }
    singleOf(::DefaultResourcesRepository) { bind<ResourcesRepository>() }
    singleOf(::DefaultResourceShortcutProvider) { bind<ResourceShortcutProvider>() }
}
