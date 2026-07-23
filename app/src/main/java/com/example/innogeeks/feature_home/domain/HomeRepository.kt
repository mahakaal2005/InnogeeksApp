package com.example.innogeeks.feature_home.domain

import com.example.innogeeks.feature_home.domain.model.ClubStats
import com.example.innogeeks.feature_home.domain.model.DomainPreview
import com.example.innogeeks.feature_home.domain.model.EventPreview

interface HomeRepository {
    suspend fun getClubStats(): Result<ClubStats>
    suspend fun getDomains(): Result<List<DomainPreview>>
    suspend fun getUpcomingEvents(): Result<List<EventPreview>>
}
