package com.example.innogeeks.feature_recruitment.data.remote

import com.example.innogeeks.core.domain.error.ApiFailure
import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.EmptyResult
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_recruitment.data.remote.dto.InterviewBookingDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.RecruitmentDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.InterviewSlotListDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.TestSlotBookingDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.TestSlotListDto

interface RecruitmentRemoteDataSource {
    suspend fun getRecruitmentStatus(): Result<RecruitmentDto, DataError.Network>

    // NOT_FOUND (404) means not booked yet — a normal empty state, not a failure to retry.
    suspend fun getTestSlotBooking(): Result<TestSlotBookingDto, DataError.Network>
    suspend fun getInterviewBooking(): Result<InterviewBookingDto, DataError.Network>

    suspend fun getTestSlots(): Result<TestSlotListDto, DataError.Network>
    suspend fun getInterviewSlots(): Result<InterviewSlotListDto, DataError.Network>

    // ApiFailure.Api carries the server's error.code (FULL, SWITCHING_DISABLED, ...).
    suspend fun bookTestSlot(testSlotId: String): EmptyResult<ApiFailure>
    suspend fun bookInterviewSlot(interviewSlotId: String): EmptyResult<ApiFailure>
}
