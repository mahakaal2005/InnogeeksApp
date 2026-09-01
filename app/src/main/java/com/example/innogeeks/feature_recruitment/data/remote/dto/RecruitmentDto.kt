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
    val testSlot: TestSlotDto,
    val interview: InterviewDto
)

@Serializable
data class TestSlotDto(
    val booked: Boolean,
    val startTime: String? = null,
    val endTime: String? = null
)

@Serializable
data class InterviewDto(
    val assigned: Boolean,
    val startTime: String? = null,
    val endTime: String? = null,
    val location: String? = null,
    val meetingUrl: String? = null
)
