package com.example.innogeeks.feature_recruitment.data.remote

import com.example.innogeeks.core.data.networking.get
import com.example.innogeeks.core.data.networking.postEnveloped
import com.example.innogeeks.core.domain.error.ApiFailure
import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.EmptyResult
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.core.domain.util.asEmptyResult
import com.example.innogeeks.core.domain.util.mapData
import com.example.innogeeks.feature_recruitment.data.remote.dto.BookInterviewSlotRequestDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.BookTestSlotRequestDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.InterviewSlotListDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.InterviewSlotListResponseDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.TestSlotListDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.TestSlotListResponseDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.InterviewBookingDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.InterviewBookingResponseDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.RecruitmentDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.RecruitmentResponseDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.TestSlotBookingDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.TestSlotBookingResponseDto
import io.ktor.client.HttpClient

class KtorRecruitmentRemoteDataSource(
    private val httpClient: HttpClient
) : RecruitmentRemoteDataSource {

    override suspend fun getRecruitmentStatus(): Result<RecruitmentDto, DataError.Network> {
        return httpClient.get<RecruitmentResponseDto>(route = "/api/v1/app/recruitment")
            .mapData { it.data }
    }

    override suspend fun getTestSlotBooking(): Result<TestSlotBookingDto, DataError.Network> {
        return httpClient.get<TestSlotBookingResponseDto>(route = "/api/v1/app/test-slot-booking")
            .mapData { it.data }
    }

    override suspend fun getInterviewBooking(): Result<InterviewBookingDto, DataError.Network> {
        return httpClient.get<InterviewBookingResponseDto>(route = "/api/v1/app/interview-booking")
            .mapData { it.data }
    }

    override suspend fun getTestSlots(): Result<TestSlotListDto, DataError.Network> {
        return httpClient.get<TestSlotListResponseDto>(route = "/api/v1/app/test-slots")
            .mapData { it.data }
    }

    override suspend fun getInterviewSlots(): Result<InterviewSlotListDto, DataError.Network> {
        return httpClient.get<InterviewSlotListResponseDto>(route = "/api/v1/app/interview-slots")
            .mapData { it.data }
    }

    override suspend fun bookTestSlot(testSlotId: String): EmptyResult<ApiFailure> {
        return httpClient.postEnveloped<BookTestSlotRequestDto, TestSlotBookingDto>(
            route = "/api/v1/app/test-slot-booking",
            body = BookTestSlotRequestDto(testSlotId)
        ).asEmptyResult()
    }

    override suspend fun bookInterviewSlot(interviewSlotId: String): EmptyResult<ApiFailure> {
        return httpClient.postEnveloped<BookInterviewSlotRequestDto, InterviewBookingDto>(
            route = "/api/v1/app/interview-booking",
            body = BookInterviewSlotRequestDto(interviewSlotId)
        ).asEmptyResult()
    }
}
