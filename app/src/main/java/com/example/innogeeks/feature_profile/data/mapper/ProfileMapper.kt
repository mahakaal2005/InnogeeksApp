package com.example.innogeeks.feature_profile.data.mapper

import com.example.innogeeks.feature_profile.data.remote.dto.ProfileDto
import com.example.innogeeks.feature_profile.domain.model.StudentProfile

fun ProfileDto.toStudentProfile(): StudentProfile = StudentProfile(
    collegeEmail = collegeEmail,
    fullName = fullName,
    phone = phone,
    batch = batch,
    year = year,
    role = role,
    domain = domain
)
