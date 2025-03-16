package com.combah.travel2.model.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpStatusCode
import io.ktor.http.parameters
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import java.util.Locale

@Serializable
data class AuthResponse(val accessToken: String, val expiresIn: Long)
data class Token(val accessToken: String, val expiration: Long)

private const val AUTH_URL = "https://us-central1-travel-164715.cloudfunctions.net/auth"
private const val CLIENT_ID = "travel-app-android"
private const val CLIENT_SECRET = "QzD70JbccmYDyI4GjqpUlt4MrpBU259iI0ho"

private suspend fun fetchToken(): Token {
    val response = HttpClient {
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
    return Token(response.accessToken, System.currentTimeMillis() + response.expiresIn)
}

@OptIn(ExperimentalSerializationApi::class)
private val client = HttpClient {
    var token: Token? = null
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            namingStrategy = JsonNamingStrategy.SnakeCase
            explicitNulls = false
        })
    }
    install(Auth) {
        bearer {
            loadTokens {
                val validToken =
                    token?.takeIf { it.expiration > System.currentTimeMillis() }
                        ?: fetchToken().also { token = it }
                BearerTokens(validToken.accessToken, refreshToken = null)
            }
            refreshTokens {
                val newToken = fetchToken().also { token = it }
                BearerTokens(newToken.accessToken, refreshToken = null)
            }
        }
    }
}

private const val SERVER_URL = "https://travel-api-master-rlbhlyi7ja-uc.a.run.app"
fun httpClient() = client

suspend inline fun <reified T> request(
    path: String,
    noinline builder: HttpRequestBuilder.() -> Unit
): T? {
    val response =
        get(path, builder)
    return if (response.status == HttpStatusCode.OK) {
        response.body<T>()
    } else {
        null
    }
}

suspend fun get(
    path: String,
    builder: HttpRequestBuilder.() -> Unit
) = httpClient().get(SERVER_URL) {
    url { path(path) }
    headers {
        append("accept-language", Locale.getDefault().language)
    }
    builder()
}
