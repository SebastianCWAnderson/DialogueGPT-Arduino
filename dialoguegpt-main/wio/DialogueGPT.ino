/****************************************************************************
  Code base on "MQTT Exmple for SeeedStudio Wio Terminal".
  Author: Salman Faris
  Source: https://www.hackster.io/Salmanfarisvp/mqtt-on-wio-terminal-4ea8f8
*****************************************************************************/

#include <rpcWiFi.h>
#include "TFT_eSPI.h"
#include <PubSubClient.h>
#include "heading.h"
#include "notes.h"

#define SPEAKER D0
#define LIGHT_SENSOR A2

unsigned long startMillis;
unsigned long currentMillis;

int notesTab[] = { 1911, 1804, 1703, 1607, 1517, 1432, 1351, 1276, 1204, 1136, 1073, 1012, 956, 902, 851, 804, 758, 716, 676, 638, 602, 568, 536, 506, 478, 451, 426, 402, 379, 358, 338, 319, 301, 284, 268, 253 };
// Update these with values suitable for your network.
const char* ssid = SSID;          // WiFi Name
const char* password = PASSWORD;  // WiFi Password




/**********  HOW TO FIND YOUR MOSQUITTO BROKER ADDRESS*******************
  In Windows command prompt, use the command:   ipconfig
  Copy the Ip address of "Wireless LAN adapter Wi-Fi: IPv4 Address"
  Enter the IP in the sever variable below.
*************************************************************************/

const char* server = my_IPv4;  // MQTT Broker URL

/* TODO
    add the corresponding topics
*/
const char* TOPIC_sub = "DialogueGPT/Response";
const char* TOPIC_pub_connection = "DialogueGPT/Connection";
const char* TOPIC_light_sensor = "DialogueGPT/Light";


TFT_eSPI tft;

WiFiClient wioClient;
PubSubClient client(wioClient);
long lastMsg = 0;
char msg[50];
int value = 0;


void setup_wifi() {

  delay(10);

  tft.setTextSize(2);
  tft.setCursor((320 - tft.textWidth("Connecting to Wi-Fi..")) / 2, 120);
  tft.print("Connecting to Wi-Fi..");

  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);  // Connecting WiFi

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");

  tft.fillScreen(TFT_BLACK);
  tft.setCursor((320 - tft.textWidth("Connected!")) / 2, 120);
  tft.print("Connected!");

  //tft.println("IP address: ");
  //tft.println(WiFi.localIP()); // Display Local IP Address
}

String getPayload() {
}

void callback(char* topic, byte* payload, unsigned int length) {
  // process payload and convert it to a string
  char buff_p[length];
  for (int i = 0; i < length; i++) {
    buff_p[i] = (char)payload[i];
  }
  buff_p[length] = '\0';
  String message = String(buff_p);
  // end of conversion
  /***************  Action with topic and messages ***********/
  setColorAndPrintMessage(message);
}

void setColorAndPrintMessage(String message) {
  int bgColor;
  int textColor;
  //automatic dark mode detection 
  if (analogRead(LIGHT_SENSOR) < 200) {
    bgColor = TFT_BLACK;
    textColor = TFT_WHITE;
  } else {
    bgColor = TFT_WHITE;
    textColor = TFT_BLACK;
  }
  String msg_lower = message;
  msg_lower.toLowerCase();
  // Check if message has the specific word, not word for word; Source: https://forum.arduino.cc/t/how-to-check-if-string-contains-given-word/280402/11
  if (msg_lower.indexOf("secretjingle") >= 0) {
    // draw triforce
    tft.fillScreen(bgColor);
    tft.fillTriangle(160, 4, 25, 235, 295, 235, TFT_ORANGE);
    tft.fillTriangle(93, 120, 160, 235, 227, 120, bgColor);
    zelda_jingle(100);
  } else if (msg_lower.indexOf("happysong") >= 0) { // song is now used as the happy song so it fits into our scope
    // draw triforce
    tft.fillScreen(bgColor);
    tft.fillTriangle(160, 4, 25, 235, 295, 235, TFT_ORANGE);
    tft.fillTriangle(93, 120, 160, 235, 227, 120, bgColor);
    gerudo_valley(160);
  } else if (msg_lower.indexOf("sadsong") >= 0) {
    tft.fillScreen(bgColor);               // Fill the screen with the background color
    tft.setTextColor(textColor, bgColor);  // set the text and background color
    tft.setTextSize(2);                    // set the size of the text
    printer(message);
    sad_tune(70);
  } else {
    // Update TFT display and print input message
    tft.fillScreen(bgColor);               // Fill the screen with the background color
    tft.setTextColor(textColor, bgColor);  // set the text and background color
    tft.setTextSize(2);                    // set the size of the text
    printer(message);
  }
}

void reconnect() {
  // Loop until we're reconnected
  while (!client.connected()) {
    //tft.setCursor((320 - tft.textWidth("Attempting MQTT connection...")) / 2, 90);
    //tft.print("Attempting MQTT connection...");
    // Create a random client ID
    String clientId = "WioTerminal";
    // Attempt to connect
    if (client.connect(clientId.c_str())) {
      //tft.println("connected");
      // Once connected, publish an announcement...
      client.publish(TOPIC_pub_connection, "hello world");
      Serial.println("Published connection message ");
      // ... and resubscribe
      client.subscribe(TOPIC_sub);
      Serial.print("Subcribed to: ");
      Serial.println(TOPIC_sub);
    } else {
      //tft.print("failed, rc=");
      //tft.print(client.state());
      //tft.println(" try again in 5 seconds");
      // Wait 5 seconds before retrying
      delay(5000);
      tft.fillScreen(TFT_BLACK);
    }
  }
}

void setup() {
  pinInit();

  tft.begin();
  tft.fillScreen(TFT_BLACK);
  tft.setRotation(3);


  Serial.println();
  Serial.begin(115200);
  setup_wifi();
  client.setServer(server, 1883);  // Connect the MQTT Server   hive_mqtt_server
  client.setCallback(callback);
}

void loop() {


  if (!client.connected()) {
    reconnect();
  }
  publishLight();
  client.loop();
}

void publishLight() {
  // Source: https://forum.arduino.cc/t/mqtt-library-pubsubclient-sending-float-int-bool-data-to-topics-char-convert/234730/2
  char light[5];
  // Source: https://forum.arduino.cc/t/using-millis-for-timing-a-beginners-guide/483573
  currentMillis = millis();                 //get the current "time" (actually the number of milliseconds since the program started)
  if (currentMillis - startMillis >= 2000)  //test whether two seconds have elapsed since start/last loop
  {
    String pubString = String(analogRead(LIGHT_SENSOR));
    pubString.toCharArray(light, pubString.length() + 1);
    client.publish(TOPIC_light_sensor, light);
    startMillis = currentMillis;  // update millis
  }
}

void pinInit() {
  pinMode(SPEAKER, OUTPUT);
  digitalWrite(SPEAKER, LOW);
}

void sound(uint8_t note, int duration) {
  for (int i = 0; i < (duration / notesTab[note]); i++) {
    digitalWrite(SPEAKER, HIGH);
    delayMicroseconds(notesTab[note]);
    digitalWrite(SPEAKER, LOW);
    delayMicroseconds(notesTab[note]);
  }
}

void sad_tune(int BPM) {
  int E = 25000000 / BPM / 2;
  int Qp = 25000 / BPM;
  int Sp = Qp / 4;
  sound(C5, E);
  delay(Sp);
  sound(C5, E);
  sound(G5, E);
  delay(Sp);
  sound(G5, E);
  sound(A5, E);
  delay(Sp);
  sound(A5, E);
  sound(G5, E);
  delay(Qp);
  sound(F5, E);
  delay(Sp);
  sound(F5, E);
  sound(E5, E);
  delay(Sp);
  sound(E5, E);
  sound(D5, E);
  delay(Sp);
  sound(D5, E);
  sound(C5, E);
}

void zelda_jingle(int BPM) {
  // Sources:
  // https://www.youtube.com/watch?v=R-8V37sy2RI - the video that I used to quickly grab the notes
  // https://www.ninsheetmusic.org/download/pdf/1642 - BPM
  int S = 25000000 / BPM / 4;
  sound(G5, S);
  sound(Gb5, S);
  sound(Eb5, S);
  sound(G4, S);
  sound(Gb4, S);
  sound(E5, S);
  sound(Ab5, S);
  sound(C6, S);
}

void gerudo_valley(int BPM) {
  // duration of the notes source: https://forum.arduino.cc/t/arduino-buzzer-playing-multiple-notes-at-one-time/1013984
  // https://pianoletternotes.blogspot.com/2017/11/gerudo-valley-from-legend-of-zelda.html - Letter notes, 160 BPM is a guess
  int Q = 25000000 / BPM;  // arbitrary number
  int H = Q * 2;
  int E = Q / 2;
  int Qp = 25000 / BPM;  // also arbitrary number
  int Wp = Qp * 4;
  int Hp = Qp * 2;
  int Ep = Qp / 2;
  int Sp = Qp / 4;
  sound(Db5, E);
  sound(Gb5, E);
  sound(Ab5, E);
  sound(A5, Q);
  delay(Qp);
  sound(Db5, E);
  sound(Gb5, E);
  sound(Ab5, E);
  sound(A5, Q);
  delay(Hp);
  delay(Qp);
  sound(Db5, E);
  sound(Gb5, E);
  sound(Ab5, E);
  sound(A5, Q);
  delay(Qp);
  sound(Db5, E);
  sound(Gb5, E);
  sound(Ab5, E);
  sound(A5, Q);
  delay(Hp);
  delay(Qp);
  sound(B4, E);
  sound(E5, E);
  sound(Gb5, E);
  sound(Ab5, Q);
  delay(Qp);
  sound(B4, E);
  sound(E5, E);
  sound(Gb5, E);
  sound(Ab5, Q);
  delay(Hp);
  delay(Qp);
  sound(Gb5, E);
  sound(Ab5, E);
  sound(Gb5, E);
  sound(F5, H);
  delay(Wp);
  delay(Hp);
  sound(Db5, E);
  sound(Gb5, E);
  sound(Ab5, E);
  sound(A5, Q);
  delay(Qp);
  sound(Db5, E);
  sound(Gb5, E);
  sound(Ab5, E);
  sound(A5, Q);
  delay(Hp);
  delay(Qp);
  sound(Db5, E);
  sound(Gb5, E);
  sound(Ab5, E);
  sound(A5, Q);
  delay(Qp);
  sound(Db5, E);
  sound(Gb5, E);
  sound(Ab5, E);
  sound(A5, Q);
  delay(Hp);
  delay(Qp);
  sound(B4, E);
  sound(E5, E);
  sound(Gb5, E);
  sound(Ab5, Q);
  delay(Qp);
  sound(B4, E);
  sound(E5, E);
  sound(Gb5, E);
  sound(Ab5, Q);
  delay(Hp);
  delay(Qp);
  sound(A5, E);
  sound(B5, E);
  sound(A5, E);
  sound(Ab5, H);
}

void printer(String message) {
  int len = 23;
  if (message.length() >= len) {
    for (int i = 0; i < 10; i++) {
      tft.drawString(message.substring(len * i, len * (i + 1)), 20, 20 + i * 18);
    }
  } else {
    tft.setCursor((320 - tft.textWidth(message)) / 2, 120);
    tft.println(message);
  }
}