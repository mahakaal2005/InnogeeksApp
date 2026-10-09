package com.example.innogeeks.core.data.networking

import edu.kiet.innogeeks.BuildConfig
import com.example.innogeeks.core.domain.error.ApiFailure
import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerializationException



// Typed helpers over Ktor that return Result<T, DataError.Network> so no throwable escapes the data layer; inline + reified so body<T>() knows its type.

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

// PATCH: same shape as post() — used for partial updates (e.g. profile edits).
suspend inline fun <reified Response, reified Request : Any> HttpClient.patch(
    route: String,
    body: Request
): Result<Response, DataError.Network> {
    return safeCall {
        patch {
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

// Catches failures that happen before a reply arrives (no DNS, bad JSON, anything else) and rethrows cancellation instead of swallowing it.
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

// The server did reply, so map its HTTP status to a parsed Success or a typed error.
suspend inline fun <reified T> responseToResult(
    response: HttpResponse
): Result<T, DataError.Network> {
    return when (response.status.value) {
        in 200..299 -> Result.Success(response.body<T>())
        400 -> Result.Error(DataError.Network.BAD_REQUEST)
        401 -> Result.Error(DataError.Network.UNAUTHORIZED)
        403 -> Result.Error(DataError.Network.FORBIDDEN)
        404 -> Result.Error(DataError.Network.NOT_FOUND)
        408 -> Result.Error(DataError.Network.REQUEST_TIMEOUT)
        409 -> Result.Error(DataError.Network.CONFLICT)
        413 -> Result.Error(DataError.Network.PAYLOAD_TOO_LARGE)
        429 -> Result.Error(DataError.Network.TOO_MANY_REQUESTS)
        503 -> Result.Error(DataError.Network.SERVICE_UNAVAILABLE)
        in 500..599 -> Result.Error(DataError.Network.SERVER_ERROR)
        else -> Result.Error(DataError.Network.UNKNOWN)
    }
}

// Envelope-aware POST for endpoints that return {"data":...} / {"error":{"code":...}}.
// Unlike post() above, this preserves error.code — the contract forbids branching on anything else.
suspend inline fun <reified Request : Any, reified Response : Any> HttpClient.postEnveloped(
    route: String,
    body: Request
): Result<Response, ApiFailure> = sendEnveloped(HttpMethod.Post, route, body)

// Envelope-aware PUT, for idempotent writes such as replacing a set of marks.
suspend inline fun <reified Request : Any, reified Response : Any> HttpClient.putEnveloped(
    route: String,
    body: Request
): Result<Response, ApiFailure> = sendEnveloped(HttpMethod.Put, route, body)

// Envelope-aware PATCH, for partial updates.
suspend inline fun <reified Request : Any, reified Response : Any> HttpClient.patchEnveloped(
    route: String,
    body: Request
): Result<Response, ApiFailure> = sendEnveloped(HttpMethod.Patch, route, body)

// Envelope-aware DELETE, which sends no body.
suspend inline fun <reified Response : Any> HttpClient.deleteEnveloped(
    route: String
): Result<Response, ApiFailure> = sendEnveloped<Unit, Response>(HttpMethod.Delete, route, null)

suspend inline fun <reified Request : Any, reified Response : Any> HttpClient.sendEnveloped(
    httpMethod: HttpMethod,
    route: String,
    body: Request?
): Result<Response, ApiFailure> {
    val response = try {
        request {
            method = httpMethod
            url(constructRoute(route))
            if (body != null) setBody(body)
        }
    } catch (e: UnresolvedAddressException) {
        e.printStackTrace()
        return Result.Error(ApiFailure.Transport(DataError.Network.NO_INTERNET))
    } catch (e: SerializationException) {
        e.printStackTrace()
        return Result.Error(ApiFailure.Transport(DataError.Network.SERIALIZATION))
    } catch (e: Exception) {
        currentCoroutineContext().ensureActive()
        e.printStackTrace()
        return Result.Error(ApiFailure.Transport(DataError.Network.UNKNOWN))
    }

    // A body that won't parse is a transport problem, not a business error.
    val envelope = try {
        response.body<ApiEnvelope<Response>>()
    } catch (e: Exception) {
        currentCoroutineContext().ensureActive()
        e.printStackTrace()
        return Result.Error(ApiFailure.Transport(DataError.Network.SERIALIZATION))
    }

    val payload = envelope.data
    if (response.status.value in 200..299 && payload != null) {
        return Result.Success(payload)
    }

    val code = envelope.error?.code
    if (!code.isNullOrBlank()) {
        return Result.Error(ApiFailure.Api(code))
    }

    // Non-2xx with no usable error code — fall back to the status mapping.
    val statusError = responseToResult<Unit>(response)
    return Result.Error(
        ApiFailure.Transport(
            if (statusError is Result.Error) statusError.error else DataError.Network.UNKNOWN
        )
    )
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
