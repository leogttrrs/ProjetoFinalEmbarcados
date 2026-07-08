# HardWareESP32

Sketch do sistema embarcado (Arduino IDE).

## Circuito

- **LDR** (divisor de tensão) → pino 34 (ADC)
- **LED** → pino 2
- **Buzzer** → pino 5

O potenciômetro da Atividade 2 foi removido: o tempo de acionamento agora vem do backend, configurado pelo app.

## Funcionamento

- Sensor tratado como booleano: ADC em 4095 = sombra, qualquer outro valor = claro.
- Sombra contínua por `tempoAcionamentoMs` → aciona LED + buzzer (se `alarmeAtivo`).
- A cada 5 s: `GET /config` no backend (parâmetros definidos pelo app).
- A cada 2 s: `POST /leituras` com o estado do sensor (sombra) e do alarme.

## Antes de gravar

1. Instale a biblioteca **ArduinoJson** (Benoit Blanchon) no Library Manager.
2. Edite no topo do sketch: `WIFI_SSID`, `WIFI_SENHA` e `BACKEND` (IP do computador que roda o backend, ex.: `http://192.168.0.42:8080`).
3. ESP32 e computador devem estar na mesma rede local.
