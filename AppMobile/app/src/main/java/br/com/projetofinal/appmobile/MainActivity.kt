package br.com.projetofinal.appmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                App()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(viewModel: AppViewModel = viewModel()) {
    var abaSelecionada by rememberSaveable { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.mensagem) {
        viewModel.mensagem?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limparMensagem()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Monitor de Luminosidade") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = abaSelecionada == 0,
                    onClick = { abaSelecionada = 0 },
                    icon = { Icon(Icons.Default.ShowChart, contentDescription = null) },
                    label = { Text("Leituras") }
                )
                NavigationBarItem(
                    selected = abaSelecionada == 1,
                    onClick = { abaSelecionada = 1 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Configuração") }
                )
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            when (abaSelecionada) {
                0 -> TelaLeituras(viewModel)
                1 -> TelaConfiguracao(viewModel)
            }
        }
    }
}

@Composable
fun TelaLeituras(viewModel: AppViewModel) {
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.carregarLeituras()
            delay(5000)
        }
    }

    Column(Modifier.padding(16.dp)) {
        Text("Últimas leituras", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        if (viewModel.leituras.isEmpty()) {
            Text("Nenhuma leitura recebida ainda. Verifique se o ESP32 e o backend estão rodando.")
        } else {
            GraficoSombra(viewModel.leituras)
            Spacer(Modifier.height(16.dp))

            val estadoLista = rememberLazyListState()

            LaunchedEffect(viewModel.leituras.firstOrNull()?.id) {
                if (estadoLista.firstVisibleItemIndex <= 2) {
                    estadoLista.animateScrollToItem(0)
                }
            }

            LazyColumn(
                state = estadoLista,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.leituras, key = { it.id }) { leitura ->
                    CartaoLeitura(leitura)
                }
            }
        }
    }
}

@Composable
fun GraficoSombra(leituras: List<Leitura>) {
    val pontos = leituras.reversed().map { if (it.sombraDetectada) 1f else 0f }
    val corLinha = MaterialTheme.colorScheme.primary

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("Sombra ao longo do tempo (alto = sombra)", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(8.dp))
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                if (pontos.size < 2) return@Canvas
                val margem = 8f
                val alturaUtil = size.height - 2 * margem
                val passoX = size.width / (pontos.size - 1)
                val caminho = Path()
                var yAnterior = 0f
                pontos.forEachIndexed { i, valor ->
                    val x = i * passoX
                    val y = margem + (1f - valor) * alturaUtil
                    if (i == 0) {
                        caminho.moveTo(x, y)
                    } else {
                        caminho.lineTo(x, yAnterior)
                        caminho.lineTo(x, y)
                    }
                    yAnterior = y
                }
                drawPath(caminho, color = corLinha, style = Stroke(width = 4f))
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2f
                )
            }
        }
    }
}

@Composable
fun CartaoLeitura(leitura: Leitura) {
    val corFundo = if (leitura.sombraDetectada) Color(0xFF37474F) else Color(0xFFFFF8E1)
    val corTexto = if (leitura.sombraDetectada) Color.White else Color(0xFF4E342E)

    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = corFundo,
            contentColor = corTexto
        )
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    if (leitura.sombraDetectada) "Sensor: SOMBRA" else "Sensor: CLARO",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    formatarTimestamp(leitura.timestamp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (leitura.alarmeDisparado) {
                Spacer(Modifier.width(6.dp))
                val corAlarme = if (leitura.sombraDetectada) Color(0xFFFF8A80)
                                else MaterialTheme.colorScheme.error
                Etiqueta("ALARME", corAlarme)
            }
        }
    }
}

@Composable
fun Etiqueta(texto: String, cor: Color) {
    Text(
        texto,
        color = cor,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.labelMedium
    )
}

fun formatarTimestamp(iso: String): String = try {
    Instant.parse(iso)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
} catch (e: Exception) {
    iso
}

@Composable
fun TelaConfiguracao(viewModel: AppViewModel) {
    var servidor by rememberSaveable { mutableStateOf(viewModel.enderecoServidor) }
    var tempo by rememberSaveable { mutableStateOf(viewModel.configuracao.tempoAcionamentoMs.toString()) }
    var alarme by rememberSaveable { mutableStateOf(viewModel.configuracao.alarmeAtivo) }

    LaunchedEffect(Unit) {
        viewModel.carregarConfiguracao()
    }

    LaunchedEffect(viewModel.configuracao) {
        tempo = viewModel.configuracao.tempoAcionamentoMs.toString()
        alarme = viewModel.configuracao.alarmeAtivo
    }

    Column(
        Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Servidor", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = servidor,
            onValueChange = {
                servidor = it
                viewModel.atualizarServidor(it)
            },
            label = { Text("Endereço do backend (IP do computador)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text("Parâmetros do sistema embarcado", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = tempo,
            onValueChange = { tempo = it.filter { c -> c.isDigit() } },
            label = { Text("Tempo p/ acionar alarme (ms)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Alarme ativo", Modifier.weight(1f))
            Switch(checked = alarme, onCheckedChange = { alarme = it })
        }

        Button(
            onClick = {
                viewModel.salvarConfiguracao(
                    Configuracao(
                        tempoAcionamentoMs = tempo.toLongOrNull() ?: 3000,
                        alarmeAtivo = alarme
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar")
        }

        if (viewModel.carregando) {
            CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
        }
    }
}
