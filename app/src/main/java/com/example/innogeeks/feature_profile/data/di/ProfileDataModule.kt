package com.example.innogeeks.feature_profile.data.di

import com.example.innogeeks.feature_profile.data.remote.KtorProfileRemoteDataSource
import com.example.innogeeks.feature_profile.data.remote.ProfileRemoteDataSource
import com.example.innogeeks.feature_profile.data.repository.DefaultProfileRepository
import com.example.innogeeks.feature_profile.domain.repository.ProfileRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val profileDataModule = module {
    singleOf(::KtorProfileRemoteDataSource) { bind<ProfileRemoteDataSource>() }
    singleOf(::DefaultProfileRepository) { bind<ProfileRepository>() }
}
