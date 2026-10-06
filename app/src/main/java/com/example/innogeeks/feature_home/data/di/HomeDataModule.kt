package com.example.innogeeks.feature_home.data.di

import com.example.innogeeks.feature_home.data.InMemoryHomeRepository
import com.example.innogeeks.feature_home.domain.HomeRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val homeDataModule = module {
    singleOf(::InMemoryHomeRepository) { bind<HomeRepository>() }
}
