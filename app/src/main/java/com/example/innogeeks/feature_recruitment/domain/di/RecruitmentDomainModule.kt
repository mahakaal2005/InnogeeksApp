package com.example.innogeeks.feature_recruitment.domain.di

import com.example.innogeeks.feature_recruitment.domain.use_case.GetInterviewBookingUseCase
import com.example.innogeeks.feature_recruitment.domain.use_case.GetRecruitmentStatusUseCase
import com.example.innogeeks.feature_recruitment.domain.use_case.GetSlotsUseCase
import com.example.innogeeks.feature_recruitment.domain.use_case.GetTestSlotBookingUseCase
import com.example.innogeeks.feature_recruitment.domain.use_case.SubmitSlotBookingUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val recruitmentDomainModule = module {
    factoryOf(::GetRecruitmentStatusUseCase)
    factoryOf(::GetTestSlotBookingUseCase)
    factoryOf(::GetInterviewBookingUseCase)
    factoryOf(::GetSlotsUseCase)
    factoryOf(::SubmitSlotBookingUseCase)
}
