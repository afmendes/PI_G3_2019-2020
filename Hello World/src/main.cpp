#include <Arduino.h>

void setup()
{
  Serial.begin(115200);
  Serial.println("Setup ready");
}

void loop()
{
  Serial.println(".");
  delay(500);
}