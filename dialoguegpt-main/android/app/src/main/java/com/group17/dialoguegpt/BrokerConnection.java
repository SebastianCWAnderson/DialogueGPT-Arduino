package com.group17.dialoguegpt;
 
import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
 
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
 
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
 
public class BrokerConnection extends AppCompatActivity {
 
    public static final String SUB_TOPIC = "DialogueGPT/Light";
    // public static final String LOCALHOST = "10.0.2.2";
    public static final String LOCALHOST = "192.168.155.33"; // Burush-PC
    private static final String MQTT_SERVER = "tcp://" + LOCALHOST + ":1883";
    public static final String CLIENT_ID = "Burush";
    public static final int QOS = 0;
 
    private boolean isConnected = false;
    private MqttClient mqttClient;
    Context context;
 
    public BrokerConnection(Context context) {
        this.context = context;
        mqttClient = new MqttClient(context, MQTT_SERVER, CLIENT_ID);
        connectToMqttBroker();
    }
 
    public void connectToMqttBroker() {
        if (!isConnected) {
            mqttClient.connect(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    isConnected = true;
                    final String successfulConnection = "Connected to MQTT broker";
                    Log.i(CLIENT_ID, successfulConnection);
 
//                    Toast.makeText(context, successfulConnection, Toast.LENGTH_LONG).show();
                    mqttClient.subscribe(SUB_TOPIC, QOS, null);
                    mqttClient.subscribe("DialogueGPT/Light", QOS, null);
                    Log.i(CLIENT_ID, "Subscribed to mqtt topics");
                }
 
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    final String failedConnection = "Failed to connect to MQTT broker";
                    Log.e(CLIENT_ID, failedConnection);
                    Toast.makeText(context, failedConnection, Toast.LENGTH_SHORT).show();
                }
            }, new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    isConnected = false;
                    final String connectionLost = "Connection to MQTT broker lost";
                    Log.w(CLIENT_ID, connectionLost);
                    Toast.makeText(context, connectionLost, Toast.LENGTH_SHORT).show();
                }
 
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    if (topic.equals(SUB_TOPIC)) {
                        String messageMQTT = message.toString();
                        if (MainActivity.isMqttMode()) {
                            if (Integer.parseInt(messageMQTT) > 200) {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            } else {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            }
                        }
                        Log.i("BROKER: ", "" + messageMQTT);
                    } else {
                        Log.i("BROKER: ", "[MQTT] Topic: " + topic + " | Message: " + message.toString());
                    }
                }
 
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d(CLIENT_ID, "Message delivered");
                }
            });
        }
    }
 
    public void publishMqttMessage(String message, String actionDescription) {
        if (!isConnected) {
            final String notConnected = "Not connected (yet)";
            Log.e(CLIENT_ID, notConnected);
            Toast.makeText(context, notConnected, Toast.LENGTH_SHORT).show();
            return;
        }
        mqttClient.publish(SUB_TOPIC, message, QOS, null);
        Log.i(CLIENT_ID, actionDescription);
    }
 
    public MqttClient getMqttClient() {
        return mqttClient;
    }
}