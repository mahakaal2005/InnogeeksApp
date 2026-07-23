package com.example.innogeeks.feature_home.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import com.example.innogeeks.feature_home.domain.HomeRepository
import com.example.innogeeks.feature_home.domain.model.ClubStats
import com.example.innogeeks.feature_home.domain.model.DomainPreview
import com.example.innogeeks.feature_home.domain.model.EventPreview
import kotlinx.coroutines.delay

class InMemoryHomeRepository : HomeRepository {
    
    override suspend fun getClubStats(): Result<ClubStats> {
        delay(500) // Simulate network delay
        return Result.success(
            ClubStats(
                totalMembers = 150,
                totalProjects = 45,
                totalDomains = 5,
                totalEvents = 24
            )
        )
    }

    override suspend fun getDomains(): Result<List<DomainPreview>> {
        delay(500)
        return Result.success(
            listOf(
                DomainPreview(
                    id = "web",
                    name = "Web Development",
                    icon = Icons.Filled.Language,
                    shortDescription = "Build scalable web applications"
                ),
                DomainPreview(
                    id = "android",
                    name = "Android",
                    icon = Icons.Filled.Android,
                    shortDescription = "Create native mobile experiences"
                ),
                DomainPreview(
                    id = "aiml",
                    name = "AI & ML",
                    icon = Icons.Filled.Computer,
                    shortDescription = "Train models and analyze data"
                ),
                DomainPreview(
                    id = "cyber",
                    name = "Cybersecurity",
                    icon = Icons.Filled.Security,
                    shortDescription = "Secure systems and networks"
                ),
                DomainPreview(
                    id = "cp",
                    name = "Competitive Programming",
                    icon = Icons.Filled.Code,
                    shortDescription = "Master algorithms and logic"
                )
            )
        )
    }

    override suspend fun getUpcomingEvents(): Result<List<EventPreview>> {
        delay(500)
        return Result.success(
            listOf(
                EventPreview(
                    id = "e1",
                    title = "Hackathon 2026",
                    dateDisplay = "15 Oct 2026, 9:00 AM",
                    location = "Main Auditorium"
                ),
                EventPreview(
                    id = "e2",
                    title = "Android Compose Workshop",
                    dateDisplay = "20 Oct 2026, 4:00 PM",
                    location = "Lab 3"
                )
            )
        )
    }
}
