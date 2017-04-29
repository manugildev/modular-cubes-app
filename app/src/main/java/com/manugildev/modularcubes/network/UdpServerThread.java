package com.manugildev.modularcubes.network;

import android.util.Log;

import com.manugildev.modularcubes.MainActivityFragment;
import com.manugildev.modularcubes.data.models.ModularCube;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.TreeMap;

public class UdpServerThread extends Thread {

    private static final String TAG = UdpServerThread.class.getCanonicalName();
    private final String gateway;
    private final MainActivityFragment fragment;
    int serverPort;
    DatagramSocket socket;
    DatagramPacket packet;

    boolean running;

    public UdpServerThread(MainActivityFragment fragment, String gateway, int serverPort) {
        super();
        this.fragment = fragment;
        this.serverPort = serverPort;
        this.gateway = gateway;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean sendMessage(String message) {
        if (socket == null) return false;
        byte[] send_data = new byte[1024];
        String str = message;
        send_data = str.getBytes();
        DatagramPacket send_packet = null;
        try {
            send_packet = new DatagramPacket(send_data, str.length(), InetAddress.getByName(gateway), 8266);
            socket.send(send_packet);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendActivate(ModularCube cube) {
        if (socket == null) return false;
        try {
            String msg = createJsonActivateMessage(cube);
            sendMessage(msg);
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }


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
            ModularCube c = new ModularCube();
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


    @Override
    public void run() {
        running = true;

        try {
            System.out.println("Starting UDP Server");
            socket = new DatagramSocket(serverPort);
            sendMessage("android");
            System.out.println("UDP Server is running");
            while (running) {
                byte[] buf = new byte[256];

                // receive request
                packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);     //this code block the program flow
                byte[] data = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());
                InetAddress address = packet.getAddress();
                int port = packet.getPort();

                final String received = new String(data);
                if (received.contains("data=") && fragment != null && fragment.getActivity() != null) //TODO: Topic based!
                    fragment.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String d = received.replace("data=", "");
                            fragment.refreshData(parseJson(d));
                        }
                    });

                System.out.println(received);
                String dString = "Response Message from android";
                buf = dString.getBytes();
                packet = new DatagramPacket(buf, buf.length, address, port);
                //socket.send(packet);
            }

            Log.e(TAG, "UDP Server ended");
        } catch (BindException e) {
            if (socket != null) {
                socket.close();
                Log.e(TAG, "socket.close()");
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            if (fragment != null) fragment.startUDP();
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            if (socket != null) {
                socket.close();
                Log.e(TAG, "socket.close()");
            }
        }
    }
}
