# Projeto Final — Desenvolvimento de Sistemas Móveis e Embarcados

Sistema de monitoramento de luminosidade com alarme, composto por três subsistemas que se comunicam pela rede local.

## Arquitetura

```
 ┌─────────────┐        ┌──────────────────────────────────────┐
 │  App Mobile │        │            BackendKtor               │
 │  (Compose)  │───────▶│  API Gateway (:8080)                 │
 └─────────────┘  HTTP  │     ├── /config   → Controle (:8081) │──▶ NeonDB
 ┌─────────────┐        │     └── /leituras → Logging  (:8082) │    (PostgreSQL)
 │    ESP32    │───────▶│                                      │
 │ LDR+LED+Buz │  HTTP  └──────────────────────────────────────┘
 └─────────────┘
```

- **ESP32**: lê o LDR como booleano (sombra/claro); quando há sombra contínua por `tempoAcionamentoMs`, aciona LED + buzzer. Busca a configuração no backend (`GET /config`) e registra as leituras (`POST /leituras`).
- **Backend (Kotlin/Ktor)**: microservices **Controle** (parâmetros) e **Logging** (histórico), acessados via **API Gateway**. Dados no NeonDB (PostgreSQL).
- **App (Kotlin/Compose)**: configura os parâmetros (`PUT /config`) e exibe o histórico de leituras com gráfico (`GET /leituras`).

## Como executar (nesta ordem)

1. **Backend** — defina a variável de ambiente `DATABASE_URL` com a connection string do Neon e rode os 3 serviços (ver `BackendKtor/README.md`).
2. **ESP32** — ajuste WiFi e IP do computador em `HardWareESP32.ino` e grave a placa (ver `HardWareESP32/README.md`).
3. **App** — instale no celular; na aba Configuração, informe `http://<IP-do-computador>:8080`.

> Celular, ESP32 e computador devem estar na **mesma rede local**. Descubra o IP do computador com `ipconfig` (Windows) ou `ifconfig` (Linux/Mac).

## Endpoints (via Gateway, porta 8080)

| Método | Rota | Uso |
|---|---|---|
| GET | `/config` | ESP32 lê os parâmetros de funcionamento |
| PUT | `/config` | App define os parâmetros |
| POST | `/leituras` | ESP32 registra uma leitura do sensor |
| GET | `/leituras?limit=50` | App obtém o histórico de leituras |

## Mapeamento dos critérios de avaliação

| Subsistema | Funcionalidade | Onde está |
|---|---|---|
| Embarcado | Leitura do sensor | `loop()` — LDR lido como sombra/claro |
| Embarcado | Controle do atuador | `loop()` — LED/buzzer conforme sombra + tempo |
| Embarcado | Configuração via backend | `buscarConfiguracao()` — `GET /config` a cada 5 s |
| Embarcado | Envio de dados ao backend | `enviarLeitura()` — `POST /leituras` a cada 2 s |
| App | Leitura dos dados do backend | `Api.buscarLeituras()` |
| App | Visualização dos dados | `TelaLeituras` — gráfico + lista |
| App | Configuração do embarcado | `TelaConfiguracao` — `PUT /config` |
| Backend | Configuração do embarcado | serviço **controle** — `GET/PUT /config` |
| Backend | Recebimento de dados | serviço **logging** — `POST /leituras` |
| Backend | Envio de dados ao app | serviço **logging** — `GET /leituras` |
