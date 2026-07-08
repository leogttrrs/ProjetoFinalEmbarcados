#include <WiFi.h>
#include <WiFiClientSecure.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>

const char* WIFI_SSID  = "VIRUS";
const char* WIFI_SENHA = "goticula";
const char* BACKEND    = "https://unwaned-pseudoameboid-latrisha.ngrok-free.dev";

WiFiClientSecure clienteSeguro;

bool iniciarRequisicao(HTTPClient& http, const String& url) {
  if (url.startsWith("https://")) {
    return http.begin(clienteSeguro, url);
  }
  return http.begin(url);
}

const int pinoLDR    = 34;
const int pinoLED    = 2;
const int pinoBuzzer = 5;

long tempoAcionamentoMs = 3000;
bool alarmeAtivo        = true;

unsigned long tempoInicioSombra = 0;
bool sombraDetectada  = false;
bool alarmeDisparado  = false;

unsigned long ultimaBuscaConfig  = 0;
unsigned long ultimoEnvioLeitura = 0;
const unsigned long INTERVALO_CONFIG  = 5000;
const unsigned long INTERVALO_LEITURA = 2000;

void setup() {
  Serial.begin(115200);
  pinMode(pinoLED, OUTPUT);
  pinMode(pinoBuzzer, OUTPUT);
  clienteSeguro.setInsecure();

  conectarWiFi();
  buscarConfiguracao();
}

void loop() {
  bool sombraAgora = (analogRead(pinoLDR) == 4095);

  if (sombraAgora) {
    if (!sombraDetectada) {
      tempoInicioSombra = millis();
      sombraDetectada = true;
    }

    unsigned long tempoPassado = millis() - tempoInicioSombra;

    Serial.printf("Sensor: SOMBRA | Acúmulo: %lums / %ldms\n",
                  tempoPassado, tempoAcionamentoMs);

    if (tempoPassado >= (unsigned long)tempoAcionamentoMs && alarmeAtivo) {
      alarmeDisparado = true;
      digitalWrite(pinoLED, HIGH);
      digitalWrite(pinoBuzzer, HIGH);
    }
  } else {
    if (sombraDetectada) {
      Serial.println("Sensor: CLARO  | Resetando contador...");
    }
    sombraDetectada = false;
    alarmeDisparado = false;
    digitalWrite(pinoLED, LOW);
    digitalWrite(pinoBuzzer, LOW);
  }

  unsigned long agora = millis();

  if (agora - ultimaBuscaConfig >= INTERVALO_CONFIG) {
    ultimaBuscaConfig = agora;
    buscarConfiguracao();
  }

  if (agora - ultimoEnvioLeitura >= INTERVALO_LEITURA) {
    ultimoEnvioLeitura = agora;
    enviarLeitura();
  }

  delay(100);
}

void conectarWiFi() {
  Serial.printf("Conectando ao WiFi '%s'", WIFI_SSID);
  WiFi.begin(WIFI_SSID, WIFI_SENHA);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.printf("\nConectado! IP do ESP32: %s\n", WiFi.localIP().toString().c_str());
}

void buscarConfiguracao() {
  if (WiFi.status() != WL_CONNECTED) return;

  HTTPClient http;
  iniciarRequisicao(http, String(BACKEND) + "/config");
  http.addHeader("ngrok-skip-browser-warning", "1");
  int codigo = http.GET();

  if (codigo == 200) {
    JsonDocument doc;
    DeserializationError erro = deserializeJson(doc, http.getString());
    if (!erro) {
      tempoAcionamentoMs = doc["tempoAcionamentoMs"] | tempoAcionamentoMs;
      alarmeAtivo        = doc["alarmeAtivo"] | alarmeAtivo;
      Serial.printf("Config recebida: tempo=%ldms alarme=%s\n",
                    tempoAcionamentoMs, alarmeAtivo ? "ON" : "OFF");
    }
  } else {
    Serial.printf("Falha ao buscar config (HTTP %d)\n", codigo);
  }
  http.end();
}

void enviarLeitura() {
  if (WiFi.status() != WL_CONNECTED) return;

  HTTPClient http;
  iniciarRequisicao(http, String(BACKEND) + "/leituras");
  http.addHeader("Content-Type", "application/json");
  http.addHeader("ngrok-skip-browser-warning", "1");

  JsonDocument doc;
  doc["sombraDetectada"] = sombraDetectada;
  doc["alarmeDisparado"] = alarmeDisparado;

  String corpo;
  serializeJson(doc, corpo);

  int codigo = http.POST(corpo);
  if (codigo == 201) {
    Serial.println("Leitura registrada no backend");
  } else {
    Serial.printf("Falha ao enviar leitura (HTTP %d)\n", codigo);
  }
  http.end();
}
