#include "BluetoothSerial.h"
#include <WiFi.h>
#include <IOXhop_FirebaseESP32.h>
#include "SPIFFS.h"

#include <NTPClient.h>
#include <WiFiUdp.h>

// Firebase Settings
#define FIREBASE_HOST "projetopi-a6e3d.firebaseio.com"
#define FIREBASE_AUTH "pNhNsfrhgEIZnZ4YB97VUI3kmgxZmaJDAiiAjV1a"

// Bluetooth name for the ESP Module
#define BT_NAME "ESP32"

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

// Define NTP Client to get time
WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP);

// Variables to save date and time
String formattedDate;

//network credentials
//this variables will be inserted via mobile application and saved on a file
String ssid;
String password;
String UUID;

//strings that will contain the directory of various variables in the cloud
String location_time;
String location_soil;
String location_exists;
String location_offset;
String location_BOMBA;


int i = 0;

//flags
int  Bomba_on = 0;
int flag_ligar_wifi = 1;
int flag_wifi_on = 0;
int flag_remote = 1;

//size of the identifier of the strings recieved via bluetooth
int identifier_size = 5;

String received_str;
String received_data_identifier;
String received_data;
char received_char;

//variable to save time
unsigned long startedWaiting;

// Set Sensor Pins
const int sensor_soilHumidity_pin = 32;
// Stores Sensor reads
int sensor_soilHumidity_value;

//const int button_pin = 20;
int button_mode = 1;

//value of offset
int soilHumidity_offset = 3000; //60% humidity standard value

// Set BOMBA GPIO
const int ledPin = 26;

BluetoothSerial SerialBT;


 /**
 * this method will read a file and return its data
 * @param directory - string containing the directory where the file is located 
 * @return returns a string with the data readed from the file
 */
String getData(String directory){
  //Serial.println("Directory: " + directory);
  File file = SPIFFS.open(directory,FILE_READ);
  String text;
  while(file.available()){
    text += char(file.read());
  }
  file.close();
  //Serial.println("Text Data: " + text);

  return text;
}

 /**
 * this method will write a file and return its data
 * @param sdata - string containing the data
 * @param directory - string containing the directory where the file is located 
 * @param writeType - constant char* with type writeType
 */
String setData(String sdata, String directory, const char* writeType){
  //Serial.println("Directory: " + directory);
  File file = SPIFFS.open(directory,writeType);
  if(file.print(sdata)){
    Serial.println("File "+directory+" written with: "+sdata);
  } else {
    Serial.println("Failed to write in "+directory+" with: "+sdata);
  }
  file.close();
  //Serial.println("Text Data: " + text);
}

/*
*setup
*defining pin modes for the sensor and the led that works as debug
*If SPIFFS do not begin it will print error on the console
*starting Bluetooth with BT_NAME
*@return prints "Setup Ended" in the consele
*/
void setup() {
  Serial.begin(115200);

  pinMode(ledPin, OUTPUT);
  pinMode(sensor_soilHumidity_pin, INPUT);
  //pinMode(button_pin, INPUT);

  //If SPIFFS do not begin it will print error on the console
  Serial.println();
   if(!SPIFFS.begin(true)){
      Serial.println("Err 1");
      return;
   }
   //starting Bluetooth with BT_NAME
  SerialBT.begin(BT_NAME); //Bluetooth device name
  Serial.println("Setup Ended");
}

/*
*this method will run on a loop
*it will print ou on the console a '*' each cycle for effects of debugging
*if data available in the buffer, it will divide de data received and make decisions based on that
*/
void loop() {
  //button_mode = analogRead(button_pin);

  Serial.print("*");

  //if data in the buffer
  if (SerialBT.available()) {
    //Serial.print("received char");
    received_char = SerialBT.read();
    received_str += received_char;
  }
  //all data received received
  //divide all the string in data and string identifier(first identifier_size chars)
  if(received_char == '\n')
    {
      Serial.println("Received command: " + received_str);
      for(i=0; i<identifier_size; i++){
        received_data_identifier += received_str[i];
      }
      for(i=i; i<received_str.length()-2; i++){
        received_data += received_str[i];
      }

  //make decisions based on the data_identifier
      if(received_data_identifier == "ssid-"){
        // Retrieves the SSID
        ssid = received_data;    
        setData(received_data,"/ssid.txt",FILE_WRITE);
        //Serial.println("SSID: " + ssid);    
      }else if(received_data_identifier == "pass-"){
        // Retrieves the password
        password = received_data;   
        setData(received_data,"/password.txt",FILE_WRITE);
        //Serial.println("PASS: " + password);     
      }else if(received_data_identifier == "ligar"){
        //signal to connect to the wifi
        flag_ligar_wifi = 1;
      }else if(received_data_identifier == "uuid-"){
        // Retrieves the UUID
        UUID = received_data;
        setData(received_data,"/UUID.txt",FILE_WRITE);
        //Serial.println("PASS: " + password);      
      }
      
      //clear data
      received_str = "";
      received_data_identifier = "";
      received_data = "";
      received_char = '0';
  }

  //Serial.println("trying to connect");

  //trying to connect to wifi
  if(flag_ligar_wifi){

    ssid = getData("/ssid.txt");
    password = getData("/password.txt"); 
    UUID = getData("/UUID.txt");
    
    location_time = "users/"+UUID+"/time";
    location_soil = "users/"+UUID+"/soilHumidity";
    location_exists = "users/"+UUID+"/exists";
    location_offset = "users/"+UUID+"/offset";
    location_BOMBA = "users/"+UUID+"/BOMBA";
    //location_remote = "users/"+UUID+"/remote";
    
    //debug prints
    Serial.println("Attemping:");
    Serial.println("SSID: " + ssid);  
    Serial.println("PASS: " + password);
    Serial.println("UUID: " + UUID);
   
    // connect to wifi.
    WiFi.begin(ssid.c_str(), password.c_str());
    Serial.print("connecting");
    
    //try to establish the connection during 30 seconds
    //if connection fails, it prints "connection failed" on the console
    unsigned long startedWaiting2 = millis();
    while (WiFi.status() != WL_CONNECTED && millis() - startedWaiting2 <= 30000) {
      Serial.print(".");
      delay(500);
    }

    //not connected to wifi
    if(WiFi.status() != WL_CONNECTED){
      flag_wifi_on = 0;
      Serial.println();
      Serial.print("\nconnection failed: ");
      
      
    //connected to wifi
    }else{
      flag_wifi_on = 1;
      Serial.print("\nconnected: ");
      Serial.println(WiFi.localIP());

      //firebase begin
      Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
      Firebase.set(location_exists.c_str(),1);
      //Firebase.set(location_remote.c_str(),1);
      
      // Initialize a NTPClient to get time
      timeClient.begin();
      // Set offset time in seconds to adjust for your timezone, for example:
      // GMT +1 = 3600
      // GMT +8 = 28800
      // GMT -1 = -3600
      // GMT 0 = 0
      timeClient.setTimeOffset(3600);
      
    }

     flag_ligar_wifi = 0; 
  }

  
  //updates flag based on wifi conection and database remote option
  /*
  if(WiFi.status() != WL_CONNECTED){
    if(flag_wifi_on) Serial.println("WIFI CONNECTION LOST");
    flag_wifi_on = 0;
  }else{
    if(Firebase.getInt(location_remote.c_str()) == 1){
      flag_wifi_on = 1;  
      flag_remote = 1;
    }else{
      WiFi.mode(WIFI_OFF);
      flag_wifi_on = 0;
      flag_remote = 0;  
      }
  }*/

   
  //if connected to the wifi - mode remote
  if(flag_wifi_on && button_mode){    
    sensor_soilHumidity_value = analogRead(sensor_soilHumidity_pin);
    //sensor_soilHumidity_value = random(0, 1023);
    

    //time
    //Serial.print("B");
    while(!timeClient.update()) {
      timeClient.forceUpdate();
    }
    //Serial.println("O");
    
    // The formattedDate comes with the following format:
    // 2018-05-28T16:00:13Z
    // We need to extract date and time
    
    formattedDate = timeClient.getFormattedDate();

    //push values to firebase
    Firebase.pushString(location_time.c_str(),formattedDate);
    delay(200);
    Serial.println(formattedDate);
    Firebase.pushInt(location_soil.c_str(),sensor_soilHumidity_value);
    delay(300);
    Serial.println(sensor_soilHumidity_value);
    Serial.println("--------------------------------");

    soilHumidity_offset = Firebase.getInt(location_offset.c_str());
    delay(300);

    int bombState = Firebase.getInt(location_BOMBA.c_str());
    delay(300);

    //BOMBA ON for 3 seconds
    if(((sensor_soilHumidity_value>soilHumidity_offset) || (bombState == 1)) && (Bomba_on == 0) ){
      Bomba_on = 1;
      Firebase.setInt(location_BOMBA.c_str(),1);
      delay(300);
      startedWaiting = millis();
      digitalWrite(ledPin, HIGH);
      Serial.println("Bomba ON ONLINE");
    }

    else if (millis() - startedWaiting > 3000) {
      if(sensor_soilHumidity_value>soilHumidity_offset){
        Bomba_on = 1;
        Firebase.setInt(location_BOMBA.c_str(),1);
        delay(300);
        startedWaiting = millis();
        digitalWrite(ledPin, HIGH);
        Serial.println("Bomba ON ONLINE");
      } else {
        Bomba_on = 0;
        Firebase.setInt(location_BOMBA.c_str(),0);
        delay(300);
        digitalWrite(ledPin, LOW);
        Serial.println("Bomba OFF ONLINE");
      }
    }
    
    delay(1000);
    
  }else{//MODE local (offline)
    sensor_soilHumidity_value = analogRead(sensor_soilHumidity_pin);
    //Serial.print(String(sensor_soilHumidity_value) + ": ");
    //sensor_soilHumidity_value = random(0, 1023);
    
    //BOMBA ON for 3 seconds
    if((sensor_soilHumidity_value>soilHumidity_offset) && (Bomba_on == 0)){
      Bomba_on = 1;
      startedWaiting = millis();
      digitalWrite(ledPin, HIGH);
      Serial.println("Bomba ON OFFLINE");
    }

    if (millis() - startedWaiting > 3000) {
      digitalWrite(ledPin, LOW);
      Serial.println("Bomba OFF OFFLINE");
      Bomba_on = 0;
    }
    
  }
  delay(20);
}
