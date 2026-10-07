package com.example.innogeeks.core.domain.model

// A user's own club domain, assigned on promotion to Member/Coordinator/Admin. Not the same as
// feature_domains.Domain, which is the club-domain content shown to guests.
enum class UserDomain(val contentDomainId: String) {
    ANDROID("appd"), // contentDomainId is the matching feature_domains.Domain.id
    WEB("webd"),
    ML("ml"),
    IOT("iot"),
    AR_VR("arvr")
}
