package com.example.innogeeks.core.data.di

import com.example.innogeeks.core.data.networking.HttpClientFactory
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.dsl.module

val coreDataModule = module {
    single<HttpClientEngine>{ OkHttp.create() }
    single { HttpClientFactory.create(get()) }
}