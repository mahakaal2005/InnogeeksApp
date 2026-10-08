package com.example.innogeeks.feature_attendance.presentation.di

import com.example.innogeeks.feature_attendance.presentation.myattendance.MyAttendanceViewModel
import com.example.innogeeks.feature_attendance.presentation.roster.SessionRosterViewModel
import com.example.innogeeks.feature_attendance.presentation.sessions.DomainSessionsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val attendancePresentationModule = module {
    viewModelOf(::MyAttendanceViewModel)
    viewModelOf(::DomainSessionsViewModel)
    viewModel { (sessionId: String) -> SessionRosterViewModel(sessionId, get(), get()) }
}
