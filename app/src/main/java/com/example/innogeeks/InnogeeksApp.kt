package com.example.innogeeks

import android.app.Application
import com.example.innogeeks.core.data.di.coreDataModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class InnogeeksApp : Application(){
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@InnogeeksApp)
            modules(
                coreDataModule
            )
        }
    }
}