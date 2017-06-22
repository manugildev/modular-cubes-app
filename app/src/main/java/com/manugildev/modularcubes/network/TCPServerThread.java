package com.manugildev.modularcubes.network;

import android.os.NetworkOnMainThreadException;
import android.util.Log;

import com.manugildev.modularcubes.MainActivity;
import com.manugildev.modularcubes.data.models.ModularCube;
import com.manugildev.modularcubes.fragments.MainActivityFragment;

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

public class TCPServerThread extends Thread {


    private static final String TAG = TCPServerThread.class.getCanonicalName();
    private final String gateway;
    private final MainActivityFragment fragment;
    private final MainActivity activity;
    int serverPort;
    DatagramSocket socket;
    DatagramPacket packet;

    boolean running;
    private boolean weGotSomething = false;
    public boolean gotConnections = true;
    private long timeSend;
    public int connectionTries = 0;

    public TCPServerThread(MainActivityFragment fragment, String gateway, int serverPort) {
        super();
        this.fragment = fragment;
        this.activity = (MainActivity) fragment.getActivity();
        this.serverPort = serverPort;
        this.gateway = gateway;
    }

    public void setRunning(boolean running) {
        this.running = running;
        try {
            if (socket != null) {
                socket.close();
                socket.disconnect();
                socket = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean sendMessage(String message) {

        if (socket == null) return false;
        if (!running) return false;
        byte[] send_data = new byte[1024];
        String str = message;
        send_data = str.getBytes();
        DatagramPacket send_packet;
        try {
            send_packet = new DatagramPacket(send_data, str.length(), InetAddress.getByName(gateway), 8266);
            if (!socket.isClosed()) {
                socket.send(send_packet);
                System.out.println("Message sent " + message);
            }
            return true;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            setRunning(false);
            activity.runOnUiThread(fragment::setDisconnected);
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            setRunning(false);
            activity.runOnUiThread(fragment::setDisconnected);
            return false;
        } catch (NetworkOnMainThreadException e) {
            e.printStackTrace();
            //setRunning(false);
            //activity.runOnUiThread(fragment::setDisconnected);
            return false;
        }


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
            ModularCube c = new ModularCube(fragment);
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
            socket = new DatagramSocket();

            System.out.println("UDP Server is running");
            sendMessage("android");
            while (running) {
                //if (fragment.isConnectedToMesh()) {
                byte[] buf = new byte[800];
                // receive request
                packet = new DatagramPacket(buf, buf.length);
                if (!socket.isClosed()) socket.receive(packet);
                else System.out.println("Seems to be closed...");//this code block the program flow
                byte[] data = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                final String received = new String(data);
                if (fragment != null && fragment.getActivity() != null) {
                    parseReceivedData(received);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            activity.getMessagesFragment().addItem(received);
                        }
                    });
                }

                System.out.println(received);
                String dString = "Response Message from android";
                buf = dString.getBytes();
                packet = new DatagramPacket(buf, buf.length, address, port);
                //socket.send(packet);
                //}
            }

            Log.e(TAG, "UDP Server ended");
        } catch (BindException e) {
            if (socket != null) {
                socket.close();
                socket.disconnect();
            }
            setRunning(false);
            //if (fragment != null) fragment.startUDP();
            e.printStackTrace();
        } catch (Exception e) {
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
                    fragment.firstCubeId = 0;
                    String d = received.replace("initial=", "");
                    TreeMap<Long, ModularCube> modularCube = parseJson(d);
                    fragment.firstCubeId = modularCube.firstKey();
                    fragment.updateInformation(parseJson(d));
                    fragment.startPollingConnections();
                }
            });
            weGotSomething = true;
        } else if (received.startsWith("connection=")) {
            weGotSomething = true;
            fragment.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String nodeId = received.replace("connection=", "");
                    //fragment.onAddcube(nodeId);
                }
            });
            weGotSomething = true;
        } else if (received.startsWith("disconnection=")) {
            fragment.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String nodeId = received.replace("disconnection=", "");
                    //fragment.onRemoveCube(nodeId);
                }
            });
            weGotSomething = true;
        } else if (received.startsWith("information=")) {
            fragment.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final String elapsed = String.valueOf(System.currentTimeMillis() - timeSend);
                    System.out.println("ELAPSED: " + elapsed);
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
            gotConnections = true;
            connectionTries = 0;
            final String elapsed = String.valueOf(System.currentTimeMillis() - fragment.sendTime);
            //Log.d("TimeElapsed", elapsed);
            //Log.d("Connections", received.replace("connections=", ""));
            fragment.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fragment.mTimeConnectionsTv.setText(elapsed);
                    try {
                        fragment.calculateDepth(received.replace("connections=", ""));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public boolean sendActivate(ModularCube cube, boolean b) {
        if (socket == null) return false;
        try {
            String msg = createJsonActivateMessage(cube, b);
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

    private String createJsonActivateMessage(ModularCube cube, boolean b) throws JSONException {
        JSONObject activate1 = new JSONObject();
        if (cube != null) {
            activate1.put("lIP", cube.getDeviceId());
            activate1.put("a", b ? 1 : 0);
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(activate1);
            return jsonArray.toString();
        }
        return "";
    }
}
