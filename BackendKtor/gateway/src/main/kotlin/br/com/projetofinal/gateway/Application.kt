package br.com.projetofinal.gateway

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.server.application.ApplicationCall
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.httpMethod
import io.ktor.server.request.receiveText
import io.ktor.server.request.uri
import io.ktor.server.response.respondText
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun main() {
    val client = HttpClient(CIO)
    val controleUrl = System.getenv("CONTROLE_URL") ?: "http://localhost:8081"
    val loggingUrl = System.getenv("LOGGING_URL") ?: "http://localhost:8082"

    embeddedServer(Netty, port = 8080) {
        routing {
            route("/config") { handle { call.encaminhar(client, controleUrl) } }
            route("/leituras") { handle { call.encaminhar(client, loggingUrl) } }
        }
    }.start(wait = true)
}

suspend fun ApplicationCall.encaminhar(client: HttpClient, destino: String) {
    val corpo = receiveText()
    val resposta = client.request(destino + request.uri) {
        method = this@encaminhar.request.httpMethod
        if (corpo.isNotEmpty()) {
            contentType(ContentType.Application.Json)
            setBody(corpo)
        }
    }
    respondText(
        text = resposta.bodyAsText(),
        contentType = ContentType.Application.Json,
        status = resposta.status
    )
}
