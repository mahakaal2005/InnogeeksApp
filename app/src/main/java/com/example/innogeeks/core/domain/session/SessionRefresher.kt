package com.example.innogeeks.core.domain.session

// Re-reads the signed-in user's role and domain from the backend into the session.
interface SessionRefresher {
    suspend fun refreshRoleAndDomain()
}
