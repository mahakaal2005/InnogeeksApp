package com.example.innogeeks.feature_recruitment.data.di

import com.example.innogeeks.feature_recruitment.data.remote.KtorRecruitmentRemoteDataSource
import com.example.innogeeks.feature_recruitment.data.remote.RecruitmentRemoteDataSource
import com.example.innogeeks.feature_recruitment.data.repository.DefaultRecruitmentRepository
import com.example.innogeeks.feature_recruitment.domain.repository.RecruitmentRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val recruitmentDataModule = module {
    singleOf(::KtorRecruitmentRemoteDataSource) { bind<RecruitmentRemoteDataSource>() }
    singleOf(::DefaultRecruitmentRepository) { bind<RecruitmentRepository>() }
}
