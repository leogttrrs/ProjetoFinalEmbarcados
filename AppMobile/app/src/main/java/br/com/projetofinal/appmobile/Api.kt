package br.com.projetofinal.appmobile

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object Api {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 8000
        }
        defaultRequest {
            header("ngrok-skip-browser-warning", "1")
        }
    }

    suspend fun buscarConfiguracao(baseUrl: String): Configuracao =
        client.get("$baseUrl/config").body()

    suspend fun salvarConfiguracao(baseUrl: String, config: Configuracao): Configuracao =
        client.put("$baseUrl/config") {
            contentType(ContentType.Application.Json)
            setBody(config)
        }.body()

    suspend fun buscarLeituras(baseUrl: String, limite: Int = 50): List<Leitura> =
        client.get("$baseUrl/leituras?limit=$limite").body()
}
