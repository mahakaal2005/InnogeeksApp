package com.example.innogeeks.feature_recruitment.domain.use_case

import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_recruitment.domain.model.TestSlotBooking
import com.example.innogeeks.feature_recruitment.domain.repository.RecruitmentRepository

// Not called yet because the Tracker reads the lighter summary in GetRecruitmentStatusUseCase.
class GetTestSlotBookingUseCase(
    private val recruitmentRepository: RecruitmentRepository
) {
    suspend operator fun invoke(): Result<TestSlotBooking, DataError.Network> =
        recruitmentRepository.getTestSlotBooking()
}
