package br.com.projetofinal.logging

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

@Serializable
data class NovaLeitura(
    val sombraDetectada: Boolean,
    val alarmeDisparado: Boolean
)

@Serializable
data class Leitura(
    val id: Long,
    val sombraDetectada: Boolean,
    val alarmeDisparado: Boolean,
    val timestamp: String
)

object LeiturasTable : Table("leituras") {
    val id = long("id").autoIncrement()
    val sombraDetectada = bool("sombra_detectada")
    val alarmeDisparado = bool("alarme_disparado")
    val timestamp = timestamp("timestamp")
    override val primaryKey = PrimaryKey(id)
}

fun main() {
    conectarBanco()
    transaction { SchemaUtils.create(LeiturasTable) }

    embeddedServer(Netty, port = 8082) {
        install(ContentNegotiation) { json() }
        routing {
            post("/leituras") {
                val nova = call.receive<NovaLeitura>()
                transaction {
                    LeiturasTable.insert {
                        it[sombraDetectada] = nova.sombraDetectada
                        it[alarmeDisparado] = nova.alarmeDisparado
                        it[timestamp] = Instant.now()
                    }
                }
                call.respond(HttpStatusCode.Created, nova)
            }

            get("/leituras") {
                val limite = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
                val leituras = transaction {
                    LeiturasTable.selectAll()
                        .orderBy(LeiturasTable.id, SortOrder.DESC)
                        .limit(limite)
                        .map {
                            Leitura(
                                id = it[LeiturasTable.id],
                                sombraDetectada = it[LeiturasTable.sombraDetectada],
                                alarmeDisparado = it[LeiturasTable.alarmeDisparado],
                                timestamp = it[LeiturasTable.timestamp].toString()
                            )
                        }
                }
                call.respond(leituras)
            }
        }
    }.start(wait = true)
}
