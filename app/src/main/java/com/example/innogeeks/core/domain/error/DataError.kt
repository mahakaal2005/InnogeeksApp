package com.example.innogeeks.core.domain.error

// Our own Error marker (from util) — NOT kotlin.Error. This is what lets a
// DataError sit in the E slot of Result<T, DataError>.
import com.example.innogeeks.core.domain.util.Error

// The fixed menu of data-layer failures; sealed so a `when` over it is exhaustive.
sealed interface DataError : Error {

    // Network failures, mostly HTTP status codes plus NO_INTERNET and SERIALIZATION, with UNKNOWN as the catch-all.
    enum class Network : DataError{
        BAD_REQUEST,
        REQUEST_TIMEOUT,
        UNAUTHORIZED,
        FORBIDDEN,
        NOT_FOUND,
        CONFLICT,
        TOO_MANY_REQUESTS,
        NO_INTERNET,
        PAYLOAD_TOO_LARGE,
        SERVER_ERROR,
        SERVICE_UNAVAILABLE,
        SERIALIZATION,
        UNKNOWN
    }

    // Local storage failures; Local.NOT_FOUND means not in our cache, unlike HTTP 404 in Network.NOT_FOUND.
    enum class Local : DataError{
        DISK_FULL,
        NOT_FOUND,
        UNKNOWN
    }
}