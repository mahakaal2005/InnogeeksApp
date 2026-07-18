package com.example.innogeeks

import android.app.Application
import com.example.innogeeks.core.data.di.coreDataModule
import com.example.innogeeks.feature_onboarding.data.di.onboardingDataModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class InnogeeksApp : Application(){
    override fun onCreate() {
        super.onCreate()

        // startKoin runs once at app launch: it reads every module's recipes and builds the
        // dependency graph. A module not listed here is never loaded — this is the single
        // place the whole app's DI is assembled. androidContext hands Koin the app Context
        // so definitions that need it (DataStore, Room later) can inject it.
        startKoin {
            androidContext(this@InnogeeksApp)
            modules(
                // core (shared across features) — listed first for readability, not required by Koin
                coreDataModule,
                // feature: onboarding
                onboardingDataModule
            )
        }
    }
}