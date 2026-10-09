package com.example.innogeeks.feature_resources.domain.di

import com.example.innogeeks.feature_resources.domain.use_case.CreateResourceUseCase
import com.example.innogeeks.feature_resources.domain.use_case.DeleteResourceUseCase
import com.example.innogeeks.feature_resources.domain.use_case.UpdateResourceUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val resourcesDomainModule = module {
    factoryOf(::CreateResourceUseCase)
    factoryOf(::UpdateResourceUseCase)
    factoryOf(::DeleteResourceUseCase)
}
