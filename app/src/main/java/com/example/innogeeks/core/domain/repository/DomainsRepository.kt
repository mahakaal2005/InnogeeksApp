package com.example.innogeeks.core.domain.repository

import com.example.innogeeks.core.domain.model.Domain

interface DomainsRepository {
    suspend fun getDomains(): Result<List<Domain>>
}
