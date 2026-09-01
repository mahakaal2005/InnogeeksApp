package com.example.innogeeks.feature_recruitment.data.remote

import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_recruitment.data.remote.dto.InterviewDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.RecruitmentDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.TestSlotDto
import kotlinx.coroutines.delay

class FakeRecruitmentRemoteDataSource : RecruitmentRemoteDataSource {

    override suspend fun getRecruitmentStatus(): Result<RecruitmentDto, DataError.Network> {
        delay(800)

        return Result.Success(
            RecruitmentDto(
                paid = true,
                decision = "PENDING",
                decisionNote = null,
                testSlot = TestSlotDto(
                    booked = true,
                    startTime = "2024-08-15T10:00:00Z",
                    endTime = "2024-08-15T11:30:00Z"
                ),
                interview = InterviewDto(
                    assigned = true,
                    startTime = "2024-08-22T09:00:00Z",
                    endTime = "2024-08-22T09:30:00Z",
                    location = "Room 204, Innovation Block",
                    meetingUrl = null
                )
            )
        )
    }
}
