package com.example.innogeeks.feature_home.presentation.di

import com.example.innogeeks.feature_home.presentation.home.HomeViewModel
import com.example.innogeeks.feature_home.presentation.memberhome.MemberHomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homePresentationModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::MemberHomeViewModel)
}