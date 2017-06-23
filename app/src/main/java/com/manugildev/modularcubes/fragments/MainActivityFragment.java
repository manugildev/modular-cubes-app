package com.manugildev.modularcubes.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.manugildev.modularcubes.MainActivity;
import com.manugildev.modularcubes.R;
import com.manugildev.modularcubes.data.models.ModularCube;
import com.manugildev.modularcubes.network.TCPServerThread;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

import static android.content.Context.AUDIO_SERVICE;
import static android.content.Context.WIFI_SERVICE;
import static com.manugildev.modularcubes.R.drawable.activate_off;
import static com.manugildev.modularcubes.R.drawable.activate_on;
import static com.manugildev.modularcubes.R.id.gridlayout;

public class MainActivityFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    public MainActivityFragment fragment;
    protected MainActivity activity;
    // Modular Cubes
    TreeMap<Long, ModularCube> lastRefresh;
    GridLayout gridLayout;

    // UI Components
    ProgressBar progressBar;
    CountDownTimer mCountDownTimer, mAnimationCountDownTimer;
    TextView mNumberOfCubesTV;
    TextView mNumberTV;
    TextView mScoreTV;
    public TextView mTimeConnectionsTv;
    CardView mCardView;
    CircularProgressBar mCircularProgressBar;
    Button mStartB, mONB, mOFFB;


    BroadcastReceiver receiver;
    WifiManager wifiManager;
    String gateway;
    String networkSSID = "CUBES_MESH";
    TCPServerThread TCPServerThread;

    //GameLogic Variables
    int currentNumber = 0;
    int currentOrientation = 0;
    int currentTime = 5000;
    int minimumTime = 3000;
    long timeLeft = 0;
    int decrementTime = 300;
    int score = 0;
    private int generalColorIndex = 0;

    public long firstCubeId = 0;
    public long sendTime;


    public ArrayList<Long> currentIds = new ArrayList<>();
    public ArrayList<Long> previousIds = new ArrayList<>();
    public ArrayList<Integer> soundIds = new ArrayList<>();
    public LinearLayout mNoCubesTV;

    private Runnable myRunnable;
    private Handler myHandler;
    private Handler myHandler2;
    private Runnable myRunnable2;
    private int delay;

    ArrayList<Long> cubesSequence;
    boolean playing = false;
    long currentCube = 0;
    long previousCube = 0;


    // Sound Stuff
    private SoundPool soundPool;
    private AudioManager audioManager;
    // Maximumn sound stream.
    private static final int MAX_STREAMS = 5;
    // Stream type.
    private static final int streamType = AudioManager.STREAM_MUSIC;
    private boolean loaded;
    private int positiveSound;
    private int negativeSound;
    private int endSound;
    private float volume;

    private FragmentInterface mCallback;

    public MainActivityFragment() {

    }

    public static MainActivityFragment newInstance() {
        return new MainActivityFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.fragment = this;
        startWifi();
        startPollingConnections();
    }

    public void startPollingConnections() {

        if (myHandler == null) {
            myHandler = new Handler();
            delay = 100; //milliseconds
            myRunnable = new Runnable() {
                public void run() {
                    //sendTime = System.currentTimeMillis();
                    myHandler.postDelayed(this, delay);
                    if (TCPServerThread != null) {
                        if (activity.mData.size() == 0) {
                            TCPServerThread.sendMessage("android");
                            startWifi();
                            delay = 240;
                        } else {
                            if (TCPServerThread.gotConnections && TCPServerThread.connectionTries < 10) {
                                TCPServerThread.gotConnections = false;
                                sendTime = System.currentTimeMillis();
                                TCPServerThread.sendMessage("connections");
                                delay = 1000;
                            } else {
                                if (TCPServerThread.connectionTries < 10) {
                                    TCPServerThread.connectionTries++;
                                    TCPServerThread.sendMessage("connections");
                                    delay = 300;
                                } else
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            setDisconnected();
                                        }
                                    });

                            }

                        }

                    }

                }
            };

            myHandler.postDelayed(myRunnable, delay);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        //Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        //((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBarTimer);
        mNumberOfCubesTV = (TextView) rootView.findViewById(R.id.tvNumberCubes);
        mStartB = (Button) rootView.findViewById(R.id.buttonStart);
        mStartB.setOnClickListener(this);
        mONB = (Button) rootView.findViewById(R.id.buttonON);
        mONB.setOnClickListener(this);
        mOFFB = (Button) rootView.findViewById(R.id.buttonOFF);
        mOFFB.setOnClickListener(this);
        mCircularProgressBar = (CircularProgressBar) rootView.findViewById(R.id.timeCircularProgressBar);
        mNoCubesTV = (LinearLayout) rootView.findViewById(R.id.noCubesLL);
        mNumberTV = (TextView) rootView.findViewById(R.id.tvNumber);
        mTimeConnectionsTv = (TextView) rootView.findViewById(R.id.timeConnections);
        mScoreTV = (TextView) rootView.findViewById(R.id.tvScore);
        PulsatorLayout mPulsator = (PulsatorLayout) rootView.findViewById(R.id.pulsator);
        mPulsator.start();
        mCardView = (CardView) rootView.findViewById(R.id.mainCardView);
        YoYo.with(Techniques.ZoomOut).duration(0).playOn(mCircularProgressBar);
        YoYo.with(Techniques.ZoomOut).duration(0).delay(0).playOn(mNumberTV);
        YoYo.with(Techniques.FadeOut).duration(0).playOn(mCardView);
        YoYo.with(Techniques.FadeIn).duration(0).playOn(mNoCubesTV);
        resetVariables();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity.mData = new TreeMap<>();
        lastRefresh = new TreeMap<>();
        gridLayout = (GridLayout) getActivity().findViewById(gridlayout);
        gridLayout.removeAllViews();
        gridLayout.setAlpha(1);
        refreshData();
        musicStuff();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (MainActivity) activity;
        try {
            mCallback = (FragmentInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement IFragmentToActivity");
        }
    }

    @Override
    public void onDetach() {
        mCallback = null;
        super.onDetach();
    }

    private void musicStuff() {
        // AudioManager audio settings for adjusting the volume
        audioManager = (AudioManager) activity.getSystemService(AUDIO_SERVICE);
        // Current volumn Index of particular stream type.
        float currentVolumeIndex = (float) audioManager.getStreamVolume(streamType);
        // Get the maximum volume index for a particular stream type.
        float maxVolumeIndex = (float) audioManager.getStreamMaxVolume(streamType);
        // Volumn (0 --> 1)
        this.volume = currentVolumeIndex / maxVolumeIndex;
        // Suggests an audio stream whose volume should be changed by
        // the hardware volume controls.
        activity.setVolumeControlStream(streamType);
        // For Android SDK >= 21
        if (Build.VERSION.SDK_INT >= 21) {
            AudioAttributes audioAttrib = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            SoundPool.Builder builder = new SoundPool.Builder();
            builder.setAudioAttributes(audioAttrib).setMaxStreams(MAX_STREAMS);
            this.soundPool = builder.build();
        }
        // for Android SDK < 21
        else {
            // SoundPool(int maxStreams, int streamType, int srcQuality)
            this.soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        }
        // When Sound Pool load complete.
        this.soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });

        // Load sound file (destroy.wav) into SoundPool.
        this.positiveSound = this.soundPool.load(activity, R.raw.positive, 1);
        // Load sound file (gun.wav) into SoundPool.
        this.negativeSound = this.soundPool.load(activity, R.raw.negative, 1);
        // Load sound file (end.wav)
        this.endSound = this.soundPool.load(activity, R.raw.end, 1);
    }

    public void playPositiveSound() {
        if (loaded) {
            float leftVolumn = volume;
            float rightVolumn = volume;
            // Play sound of gunfire. Returns the ID of the new stream.
            int streamId = this.soundPool.play(this.positiveSound, leftVolumn, rightVolumn, 1, 0, 1f);
        }
    }

    public void playEndSound() {
        if (loaded) {
            float leftVolumn = volume;
            float rightVolumn = volume;
            // Play sound of gunfire. Returns the ID of the new stream.
            int streamId = this.soundPool.play(this.endSound, leftVolumn, rightVolumn, 1, 0, 1f);
        }
    }

    public void playNegativeSound() {
        if (loaded) {
            float leftVolumn = volume;
            float rightVolumn = volume;

            // Play sound objects destroyed. Returns the ID of the new stream.
            int streamId = this.soundPool.play(this.negativeSound, leftVolumn, rightVolumn, 1, 0, 1f);
        }
    }

    public void startTimer(final int milliseconds) {
        final float[] i = {0};
        //generateRandomNumber();
        mCircularProgressBar.setProgressWithAnimation(100, 250);
        mAnimationCountDownTimer = new CountDownTimer(250, 1) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                mCountDownTimer.cancel();
                mCountDownTimer.start();
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
                pauseGameA();
            }
        };

    }

    private void generateRandomNumber() {
        Random r = new Random();
        int tempNumber;
        do {
            int numberOfCubes = activity.mData.size();
            tempNumber = r.nextInt(((numberOfCubes * 6) + 1) - numberOfCubes) + numberOfCubes;
        } while (currentNumber == tempNumber);
        currentNumber = tempNumber;
        //mNumberTV.setText(currentNumber + "");

    }

    public void refreshData() {
        if (fragment.getActivity() != null)
            for (Map.Entry<Long, ModularCube> entry : activity.mData.entrySet()) {
                Long key = entry.getKey();
                ModularCube cube = entry.getValue();
                //gridLayout.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeOut).duration(0).playOn(mNoCubesTV);
                if (!lastRefresh.containsKey(key)) {
                    createViewForCube(cube);
                    gridLayout.addView(cube.getView(), new LayoutParams(0, 0));
                    animateCubeOnCreate(cube.getView());
                    refreshGridLayout();
                    activity.mData.put(key, cube);
                    mCallback.addItem(cube);
                    lastRefresh.put(key, cube);
                } else {
                    if (activity.mData.get(cube.getDeviceId()).updateCube(cube)) {
                        //mData.put(key, cube);
                        lastRefresh.put(key, cube);
                    }

                }
            }
        updateNumberOfCubesTextView();
        //if (activity.mData.size() != 0)
        //checkSumOfCubes();

    }

    private void updateNumberOfCubesTextView() {
        try {
            Resources res = getResources();
            String text = res.getString(R.string.number_of_cubes, activity.mData.size() + "\n", (float) timeLeft / 1000);
            mNumberOfCubesTV.setText(text);

            //mNumberTV.setText(String.valueOf((float) timeLeft / 1000));
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
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
        String cubeColor = getMatColor("500");
        CardView cardView = (CardView) viewCube.findViewById(R.id.cardView);
        cardView.setCardBackgroundColor(Color.parseColor(cubeColor));
        cube.setColor(cubeColor);
        textSwitcherOrientation.setText(String.valueOf(cube.getCurrentOrientation()));
        textViewID.setText(String.valueOf(cube.getDeviceId()));
        touchFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TCPServerThread.sendActivate(cube, true);
            }
        });
        ImageView imageViewLight = (ImageView) viewCube.findViewById(R.id.imageViewLight);
        if (cube.isActivated()) imageViewLight.setImageResource(activate_on);
        else imageViewLight.setImageResource(activate_off);
        cube.setView(viewCube);

        Log.d("Calling", "createViewForCube() " + cube.getView().getId());
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
        int width = (int) ((size.x - 100) - spacing * 2);
        if (columnsRows.x != 0 && columnsRows.y != 0) {
            int maxSize = width / 2;
            int height = d == 1 ? maxSize : (width / columnsRows.x) * columnsRows.y;
            params.width = d == 1 ? maxSize : width;
            params.height = height;
            try {
                gridLayout.setColumnCount(columnsRows.x);
                gridLayout.setRowCount(columnsRows.y);
                gridLayout.setLayoutParams(params);
                int cubeSize = height / columnsRows.y;
                createAndSetUpCubes(cubeSize);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startPollingConnections();
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
        v.animate().scaleX(1).scaleY(1).setDuration((int) (Math.random() * 200 + 150));
    }

    public void changeTextInButton(final ModularCube cube) {
        int id = cube.getViewId();
        if (getActivity() != null && getActivity().findViewById(id) != null && id != 0) {
            TextSwitcher textSwitcherOrientation = (TextSwitcher) getActivity().findViewById(id).findViewById(R.id.textSwitcherOrientation);
            TextView tV_id = (TextView) getActivity().findViewById(id).findViewById(R.id.textViewID);
            //int previousNumber = Integer.valueOf(((TextView)textSwitcherOrientation.getCurrentView()).getText().toString());
            textSwitcherOrientation.setInAnimation(getActivity(), android.R.anim.slide_in_left);
            textSwitcherOrientation.setOutAnimation(getActivity(), android.R.anim.slide_out_right);
            textSwitcherOrientation.setText(String.valueOf(cube.getCurrentOrientation()));
            tV_id.setText(String.valueOf(cube.getDeviceId()));
        }
    }

    public void changeActivatedLight(int id, Boolean activated) {
        if (getActivity() != null && getActivity().findViewById(id) != null && id != 0) {
            ImageView light = (ImageView) getActivity().findViewById(id).findViewById(R.id.imageViewLight);
            if (activated) {
                light.setImageResource(activate_on);
            } else {
                light.setImageResource(activate_off);
            }
        }
    }

    public void changeCubeDepth(int id, int depth) {
        if (getActivity() != null && getActivity().findViewById(id) != null && id != 0) {
            TextView depthTV = (TextView) getActivity().findViewById(id).findViewById(R.id.depthTV);
            depthTV.setText(String.valueOf(depth));
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (isChecked) {
            for (Map.Entry<Long, ModularCube> entry : activity.mData.entrySet()) {
                final ModularCube cube = entry.getValue();
                cube.setActivated(false);
                TCPServerThread.sendActivate(cube, true);
            }
        } else {
            for (Map.Entry<Long, ModularCube> entry : activity.mData.entrySet()) {
                final ModularCube cube = entry.getValue();
                cube.setActivated(true);
                TCPServerThread.sendActivate(cube, true);
            }
        }

    }

    private void startWifi() {
        wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        if (!info.getSSID().contains(networkSSID)) {
            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + networkSSID + "\"";
            wifiManager.addNetwork(conf);
            List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
            if (list != null)
                for (WifiConfiguration i : list) {
                    if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                        wifiManager.disconnect();
                        wifiManager.enableNetwork(i.networkId, true);
                        wifiManager.reconnect();
                        break;
                    }
                }
            if (receiver == null) {
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
                            } /*else {
                            setDisconnected();
                        }*/
                        }
                    }
                };
                try {
                    IntentFilter intentFilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                    getActivity().registerReceiver(receiver, intentFilter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            gateway = Formatter.formatIpAddress(wifiManager.getDhcpInfo().gateway);
            startUDP();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mCountDownTimer != null) mCountDownTimer.cancel();
        if (TCPServerThread != null) TCPServerThread.setRunning(false);
        if (myHandler != null) myHandler.removeCallbacks(myRunnable);
        try {
            getActivity().unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void startUDP() {
        if (TCPServerThread != null) {
            TCPServerThread.setRunning(false);
            TCPServerThread.interrupt();
            TCPServerThread = null;
        }
        if (myHandler != null) {
            myHandler.removeCallbacks(myRunnable);
            myHandler = null;
        }
        TCPServerThread = new TCPServerThread(fragment, gateway, 8266);
        TCPServerThread.setRunning(false);
        TCPServerThread.start();
        startPollingConnections();

    }


    @Override
    public void onPause() {
        // TCPServerThread.setRunning(false);
        if (mCountDownTimer != null) mCountDownTimer.cancel();
        super.onPause();
        myHandler.removeCallbacks(myRunnable);
        myHandler = null;
    }

    @Override
    public void onDestroy() {
        if (mCountDownTimer != null) mCountDownTimer.cancel();
        if (TCPServerThread != null) TCPServerThread.setRunning(false);
        if (myHandler != null) myHandler.removeCallbacks(myRunnable);

        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonStart:
                if (mStartB.getText().equals("START")) {
                    startGameA();
                    //TCPServerThread.sendMessage("all=start");

                } else {
                    pauseGameA();
                    mStartB.setText("START");
                }
                break;
            case R.id.buttonON:
                //sendTime = System.currentTimeMillis();
                startAllNodes();

                break;
            case R.id.buttonOFF:
                stopAllNodes();
                break;
        }

    }

    public void startAllNodes() {
        sendTime = System.currentTimeMillis();
        if (TCPServerThread != null) TCPServerThread.sendMessage("all=start");
        for (Map.Entry<Long, ModularCube> entry : activity.mData.entrySet()) {
            ModularCube cube = entry.getValue();
            cube.setActivated(true);
        }
    }

    public void stopAllNodes() {
        sendTime = System.currentTimeMillis();
        if (TCPServerThread != null) TCPServerThread.sendMessage("all=stop");
        for (Map.Entry<Long, ModularCube> entry : activity.mData.entrySet()) {
            ModularCube cube = entry.getValue();
            cube.setActivated(false);
        }
    }


    private void startGameA() {
        if (activity.mData.size() > 0) {
            mStartB.setText("PAUSE");
            playing = true;
            YoYo.with(Techniques.FadeOut).duration(500).playOn(mCardView);
            YoYo.with(Techniques.ZoomIn).duration(800).playOn(mCircularProgressBar);
            YoYo.with(Techniques.ZoomIn).duration(800).playOn(mNumberTV);
            //mCircularProgressBar.animate().scaleX(1).scaleY(1).setDuration(500);
            //mNumberTV.animate().scaleX(1).scaleY(1).setDuration(500).setStartDelay(300);
            //mNumberTV.animate().alpha(1).setDuration(500).setStartDelay(300);
            stopAllNodes();
            resetVariables();
            activateNext();
            startTimer(currentTime);
        }
    }

    public void activateNext() {
        System.out.println("ACTIVATENEXT");
        if (playing && cubesSequence != null)
            if (cubesSequence.size() != 0) {
                playPositiveSound();
                currentOrientation = (int) (Math.random() * 6) + 1;
                setOrientationText();
                previousCube = currentCube;
                currentCube = cubesSequence.get(0);
                TCPServerThread.sendActivate(activity.mData.get(currentCube), true, true);
                cubesSequence.remove(0);
            } else {
                playEndSound();
                pauseGameA();
            }
    }

    public void setOrientationText() {
        Resources res = getResources();
        String text = res.getString(R.string.get_an_orientation, currentOrientation);
        mNumberTV.setText(text);
    }

    private void pauseGameA() {
        YoYo.with(Techniques.FadeIn).duration(500).playOn(mCardView);
        YoYo.with(Techniques.ZoomOut).duration(300).playOn(mCircularProgressBar);
        YoYo.with(Techniques.ZoomOut).duration(300).playOn(mNumberTV);

        //gridLayout.animate().scaleX(1).scaleY(1).setDuration(300).setStartDelay(300).setStartDelay(300);
        //mCircularProgressBar.animate().scaleX(5).scaleY(5).setDuration(300);
        //mNumberTV.animate().scaleX(0).scaleY(0).setDuration(300);
        //mNumberTV.animate().alpha(0).setDuration(300);
        mCountDownTimer.cancel();
        playing = false;
        mStartB.setText("START");
        stopAllNodes();
    }


    private void startGame() {
        resetVariables();
        gridLayout.animate().scaleX(0).scaleY(0).setDuration(300);
        mNumberTV.setScaleX(0);
        mNumberTV.setScaleY(0);
        mCircularProgressBar.animate().scaleX(1).scaleY(1).setDuration(500);
        mNumberTV.animate().scaleX(1).scaleY(1).setDuration(500).setStartDelay(300);
        mNumberTV.animate().alpha(1).setDuration(500).setStartDelay(300);
        startTimer(currentTime);
    }

    private void resetVariables() {

        currentNumber = 0;
        currentTime = 5000;
        timeLeft = 0;
        changeScore(0);

        ArrayList<Long> ids = new ArrayList<Long>();
        ids.clear();
        if (activity.mData != null && activity.mData.size() != 0) {
            for (Map.Entry<Long, ModularCube> entry : activity.mData.entrySet()) {
                ids.add(entry.getKey());
            }

            int[] numbers = new int[10];
            for (int i = 0; i < numbers.length; i++) {
                numbers[i] = (int) ((Math.random() * ids.size()));
                if (i != 0 && ids.size() != 1) {
                    while (numbers[i] == numbers[i - 1]) {
                        numbers[i] = (int) ((Math.random() * ids.size()));
                    }
                }

            }
            System.out.println("Numbers Generated: " + Arrays.toString(numbers));

            cubesSequence = new ArrayList<Long>();
            cubesSequence.clear();
            for (int i = 0; i < numbers.length; i++) {
                cubesSequence.add(ids.get(numbers[i]));
            }
            System.out.println("Numbers Generated: " + cubesSequence.toString());

        }
    }

    private void pauseGame() {
        gridLayout.animate().scaleX(1).scaleY(1).setDuration(300).setStartDelay(300).setStartDelay(300);
        mCircularProgressBar.animate().scaleX(5).scaleY(5).setDuration(300);
        mNumberTV.animate().scaleX(0).scaleY(0).setDuration(300);
        mNumberTV.animate().alpha(0).setDuration(300);
        mCountDownTimer.cancel();
    }

    private void finishGame() {
        mAnimationCountDownTimer.cancel();
        mCountDownTimer.cancel();
        mStartB.setText("START");
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
        for (Map.Entry<Long, ModularCube> entry : activity.mData.entrySet()) {
            tempCube = entry.getValue();
            total += tempCube.getCurrentOrientation();
        }
        if (total == currentNumber && total != 0) {
            currentTime = Math.max(minimumTime, currentTime -= decrementTime);
            if (mCountDownTimer != null)
                mCountDownTimer.cancel();
            changeScore(score += 1);
            startTimer(currentTime);
        }
    }

    public void addCube(String nodeId) {
        mCallback.communicateToFragment2();
        long id = Long.valueOf(nodeId);
        ModularCube c = new ModularCube(fragment);
        c.setIp("-1");
        c.setDeviceId(id);
        c.setActivity(this);
        c.setCurrentOrientation(1);
        c.setActivated(false);
        if (activity.mData.containsKey(id)) {
            removeCube(String.valueOf(id));
        }
        activity.mData.put(id, c);
        refreshData();
    }


    public void updateInformation(TreeMap<Long, ModularCube> cubeInformation) {
        Long key = cubeInformation.firstKey();
        if (activity.mData.containsKey(key))
            if (cubeInformation.firstEntry().getValue().getCurrentOrientation() !=
                    activity.mData.get(key).getCurrentOrientation())
                mCallback.updatedCube(cubeInformation.firstEntry().getValue());

        if (playing) {
            if (cubeInformation.firstKey() == currentCube) {
                /*if (cubeInformation.get(currentCube).getCurrentOrientation() !=
                        activity.mData.get(currentCube).getCurrentOrientation()) {*/
                if (cubeInformation.get(currentCube).getCurrentOrientation() == currentOrientation) {
                    TCPServerThread.sendActivate(activity.mData.get(currentCube), false, true);
                    activateNext();
                    changeScore(score += 1);
                    addTimer();
                    //System.out.println("CubeGame: " + currentCube);
                }
            } else {
                if (cubeInformation.firstKey() != previousCube)
                    playNegativeSound();
            }
        }

        if (activity.mData.containsKey(cubeInformation.firstKey())) {
            ModularCube cube = cubeInformation.firstEntry().getValue();
            System.out.println(cube);
            activity.mData.get(cube.getDeviceId()).setCurrentOrientation(cube.getCurrentOrientation());
            activity.mData.get(cube.getDeviceId()).setActivated(cube.isActivated());
        } else {
            activity.mData.put(cubeInformation.firstKey(), cubeInformation.get(cubeInformation.firstKey()));
        }
        refreshData();
    }

    private void addTimer() {
        currentTime = Math.max(minimumTime, currentTime -= decrementTime);
        if (mCountDownTimer != null)
            mCountDownTimer.cancel();
        startTimer(currentTime);
    }


    public void removeCube(String nodeId) {
        mCallback.communicateToFragment2();
        long id = Long.valueOf(nodeId);
        if (activity.mData.get(id) != null) {
            mCallback.removeItem(activity.mData.get(id));
            View namebar = gridLayout.findViewById(activity.mData.get(id).getViewId());
            if (namebar != null) {
                ViewGroup parent = (ViewGroup) namebar.getParent();
                if (parent != null) parent.removeView(namebar);

            }
            //activity.mData.get(id).cubeAudio.stop();
            activity.mData.remove(id);
            lastRefresh.remove(id);
            refreshGridLayout();
        }

    }

    public void calculateDepth(String json) throws JSONException {
        currentIds.clear();
        JSONArray jsonArray = new JSONArray(json);
        for (int i = 0, size = jsonArray.length(); i < size; i++) {
            parseArrayElement(jsonArray.getJSONObject(i), 1);
        }

        for (long element : previousIds) {
            if (!currentIds.contains(element)) {
                removeCube(String.valueOf(element));
            }
        }
        previousIds = new ArrayList<>(currentIds);
        refreshData();
    }

    private void parseArrayElement(JSONObject jsonObject, int depth) throws JSONException {
        String nodeId = jsonObject.getString("nodeId");
        JSONArray subs = jsonObject.getJSONArray("subs");
        depth += 1;
        for (int i = 0, size = subs.length(); i < size; i++) {
            parseArrayElement(subs.getJSONObject(i), depth);
        }
        long id = Long.valueOf(nodeId);
        if (activity.mData.containsKey(id)) {
            activity.mData.get(id).setDepth(depth);
        } else {
            addCube(nodeId);
            activity.mData.get(id).setDepth(depth);
        }
        currentIds.add(id);

    }


    public void setDisconnected() {
        System.out.println("Disconnected");
        YoYo.with(Techniques.FadeIn).duration(500).delay(0).playOn(mNoCubesTV);
        YoYo.with(Techniques.FadeOut).duration(300).delay(0).playOn(mCardView);

        Iterator it = activity.mData.values().iterator();
        while (it.hasNext()) {
            ModularCube item = (ModularCube) it.next();
            removeCube(String.valueOf(item.getDeviceId()));
            it = activity.mData.values().iterator();
        }
        //TCPServerThread = null;
        //activity.mData.clear();
        activity.mData.clear();
        //myHandler.removeCallbacks(myRunnable);
        if (mCountDownTimer != null) mCountDownTimer.cancel();
        if (TCPServerThread != null) TCPServerThread.setRunning(false);
        WifiManager wm = (WifiManager) getActivity().getApplicationContext().getSystemService(WIFI_SERVICE);
        // if (!isConnectedToMesh()) wm.disconnect();
        startWifi();
        startPollingConnections();

    }

    public TCPServerThread getTCPServerThread() {
        return TCPServerThread;
    }

    public void sendMessage(final String s) {
        TCPServerThread.sendMessage(s);
    }

    public void sendActivate(long currentCube, boolean b, boolean r) {
        TCPServerThread.sendActivate(activity.mData.get(currentCube), b, r);
    }

    private String getMatColor(String typeColor) {
        String returnColor = "#FFFFFFF";
        int arrayId = getResources().getIdentifier("mdcolor_" + typeColor, "array", activity.getApplicationContext().getPackageName());
        if (arrayId != 0) {
            TypedArray colors = getResources().obtainTypedArray(arrayId);
            //int index = (int) (Math.random() * colors.length());
            returnColor = colors.getString(generalColorIndex);
            generalColorIndex++;
            if (generalColorIndex == colors.length() - 1) generalColorIndex = 0;
            colors.recycle();

        }
        return returnColor;
    }

    public boolean isConnectedToMesh() {
        WifiManager wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        if (info.getSupplicantState() == SupplicantState.COMPLETED) {
            String ssid = info.getSSID();
            if (ssid.contains(networkSSID))
                return true;
            else return false;
        }
        return false;

    }

    public void firstAnimation() {

        YoYo.with(Techniques.FadeIn).duration(500).playOn(mCardView);
        YoYo.with(Techniques.FadeOut).duration(300).playOn(mNoCubesTV);

    }
}