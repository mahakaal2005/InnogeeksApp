package com.example.innogeeks.feature_recruitment.domain.use_case

import com.example.innogeeks.core.domain.util.EmptyResult
import com.example.innogeeks.feature_recruitment.domain.model.SlotBookingFailure
import com.example.innogeeks.feature_recruitment.domain.model.SlotKind
import com.example.innogeeks.feature_recruitment.domain.repository.RecruitmentRepository

// Books the first slot or switches to another; the server decides who wins a contested seat.
class SubmitSlotBookingUseCase(
    private val recruitmentRepository: RecruitmentRepository
) {
    suspend operator fun invoke(kind: SlotKind, slotId: String): EmptyResult<SlotBookingFailure> =
        recruitmentRepository.bookSlot(kind, slotId)
}
