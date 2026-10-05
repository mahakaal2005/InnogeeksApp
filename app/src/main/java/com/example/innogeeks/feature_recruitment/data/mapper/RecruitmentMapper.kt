package com.example.innogeeks.feature_recruitment.data.mapper

import com.example.innogeeks.feature_recruitment.data.remote.dto.InterviewBookingDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.InterviewDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.InterviewSlotListDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.RecruitmentDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.TestSlotBookingDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.TestSlotDto
import com.example.innogeeks.feature_recruitment.data.remote.dto.TestSlotListDto
import com.example.innogeeks.feature_recruitment.domain.model.Decision
import com.example.innogeeks.feature_recruitment.domain.model.Interview
import com.example.innogeeks.feature_recruitment.domain.model.InterviewBooking
import com.example.innogeeks.feature_recruitment.domain.model.RecruitmentStatus
import com.example.innogeeks.feature_recruitment.domain.model.SlotList
import com.example.innogeeks.feature_recruitment.domain.model.SlotOption
import com.example.innogeeks.feature_recruitment.domain.model.TestResult
import com.example.innogeeks.feature_recruitment.domain.model.TestSlot
import com.example.innogeeks.feature_recruitment.domain.model.TestSlotBooking

fun RecruitmentDto.toRecruitmentStatus(): RecruitmentStatus = RecruitmentStatus(
    paid = paid,
    decision = decision.toDecision(),
    decisionNote = decisionNote,
    testResult = testResult.toTestResult(),
    testSlot = testSlot.toTestSlot(),
    interview = interview.toInterview()
)

private fun String.toDecision(): Decision = when (this) {
    "PENDING" -> Decision.PENDING
    "SELECTED" -> Decision.SELECTED
    "WAITLISTED" -> Decision.WAITLISTED
    "REJECTED" -> Decision.REJECTED
    else -> Decision.PENDING // fallback for unknown values
}

private fun String.toTestResult(): TestResult = when (this) {
    "PASSED" -> TestResult.PASSED
    "FAILED" -> TestResult.FAILED
    else -> TestResult.PENDING // fallback for unknown values
}

private fun TestSlotDto.toTestSlot(): TestSlot = TestSlot(
    booked = booked,
    startTime = startTime,
    endTime = endTime,
    switchingEnabled = switchingEnabled
)

private fun InterviewDto.toInterview(): Interview = Interview(
    assigned = assigned,
    startTime = startTime,
    endTime = endTime,
    location = location,
    meetingUrl = meetingUrl,
    switchingEnabled = switchingEnabled
)

fun TestSlotListDto.toSlotList(): SlotList = SlotList(
    slots = slots.map {
        SlotOption(
            id = it.testSlotId,
            startTime = it.startTime,
            endTime = it.endTime,
            location = null,
            capacity = it.capacity,
            remaining = it.remaining,
            isMine = it.isMine
        )
    },
    switchingEnabled = switchingEnabled
)

fun InterviewSlotListDto.toSlotList(): SlotList = SlotList(
    slots = slots.map {
        SlotOption(
            id = it.interviewSlotId,
            startTime = it.startTime,
            endTime = it.endTime,
            location = it.location,
            capacity = it.capacity,
            remaining = it.remaining,
            isMine = it.isMine
        )
    },
    switchingEnabled = switchingEnabled
)

fun TestSlotBookingDto.toTestSlotBooking(): TestSlotBooking = TestSlotBooking(
    testSlotId = testSlotId,
    startTime = startTime,
    endTime = endTime,
    bookedAt = bookedAt
)

fun InterviewBookingDto.toInterviewBooking(): InterviewBooking = InterviewBooking(
    interviewSlotId = interviewSlotId,
    startTime = startTime,
    endTime = endTime,
    location = location,
    meetingUrl = meetingUrl,
    bookedAt = bookedAt
)
