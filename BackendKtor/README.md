# BackendKtor

Backend em Kotlin/Ktor com três módulos:

| Módulo | Porta | Responsabilidade |
|---|---|---|
| `gateway` | 8080 | API Gateway — ponto único de entrada, encaminha para os microservices |
| `controle` | 8081 | `GET/PUT /config` — parâmetros de funcionamento do embarcado |
| `logging` | 8082 | `POST/GET /leituras` — histórico de leituras do sensor |

Banco: **NeonDB (PostgreSQL)** via Exposed. As tabelas são criadas automaticamente na primeira execução.

## Configuração

Defina a variável de ambiente `DATABASE_URL` com a connection string do Neon (o formato do painel funciona direto):

```powershell
# PowerShell
$env:DATABASE_URL = "postgresql://usuario:senha@ep-xxxx.neon.tech/neondb?sslmode=require"
```

## Execução

Abra o projeto no IntelliJ (ele baixa o Gradle automaticamente) ou use o Gradle instalado. Rode **em 3 terminais** (o gateway pode ser o último):

```powershell
gradle :controle:run
gradle :logging:run
gradle :gateway:run
```

> No IntelliJ: rode a função `main` de cada módulo (lembrando de definir `DATABASE_URL` em Run Configuration → Environment variables para `controle` e `logging`).

## Teste rápido (sem ESP32/app)

```powershell
curl http://localhost:8080/config
curl -X PUT http://localhost:8080/config -H "Content-Type: application/json" -d '{"tempoAcionamentoMs":2000,"alarmeAtivo":true}'
curl -X POST http://localhost:8080/leituras -H "Content-Type: application/json" -d '{"sombraDetectada":true,"alarmeDisparado":false}'
curl http://localhost:8080/leituras?limit=10
```
