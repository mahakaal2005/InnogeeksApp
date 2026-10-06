package com.example.innogeeks.core.domain.session

// Revokes the access token on the server; failures are swallowed because local sign-out must always happen.
interface SessionRevoker {
    suspend fun revokeRemoteSession()
}
