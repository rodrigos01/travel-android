package travel.vola.android.model.network

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.parameters
import io.ktor.http.parsing.ParseException
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import travel.vola.android.BuildConfig
import travel.vola.android.ui.applicationContext
import java.util.Locale

@Serializable
data class AuthResponse(val accessToken: String, val expiresIn: Long)
data class Token(val accessToken: String, val expiration: Long)

private const val AUTH_URL = "https://us-central1-travel-164715.cloudfunctions.net/auth"
private const val CLIENT_ID = "travel-app-android"
private const val CLIENT_SECRET = "QzD70JbccmYDyI4GjqpUlt4MrpBU259iI0ho"
private const val MAX_AUTH_RETRIES = 3

private suspend fun updateToken(): Token {
    val response = HttpClient {
        install(ContentEncoding) {
            gzip()
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                namingStrategy = JsonNamingStrategy.SnakeCase
                explicitNulls = false
            })
        }
    }.submitForm(url = AUTH_URL, formParameters = parameters {
        append("grant_type", "client_credentials")
        append("client_id", CLIENT_ID)
        append("client_secret", CLIENT_SECRET)
        append("scope", SERVER_URL)
    }).body<AuthResponse>()
    val newToken = Token(response.accessToken, System.currentTimeMillis() + response.expiresIn)
    applicationContext.dataStore.edit {
        it[stringPreferencesKey("token")] = newToken.accessToken
        it[longPreferencesKey("expiration")] = newToken.expiration
    }
    return newToken
}

private val Context.dataStore by preferencesDataStore("auth")

@OptIn(ExperimentalSerializationApi::class)
private val client = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            namingStrategy = JsonNamingStrategy.SnakeCase
            explicitNulls = false
        })
    }
    if (BuildConfig.REQUIRES_AUTH) {
        install(Auth) {
            bearer {
                loadTokens {
                    val token = applicationContext.dataStore.data.map { preferences ->
                        val token = preferences[stringPreferencesKey("token")]
                        val expiration = preferences[longPreferencesKey("expiration")]
                        if (token != null && expiration != null) {
                            Token(token, expiration)
                        } else {
                            null
                        }
                    }.firstOrNull() ?: updateToken()
                    BearerTokens(token.accessToken, refreshToken = null)
                }
                refreshTokens {
                    val newToken = updateToken()
                    BearerTokens(newToken.accessToken, refreshToken = null)
                }
            }
        }
    }
}

private const val SERVER_URL = BuildConfig.SERVER_URL
fun httpClient() = client

suspend inline fun <reified T> request(
    path: String, noinline builder: HttpRequestBuilder.() -> Unit = {}
): T? {
    val response = get(path, builder)
    return if (response.status == HttpStatusCode.OK) {
        response.body<T>()
    } else {
        null
    }
}

suspend fun get(
    path: String, builder: HttpRequestBuilder.() -> Unit = {}, retryCount: Int = 0,
): HttpResponse {
    try {
        return httpClient().get(SERVER_URL) {
            url { path(path) }
            headers {
                append("accept-language", Locale.getDefault().language)
            }
            builder()
        }
    } catch (e: ParseException) {
        if (retryCount < MAX_AUTH_RETRIES) {
            // Ktor can't handle malformed auth 401 headers so we have to handle them ourselves
            updateToken()
            return get(path, builder, retryCount + 1)
        } else {
            throw e
        }
    }
}
