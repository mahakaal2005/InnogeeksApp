package com.example.innogeeks.core.data.di

import com.example.innogeeks.core.data.networking.HttpClientFactory
import com.example.innogeeks.core.data.session.DataStoreSessionRepository
import com.example.innogeeks.core.domain.session.SessionRepository
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val coreDataModule = module {
    // For work that must outlive a screen, such as a delete whose undo window is still open.
    single<CoroutineScope>(named("app")) { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single<HttpClientEngine>{ OkHttp.create() }
    // Single instance: DataStore throws if the same file is opened twice in one process.
    single { DataStoreSessionRepository(androidContext()) } bind SessionRepository::class
    // Declared after the session repo — the bearer provider reads the token from it.
    single { HttpClientFactory.create(engine = get(), sessionRepository = get()) }
}