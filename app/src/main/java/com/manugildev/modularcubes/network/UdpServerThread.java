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
import java.net.UnknownHostException;
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
    private boolean weGotSomething = false;
    private long timeSend;

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
        weGotSomething = false;
        while (!weGotSomething) {

            if (socket == null) return false;
            byte[] send_data = new byte[1024];
            String str = message;
            send_data = str.getBytes();
            DatagramPacket send_packet;
            try {
                send_packet = new DatagramPacket(send_data, str.length(), InetAddress.getByName(gateway), 8266);
                socket.send(send_packet);
                System.out.println("Message sent " + message);
                break;
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public boolean sendActivate(ModularCube cube) {
        if (socket == null) return false;
        try {
            String msg = createJsonActivateMessage(cube);
            timeSend = System.currentTimeMillis();
            fragment.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fragment.mTimeConnectionsTv.setText("0");
                }
            });
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
            c.setActivity(fragment);
            c.setDeviceId(Long.valueOf(key));
            c.setCurrentOrientation(cubeJson.getInt("cO"));
            c.setActivated(cubeJson.getInt("a") == 1);
            modularCubes.put(c.getDeviceId(), c);
            break;
            /*if (cubeJson.has("c")) {
                parseCubes(modularCubes, cubeJson.getJSONObject("c"));
            }*/
        }
        return modularCubes;
    }


    @Override
    public void run() {
        running = true;

        try {
            System.out.println("Starting UDP Server");
            socket = new DatagramSocket(serverPort);

            System.out.println("UDP Server is running");
            sendMessage("android");
            while (running) {
                byte[] buf = new byte[800];
                // receive request
                packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);     //this code block the program flow
                byte[] data = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());
                InetAddress address = packet.getAddress();
                int port = packet.getPort();

                String received = new String(data);
                if (fragment != null && fragment.getActivity() != null) {
                    parseReceivedData(received);
                }

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
            }
            setRunning(false);
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

    private void parseReceivedData(final String received) {
        if (received.startsWith("initial=")) {
            fragment.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String d = received.replace("initial=", "");
                    fragment.updateInformation(parseJson(d));
                }
            });
            weGotSomething = true;
        } else if (received.startsWith("connection=")) {
            fragment.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String nodeId = received.replace("connection=", "");
                    //fragment.addCube(nodeId);
                }
            });
            weGotSomething = true;
        } else if (received.startsWith("disconnection=")) {
            fragment.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String nodeId = received.replace("disconnection=", "");
                    //fragment.removeCube(nodeId);
                }
            });
            weGotSomething = true;
        } else if (received.startsWith("information=")) {
            fragment.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final String elapsed = String.valueOf(System.currentTimeMillis() - timeSend);
                    String d = received.replace("information=", "");
                    fragment.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fragment.mTimeConnectionsTv.setText(elapsed);
                        }
                    });
                    fragment.updateInformation(parseJson(d));
                }
            });
            weGotSomething = true;
        } else if (received.startsWith("connections=")) {
            final String elapsed = String.valueOf(System.currentTimeMillis() - fragment.sendTime);
            Log.d("TimeElapsed", elapsed);
            Log.d("Connections", received.replace("connections=", ""));
            fragment.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fragment.mTimeConnectionsTv.setText(elapsed);
                }
            });
        }
    }
}
