package com.group17.dialoguegpt;

import android.content.Context;

//import org.eclipse.paho.android.service.MqttAndroidClient;
import info.mqtt.android.service.Ack;
import info.mqtt.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttClient {

    private MqttAndroidClient mqttAndroidClient;

    public MqttClient(Context context, String serverURL, String ClientID) {
        mqttAndroidClient = new MqttAndroidClient(context, serverURL, ClientID, Ack.AUTO_ACK);
    }
    // connect (try without credentials)
    public void connect(IMqttActionListener connectionCallback, MqttCallback clientCallback) {
        mqttAndroidClient.setCallback(clientCallback);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);

        mqttAndroidClient.connect(options, null, connectionCallback);

    }

    // disconnect
    public void disconnect(IMqttActionListener disconnectionCallback) {
        mqttAndroidClient.disconnect(null, disconnectionCallback);
    }

    // subscribe to topic
    public void subscribe(String topic, int qos, IMqttActionListener subscriptionCallback) {
        mqttAndroidClient.subscribe(topic, qos, null, subscriptionCallback);
    }

    // unsubscribe from topic
    public void unsubscribe(String topic, IMqttActionListener unsubscriptionCallback) {
        mqttAndroidClient.unsubscribe(topic, null, unsubscriptionCallback);
    }

    // publish message to a topic
    public void publish(String topic, String message, int qos, IMqttActionListener publishCallback) {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(message.getBytes());
        mqttMessage.setQos(qos);

        mqttAndroidClient.publish(topic, mqttMessage, null, publishCallback);
    }

}
