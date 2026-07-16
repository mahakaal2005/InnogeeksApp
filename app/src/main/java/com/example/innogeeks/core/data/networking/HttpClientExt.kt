package com.example.innogeeks.core.data.networking

import com.example.innogeeks.BuildConfig
import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerializationException



// Typed extension helpers over Ktor. Data sources call get/post/delete and receive a
// clean Result<T, DataError.Network> — every exception and HTTP error is handled here,
// so nothing above the data layer ever sees a raw throwable.
//
// inline + reified: these are inline so the reified type (Response/Request) survives at
// runtime — that's what lets response.body<T>() know which type to deserialize into.

// GET: reifies only Response (we receive+parse a reply; we send no typed body).
// queryParameters become ?key=value filters; empty map = no query string.
suspend inline fun <reified Response : Any> HttpClient.get(
    route: String,
    queryParameters: Map<String, Any?> = mapOf()
): Result<Response, DataError.Network> {
    return safeCall {
        get {
            url(constructRoute(route))
            queryParameters.forEach { (key, value) ->
                parameter(key, value)
            }
        }
    }
}

// POST: reifies BOTH — Request (serialize `body` -> JSON via setBody) AND Response
// (parse the reply). Two type-sensitive JSON conversions, so two reified types.
suspend inline fun <reified Response , reified Request : Any> HttpClient.post(
    route: String,
    body: Request
) : Result<Response, DataError.Network>{
    return safeCall {
        post {
            url(constructRoute(route))
            setBody(body)
        }
    }
}

suspend inline fun <reified Response : Any> HttpClient.delete(
    route: String,
    queryParameters: Map<String, Any?> = mapOf()
): Result<Response, DataError.Network> {
    return safeCall {
        delete {
            url(constructRoute(route))
            queryParameters.forEach { (key, value) ->
                parameter(key,value)
            }
        }
    }
}

// Runs the call and catches failures that happen BEFORE a reply arrives:
//   no DNS -> NO_INTERNET, bad JSON -> SERIALIZATION, anything else -> UNKNOWN.
// ensureActive() re-throws coroutine cancellation (user left the screen) instead of
// swallowing it as a fake UNKNOWN error. If no throw, hand off to responseToResult.
suspend inline fun <reified T> safeCall(
    execute: () -> HttpResponse
): Result<T, DataError.Network> {
    val response = try {
        execute()
    } catch (e: UnresolvedAddressException) {
        e.printStackTrace()
        return Result.Error(DataError.Network.NO_INTERNET)
    } catch (e: SerializationException) {
        e.printStackTrace()
        return Result.Error(DataError.Network.SERIALIZATION)
    } catch (e: Exception) {
        currentCoroutineContext().ensureActive()
        e.printStackTrace()
        return Result.Error(DataError.Network.UNKNOWN)
    }
    return responseToResult(response)
}

// The server DID reply — map its HTTP status to Success (parse body) or a typed error.
// (The exception-based errors come from safeCall; the status-based ones come from here —
// together they cover every DataError.Network case.)
suspend inline fun <reified T> responseToResult(
    response: HttpResponse
): Result<T, DataError.Network> {
    return when (response.status.value) {
        in 200..299 -> Result.Success(response.body<T>())
        401 -> Result.Error(DataError.Network.UNAUTHORIZED)
        408 -> Result.Error(DataError.Network.REQUEST_TIMEOUT)
        409 -> Result.Error(DataError.Network.CONFLICT)
        413 -> Result.Error(DataError.Network.PAYLOAD_TOO_LARGE)
        429 -> Result.Error(DataError.Network.TOO_MANY_REQUESTS)
        in 500..599 -> Result.Error(DataError.Network.SERVER_ERROR)
        else -> Result.Error(DataError.Network.UNKNOWN)
    }
}

// Prepends BASE_URL to a route (BuildConfig.BASE_URL is the value set in build.gradle.kts).
// BASE_URL has no trailing slash + routes start with "/", so we get exactly one slash.
fun constructRoute(route: String): String {
    return when {
        route.contains(BuildConfig.BASE_URL) -> route
        route.startsWith("/") -> BuildConfig.BASE_URL + route
        else -> BuildConfig.BASE_URL + "/$route"
    }
}