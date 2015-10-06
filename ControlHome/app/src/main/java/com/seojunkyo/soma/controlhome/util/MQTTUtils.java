package com.seojunkyo.soma.controlhome.util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTTUtils {

	private static MqttClient client;
    private String clientHandle = null;
    private Context context = null;

	public static MqttClient getClient() {
		return client;
	}

	public static boolean connect(String url, String mqttClient) {
		try {
			MemoryPersistence persistance = new MemoryPersistence();
			client = new MqttClient("tcp://" + url + ":1883", mqttClient, persistance);
			client.connect();
			return true;
		} catch (MqttException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean pub(String topic, String payload) {
		MqttMessage message = new MqttMessage(payload.getBytes());
		try {
			client.publish(topic, message);
			return true;
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		}
		return false;
	}

    public static boolean sub(String TOPIC) {
        try {
            client.subscribe(TOPIC);
            return true;
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return false;
    }
    public void onDestroy() {
        try {
            client.disconnect(0);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
