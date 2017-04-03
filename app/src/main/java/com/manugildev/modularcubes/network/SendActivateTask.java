package com.manugildev.modularcubes.network;

import android.os.AsyncTask;

import com.manugildev.modularcubes.MainActivityFragment;
import com.manugildev.modularcubes.data.models.ModularCube;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.TreeMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SendActivateTask extends AsyncTask<String, Void, TreeMap<Integer, ModularCube>> {

    MainActivityFragment fragment;
    TreeMap<Integer, ModularCube> modularCubes;
    OkHttpClient client;
    String bodyStr;

    public SendActivateTask(MainActivityFragment fragment, ModularCube cube) {
        this.fragment = fragment;
        this.modularCubes = new TreeMap<Integer, ModularCube>();
        this.client = new OkHttpClient();
        JSONObject activate1 = new JSONObject();
        try {
            activate1.put("lIP", cube.getIp());
            activate1.put("dId", cube.getDeviceId());
            activate1.put("a", cube.isActivated() ? 1 : 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(activate1);
        //"[{"lIP":"192.168.4.2","a":1,"dId":1722370},{"lIP":"192.168.43.4","a":1,"dId":15680347}]"
        bodyStr = '"' + jsonArray.toString() + '"';
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected TreeMap<Integer, ModularCube> doInBackground(String... params) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, bodyStr);
        String response = "";
        String url = "https://io.adafruit.com/api/v2/gikdew/feeds/data";
        String aioKey = "dbd315bf60794acf9a8c51bc2e19c371"; //TODO: This to file
        Request request = new Request.Builder().url(url).put(body)
                                               .addHeader("x-aio-key", aioKey).build();

        try {
            Response r = client.newCall(request).execute();
            response = r.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(response);

        return modularCubes;
    }

    @Override
    protected void onPostExecute(TreeMap<Integer, ModularCube> modularCubes) {
        fragment.refreshData(modularCubes);
        try {
            Thread.sleep(1000);
            new FetchDataTask(fragment).execute();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
