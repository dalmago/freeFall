
// I2Cdev and MPU6050 must be installed as libraries, or else the .cpp/.h files
// for both classes must be in the include path of your project
#include "I2Cdev.h"
#include "MPU6050.h"
#include "Wire.h"

#define scale 1000
#define timeExecution 50


MPU6050 accelgyro;

int16_t ax, ay, az;
int sum1, sum2, sum3, dif, acceleration;
int speed = 0;
int count = 0;

#define LED_PIN 13
bool blinkState = false;

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
    
}

void loop() {
    // read raw accel/gyro measurements from device
    //accelgyro.getMotion6(&ax, &ay, &az, &gx, &gy, &gz);
    // these methods (and a few others) are also available
    sum3 = sum2;
    sum2 = sum1;
    accelgyro.getAcceleration(&ax, &ay, &az);
    sum1 = ax+ay+az;
    dif = (sum1-sum2)+(sum1-sum3)+(sum2-sum3);
    acceleration = dif/scale;
    if(count < 10)
       speed = 0;
    else 
       //if(speed > 0 && acceleration == 0)
         //  speed = speed - speed*0.4;
       //else
           speed = abs(speed + (acceleration *timeExecution));
    
    // display tab  -separated accel/gyro x/y/z values
    Serial.print("a:\t");
    Serial.print(ax); Serial.print("\t");
    Serial.print(ay); Serial.print("\t");
    Serial.print(az); Serial.print("\t");
    Serial.print(sum1);Serial.print("\t");
    Serial.print(sum2);Serial.print("\t");
    Serial.print(sum3);Serial.print("\t");
    Serial.print(dif);Serial.print("\t");
    Serial.print(acceleration);Serial.print("\t");
    Serial.println(speed);Serial.print("\t");
    
    // blink LED to indicate activity
    blinkState = !blinkState;
    digitalWrite(LED_PIN, blinkState);
    delay(timeExecution);
    count++;
}
