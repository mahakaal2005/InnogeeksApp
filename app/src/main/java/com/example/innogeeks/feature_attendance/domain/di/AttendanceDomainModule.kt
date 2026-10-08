package com.example.innogeeks.feature_attendance.domain.di

import com.example.innogeeks.feature_attendance.domain.use_case.CreateSessionUseCase
import com.example.innogeeks.feature_attendance.domain.use_case.GetDomainSessionsUseCase
import com.example.innogeeks.feature_attendance.domain.use_case.GetMyAttendanceUseCase
import com.example.innogeeks.feature_attendance.domain.use_case.GetSessionRosterUseCase
import com.example.innogeeks.feature_attendance.domain.use_case.SubmitMarksUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val attendanceDomainModule = module {
    factoryOf(::GetMyAttendanceUseCase)
    factoryOf(::GetDomainSessionsUseCase)
    factoryOf(::GetSessionRosterUseCase)
    factoryOf(::CreateSessionUseCase)
    factoryOf(::SubmitMarksUseCase)
}
