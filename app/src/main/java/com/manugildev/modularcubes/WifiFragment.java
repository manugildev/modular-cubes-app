package com.manugildev.modularcubes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.manugildev.modularcubes.network.TcpClient;

import java.util.List;


public class WifiFragment extends Fragment {

    String networkSSID = "CUBES_MESH";
    BroadcastReceiver receiver;
    WifiManager wifiManager;
    TcpClient mTcpClient;
    private Button button;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getActivity().getApplicationContext();
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);


        WifiInfo info = wifiManager.getConnectionInfo();

        if (!info.getSSID().contains(networkSSID)) {
            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + networkSSID + "\"";
            wifiManager.addNetwork(conf);
            List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration i : list) {
                if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                    wifiManager.disconnect();
                    wifiManager.enableNetwork(i.networkId, true);
                    wifiManager.reconnect();
                    break;
                }
            }
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    final String action = intent.getAction();

                    if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                        boolean connected = info.isConnected();
                        if (connected) {
                            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                            String ssid = wifiInfo.getSSID();
                            Log.d("SSID", ssid + " " + networkSSID);
                            if (ssid.contains(networkSSID)) {
                                int gateway = wifiManager.getDhcpInfo().gateway;
                                startTCP(Formatter.formatIpAddress(gateway));
                            }
                        }
                    }
                }
            };
            getActivity().registerReceiver(receiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
        } else {
            int gateway = wifiManager.getDhcpInfo().gateway;
            startTCP(Formatter.formatIpAddress(gateway));
        }
    }

    private void startTCP(String gateway) {
        mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
            @Override
            public void messageReceived(String message) {
                Log.d("Message", message);
            }
        }, gateway);
        new ConnectTask(this, gateway).execute("");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wifi, container, false);
        button = (Button) rootView.findViewById(R.id.button);
        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    class ConnectTask extends AsyncTask<String, String, TcpClient> {
        private final String gateway;
        private final WifiFragment fragment;

        public ConnectTask(WifiFragment fragment, String gateway) {
            this.gateway = gateway;
            this.fragment = fragment;
        }

        @Override
        protected TcpClient doInBackground(String... message) {
            mTcpClient.run();
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            //response received from server
            Log.d("test", "response " + values[0]);
            //process server response here....

        }

    }

    @Override
    public void onDestroy() {
        mTcpClient.stopClient();
        super.onDestroy();
    }
}
