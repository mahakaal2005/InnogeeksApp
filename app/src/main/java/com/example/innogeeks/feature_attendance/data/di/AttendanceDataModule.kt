package com.example.innogeeks.feature_attendance.data.di

import com.example.innogeeks.core.domain.attendance.AttendanceSummaryProvider
import com.example.innogeeks.feature_attendance.data.provider.DefaultAttendanceSummaryProvider
import com.example.innogeeks.feature_attendance.data.remote.AttendanceRemoteDataSource
import com.example.innogeeks.feature_attendance.data.remote.FakeAttendanceRemoteDataSource
import com.example.innogeeks.feature_attendance.data.repository.DefaultAttendanceRepository
import com.example.innogeeks.feature_attendance.domain.repository.AttendanceRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

// Fake until the backend ships the attendance endpoints; swap to KtorAttendanceRemoteDataSource then.
val attendanceDataModule = module {
    singleOf(::FakeAttendanceRemoteDataSource) { bind<AttendanceRemoteDataSource>() }
    singleOf(::DefaultAttendanceRepository) { bind<AttendanceRepository>() }
    singleOf(::DefaultAttendanceSummaryProvider) { bind<AttendanceSummaryProvider>() }
}
