package com.manugildev.modularcubes.network;

import android.os.AsyncTask;
import android.util.Log;

import com.manugildev.modularcubes.MainActivityFragment;
import com.manugildev.modularcubes.data.models.ModularCube;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.TreeMap;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FetchDataTask extends AsyncTask<String, Void, TreeMap<Long, ModularCube>> {

    MainActivityFragment fragment;
    TreeMap<Long, ModularCube> modularCubes;
    OkHttpClient client;

    public FetchDataTask(MainActivityFragment fragment) {
        this.fragment = fragment;
        this.modularCubes = new TreeMap<>();
        this.client = new OkHttpClient();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected TreeMap<Long, ModularCube> doInBackground(String... params) {
        String response = "";
        String url = "https://io.adafruit.com/api/v2/gikdew/feeds/data";
        String aioKey = "dbd315bf60794acf9a8c51bc2e19c371"; //TODO: This to file
        Request request = new Request.Builder().url(url).addHeader("x-aio-key", aioKey).build();

        try {
            Response r = client.newCall(request).execute();
            response = r.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        parseJson(response);

        return modularCubes;
    }

    private void parseJson(String response) {
        if (!response.contains("Retry later"))
            try {
                JSONObject jsonObject = new JSONObject(response);
                String lastValueStr = jsonObject.getString("last_value");
                lastValueStr = lastValueStr.replace('\"', '"').substring(1, lastValueStr.length() - 1);
                JSONObject lastValueJson = new JSONObject(lastValueStr);
                parseCubes(lastValueJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        else
            try {
                Thread.sleep(5000);
                new FetchDataTask(fragment).execute();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }

    private void parseCubes(JSONObject lastValueJson) throws JSONException {
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
            Log.d("Cube", c.toString());
            if (cubeJson.has("c")) {
                parseCubes(cubeJson.getJSONObject("c"));
            }

        }
    }

    @Override
    protected void onPostExecute(TreeMap<Long, ModularCube> modularCubes) {
        fragment.refreshData(modularCubes);
        //fragment.callAsynchronousTask(5000);
    }
}
