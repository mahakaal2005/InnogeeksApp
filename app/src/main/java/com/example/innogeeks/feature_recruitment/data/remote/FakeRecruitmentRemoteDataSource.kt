package com.example.innogeeks.feature_recruitment.data.remote

import com.example.innogeeks.core.domain.error.ApiFailure
import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.EmptyResult
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_recruitment.data.remote.dto.InterviewBookingDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.InterviewDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.RecruitmentDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.TestSlotBookingDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.InterviewSlotListDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.InterviewSlotOptionDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.TestSlotDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.TestSlotListDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.TestSlotOptionDto
import kotlinx.coroutines.delay

class FakeRecruitmentRemoteDataSource : RecruitmentRemoteDataSource {

    // In-memory seats so a dev run shows the same full/switch behaviour as the server.
    private var myTestSlotId: String? = null
    private var myInterviewSlotId: String? = null
    private val testRemaining = mutableMapOf("t1" to 2, "t2" to 0, "t3" to 5)
    private val interviewRemaining = mutableMapOf("i1" to 1, "i2" to 3)

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

    override suspend fun getTestSlotBooking(): Result<TestSlotBookingDto, DataError.Network> {
        delay(600)
        return Result.Success(
            TestSlotBookingDto(
                testSlotId = "fake-test-slot-1",
                startTime = "2024-08-15T10:00:00Z",
                endTime = "2024-08-15T11:30:00Z",
                bookedAt = "2024-08-01T12:00:00Z"
            )
        )
    }

    override suspend fun getInterviewBooking(): Result<InterviewBookingDto, DataError.Network> {
        delay(600)
        return Result.Success(
            InterviewBookingDto(
                interviewSlotId = "fake-interview-slot-1",
                startTime = "2024-08-22T09:00:00Z",
                endTime = "2024-08-22T09:30:00Z",
                location = "Room 204, Innovation Block",
                meetingUrl = null,
                bookedAt = "2024-08-01T12:00:00Z"
            )
        )
    }

    override suspend fun getTestSlots(): Result<TestSlotListDto, DataError.Network> {
        delay(600)
        val times = mapOf(
            "t1" to ("2030-08-15T10:00:00Z" to "2030-08-15T11:30:00Z"),
            "t2" to ("2030-08-15T12:00:00Z" to "2030-08-15T13:30:00Z"),
            "t3" to ("2030-08-16T10:00:00Z" to "2030-08-16T11:30:00Z")
        )
        return Result.Success(
            TestSlotListDto(
                switchingEnabled = true,
                slots = times.map { (id, window) ->
                    TestSlotOptionDto(id, window.first, window.second, 20, testRemaining.getValue(id), id == myTestSlotId)
                }
            )
        )
    }

    override suspend fun getInterviewSlots(): Result<InterviewSlotListDto, DataError.Network> {
        delay(600)
        val times = mapOf(
            "i1" to ("2030-08-22T09:00:00Z" to "2030-08-22T09:30:00Z"),
            "i2" to ("2030-08-22T10:00:00Z" to "2030-08-22T10:30:00Z")
        )
        return Result.Success(
            InterviewSlotListDto(
                switchingEnabled = true,
                slots = times.map { (id, window) ->
                    InterviewSlotOptionDto(
                        id, window.first, window.second, "Room 204, Innovation Block", null,
                        4, interviewRemaining.getValue(id), id == myInterviewSlotId
                    )
                }
            )
        )
    }

    override suspend fun bookTestSlot(testSlotId: String): EmptyResult<ApiFailure> {
        delay(500)
        if (testSlotId == myTestSlotId) return Result.Success(Unit)
        if ((testRemaining[testSlotId] ?: return Result.Error(ApiFailure.Api("TEST_SLOT_NOT_FOUND"))) <= 0) {
            return Result.Error(ApiFailure.Api("TEST_SLOT_FULL"))
        }
        myTestSlotId?.let { testRemaining[it] = testRemaining.getValue(it) + 1 }
        testRemaining[testSlotId] = testRemaining.getValue(testSlotId) - 1
        myTestSlotId = testSlotId
        return Result.Success(Unit)
    }

    override suspend fun bookInterviewSlot(interviewSlotId: String): EmptyResult<ApiFailure> {
        delay(500)
        if (interviewSlotId == myInterviewSlotId) return Result.Success(Unit)
        if ((interviewRemaining[interviewSlotId] ?: return Result.Error(ApiFailure.Api("INTERVIEW_SLOT_NOT_FOUND"))) <= 0) {
            return Result.Error(ApiFailure.Api("INTERVIEW_SLOT_FULL"))
        }
        myInterviewSlotId?.let { interviewRemaining[it] = interviewRemaining.getValue(it) + 1 }
        interviewRemaining[interviewSlotId] = interviewRemaining.getValue(interviewSlotId) - 1
        myInterviewSlotId = interviewSlotId
        return Result.Success(Unit)
    }
}
