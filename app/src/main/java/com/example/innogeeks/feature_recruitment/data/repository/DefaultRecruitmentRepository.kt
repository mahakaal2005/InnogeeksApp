package com.example.innogeeks.feature_recruitment.data.repository

import com.example.innogeeks.core.domain.error.ApiFailure
import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.EmptyResult
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.core.domain.util.mapData
import com.example.innogeeks.core.domain.util.mapError
import com.example.innogeeks.feature_recruitment.data.mapper.toInterviewBooking
import com.example.innogeeks.feature_recruitment.data.mapper.toRecruitmentStatus
import com.example.innogeeks.feature_recruitment.data.mapper.toSlotList
import com.example.innogeeks.feature_recruitment.data.mapper.toTestSlotBooking
import com.example.innogeeks.feature_recruitment.data.remote.RecruitmentRemoteDataSource
import com.example.innogeeks.feature_recruitment.domain.model.InterviewBooking
import com.example.innogeeks.feature_recruitment.domain.model.RecruitmentStatus
import com.example.innogeeks.feature_recruitment.domain.model.SlotBookingError
import com.example.innogeeks.feature_recruitment.domain.model.SlotBookingFailure
import com.example.innogeeks.feature_recruitment.domain.model.SlotKind
import com.example.innogeeks.feature_recruitment.domain.model.SlotList
import com.example.innogeeks.feature_recruitment.domain.model.TestSlotBooking
import com.example.innogeeks.feature_recruitment.domain.repository.RecruitmentRepository

class DefaultRecruitmentRepository(
    private val remoteDataSource: RecruitmentRemoteDataSource
) : RecruitmentRepository {

    override suspend fun getRecruitmentStatus(): Result<RecruitmentStatus, DataError.Network> =
        remoteDataSource.getRecruitmentStatus().mapData { it.toRecruitmentStatus() }

    override suspend fun getTestSlotBooking(): Result<TestSlotBooking, DataError.Network> =
        remoteDataSource.getTestSlotBooking().mapData { it.toTestSlotBooking() }

    override suspend fun getInterviewBooking(): Result<InterviewBooking, DataError.Network> =
        remoteDataSource.getInterviewBooking().mapData { it.toInterviewBooking() }

    override suspend fun getSlots(kind: SlotKind): Result<SlotList, DataError.Network> = when (kind) {
        SlotKind.TEST -> remoteDataSource.getTestSlots().mapData { it.toSlotList() }
        SlotKind.INTERVIEW -> remoteDataSource.getInterviewSlots().mapData { it.toSlotList() }
    }

    override suspend fun bookSlot(kind: SlotKind, slotId: String): EmptyResult<SlotBookingFailure> {
        val result = when (kind) {
            SlotKind.TEST -> remoteDataSource.bookTestSlot(slotId)
            SlotKind.INTERVIEW -> remoteDataSource.bookInterviewSlot(slotId)
        }
        return result.mapError { failure ->
            when (failure) {
                is ApiFailure.Api -> SlotBookingFailure.Rejected(SlotBookingError.fromCode(failure.code))
                is ApiFailure.Transport -> SlotBookingFailure.Transport(failure.error)
            }
        }
    }
}
