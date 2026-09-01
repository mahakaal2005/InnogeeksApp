package com.example.innogeeks.core.domain.session

import com.example.innogeeks.core.domain.model.UserDomain
import com.example.innogeeks.core.domain.model.UserRole

// Who is using the app right now. Guest is the cold-start default, not an error state.
sealed interface Session {
    data object Guest : Session

    // domain is null until promoted past REGISTERED, and cleared back to null on demotion.
    data class Authenticated(
        val collegeEmail: String,
        val role: UserRole,
        val domain: UserDomain?
    ) : Session
}
