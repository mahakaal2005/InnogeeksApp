package com.example.innogeeks

import android.app.Application
import com.example.innogeeks.core.data.di.coreDataModule
import com.example.innogeeks.feature_onboarding.data.di.onboardingDataModule
import com.example.innogeeks.feature_onboarding.presentation.onboardingPresentationModule
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
                // core
                coreDataModule,
                // feature: onboarding
                onboardingDataModule,
                onboardingPresentationModule,
                // feature: home
                com.example.innogeeks.feature_home.data.di.homeDataModule,
                com.example.innogeeks.feature_home.presentation.di.homePresentationModule
            )
        }
    }
}