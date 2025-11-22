// GameController.ino  (single-file version) - using hd44780_I2Cexp (no LiquidCrystal_I2C)


#include <SoftwareSerial.h>
#include <Wire.h>
#include <hd44780.h>
#include <hd44780ioClass/hd44780_I2Cexp.h>


// Auto-detect I2C LCD backpack (no need for 0x27/0x3F address)
hd44780_I2Cexp lcd;


// XBee stays on pins 11/12 (3.3 V power to XBee!)
SoftwareSerial mySerial(11, 12);   // RX, TX


const int PIN_LED0 = 2;
const int PIN_LED1 = 3;
const int PIN_LED2 = 4;
const int PIN_LED3 = 5;


const int PIN_BTN0 = 6;
const int PIN_BTN1 = 7;
const int PIN_BTN2 = 8;
const int PIN_BTN3 = 9;


// Buzzer & Vibration motor pins
const int PIN_BUZZER = 10;  // passive buzzer + to D13, - to GND
const int PIN_VIBE   = 13;  // LilyPad vibe + to D10, - to GND


// -------- FEEDBACK FUNCTIONS --------
void clearLeds() {
  digitalWrite(PIN_LED0, LOW);
  digitalWrite(PIN_LED1, LOW);
  digitalWrite(PIN_LED2, LOW);
  digitalWrite(PIN_LED3, LOW);
}


void showColor(int idx) {
  clearLeds();
  int pin = PIN_LED0;
  if (idx == 1)      pin = PIN_LED1;
  else if (idx == 2) pin = PIN_LED2;
  else if (idx == 3) pin = PIN_LED3;
  digitalWrite(pin, HIGH);
}


// Buzzer helpers
void okBeep()    { tone(PIN_BUZZER, 1000, 200); delay(210); }
void errorBeep() { tone(PIN_BUZZER,  200, 400); delay(410); }

// 🎵 New: note for each color
void playColorTone(int idx) {
  // simple scale: change these if you want
  const int freqs[4] = {
    262,  // C4  - color 0 (e.g., RED)
    330,  // E4  - color 1 (BLUE)
    392,  // G4  - color 2 (GREEN)
    494   // B4  - color 3 (YELLOW)
  };

  if (idx < 0 || idx > 3) return;

  tone(PIN_BUZZER, freqs[idx], 180);  // play for ~180 ms
  delay(190);                         // small wait so tones don't overlap
}

// Vibration helpers
void vibrateMs(unsigned long ms) { digitalWrite(PIN_VIBE, HIGH); delay(ms); digitalWrite(PIN_VIBE, LOW); }
void vibeOK() { for (int i=0;i<2;i++){ digitalWrite(PIN_VIBE,HIGH); delay(80); digitalWrite(PIN_VIBE,LOW); delay(70);} }
void vibeFail(){ vibrateMs(400); }


// Combo effects
void fxOK()   { okBeep();   vibeOK();  }
void fxFail() { errorBeep(); vibeFail(); }


// -------- BUTTON / SERIAL LOGIC --------
unsigned long lastBtnTime[4] = {0,0,0,0};
const unsigned long debounceMs = 150;


// -------- LCD helper --------
void lcdMsg(const char* line1, const char* line2 = "") {
  lcd.clear();
  lcd.setCursor(0, 0); lcd.print(line1);
  lcd.setCursor(0, 1); lcd.print(line2);
}


// -------- SETUP --------
void setup() {
  // LCD init (auto-detect backpack)
  int status = lcd.begin(16, 2);   // 16x2 LCD
  (void)status;                    // ignore status for now
  lcd.backlight();
  lcdMsg("Game Ready!");
  delay(800);


  // Serial links
  Serial.begin(9600);    // optional local debug
  mySerial.begin(9600);  // XBee / other Arduino link


  // I/O
  pinMode(PIN_LED0, OUTPUT);
  pinMode(PIN_LED1, OUTPUT);
  pinMode(PIN_LED2, OUTPUT);
  pinMode(PIN_LED3, OUTPUT);


  pinMode(PIN_BTN0, INPUT_PULLUP);
  pinMode(PIN_BTN1, INPUT_PULLUP);
  pinMode(PIN_BTN2, INPUT_PULLUP);
  pinMode(PIN_BTN3, INPUT_PULLUP);


  pinMode(PIN_BUZZER, OUTPUT);
  pinMode(PIN_VIBE, OUTPUT);
  digitalWrite(PIN_VIBE, LOW);


  clearLeds();
  mySerial.println("GameController ready");
}


// -------- HANDLERS --------
void handleSerialCommand() {
  if (!mySerial.available()) return;
  char c = (char)mySerial.read();

  if (c >= '0' && c <= '3') {
    int idx = c - '0';
    showColor(idx);
    playColorTone(idx);  // 🎵 play note for this LED/color

    // LCD: show which color index was requested
    lcd.clear();
    lcd.setCursor(0,0); lcd.print("Show color:");
    lcd.setCursor(0,1); lcd.print(idx);
  }
  else if (c == 'X') {
    clearLeds();
    lcdMsg("New round");
  }
  else if (c == 'O') {
    fxOK();
    lcdMsg("Correct!");
  }
  else if (c == 'W') {
    fxFail();
    lcdMsg("Wrong, try again");
  }
  else if (c == 'V') {
    vibrateMs(200);
    lcdMsg("Vibrate");
  }
}



// void handleButtons() {
//   int pins[4] = {PIN_BTN0, PIN_BTN1, PIN_BTN2, PIN_BTN3};


//   for (int i = 0; i < 4; i++) {
//     int state = digitalRead(pins[i]); // LOW = pressed (INPUT_PULLUP)
//     if (state == LOW) {
//       unsigned long now = millis();
//       if (now - lastBtnTime[i] > debounceMs) {
//         // haptic tick
//         digitalWrite(PIN_VIBE, HIGH); delay(40); digitalWrite(PIN_VIBE, LOW);


//         // notify peer as '0'..'3'
//         mySerial.write('0' + i);
//         lastBtnTime[i] = now;


//         // LCD quick status
//         lcd.setCursor(0,1);
//         lcd.print("Pressed: ");
//         lcd.print(i);
//         lcd.print("   "); // pad to clear leftovers
//       }
//     }
//   }
// }
int lastBtnState[4] = {HIGH, HIGH, HIGH, HIGH};  // previous reading for each button

void handleButtons() {
  int pins[4] = {PIN_BTN0, PIN_BTN1, PIN_BTN2, PIN_BTN3};

  for (int i = 0; i < 4; i++) {
    int state = digitalRead(pins[i]); // current state
    unsigned long now = millis();

    // We only care when it changes from HIGH -> LOW (new press)
    if (state == LOW && lastBtnState[i] == HIGH && (now - lastBtnTime[i] > debounceMs)) {

  // notify PC as '0'..'3'
  mySerial.write('0' + i);

  // DEBUG
  Serial.print("BUTTON SENT INDEX: ");
  Serial.println(i);

  // 🎵 play note for this button/color
  playColorTone(i);

  // haptic tick
  digitalWrite(PIN_VIBE, HIGH); delay(40); digitalWrite(PIN_VIBE, LOW);

  lastBtnTime[i] = now;

  // LCD quick status
  lcd.setCursor(0,1);
  lcd.print("Pressed: ");
  lcd.print(i);
  lcd.print("   ");
}


    // update last state
    lastBtnState[i] = state;
  }
}


// -------- LOOP --------
void loop() {
  handleSerialCommand();
  handleButtons();
}
