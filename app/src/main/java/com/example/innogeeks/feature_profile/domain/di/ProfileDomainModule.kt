package com.example.innogeeks.feature_profile.domain.di

import com.example.innogeeks.feature_profile.domain.use_case.CancelAccountDeletionUseCase
import com.example.innogeeks.feature_profile.domain.use_case.GetProfileUseCase
import com.example.innogeeks.feature_profile.domain.use_case.RequestAccountDeletionUseCase
import com.example.innogeeks.feature_profile.domain.use_case.UpdateProfileUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val profileDomainModule = module {
    factoryOf(::GetProfileUseCase)
    factoryOf(::UpdateProfileUseCase)
    factoryOf(::RequestAccountDeletionUseCase)
    factoryOf(::CancelAccountDeletionUseCase)
}
