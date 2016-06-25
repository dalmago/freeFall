#define ONBOARD_LED 13
#define HC05KEY 29

#define DEBUG

#define Serial_blth Serial1
#define Serial_debug Serial

#define HANDSHAKE "ready!"
#define HANDSHAKE_ANS "start!"

// 1 or 0
#define CONFIGURE_BLTH 0

void setup() {
  pinMode(HC05KEY, OUTPUT);
  digitalWrite(HC05KEY, 0);
  pinMode(ONBOARD_LED, OUTPUT);
  digitalWrite(ONBOARD_LED, LOW);
  String ans;

  Serial_blth.begin(9600);
#ifdef DEBUG
  Serial_debug.begin(9600);
#endif

  delay(5000); // Let HC-05 initialize



#if CONFIGURE_BLTH
#ifdef DEBUG
  Serial_debug.println("Setup commands");
#endif

  digitalWrite(ONBOARD_LED, 1);
  digitalWrite(HC05KEY, HIGH); // Enter AT mode
  delay(5000);

  Serial_blth.write("AT+RMAAD\r\n"); // Clear any paired devices
  ans = Serial_blth.readString();
  Serial_blth.write("AT+ROLE=0\r\n"); // Set mode to SLAVE
  ans += Serial_blth.readString();
  Serial_blth.write("AT+UART=9600,0,0\r\n"); // Baud Rate
  ans += Serial_blth.readString();
  Serial_blth.write("AT+NAME=blthFreeFall\r\n"); // Device name
  ans += Serial_blth.readString();
  Serial_blth.write("AT+PSWD=4321\r\n");  // Pair password
  ans += Serial_blth.readString();

#ifdef DEBUG
  Serial_debug.println(ans);
#endif

  delay(2000);
  digitalWrite(HC05KEY, LOW); // Exit AT mode
  delay(5000);
  digitalWrite(ONBOARD_LED, LOW);
#endif

  ans = "";
  while (ans != HANDSHAKE) {
    if (Serial_blth.available()) {
      ans += Serial_blth.readString();
#ifdef DEBUG
      Serial_debug.println(ans);
#endif
    }
  }
  
  digitalWrite(ONBOARD_LED, HIGH);
  Serial_blth.write(HANDSHAKE_ANS);

  delay(1000);
  digitalWrite(ONBOARD_LED, LOW);
}
int i = 0;
void loop() {
  Serial_blth.write(i++);
  delay(100);
}
