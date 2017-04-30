package com.manugildev.modularcubes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.Formatter;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.manugildev.modularcubes.data.models.ModularCube;
import com.manugildev.modularcubes.network.FetchDataTask;
import com.manugildev.modularcubes.network.UdpServerThread;
import com.manugildev.modularcubes.ui.FlatColors;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import static com.manugildev.modularcubes.R.drawable.activate_off;
import static com.manugildev.modularcubes.R.drawable.activate_on;
import static com.manugildev.modularcubes.R.id.gridlayout;

public class MainActivityFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private static final String ACTIVATE_TOPIC = "activate";
    public static final String DATA_TOPIC = "data";

    // Modular Cubes
    TreeMap<Long, ModularCube> mData;
    GridLayout gridLayout;
    final MainActivityFragment fragment;

    // UI Components
    ProgressBar progressBar;
    CountDownTimer mCountDownTimer, mAnimationCountDownTimer;
    TextView mNumberOfCubesTV, mNumberTV, mScoreTV;
    CircularProgressBar mCircularProgressBar;
    Button mStartB, mSumOkB;

    FetchDataTask fetchDataTask;
    BroadcastReceiver receiver;
    WifiManager wifiManager;
    String gateway;
    String networkSSID = "CUBES_MESH";
    UdpServerThread udpServerThread;

    //GameLogic Variables
    int currentNumber = 0;
    int currentTime = 5000;
    int minimumTime = 2000;
    long timeLeft = 0;
    int decrementTime = 300;
    int score = 0;

    public MainActivityFragment() {
        this.fragment = this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startWifi();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBarTimer);
        mNumberOfCubesTV = (TextView) rootView.findViewById(R.id.tvNumberCubes);
        mStartB = (Button) rootView.findViewById(R.id.buttonStart);
        mStartB.setOnClickListener(this);
        mCircularProgressBar = (CircularProgressBar) rootView.findViewById(R.id.timeCircularProgressBar);
        mCircularProgressBar.setScaleX(5);
        mCircularProgressBar.setScaleY(5);
        mNumberTV = (TextView) rootView.findViewById(R.id.tvNumber);
        mNumberTV.setAlpha(0);
        mNumberTV.setScaleX(5);
        mNumberTV.setScaleY(5);
        mSumOkB = (Button) rootView.findViewById(R.id.buttonSumOK);
        mSumOkB.setOnClickListener(this);
        mScoreTV = (TextView) rootView.findViewById(R.id.tvScore);
        resetVariables();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mData = new TreeMap<>();
        gridLayout = (GridLayout) getActivity().findViewById(gridlayout);
        gridLayout.removeAllViews();
        gridLayout.setAlpha(1);
        refreshData(mData);
    }

    public void startTimer(final int milliseconds) {
        final float[] i = {0};
        generateRandomNumber();
        mCircularProgressBar.setProgressWithAnimation(100, 250);
        mAnimationCountDownTimer = new CountDownTimer(250, 1) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                mCountDownTimer.cancel();
                mCountDownTimer.start();
                generateRandomNumber();
            }
        };
        mAnimationCountDownTimer.cancel();
        mAnimationCountDownTimer.start();
        mCountDownTimer = new CountDownTimer(milliseconds, 1) {
            @Override
            public void onTick(long l) {
                float percent = (float) l / (float) milliseconds;
                mCircularProgressBar.setProgress(percent * 100);
                timeLeft = l;
                updateNumberOfCubesTextView();
            }

            @Override
            public void onFinish() {
                mCircularProgressBar.setProgress(0);
                timeLeft = 0;
                updateNumberOfCubesTextView();
                finishGame();
            }
        };

    }

    private void generateRandomNumber() {
        Random r = new Random();
        int tempNumber;
        do {
            int numberOfCubes = mData.size();
            tempNumber = r.nextInt(((numberOfCubes * 6) + 1) - numberOfCubes) + numberOfCubes;
        } while (currentNumber == tempNumber);
        currentNumber = tempNumber;
        mNumberTV.setText(currentNumber + "");

    }

    public void refreshData(TreeMap<Long, ModularCube> modularCubes) {
        if (modularCubes.size() < mData.size()) {
            mData.clear();
            gridLayout.removeAllViews();
        }
        for (Map.Entry<Long, ModularCube> entry : modularCubes.entrySet()) {
            Long key = entry.getKey();
            ModularCube cube = entry.getValue();
            if ((mData == null || !mData.containsKey(key)) && fragment.getActivity() != null) {
                cube.setActivity(this);
                createViewForCube(cube);
                mData.put(key, cube);
                gridLayout.addView(cube.getView(), new LayoutParams(0, 0));
                animateCubeOnCreate(cube.getView());
                refreshGridLayout();
            } else {
                if (mData.get(cube.getDeviceId()).updateCube(cube))
                    refreshGridLayout();
            }
        }
        updateNumberOfCubesTextView();
        if (mData.size() != 0)
            checkSumOfCubes();
    }

    private void updateNumberOfCubesTextView() {
        Resources res = getResources();
        String text = res.getString(R.string.number_of_cubes, mData.size() + "\n", (float) timeLeft / 1000);
        mNumberOfCubesTV.setText(text);
    }

    private void createViewForCube(final ModularCube cube) {
        View viewCube = LayoutInflater.from(fragment.getActivity()).inflate(R.layout.view_modular_cube, gridLayout, false);
        TextSwitcher textSwitcherOrientation = (TextSwitcher) viewCube.findViewById(R.id.textSwitcherOrientation);
        textSwitcherOrientation.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView myText = new TextView(getActivity());
                myText.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL);
                myText.setTextSize(50);
                myText.setTextColor(Color.WHITE);
                return myText;
            }
        });
        TextView textViewID = (TextView) viewCube.findViewById(R.id.textViewID);
        FrameLayout touchFrameLayout = (FrameLayout) viewCube.findViewById(R.id.touchFrameLayaout);
        viewCube.setId(View.generateViewId());
        cube.setViewId(viewCube.getId());
        viewCube.setBackgroundColor(FlatColors.allColors.get(mData.size()));
        textSwitcherOrientation.setText(String.valueOf(cube.getCurrentOrientation()));
        textViewID.setText(String.valueOf(cube.getDeviceId()));
        touchFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                udpServerThread.sendActivate(cube);
            }
        });
        ImageView imageViewLight = (ImageView) viewCube.findViewById(R.id.imageViewLight);
        if (cube.isActivated()) imageViewLight.setImageResource(activate_on);
        else imageViewLight.setImageResource(activate_off);
        cube.setView(viewCube);

        Log.d("Calling", "createViewForCube()");
    }

    public void refreshGridLayout() {
        Log.d("Calling", "refresGridLayout()");
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        Resources r = getResources();
        display.getSize(size);
        int d = gridLayout.getChildCount();
        Point columnsRows = calculateColumnsAndRows(d);

        ViewGroup.LayoutParams params = gridLayout.getLayoutParams();
        float spacing = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());
        int width = (int) (size.x - spacing * 2);
        int maxSize = width / 2;
        int height = d == 1 ? maxSize : (width / columnsRows.x) * columnsRows.y;
        params.width = d == 1 ? maxSize : width;
        params.height = height;
        gridLayout.setColumnCount(columnsRows.x);
        gridLayout.setRowCount(columnsRows.y);
        gridLayout.setLayoutParams(params);

        int cubeSize = height / columnsRows.y;
        createAndSetUpCubes(cubeSize);
    }

    private Point calculateColumnsAndRows(int d) {
        int columns = (int) Math.sqrt(d);
        int rows = Math.sqrt(d) % 1 != 0 ? columns + 1 : columns;
        if (Math.abs(rows * rows - d) < Math.abs(columns * columns - d)) {
            columns = rows;
        }
        columns = d == 2 ? rows-- : columns;
        columns = d == 1 ? 1 : columns;
        rows = d == 1 ? 1 : rows;
        return new Point(columns, rows);
    }

    private void createAndSetUpCubes(int cubeSize) {
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View v = gridLayout.getChildAt(i);
            LayoutParams l = v.getLayoutParams();
            l.height = cubeSize;
            l.width = cubeSize;
        }
    }

    private void animateCubeOnCreate(View v) {
        v.setScaleX(0.0f);
        v.setScaleY(0.0f);
        v.animate().scaleX(1).scaleY(1).setDuration(300).setStartDelay((int) (Math.random() * 200 + 30));
    }

    public void changeTextInButton(final ModularCube cube) {
        int id = cube.getViewId();
        if (getActivity() != null && getActivity().findViewById(id) != null) {
            TextSwitcher textSwitcherOrientation = (TextSwitcher) getActivity().findViewById(id).findViewById(R.id.textSwitcherOrientation);
            TextView tV_id = (TextView) getActivity().findViewById(id).findViewById(R.id.textViewID);
            //int previousNumber = Integer.valueOf(((TextView)textSwitcherOrientation.getCurrentView()).getText().toString());
            textSwitcherOrientation.setInAnimation(getActivity(), android.R.anim.slide_in_left);
            textSwitcherOrientation.setOutAnimation(getActivity(), android.R.anim.slide_out_right);
            textSwitcherOrientation.setText(String.valueOf(cube.getCurrentOrientation()));
            tV_id.setText(String.valueOf(cube.getDeviceId()));
        }
    }

    public void changeActivatedLight(long id, Boolean activated) {
        if (getActivity() != null && getActivity().findViewById((int) id) != null) {
            ImageView light = (ImageView) getActivity().findViewById((int) id).findViewById(R.id.imageViewLight);
            if (activated) {
                light.setImageResource(activate_on);
            } else {
                light.setImageResource(activate_off);
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (isChecked) {
            for (Map.Entry<Long, ModularCube> entry : mData.entrySet()) {
                final ModularCube cube = entry.getValue();
                cube.setActivated(false);
                udpServerThread.sendActivate(cube);
            }
        } else {
            for (Map.Entry<Long, ModularCube> entry : mData.entrySet()) {
                final ModularCube cube = entry.getValue();
                cube.setActivated(true);
                udpServerThread.sendActivate(cube);
            }
        }

    }

    private void startWifi() {
        wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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
                                gateway = Formatter.formatIpAddress(wifiManager.getDhcpInfo().gateway);
                                startUDP();
                            }
                        }
                    }
                }
            };
            getActivity().registerReceiver(receiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
        } else {
            gateway = Formatter.formatIpAddress(wifiManager.getDhcpInfo().gateway);
            startUDP();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mCountDownTimer.cancel();
        try {
            getActivity().unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startUDP() {
        udpServerThread = new UdpServerThread(fragment, gateway, 8266);
        udpServerThread.setRunning(false);
        udpServerThread.start();
    }

    @Override
    public void onPause() {
        // udpServerThread.setRunning(false);
        mCountDownTimer.cancel();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mCountDownTimer.cancel();
        if (udpServerThread != null)
            udpServerThread.setRunning(false);
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.buttonStart) {
            if (mStartB.getText().equals("START GAME")) {
                startGame();
                mStartB.setText("PAUSE");
            } else {
                pauseGame();
                mStartB.setText("START GAME");
            }
        } else if (view.getId() == R.id.buttonSumOK) {
            currentTime = Math.max(minimumTime, currentTime -= decrementTime);
            changeScore(score += 1);
            if (mCountDownTimer != null)
                mCountDownTimer.cancel();
            startTimer(currentTime);
        }
    }

    private void startGame() {
        resetVariables();
        mNumberTV.setScaleX(5);
        mNumberTV.setScaleY(5);
        gridLayout.animate().scaleX(0).scaleY(0).setDuration(300);
        mCircularProgressBar.animate().scaleX(1).scaleY(1).setDuration(500);
        mNumberTV.animate().scaleX(1).scaleY(1).setDuration(300);
        mNumberTV.animate().alpha(1).setDuration(300);
        startTimer(currentTime);
    }

    private void resetVariables() {
        currentNumber = 0;
        currentTime = 5000;
        timeLeft = 0;
        changeScore(0);
    }

    private void pauseGame() {
        gridLayout.animate().scaleX(1).scaleY(1).setDuration(300).setStartDelay(300);
        mCircularProgressBar.animate().scaleX(5).scaleY(5).setDuration(300);
        mNumberTV.animate().scaleX(0).scaleY(0).setDuration(300);
        mNumberTV.animate().alpha(0).setDuration(300);
        mCountDownTimer.cancel();
    }

    private void finishGame() {
        mAnimationCountDownTimer.cancel();
        mCountDownTimer.cancel();
        mStartB.setText("START GAME");
        gridLayout.animate().scaleX(1).scaleY(1).setDuration(300).setStartDelay(300);
        mCircularProgressBar.animate().scaleX(5).scaleY(5).setDuration(300);
        mNumberTV.animate().scaleX(0).scaleY(0).setDuration(300);
        mNumberTV.animate().alpha(0).setDuration(300);

    }

    private void changeScore(int score) {
        this.score = score;
        Resources res = getResources();
        String text = res.getString(R.string.score, score);
        mScoreTV.setText(text);
    }


    private void checkSumOfCubes() {
        int total = 0;
        ModularCube tempCube;
        for (Map.Entry<Long, ModularCube> entry : mData.entrySet()) {
            tempCube = entry.getValue();
            total += tempCube.getCurrentOrientation();
        }
        Log.d("Total", String.valueOf(total));
        Log.d("CurrentNumber", String.valueOf(currentNumber));
        if (total == currentNumber) {
            currentTime = Math.max(minimumTime, currentTime -= decrementTime);
            if (mCountDownTimer != null)
                mCountDownTimer.cancel();
            changeScore(score += 1);
            startTimer(currentTime);
        } else {

        }
    }
}