
// I2Cdev and MPU6050 must be installed as libraries, or else the .cpp/.h files
// for both classes must be in the include path of your project
#include "I2Cdev.h"
#include "MPU6050.h"
#include "Wire.h"
#include "Timer.h"

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

#define LED_PIN 13
bool blinkState = true;

void setup() {
   
     Wire.begin();
    // (38400 chosen because it works as well at 8MHz as it does at 16MHz, but
    Serial.begin(38400);

    // initialize device
    Serial.println("Initializing I2C devices...");
    accelgyro.initialize();

    // verify connection
    Serial.println("Testing device connections...");
    Serial.println(accelgyro.testConnection() ? "MPU6050 connection successful" : "MPU6050 connection failed");

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
        modulo = (sqrt(ax_med*ax_med + ay_med*ay_med + az_med*az_med));

        if(count < AMOSTRAS_ADAPT)
        {
            speedFall = 0;
            count++;
            repouso = modulo;
            if (count == 1)
            {
                Serial.println("Adaptando a media");
            }
        }
        else
        { 
            int16_t resultante;

            acceleration = modulo/scale;      // Apenas para mostrar na tela o modulo

            resultante = repouso - modulo;
            if (((resultante) > (ERRO_ASSOCIADO)) || ((resultante) < -(ERRO_ASSOCIADO)))
            {
                speedFall = speedFall + ((resultante * TIME_EXECUTION)/scale);
            }
            
            displaySpeed = abs(speedFall);

#ifdef _DEBUG        
        // display tab  -separated accel/gyro x/y/z values
            Serial.print("\t");
            Serial.print("a:\t");
            Serial.print(ax_med); Serial.print("\t");
            Serial.print(ay_med); Serial.print("\t");
            Serial.print(az_med); Serial.print("\t");
            Serial.print(repouso); Serial.print("\t");
            Serial.print(modulo); Serial.print("\t");
            Serial.print(acceleration);Serial.print("\t");
            Serial.print(speedFall);Serial.print("\t");
            Serial.println(displaySpeed/1000);

#endif

#ifndef _DEBUG
            Serial.print("\t");
            Serial.print(displaySpeed/2778);
            Serial.println(" km//h");
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

    ax_med = ((ax_med*PONDERACAO_MEDIA) + ax)/(1 + PONDERACAO_MEDIA);      // Varia os eixos conforme uma média
    ay_med = ((ay_med*PONDERACAO_MEDIA) + ay)/(1 + PONDERACAO_MEDIA);
    az_med = ((az_med*PONDERACAO_MEDIA) + az)/(1 + PONDERACAO_MEDIA);
    dadosNovos = true;
}
