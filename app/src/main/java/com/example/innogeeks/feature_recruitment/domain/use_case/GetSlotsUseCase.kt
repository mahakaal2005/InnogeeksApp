package com.example.innogeeks.feature_recruitment.domain.use_case

import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_recruitment.domain.model.SlotKind
import com.example.innogeeks.feature_recruitment.domain.model.SlotList
import com.example.innogeeks.feature_recruitment.domain.repository.RecruitmentRepository

class GetSlotsUseCase(
    private val recruitmentRepository: RecruitmentRepository
) {
    suspend operator fun invoke(kind: SlotKind): Result<SlotList, DataError.Network> =
        recruitmentRepository.getSlots(kind)
}
