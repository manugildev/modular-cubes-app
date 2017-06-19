package com.manugildev.modularcubes.network;

import android.util.Log;

import com.manugildev.modularcubes.fragments.MainActivityFragment;
import com.manugildev.modularcubes.data.models.ModularCube;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.TreeMap;

public class MQTTHandler implements MqttCallback {

    private MainActivityFragment fragment;
    private MqttAndroidClient client;
    private boolean connected;

    public MQTTHandler(final MainActivityFragment fragment) {
        this.fragment = fragment;
        this.connected = false;
    }

    //================================================================================
    // MQTT METHODS
    //================================================================================
    public void connect(final String... subscriptions) {
        String clientId = MqttClient.generateClientId();
        // TODO: Send the domain to the constant's place
        client = new MqttAndroidClient(fragment.getActivity().getApplicationContext(), "tcp://178.62.89.130:1883", clientId);
        client.setCallback(this);
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    connected = true;
                    // On Connected, subscribe to the topics
                    for (String s : subscriptions)
                        subscribe(s);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public boolean publishActivate(String topic, ModularCube cube) {
        if (connected) {
            try {
                String msg = createJsonActivateMessage(cube);
                if (!msg.isEmpty()) {
                    client.publish(topic, msg.getBytes(), 0, false);
                    if (cube.isActivated()) cube.setActivated(false);
                    else cube.setActivated(true);
                    return true;
                } else return false;
            } catch (MqttException | JSONException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            Log.d("MQTTT", "publish(): You are not connected to the server.");
            return false;
        }
    }

    public boolean publish(String topic, String msg) {
        if (connected) {
            try {
                client.publish(topic, msg.getBytes(), 0, false);
                return true;
            } catch (MqttException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            Log.d("MQTTT", "publish(): You are not connected to the server.");
            return false;
        }
    }

    public void subscribe(String topic) {
        if (connected) {
            int qos = 1;
            try {
                IMqttToken subToken = client.subscribe(topic, qos);
                subToken.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("MQTTT", "subscribe(): You are not connected to the server.");
        }
    }

    public void unsubscribe(String topic) {
        if (connected) {
            try {
                IMqttToken unsubToken = client.unsubscribe(topic);
                unsubToken.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("MQTTT", "unsubscribe(): You are not connected to the server.");
        }
    }

    public void disconnect() {
        if (connected) {
            try {
                IMqttToken disconToken = client.disconnect();
                disconToken.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("MQTTT", "disconnect(): You are not connected to the server.");
        }
    }

    //================================================================================
    // MQTT CALLBACKS
    //================================================================================
    @Override
    public void connectionLost(Throwable cause) {
        this.connected = false;
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.d("MQTT", "Message Received: " + message.toString());
        // if (topic.equals(MainActivityFragment.DATA_TOPIC)) {
        //fragment.refreshData(parseJson(message.toString()));
        //}
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    //================================================================================
    // JSON (Coding and Decoding)
    //================================================================================
    private String createJsonActivateMessage(ModularCube cube) throws JSONException {
        JSONObject activate1 = new JSONObject();
        activate1.put("lIP", cube.getDeviceId());
        activate1.put("a", cube.isActivated() ? 0 : 1);
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(activate1);
        return jsonArray.toString();
    }

    private TreeMap<Long, ModularCube> parseJson(String response) {
        TreeMap<Long, ModularCube> modularCubes = new TreeMap<>();
        try {
            JSONObject responseJson = new JSONObject(response);
            return parseCubes(modularCubes, responseJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return modularCubes;
    }

    private TreeMap<Long, ModularCube> parseCubes(TreeMap<Long, ModularCube> modularCubes, JSONObject lastValueJson) throws JSONException {

        Iterator<String> iter = lastValueJson.keys();

        while (iter.hasNext()) {
            String key = iter.next();
            JSONObject cubeJson = lastValueJson.getJSONObject(key);
            ModularCube c = new ModularCube(fragment);
            c.setIp(key);
            c.setDeviceId(Long.valueOf(key));
            c.setCurrentOrientation(cubeJson.getInt("cO"));
            c.setActivated(cubeJson.getInt("a") == 1);
            modularCubes.put(c.getDeviceId(), c);
            if (cubeJson.has("c")) {
                parseCubes(modularCubes, cubeJson.getJSONObject("c"));
            }
        }
        return modularCubes;
    }

    public void destroy() {
        client.unregisterResources();
        client.close();
    }
}
