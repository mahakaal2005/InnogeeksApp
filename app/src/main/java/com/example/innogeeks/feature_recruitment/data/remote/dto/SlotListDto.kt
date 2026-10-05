package com.example.innogeeks.feature_recruitment.data.remote.dto

import kotlinx.serialization.Serializable

// GET /test-slots
@Serializable
data class TestSlotListResponseDto(val data: TestSlotListDto)

@Serializable
data class TestSlotListDto(
    val switchingEnabled: Boolean = true,
    val slots: List<TestSlotOptionDto> = emptyList()
)

@Serializable
data class TestSlotOptionDto(
    val testSlotId: String,
    val startTime: String,
    val endTime: String,
    val capacity: Int,
    val remaining: Int,
    val isMine: Boolean
)

// GET /interview-slots
@Serializable
data class InterviewSlotListResponseDto(val data: InterviewSlotListDto)

@Serializable
data class InterviewSlotListDto(
    val switchingEnabled: Boolean = true,
    val slots: List<InterviewSlotOptionDto> = emptyList()
)

@Serializable
data class InterviewSlotOptionDto(
    val interviewSlotId: String,
    val startTime: String,
    val endTime: String,
    val location: String? = null,
    val meetingUrl: String? = null,
    val capacity: Int,
    val remaining: Int,
    val isMine: Boolean
)

// POST /test-slot-booking and POST /interview-booking bodies
@Serializable
data class BookTestSlotRequestDto(val testSlotId: String)

@Serializable
data class BookInterviewSlotRequestDto(val interviewSlotId: String)
