package com.example.innogeeks.core.domain.session

import com.example.innogeeks.core.domain.model.UserDomain
import com.example.innogeeks.core.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    // Emits on every sign-in/sign-out so the whole UI tree reacts without manual refresh.
    val session: Flow<Session>

    // Data-layer only — the Ktor bearer provider reads this. Never expose the token to presentation.
    suspend fun currentAccessToken(): String?

    suspend fun signIn(accessToken: String, collegeEmail: String, role: UserRole, domain: UserDomain?)

    // Re-syncs role/domain without a full sign-in, e.g. on splash — role is never in the JWT.
    suspend fun updateRoleAndDomain(role: UserRole, domain: UserDomain?)

    suspend fun signOut()

    suspend fun hasSeenIntro(): Boolean

    suspend fun markIntroSeen()
}
