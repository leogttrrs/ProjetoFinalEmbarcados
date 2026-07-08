package br.com.projetofinal.appmobile

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("app_config", Context.MODE_PRIVATE)

    var enderecoServidor by mutableStateOf(
        prefs.getString("servidor", "https://unwaned-pseudoameboid-latrisha.ngrok-free.dev")!!
    )
        private set

    var leituras by mutableStateOf<List<Leitura>>(emptyList())
        private set

    var configuracao by mutableStateOf(Configuracao())
        private set

    var carregando by mutableStateOf(false)
        private set

    var mensagem by mutableStateOf<String?>(null)
        private set

    fun atualizarServidor(url: String) {
        enderecoServidor = url.trim().removeSuffix("/")
        prefs.edit().putString("servidor", enderecoServidor).apply()
    }

    fun limparMensagem() {
        mensagem = null
    }

    fun carregarLeituras() {
        viewModelScope.launch {
            try {
                leituras = Api.buscarLeituras(enderecoServidor)
            } catch (e: Exception) {
                mensagem = "Erro ao buscar leituras: ${e.message}"
            }
        }
    }

    fun carregarConfiguracao() {
        viewModelScope.launch {
            carregando = true
            try {
                configuracao = Api.buscarConfiguracao(enderecoServidor)
                mensagem = "Configuração carregada do servidor"
            } catch (e: Exception) {
                mensagem = "Erro ao carregar configuração: ${e.message}"
            } finally {
                carregando = false
            }
        }
    }

    fun salvarConfiguracao(nova: Configuracao) {
        viewModelScope.launch {
            carregando = true
            try {
                configuracao = Api.salvarConfiguracao(enderecoServidor, nova)
                mensagem = "Configuração salva com sucesso"
            } catch (e: Exception) {
                mensagem = "Erro ao salvar configuração: ${e.message}"
            } finally {
                carregando = false
            }
        }
    }
}
