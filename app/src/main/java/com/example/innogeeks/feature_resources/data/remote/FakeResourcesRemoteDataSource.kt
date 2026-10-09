package com.example.innogeeks.feature_resources.data.remote

import android.content.Context
import com.example.innogeeks.core.data.fake.FakeStore
import com.example.innogeeks.core.domain.error.ApiFailure
import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.model.UserRole
import com.example.innogeeks.core.domain.session.Session
import com.example.innogeeks.core.domain.session.SessionRepository
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_resources.data.dto.DeletedDto
import com.example.innogeeks.feature_resources.data.dto.ResourceBodyDto
import com.example.innogeeks.feature_resources.data.dto.ResourceDto
import com.example.innogeeks.feature_resources.data.dto.ResourceListDto
import com.example.innogeeks.feature_resources.domain.model.ResourceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Seeded from assets/resources.json and kept in memory, enforcing the same rules the server will:
// only a Coordinator/Admin, and only inside their own domain.
class FakeResourcesRemoteDataSource(
    private val context: Context,
    private val sessionRepository: SessionRepository
) : ResourcesRemoteDataSource {

    private val json = Json { ignoreUnknownKeys = true }
    private val store = FakeStore(context)
    private var items: MutableList<ResourceDto>? = null
    private var nextId = 1

    // seedHash lets an app update that changes resources.json replace a stale saved copy.
    @Serializable
    private data class Snapshot(val seedHash: Int, val items: List<ResourceDto>)

    override suspend fun getResources(): Result<ResourceListDto, DataError.Network> {
        return try {
            Result.Success(ResourceListDto(loaded().toList()))
        } catch (e: Exception) {
            Result.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun createResource(body: ResourceBodyDto): Result<ResourceDto, ApiFailure> {
        delay(500)
        val domainId = writableDomainId() ?: return denied()
        invalid(body)?.let { return it }
        val created = ResourceDto(
            id = "new-${nextId++}", domainId = domainId, type = body.type, title = body.title,
            description = body.description, author = body.author, date = today(), level = body.level, url = body.url
        )
        loaded().add(0, created)
        save()
        return Result.Success(created)
    }

    override suspend fun updateResource(id: String, body: ResourceBodyDto): Result<ResourceDto, ApiFailure> {
        delay(500)
        val domainId = writableDomainId() ?: return denied()
        val index = loaded().indexOfFirst { it.id == id }
        if (index < 0) return Result.Error(ApiFailure.Api("RESOURCE_NOT_FOUND"))
        if (loaded()[index].domainId != domainId) return Result.Error(ApiFailure.Api("RESOURCE_NOT_IN_DOMAIN"))
        invalid(body)?.let { return it }
        val updated = loaded()[index].copy(
            type = body.type, title = body.title, description = body.description,
            author = body.author, level = body.level, url = body.url
        )
        loaded()[index] = updated
        save()
        return Result.Success(updated)
    }

    override suspend fun deleteResource(id: String): Result<DeletedDto, ApiFailure> {
        delay(300)
        val domainId = writableDomainId() ?: return denied()
        val existing = loaded().firstOrNull { it.id == id } ?: return Result.Error(ApiFailure.Api("RESOURCE_NOT_FOUND"))
        if (existing.domainId != domainId) return Result.Error(ApiFailure.Api("RESOURCE_NOT_IN_DOMAIN"))
        loaded().remove(existing)
        save()
        return Result.Success(DeletedDto())
    }

    private suspend fun loaded(): MutableList<ResourceDto> {
        items?.let { return it }
        val seedText = withContext(Dispatchers.IO) { context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() } }
        val saved = store.read(SNAPSHOT_FILE, Snapshot.serializer())?.takeIf { it.seedHash == seedText.hashCode() }
        val list = saved?.items?.toMutableList() ?: json.decodeFromString<List<ResourceDto>>(seedText).toMutableList()
        seedHash = seedText.hashCode()
        nextId = (list.mapNotNull { it.id.removePrefix("new-").toIntOrNull() }.maxOrNull() ?: 0) + 1
        items = list
        return list
    }

    private var seedHash = 0

    private suspend fun save() {
        store.write(SNAPSHOT_FILE, Snapshot.serializer(), Snapshot(seedHash, loaded().toList()))
    }

    // The domain the caller may write to, or null when their role or domain does not allow it.
    private suspend fun writableDomainId(): String? {
        val session = sessionRepository.session.first() as? Session.Authenticated ?: return null
        if (session.role != UserRole.COORDINATOR && session.role != UserRole.ADMIN) return null
        return session.domain?.contentDomainId
    }

    private suspend fun denied(): Result.Error<ApiFailure> {
        val session = sessionRepository.session.first() as? Session.Authenticated
        val code = when {
            session == null || (session.role != UserRole.COORDINATOR && session.role != UserRole.ADMIN) -> "RESOURCE_FORBIDDEN_ROLE"
            else -> "RESOURCE_NO_DOMAIN"
        }
        return Result.Error(ApiFailure.Api(code))
    }

    private fun invalid(body: ResourceBodyDto): Result.Error<ApiFailure>? {
        val validType = ResourceType.entries.any { it.name == body.type }
        val validUrl = body.url.startsWith("https://") || body.url.startsWith("http://")
        return if (body.title.isBlank() || !validUrl || !validType) Result.Error(ApiFailure.Api("VALIDATION_ERROR")) else null
    }

    private fun today(): String {
        val date = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return "${MONTHS[date.monthNumber - 1]} ${date.year}"
    }

    private companion object {
        const val ASSET_NAME = "resources.json"
        const val SNAPSHOT_FILE = "fake_resources.json"
        val MONTHS = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    }
}
