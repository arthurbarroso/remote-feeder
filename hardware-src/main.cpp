#include <Arduino.h>
#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <WiFi.h>
#include <PubSubClient.h>
#include <ESP32Servo.h>

LiquidCrystal_I2C lcd(0x27, 16, 2);

const char* mqtt_host = "";
const int mqtt_port = 1883;
const char* topic = "feeder/feed";
const char* mqtt_pass = "";
const char* mqtt_user = "";

const char* ssid = "";
const char* password = "";

WiFiClient espClient;
PubSubClient client(espClient);

Servo servo;

void callback(char* topic, byte* payload, unsigned int length){
    Serial.print("Message arrived in topic: ");
    Serial.println(topic);

    Serial.print("Message: ");
    for (int i = 0; i < length; i++) {
        Serial.print((char)payload[i]);
    }

    char my_payload[length + 1];
    memcpy(my_payload, payload, length);
    my_payload[length + 1] = '\0';

    if(String(topic) == "feeder/feed"){
        lcd.clear();
        lcd.setCursor(0, 1);
        lcd.print("Feeding pet!");
        servo.write(130);
        Serial.println("reset servo");
        delay(1000);
        servo.write(180);
        Serial.println("opened servo");
        delay(6000);
        servo.write(130);
        Serial.println("reset servo");
        delay(6000);
        lcd.clear();
        Serial.println("Publishing");
        Serial.println(my_payload);
        client.publish("feeder/client-fed", my_payload);
        delay(3000);
        lcd.print("Waiting:MSGS");
    }


    Serial.println();
    Serial.println("-----------------");
}

void setup() {
    Serial.begin(921600);
    delay(1000);
    lcd.init();
    lcd.backlight();
    lcd.print("Starting...");
    WiFi.mode(WIFI_STA);
    WiFi.begin(ssid, password);
    lcd.clear();
    lcd.print("Connecting:WIFI");
    while(WiFi.status() != WL_CONNECTED){
        Serial.print("... connecting");
        delay(100);
    }

    lcd.clear();
    lcd.setCursor(0, 1);
    lcd.print("Connected:WIFI!");


    servo.attach(18);

    // 130 (closed)
    servo.write(130);

    delay(5000);
    client.setServer(mqtt_host, mqtt_port);
    client.setCallback(callback);
    client.setBufferSize(512);

    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("Connecting:MQTT");
    while(!client.connected()){
        Serial.println("Connecting:MQTT");
        String clientId = "ESP32Client-";
        clientId += String(random(0xffff), HEX);
        if (client.connect(clientId.c_str(), mqtt_user, mqtt_pass)) {
            lcd.clear();
            lcd.setCursor(0, 1);
            lcd.print("Connected:MQTT!");
            Serial.println("Connected to mqtt");
        } else {
            Serial.print("Failed to connect to mqtt with state: ");
            Serial.print(client.state());
            delay(2000);
        }
    }

    delay(4000);
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("Waiting:MSGS");

    client.subscribe("feeder/feed");
}

void loop() {
    client.loop();
}
