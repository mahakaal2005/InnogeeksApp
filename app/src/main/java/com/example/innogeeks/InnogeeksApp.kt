package com.example.innogeeks

import android.app.Application
import com.example.innogeeks.core.data.di.coreDataModule
import com.example.innogeeks.feature_domains.data.di.domainsDataModule
import com.example.innogeeks.feature_domains.presentation.di.domainsPresentationModule
import com.example.innogeeks.feature_events.data.di.eventsDataModule
import com.example.innogeeks.feature_events.presentation.di.eventsPresentationModule
import com.example.innogeeks.feature_home.data.di.homeDataModule
import com.example.innogeeks.feature_home.presentation.di.homePresentationModule
import com.example.innogeeks.feature_onboarding.data.di.onboardingDataModule
import com.example.innogeeks.feature_onboarding.domain.di.onboardingDomainModule
import com.example.innogeeks.feature_onboarding.presentation.onboardingPresentationModule
import com.example.innogeeks.feature_profile.data.di.profileDataModule
import com.example.innogeeks.feature_profile.domain.di.profileDomainModule
import com.example.innogeeks.feature_profile.presentation.di.profilePresentationModule
import com.example.innogeeks.feature_attendance.data.di.attendanceDataModule
import com.example.innogeeks.feature_attendance.domain.di.attendanceDomainModule
import com.example.innogeeks.feature_attendance.presentation.di.attendancePresentationModule
import com.example.innogeeks.feature_recruitment.data.di.recruitmentDataModule
import com.example.innogeeks.feature_recruitment.domain.di.recruitmentDomainModule
import com.example.innogeeks.feature_recruitment.presentation.di.recruitmentPresentationModule
import com.example.innogeeks.feature_resources.data.di.resourcesDataModule
import com.example.innogeeks.feature_resources.domain.di.resourcesDomainModule
import com.example.innogeeks.feature_resources.presentation.di.resourcesPresentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class InnogeeksApp : Application(){
    override fun onCreate() {
        super.onCreate()

        // startKoin runs once at launch and assembles every module's recipes into the app's DI graph.
        startKoin {
            androidContext(this@InnogeeksApp)
            modules(
                // core
                coreDataModule,
                // feature: onboarding
                onboardingDataModule,
                onboardingDomainModule,
                onboardingPresentationModule,
                // feature: home
                homeDataModule,
                homePresentationModule,
                // feature: attendance
                attendanceDataModule,
                attendanceDomainModule,
                attendancePresentationModule,
                // feature: domains
                domainsDataModule,
                domainsPresentationModule,
                // feature: events
                eventsDataModule,
                eventsPresentationModule,
                // feature: profile
                profileDataModule,
                profileDomainModule,
                profilePresentationModule,
                // feature: recruitment
                recruitmentDataModule,
                recruitmentDomainModule,
                recruitmentPresentationModule,
                // feature: resources
                resourcesDataModule,
                resourcesDomainModule,
                resourcesPresentationModule
            )
        }
    }
}