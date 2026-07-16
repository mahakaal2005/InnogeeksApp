package com.example.innogeeks.core.data.networking

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

// Assembles a fully-configured Ktor HttpClient. A class (not a top-level fun) so Koin
// can provide it via singleOf(::HttpClientFactory) in Lesson 6.
class HttpClientFactory {

    // The engine is INJECTED, not hardcoded — Ktor is engine-agnostic (OkHttp, CIO...).
    // Real code passes OkHttp.create(); a test could pass a fake. Depend on the
    // abstraction (HttpClientEngine), not the concrete engine.
    fun build(engine: HttpClientEngine) : HttpClient{
        return HttpClient(engine){

            // Logs every request/response to Logcat — invaluable for debugging API calls.
            // LogLevel.ALL = headers + bodies. (Dial down in a real production build.)
            install(Logging){
                logger = object : Logger{
                    override fun log(message: String) {
                        println(message)
                    }
                }
                level = LogLevel.ALL
            }

            // Auto-converts JSON <-> Kotlin objects (response.body<Dto>() just works).
            // ignoreUnknownKeys = don't crash if the backend adds a field our DTO lacks.
            install(ContentNegotiation){
                json(
                    json = Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        prettyPrint = true
                    }
                )
            }

            // Defaults stamped onto EVERY request (like pre-printed letterhead). Here:
            // "my body is JSON". Later, base URL + auth token can also live here.
            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }
    }
}