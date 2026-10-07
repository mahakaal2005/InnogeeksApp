package com.example.innogeeks.feature_resources.data

import com.example.innogeeks.core.domain.model.UserDomain
import com.example.innogeeks.feature_resources.data.dto.ResourceDto
import com.example.innogeeks.feature_resources.data.mapper.toResourceItem
import com.example.innogeeks.feature_resources.domain.model.ResourceType
import java.io.File
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class ResourcesAssetTest {

    private val dtos = Json.decodeFromString<List<ResourceDto>>(File("src/main/assets/resources.json").readText())

    @Test
    fun everyDomainAndTypeHasTenItems() {
        val domainIds = UserDomain.entries.map { it.contentDomainId }
        for (domain in domainIds) for (type in ResourceType.entries) {
            assertEquals("$domain/$type", 10, dtos.count { it.domainId == domain && it.type == type.name })
        }
        assertEquals(domainIds.size * ResourceType.entries.size * 10, dtos.size)
    }

    @Test
    fun everyItemMapsAndIdsAndUrlsAreUnique() {
        assertEquals(dtos.size, dtos.mapNotNull { it.toResourceItem() }.size)
        assertEquals(dtos.size, dtos.map { it.id }.toSet().size)
        assertEquals(dtos.size, dtos.map { it.url }.toSet().size)
    }
}
