package travel.vola.android.model.network

import com.google.firebase.auth.FirebaseAuth
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.call.replaceResponse
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.plugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import travel.vola.android.BuildConfig
import java.util.Locale

// Every route but the health check now requires a Bearer token (see
// travel-node's api/auth/interceptor.js). loadTokens/refreshTokens fetch the
// signed-in user's Firebase ID token; getIdToken(false) returns the cached
// token if it's not expired, getIdToken(true) forces a refresh, mirroring
// Ktor's own load/refresh split. Returns null rather than throwing when
// there's no signed-in user - StartupViewModel hits the health check before
// the sign-in gate has a chance to run, and Ktor's bearer provider treats a
// null token as "send the request with no Authorization header" rather than
// failing the request outright, so an unauthenticated call still reaches
// the server (which 401s it if it isn't the health check).
private suspend fun currentIdToken(forceRefresh: Boolean): String? {
    val user = FirebaseAuth.getInstance().currentUser ?: return null
    return user.getIdToken(forceRefresh).await().token
}

@OptIn(ExperimentalSerializationApi::class)
private val client = HttpClient {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                namingStrategy = JsonNamingStrategy.SnakeCase
                explicitNulls = false
            },
        )
    }
    install(Auth) {
        bearer {
            loadTokens {
                currentIdToken(forceRefresh = false)?.let { BearerTokens(it, refreshToken = null) }
            }
            refreshTokens {
                currentIdToken(forceRefresh = true)?.let { BearerTokens(it, refreshToken = null) }
            }
        }
    }
}.apply {
    plugin(HttpSend).intercept { request ->
        val call = execute(request)
        if (call.response.status != HttpStatusCode.Unauthorized) {
            call
        } else {
            // Fixes malformed WWW-Authenticate response headers so that Ktor can parse them
            val content = call.response.bodyAsChannel()
            val originalAuthHeader = call.request.headers[HttpHeaders.WWWAuthenticate]
            val fixedAuthHeader =
                originalAuthHeader?.removePrefix("Bearer ")?.replace(" ", ",") ?: ""
            call.replaceResponse(
                headers = Headers.build {
                    appendAll(call.response.headers)
                    remove(HttpHeaders.WWWAuthenticate)
                    append(HttpHeaders.WWWAuthenticate, "Bearer $fixedAuthHeader")
                },
                content = { content },
            )
        }
    }
}

// internal + @PublishedApi so the public inline put() can reference it (an
// inline function's body is copied to call sites, so it can't touch
// private declarations).
@PublishedApi
internal const val SERVER_URL = BuildConfig.SERVER_URL
fun httpClient() = client

suspend inline fun <reified T> request(
    path: String,
    noinline builder: HttpRequestBuilder.() -> Unit = {},
): T? {
    val response = get(path, builder)
    return if (response.status == HttpStatusCode.OK) {
        response.body<T>()
    } else {
        null
    }
}

suspend fun get(
    path: String,
    builder: HttpRequestBuilder.() -> Unit = {},
): HttpResponse {
    return httpClient().get(SERVER_URL) {
        url { path(path) }
        headers {
            append(HttpHeaders.AcceptLanguage, Locale.getDefault().language)
        }
        builder()
    }
}

suspend inline fun <reified T> put(
    path: String,
    body: T,
    noinline builder: HttpRequestBuilder.() -> Unit = {},
): HttpResponse {
    return httpClient().put(SERVER_URL) {
        url { path(path) }
        contentType(ContentType.Application.Json)
        setBody(body)
        headers {
            append(HttpHeaders.AcceptLanguage, Locale.getDefault().language)
        }
        builder()
    }
}

suspend fun delete(
    path: String,
    builder: HttpRequestBuilder.() -> Unit = {},
): HttpResponse {
    return httpClient().delete(SERVER_URL) {
        url { path(path) }
        headers {
            append(HttpHeaders.AcceptLanguage, Locale.getDefault().language)
        }
        builder()
    }
}
