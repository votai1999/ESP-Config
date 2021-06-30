# 1 "f:\\EspConfig\\CodeEsp\\EspConfig.ino"
// #include <WiFi.h> // define ESP32
# 3 "f:\\EspConfig\\CodeEsp\\EspConfig.ino" 2
# 4 "f:\\EspConfig\\CodeEsp\\EspConfig.ino" 2
# 5 "f:\\EspConfig\\CodeEsp\\EspConfig.ino" 2




Ticker Timer;
WiFiServer server(80);
//*********** variable ***********//
const char *ssid = "ESP Config CLB Pioneer"; //Enter your wifi SSID Wifi AP Mode
const char *password = ""; //Enter your wifi Password
String SSI, PASS;
int count_1 = 0;
int statusLed = 1;

//*********** Set up ***********//
void setup()
{
  pinMode(2 /* Led Status*/, 0x01);
  Serial.begin(115200);
  EEPROM.begin(512);
  Timer.attach(0.2, Timer_Count2); // //Timer 0.2s
  if (restoreConfig())
  {
    if (checkConnection())
      return;
  }
  Mode_WIFI_AP();
}

void Timer_Count2()
{
  switch (statusLed)
  {
  case 0:
    digitalWrite(2 /* Led Status*/, 0);
    break;
  case 1:
    digitalWrite(2 /* Led Status*/, !digitalRead(2 /* Led Status*/));
    break;
  case 2:
    digitalWrite(2 /* Led Status*/, 1);
    break;
  default:
    2;
    break;
  }
}

//*********** Read SSID & PASS in eeprom ***********//
boolean restoreConfig()
{
  Serial.println("Reading EEPROM...");
  String ssid = "";
  String pass = "";
  if (EEPROM.read(0) != 0)
  {
    for (int i = 0; i < 30; ++i)
    {
      ssid += char(EEPROM.read(i));
    }
    Serial.print("SSID: ");
    Serial.println(ssid);
    for (int i = 30; i < 60; ++i)
    {
      pass += char(EEPROM.read(i));
    }
    Serial.print("Password: ");
    Serial.println(pass);
    WiFi.begin(ssid.c_str(), pass.c_str()); //ket noi voi mang WIFI duoc luu trong EEPROM
    return true;
  }
  else
  {
    Serial.println("Config not found.");
    return false;
  }
}

//*********** Config Wifi AP Mode ***********//
void Mode_WIFI_AP()
{
  WiFi.mode(WIFI_AP);
  WiFi.softAP(ssid, password);
  Serial.println("");
  Serial.println("Wifi Mode AP");
  for (int i = 0; i < 10; i++)
  {
    Serial.print(".");
    delay(500);
  }
  Serial.println("");
  Serial.println("IP address: ");
  Serial.println(WiFi.softAPIP());
  server.begin();
}

//*********** Check connect to wifi STA Mode ***********//
boolean checkConnection()
{
  int count = 0;
  Serial.print("Waiting for Wi-Fi connection");
  while (count < 30)
  {
    if (WiFi.status() == WL_CONNECTED)
    { //neu ket noi thanh cong thi in ra connected!
      Serial.println();
      Serial.println("Connected!");
      WiFi.mode(WIFI_STA);
      Serial.println("IP address: ");
      Serial.println(WiFi.localIP());
      server.begin();
      statusLed = 0;
      return (true);
    }
    delay(500);
    Serial.print(".");
    statusLed = 1;
    count++;
  }
  return false;
}

//*********** While(1) ***********//
void loop()
{
  WiFiClient client = server.available();
  client.setNoDelay(1);
  if (client)
  {
    if (client.connected())
      Serial.println("Client Connected");
    while (client.connected())
    {
      while (client.available() > 0)
      {
        // read data from the connected client
        String buf = client.readStringUntil('\n');
        Serial.println(buf);
        //*********** Get SSID & PASS from smartphone ***********//
        if (buf.startsWith("*"))
        {
          Serial.println("Get SSID & PASS");
          for (int i = 0; i < 60; ++i)
          {
            EEPROM.write(i, 0);
          }
          buf = buf.substring(1, buf.length());
          SSI = getValue(buf, ',', 0);
          for (int i = 0; i < SSI.length(); ++i)
          {
            EEPROM.write(i, SSI[i]);
          }
          PASS = getValue(buf, ',', 1);
          for (int i = 0; i < PASS.length(); ++i)
          {
            EEPROM.write(30 + i, PASS[i]);
          }
          EEPROM.commit();
          delay(500);
          ESP.restart();
        }
      }
    }
    client.stop();
    Serial.println("Client disconnected");
  }
}

//*********** get String Value ***********//
String getValue(String data, char separator, int index)
{
  int found = 0;
  int strIndex[] = {0, -1};
  int maxIndex = data.length() - 1;

  for (int i = 0; i <= maxIndex && found <= index; i++)
  {
    if (data.charAt(i) == separator || i == maxIndex)
    {
      found++;
      strIndex[0] = strIndex[1] + 1;
      strIndex[1] = (i == maxIndex) ? i + 1 : i;
    }
  }
  return found > index ? data.substring(strIndex[0], strIndex[1]) : "";
}
