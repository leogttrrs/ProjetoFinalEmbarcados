# AppMobile

App Android em Kotlin + Jetpack Compose.

## Telas

- **Leituras**: gráfico de luminosidade + lista do histórico (busca `GET /leituras` no gateway, atualiza a cada 5 s).
- **Configuração**: endereço do backend + parâmetros do embarcado (tempo de acionamento, alarme on/off), enviados com `PUT /config`.

## Execução

1. Abra a pasta `AppMobile` no **Android Studio** e aguarde o sync do Gradle.
2. Conecte o celular via USB (com depuração USB ativa) e rode o app — o emulador não acessa a rede local.
3. No app, aba **Configuração**, informe o endereço do backend: `http://<IP-do-computador>:8080`.

> O celular precisa estar na mesma rede WiFi do computador que roda o backend. O manifest já permite tráfego HTTP (`usesCleartextTraffic`).
