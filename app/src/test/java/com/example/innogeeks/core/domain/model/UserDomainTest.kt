package com.example.innogeeks.core.domain.model

import com.example.innogeeks.feature_domains.data.InMemoryDomainsRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class UserDomainTest {

    @Test
    fun everyUserDomainMapsToAContentDomain() = runBlocking {
        val contentIds = InMemoryDomainsRepository().getDomains().getOrThrow().map { it.id }.toSet()
        assertEquals(contentIds, UserDomain.entries.map { it.contentDomainId }.toSet())
    }
}
