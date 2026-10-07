package com.example.innogeeks.feature_resources.data

import com.example.innogeeks.feature_resources.data.dto.ResourceDto
import com.example.innogeeks.feature_resources.data.mapper.toResourceItem
import com.example.innogeeks.feature_resources.domain.model.ResourceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResourceMapperTest {

    private fun dto(type: String = "LINK", url: String = "https://example.com") = ResourceDto(
        id = "id", domainId = "webd", type = type, emoji = "🌐", title = "t",
        description = "d", author = "a", date = "2026", level = "Beginner", url = url
    )

    @Test
    fun validDtoMapsToItem() {
        val item = dto(type = "PDF").toResourceItem()
        assertEquals(ResourceType.PDF, item?.type)
        assertEquals("https://example.com", item?.url)
    }

    @Test
    fun unknownTypeIsDropped() = assertNull(dto(type = "PODCAST").toResourceItem())

    @Test
    fun blankUrlIsDropped() = assertNull(dto(url = "").toResourceItem())

    @Test
    fun nonHttpUrlIsDropped() = assertNull(dto(url = "javascript:alert(1)").toResourceItem())
}
