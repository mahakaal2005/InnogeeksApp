package com.example.innogeeks.feature_profile.data.remote

import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_profile.data.remote.dto.ProfileDto
import com.example.innogeeks.feature_profile.data.remote.dto.UpdateProfileRequestDto
import kotlinx.coroutines.delay

class FakeProfileRemoteDataSource : ProfileRemoteDataSource {

    // In-memory so a fake edit survives across getProfile() calls within one app session.
    private var current = ProfileDto(
        collegeEmail = "setup@kiet.edu",
        fullName = "Atul Kumar",
        phone = "+91 98765 43210",
        // Matches the backend's role ladder (APP_API_CONTRACT.md §12): REGISTERED until an
        // admin promotes, domain stays null until then.
        batch = "2025-29",
        year = 1,
        role = "REGISTERED",
        domain = null
    )

    override suspend fun getProfile(): Result<ProfileDto, DataError.Network> {
        delay(800)
        return Result.Success(current)
    }

    override suspend fun updateProfile(
        request: UpdateProfileRequestDto
    ): Result<ProfileDto, DataError.Network> {
        delay(600)
        current = current.copy(
            fullName = request.fullName ?: current.fullName,
            phone = request.phone ?: current.phone
        )
        return Result.Success(current)
    }
}
