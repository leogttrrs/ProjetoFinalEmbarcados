package br.com.projetofinal.controle

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

@Serializable
data class Configuracao(
    val tempoAcionamentoMs: Long = 3000,
    val alarmeAtivo: Boolean = true
)

object ConfiguracaoTable : Table("configuracao") {
    val id = integer("id")
    val tempoAcionamentoMs = long("tempo_acionamento_ms")
    val alarmeAtivo = bool("alarme_ativo")
    override val primaryKey = PrimaryKey(id)
}

fun buscarConfiguracao(): Configuracao = transaction {
    ConfiguracaoTable.selectAll().first().let {
        Configuracao(
            tempoAcionamentoMs = it[ConfiguracaoTable.tempoAcionamentoMs],
            alarmeAtivo = it[ConfiguracaoTable.alarmeAtivo]
        )
    }
}

fun salvarConfiguracao(config: Configuracao) = transaction {
    ConfiguracaoTable.update({ ConfiguracaoTable.id eq 1 }) {
        it[tempoAcionamentoMs] = config.tempoAcionamentoMs
        it[alarmeAtivo] = config.alarmeAtivo
    }
}

fun main() {
    conectarBanco()

    transaction {
        SchemaUtils.create(ConfiguracaoTable)
        if (ConfiguracaoTable.selectAll().empty()) {
            val padrao = Configuracao()
            ConfiguracaoTable.insert {
                it[id] = 1
                it[tempoAcionamentoMs] = padrao.tempoAcionamentoMs
                it[alarmeAtivo] = padrao.alarmeAtivo
            }
        }
    }

    embeddedServer(Netty, port = 8081) {
        install(ContentNegotiation) { json() }
        routing {
            get("/config") {
                call.respond(buscarConfiguracao())
            }
            put("/config") {
                val config = call.receive<Configuracao>()
                salvarConfiguracao(config)
                call.respond(config)
            }
        }
    }.start(wait = true)
}
