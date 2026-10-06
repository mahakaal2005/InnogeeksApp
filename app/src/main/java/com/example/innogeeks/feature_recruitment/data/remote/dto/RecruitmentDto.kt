package com.example.innogeeks.feature_recruitment.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RecruitmentResponseDto(
    val data: RecruitmentDto
)

@Serializable
data class RecruitmentDto(
    val paid: Boolean,
    val decision: String,
    val decisionNote: String? = null,
    val testResult: String = "PENDING",
    val testSlot: TestSlotDto,
    val interview: InterviewDto
)

@Serializable
data class TestSlotDto(
    val booked: Boolean,
    val startTime: String? = null,
    val endTime: String? = null,
    val switchingEnabled: Boolean = true
)

@Serializable
data class InterviewDto(
    val assigned: Boolean,
    val startTime: String? = null,
    val endTime: String? = null,
    val location: String? = null,
    val meetingUrl: String? = null,
    val switchingEnabled: Boolean = true
)

// GET /test-slot-booking carries the slot id and assignment time; a 404 TEST_SLOT_NOT_BOOKED means no slot yet and arrives as DataError.Network.NOT_FOUND.
@Serializable
data class TestSlotBookingResponseDto(
    val data: TestSlotBookingDto
)

@Serializable
data class TestSlotBookingDto(
    val testSlotId: String,
    val startTime: String,
    val endTime: String,
    val bookedAt: String
)

// GET /interview-booking — same relationship to RecruitmentDto.interview as the test-slot
// pair above; 404 INTERVIEW_SLOT_NOT_BOOKED means not assigned yet.
@Serializable
data class InterviewBookingResponseDto(
    val data: InterviewBookingDto
)

@Serializable
data class InterviewBookingDto(
    val interviewSlotId: String,
    val startTime: String,
    val endTime: String,
    val location: String? = null,
    val meetingUrl: String? = null,
    val bookedAt: String
)
