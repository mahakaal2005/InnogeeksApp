package com.example.innogeeks.feature_domains.data.di

import com.example.innogeeks.feature_domains.data.InMemoryDomainsRepository
import com.example.innogeeks.core.domain.repository.DomainsRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val domainsDataModule = module {
    singleOf(::InMemoryDomainsRepository) { bind<DomainsRepository>() }
}
