package com.example.innogeeks.feature_recruitment.domain.model

data class RecruitmentStatus(
    val paid: Boolean,
    val decision: Decision,
    val decisionNote: String?,
    val testResult: TestResult = TestResult.PENDING,
    val testSlot: TestSlot,
    val interview: Interview
)

enum class Decision {
    PENDING,
    SELECTED,
    WAITLISTED,
    REJECTED
}

enum class TestResult {
    PENDING,
    PASSED,
    FAILED
}

data class TestSlot(
    val booked: Boolean,
    val startTime: String?, // ISO 8601 string, null when not booked
    val endTime: String?,
    val switchingEnabled: Boolean = true
)

data class Interview(
    val assigned: Boolean,
    val startTime: String?, // ISO 8601 string, null when not assigned
    val endTime: String?,
    val location: String?,
    val meetingUrl: String?,
    val switchingEnabled: Boolean = true
)
