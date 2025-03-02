package com.combah.travel2.model.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

@OptIn(ExperimentalSerializationApi::class)
fun httpClient() = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            namingStrategy = JsonNamingStrategy.SnakeCase
            explicitNulls = false
        })
    }
}

const val SERVER_URL = "https://travel-api-master-rlbhlyi7ja-uc.a.run.app"

suspend inline fun <reified T> request(path: String, builder: HttpRequestBuilder.() -> Unit): T? {
    val response =
        httpClient().get(SERVER_URL) {
            url { path(path) }
            builder()
        }
    return if (response.status == HttpStatusCode.OK) {
        response.body<T>()
    } else {
        null
    }
}
