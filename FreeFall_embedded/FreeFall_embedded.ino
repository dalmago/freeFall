
// I2Cdev and MPU6050 must be installed as libraries, or else the .cpp/.h files
// for both classes must be in the include path of your project
#include "I2Cdev.h"
#include "MPU6050.h"
#include "Wire.h"
#include "Timer.h"

#define HC05KEY 3
#define Serial_blth Serial
#define Serial_debug Serial1
#define HANDSHAKE "ready!"
#define HANDSHAKE_ANS "start!"
#define CONFIGURE_BLTH 0 // 1 or 0

//#define _DEBUG

#define LED_PIN 13

#define scale 150
#define TIME_EXECUTION 10    //ms

#define PONDERACAO_MEDIA 10

#define ERRO_ASSOCIADO 4000

#define AMOSTRAS_ADAPT 100

MPU6050 accelgyro;

Timer t;

int16_t ax, ay, az;                   // Variaveis que recebem os valores do acelerometro
int32_t ax_med, ay_med, az_med;       // Variaveis que recebem os valores medios dos eixos

int32_t modulo, repouso;
bool dadosNovos = true;

int16_t sum1, sum2, sum3, dif, acceleration, acceleration2;
int32_t speedFall = 0;
int32_t displaySpeed = 0;
int16_t count = 0;

bool blinkState = true;

String ans;

void setup() {
  pinMode(HC05KEY, OUTPUT);
  digitalWrite(HC05KEY, 0);
  Serial_blth.begin(38400);
  // (38400 chosen because it works as well at 8MHz as it does at 16MHz, but

#ifdef _DEBUG
  Serial_debug.begin(38400);
#endif

#if CONFIGURE_BLTH
  delay(5000); // Let HC-05 initialize
  digitalWrite(LED_PIN, 1);
  digitalWrite(HC05KEY, HIGH); // Enter AT mode
  delay(5000);

  Serial_blth.write("AT+RMAAD\r\n"); // Clear any paired devices
  ans = Serial_blth.readString();
  Serial_blth.write("AT+ROLE=0\r\n"); // Set mode to SLAVE
  ans += Serial_blth.readString();
  Serial_blth.write("AT+UART=38400,0,0\r\n"); // Baud Rate
  ans += Serial_blth.readString();
  Serial_blth.write("AT+NAME=blthFreeFall\r\n"); // Device name
  ans += Serial_blth.readString();
  Serial_blth.write("AT+PSWD=4321\r\n");  // Pair password
  ans += Serial_blth.readString();

#ifdef _DEBUG
  Serial_debug.println(ans);
#endif

  delay(2000);
  digitalWrite(HC05KEY, LOW); // Exit AT mode
  delay(5000);
  digitalWrite(LED_PIN, LOW);
#endif

  ans = "";
  while (ans != HANDSHAKE) {
    if (Serial_blth.available()) {
      ans += Serial_blth.readString();
    }
  }
  Serial_blth.write(HANDSHAKE_ANS);

  Wire.begin();

  // initialize device
#ifdef _DEBUG
  Serial.println("Initializing I2C devices...");
#endif
  //
  accelgyro.initialize();

  // verify connection
#ifdef _DEBUG
  Serial.println("Testing device connections...");
  Serial.println(accelgyro.testConnection() ? "MPU6050 connection successful" : "MPU6050 connection failed");
#endif
  pinMode(LED_PIN, OUTPUT);

  digitalWrite(LED_PIN, blinkState);

  delay(1000);

  t.every(TIME_EXECUTION, readAcceleration);          // Habilita leitura de dados
}


void loop()
{
  t.update();
  if (dadosNovos)
  {
    dadosNovos = false;
    modulo = (sqrt(ax_med * ax_med + ay_med * ay_med + az_med * az_med));

    if (count < AMOSTRAS_ADAPT)
    {
      speedFall = 0;
      count++;
      repouso = modulo;
      if (count == 1)
      {
#ifdef _DEBUG
        Serial_debug.println("Adaptando a media");
#endif
      }
    }
    else
    {
      int16_t resultante;

      acceleration = modulo / scale;    // Apenas para mostrar na tela o modulo

      resultante = repouso - modulo;
      if (((resultante) > (ERRO_ASSOCIADO)) || ((resultante) < -(ERRO_ASSOCIADO)))
      {
        speedFall = speedFall + ((resultante * TIME_EXECUTION) / scale);
      }

      displaySpeed = abs(speedFall);

#ifdef _DEBUG
      // display tab  -separated accel/gyro x/y/z values
      Serial_debug.print("\t");
      Serial_debug.print("a:\t");
      Serial_debug.print(ax_med); Serial_debug.print("\t");
      Serial_debug.print(ay_med); Serial_debug.print("\t");
      Serial_debug.print(az_med); Serial_debug.print("\t");
      Serial_debug.print(repouso); Serial_debug.print("\t");
      Serial_debug.print(modulo); Serial_debug.print("\t");
      Serial_debug.print(acceleration); Serial_debug.print("\t");
      Serial_debug.print(speedFall); Serial_debug.print("\t");
      Serial_debug.println(displaySpeed / 1000);
#else
      //Serial.print("\t");
      displaySpeed = (displaySpeed / 2778);
      //Serial_blth.write(((char*)displaySpeed), sizeof(displaySpeed));
      Serial_blth.write(displaySpeed);
      //Serial.println(" km//h");
#endif


      // blink LED to indicate activity
      blinkState = !blinkState;
      digitalWrite(LED_PIN, blinkState);
    }
  }
  //else        // Serve apenas para verificar se está "dando tempo" de tratar antes de estourar o tempo
  //{
  //    Serial.println("\n####");
  //}
}

void readAcceleration()
{
  accelgyro.getAcceleration(&ax, &ay, &az);       // Lê os dados do acelerometro

  ax_med = ((ax_med * PONDERACAO_MEDIA) + ax) / (1 + PONDERACAO_MEDIA);  // Varia os eixos conforme uma média
  ay_med = ((ay_med * PONDERACAO_MEDIA) + ay) / (1 + PONDERACAO_MEDIA);
  az_med = ((az_med * PONDERACAO_MEDIA) + az) / (1 + PONDERACAO_MEDIA);
  dadosNovos = true;
}
