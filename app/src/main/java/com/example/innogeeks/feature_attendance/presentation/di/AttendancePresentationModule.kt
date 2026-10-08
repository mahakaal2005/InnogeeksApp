package com.example.innogeeks.feature_attendance.presentation.di

import com.example.innogeeks.feature_attendance.presentation.myattendance.MyAttendanceViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val attendancePresentationModule = module {
    viewModelOf(::MyAttendanceViewModel)
}
