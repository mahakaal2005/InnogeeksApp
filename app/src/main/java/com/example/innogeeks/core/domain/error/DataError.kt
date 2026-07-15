package com.example.innogeeks.core.domain.error

// Our own Error marker (from util) — NOT kotlin.Error. This is what lets a
// DataError sit in the E slot of Result<T, DataError>.
import com.example.innogeeks.core.domain.util.Error

// The fixed "menu" of things that can go wrong in the data layer.
// sealed => the only implementers are the two enums below, so `when` over a
// DataError is exhaustive at the category level (is Network / is Local, no else).
sealed interface DataError : Error {

    // Failures from a network/API call. Most map to HTTP status codes
    // (see safeCall/responseToResult later); NO_INTERNET (request never left the
    // device) and SERIALIZATION (response arrived but couldn't parse) are the
    // two non-HTTP cases. UNKNOWN is the catch-all.
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

    // Failures from local storage (Room/disk), used in later lessons. Fewer cases
    // because fewer things go wrong locally. NOT_FOUND here means "not in our cache"
    // — distinct from Network.NOT_FOUND (HTTP 404); nesting keeps them from colliding.
    enum class Local : DataError{
        DISK_FULL,
        NOT_FOUND,
        UNKNOWN
    }
}