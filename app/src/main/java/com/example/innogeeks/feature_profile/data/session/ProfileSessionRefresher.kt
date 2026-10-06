package com.example.innogeeks.feature_profile.data.session

import com.example.innogeeks.core.domain.model.UserDomain
import com.example.innogeeks.core.domain.model.UserRole
import com.example.innogeeks.core.domain.session.SessionRefresher
import com.example.innogeeks.core.domain.session.SessionRepository
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_profile.domain.repository.ProfileRepository

class ProfileSessionRefresher(
    private val profileRepository: ProfileRepository,
    private val sessionRepository: SessionRepository
) : SessionRefresher {

    // A failed fetch leaves the previously stored role and domain in place.
    override suspend fun refreshRoleAndDomain() {
        val profile = profileRepository.getProfile()
        if (profile is Result.Success) {
            val role = runCatching { UserRole.valueOf(profile.data.role) }.getOrDefault(UserRole.REGISTERED)
            val domain = profile.data.domain?.let { runCatching { UserDomain.valueOf(it) }.getOrNull() }
            sessionRepository.updateRoleAndDomain(role, domain)
        }
    }
}
