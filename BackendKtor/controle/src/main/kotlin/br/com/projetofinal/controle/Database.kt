package br.com.projetofinal.controle

import org.jetbrains.exposed.sql.Database
import java.io.File
import java.net.URI

private fun lerDatabaseUrl(): String? {
    System.getenv("DATABASE_URL")?.let { return it }
    var dir: File? = File("").absoluteFile
    while (dir != null) {
        val env = File(dir, ".env")
        if (env.exists()) {
            for (linha in env.readLines()) {
                val partes = linha.split("=", limit = 2)
                if (partes.size == 2 && partes[0].trim().removePrefix("﻿") == "DATABASE_URL") {
                    return partes[1].trim().removeSurrounding("\"")
                }
            }
        }
        dir = dir.parentFile
    }
    return null
}

fun conectarBanco() {
    val raw = lerDatabaseUrl()
        ?: error("Defina DATABASE_URL (variável de ambiente ou arquivo .env) com a connection string do NeonDB")

    if (raw.startsWith("jdbc:")) {
        Database.connect(raw, driver = "org.postgresql.Driver")
        return
    }

    val uri = URI(raw)
    val (usuario, senha) = uri.userInfo.split(":", limit = 2)
    val porta = if (uri.port != -1) ":${uri.port}" else ""
    val jdbcUrl = "jdbc:postgresql://${uri.host}$porta${uri.path}?sslmode=require"

    Database.connect(jdbcUrl, driver = "org.postgresql.Driver", user = usuario, password = senha)
}
