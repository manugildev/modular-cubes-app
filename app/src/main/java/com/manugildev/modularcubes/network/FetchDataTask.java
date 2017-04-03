package com.manugildev.modularcubes.network;

import android.os.AsyncTask;

import com.manugildev.modularcubes.MainActivityFragment;
import com.manugildev.modularcubes.data.models.ModularCube;

import java.io.IOException;
import java.util.TreeMap;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FetchDataTask extends AsyncTask<String, Void, TreeMap<Integer, ModularCube>> {

    MainActivityFragment fragment;
    TreeMap<Integer, ModularCube> modularCubes;
    OkHttpClient client;

    public FetchDataTask(MainActivityFragment fragment) {
        this.fragment = fragment;
        this.modularCubes = new TreeMap<Integer, ModularCube>();
        this.client = new OkHttpClient();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected TreeMap<Integer, ModularCube> doInBackground(String... params) {
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
        System.out.println(response);
        parseJson();

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
