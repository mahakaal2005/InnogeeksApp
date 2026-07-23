package com.example.innogeeks.feature_home.data.di

import com.example.innogeeks.feature_home.data.InMemoryHomeRepository
import com.example.innogeeks.feature_home.domain.HomeRepository
import org.koin.dsl.module

val homeDataModule = module {
    single<HomeRepository> { InMemoryHomeRepository() }
}
