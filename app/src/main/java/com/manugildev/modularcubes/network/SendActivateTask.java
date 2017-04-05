package com.manugildev.modularcubes.network;

import android.animation.Animator;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import com.manugildev.modularcubes.MainActivityFragment;
import com.manugildev.modularcubes.R;
import com.manugildev.modularcubes.data.models.ModularCube;
import com.manugildev.modularcubes.ui.ProgressBarAnimation;

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

public class SendActivateTask extends AsyncTask<String, Integer, TreeMap<Integer, ModularCube>> {

    MainActivityFragment fragment;
    TreeMap<Integer, ModularCube> modularCubes;
    OkHttpClient client;
    String bodyStr;
    ProgressBar progressBar;
    ModularCube cube;
    private Integer previous;

    public SendActivateTask(MainActivityFragment fragment, ModularCube cube) {
        this.cube = cube;
        this.fragment = fragment;
        this.progressBar = (ProgressBar) cube.getView().findViewById(R.id.progressBar);
        this.modularCubes = new TreeMap<>();
        this.client = new OkHttpClient();
        this.previous = 0;
        JSONObject activate1 = new JSONObject();
        JSONObject bodyJson = new JSONObject();
        try {
            activate1.put("lIP", cube.getIp());
            activate1.put("a", cube.isActivated() ? 0 : 1);
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(activate1);
            bodyJson.put("value", '"' + jsonArray.toString() + '"');
            bodyStr = bodyJson.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected TreeMap<Integer, ModularCube> doInBackground(String... params) {
        String response = "Retry later";
        while (response.contains("Retry later")) {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, bodyStr);
            String url = "https://io.adafruit.com/api/v2/gikdew/feeds/activate/data";
            String aioKey = "dbd315bf60794acf9a8c51bc2e19c371"; //TODO: This to file
            Request request = new Request.Builder().url(url).post(body).addHeader("x-aio-key", aioKey).build();
            publishProgress((int) (Math.random() * 70 + 10));
            try {
                Response r = client.newCall(request).execute();
                publishProgress(100);
                response = r.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return modularCubes;
    }

    @Override
    protected void onPostExecute(TreeMap<Integer, ModularCube> modularCubes) {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (cube.isActivated()) cube.setActivated(false);
        else cube.setActivated(true);
        progressBar.setAlpha(1f);
        progressBar.animate().alpha(0).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animator) {
                progressBar.setAlpha(1f);
                progressBar.setVisibility(View.GONE);
                progressBar.setProgress(0);
            }

            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });

    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        ProgressBarAnimation progressBarAnimation;
        super.onProgressUpdate(values);
        if (this.progressBar != null) {
            progressBarAnimation = new ProgressBarAnimation(progressBar, previous, values[0]);
            progressBarAnimation.setDuration(100);
            progressBar.startAnimation(progressBarAnimation);
            progressBar.setProgress(values[0]);
            previous = values[0];
        }
    }
}
