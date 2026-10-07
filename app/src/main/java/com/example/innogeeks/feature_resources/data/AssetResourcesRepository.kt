package com.example.innogeeks.feature_resources.data

import android.content.Context
import com.example.innogeeks.feature_resources.data.dto.ResourceDto
import com.example.innogeeks.feature_resources.data.mapper.toResourceItem
import com.example.innogeeks.feature_resources.domain.ResourcesRepository
import com.example.innogeeks.feature_resources.domain.model.ResourceItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

// Reads the curated list from assets/resources.json until a resources endpoint exists.
class AssetResourcesRepository(private val context: Context) : ResourcesRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private var cached: List<ResourceItem>? = null

    override suspend fun getResources(): Result<List<ResourceItem>> = withContext(Dispatchers.IO) {
        cached?.let { return@withContext Result.success(it) }
        runCatching {
            val text = context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() }
            json.decodeFromString<List<ResourceDto>>(text).mapNotNull { it.toResourceItem() }
        }.onSuccess { cached = it }
    }

    private companion object {
        const val ASSET_NAME = "resources.json"
    }
}
