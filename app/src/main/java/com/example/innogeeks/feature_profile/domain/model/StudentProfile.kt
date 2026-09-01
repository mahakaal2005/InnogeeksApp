package com.example.innogeeks.feature_profile.domain.model

data class StudentProfile(
    val collegeEmail: String,
    val fullName: String?,
    val phone: String?,
    val batch: String?,
    val year: Int?,
    val role: String,
    val domain: String?
)
