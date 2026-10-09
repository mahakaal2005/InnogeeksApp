package com.example.innogeeks.core.data.fake

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.File

// Fake data sources keep their state in a JSON file so demo edits survive a restart; delete with the fakes at go-live.
class FakeStore(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    // A missing or corrupt file reads as null, and the fake falls back to its seed.
    suspend fun <T> read(name: String, serializer: KSerializer<T>): T? = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(context.filesDir, name)
            if (file.exists()) json.decodeFromString(serializer, file.readText()) else null
        }.getOrNull()
    }

    suspend fun <T> write(name: String, serializer: KSerializer<T>, value: T) {
        withContext(Dispatchers.IO) {
            runCatching { File(context.filesDir, name).writeText(json.encodeToString(serializer, value)) }
        }
    }
}
