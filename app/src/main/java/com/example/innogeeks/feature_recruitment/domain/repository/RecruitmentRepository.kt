package com.example.innogeeks.feature_recruitment.domain.repository

import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.EmptyResult
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_recruitment.domain.model.InterviewBooking
import com.example.innogeeks.feature_recruitment.domain.model.RecruitmentStatus
import com.example.innogeeks.feature_recruitment.domain.model.SlotBookingFailure
import com.example.innogeeks.feature_recruitment.domain.model.SlotKind
import com.example.innogeeks.feature_recruitment.domain.model.SlotList
import com.example.innogeeks.feature_recruitment.domain.model.TestSlotBooking

interface RecruitmentRepository {
    suspend fun getRecruitmentStatus(): Result<RecruitmentStatus, DataError.Network>
    suspend fun getTestSlotBooking(): Result<TestSlotBooking, DataError.Network>
    suspend fun getInterviewBooking(): Result<InterviewBooking, DataError.Network>
    suspend fun getSlots(kind: SlotKind): Result<SlotList, DataError.Network>
    suspend fun bookSlot(kind: SlotKind, slotId: String): EmptyResult<SlotBookingFailure>
}
