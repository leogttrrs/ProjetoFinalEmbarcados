package br.com.projetofinal.appmobile

import kotlinx.serialization.Serializable

@Serializable
data class Configuracao(
    val tempoAcionamentoMs: Long = 3000,
    val alarmeAtivo: Boolean = true
)

@Serializable
data class Leitura(
    val id: Long,
    val sombraDetectada: Boolean,
    val alarmeDisparado: Boolean,
    val timestamp: String
)
