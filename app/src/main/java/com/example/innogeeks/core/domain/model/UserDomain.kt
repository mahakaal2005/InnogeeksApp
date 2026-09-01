package com.example.innogeeks.core.domain.model

// A user's own club domain, assigned on promotion to Member/Coordinator/Admin. Not the same as
// feature_domains.Domain, which is the club-domain content shown to guests.
enum class UserDomain {
    ANDROID,
    WEB,
    ML,
    IOT,
    AR_VR
}
