package com.example.innogeeks.core.domain.model

// COORDINATOR = 2nd-years running the domain day-to-day. TEAM = 3rd-years who make up the rest of it.
enum class DomainMemberRole {
    COORDINATOR,
    TEAM
}

data class DomainMember(
    val name: String,
    val initials: String,
    val role: DomainMemberRole
)

// Accent index maps to a colour pair resolved from the theme at render time.
data class Domain(
    val id: String,
    val name: String,
    val tagline: String,
    val description: String,
    val accentIndex: Int,
    val memberCount: Int,
    val techStack: List<String>,
    val members: List<DomainMember>
)
