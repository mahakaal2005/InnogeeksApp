package com.example.innogeeks.feature_profile.data.remote

import com.example.innogeeks.core.data.networking.get
import com.example.innogeeks.core.data.networking.patch
import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_profile.data.remote.dto.ProfileDto
import com.example.innogeeks.feature_profile.data.remote.dto.UpdateProfileRequestDto
import io.ktor.client.HttpClient

class KtorProfileRemoteDataSource(
    private val httpClient: HttpClient
) : ProfileRemoteDataSource {

    override suspend fun getProfile(): Result<ProfileDto, DataError.Network> =
        httpClient.get(route = "/api/v1/app/me")

    override suspend fun updateProfile(
        request: UpdateProfileRequestDto
    ): Result<ProfileDto, DataError.Network> =
        httpClient.patch(route = "/api/v1/app/me", body = request)
}
