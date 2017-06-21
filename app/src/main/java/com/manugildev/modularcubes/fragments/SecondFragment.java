package com.manugildev.modularcubes.fragments;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.manugildev.modularcubes.MainActivity;
import com.manugildev.modularcubes.R;
import com.manugildev.modularcubes.data.models.ModularCube;
import com.manugildev.modularcubes.ui.FlatColors;
import com.manugildev.modularcubes.ui.second.MyRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Map;

import jp.wasabeef.recyclerview.animators.ScaleInAnimator;
import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

import static android.content.Context.AUDIO_SERVICE;

public class SecondFragment extends Fragment implements View.OnClickListener, MyRecyclerViewAdapter.ItemClickListener {

    private FragmentInterface mCallback;
    private MainActivity activity;
    private SecondFragment fragment;
    Thread sequenceThread;

    // Game Variables
    private ArrayList<Long> cubesSequence = new ArrayList<>();
    private int movementsLeft;
    private boolean playing = false;
    private long currentCube = 0;
    private int currentLevel = 1;
    private long previousCube = 0;
    private long score = 0;

    // VIEWS
    private Button testButton;
    private MyRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    private KonfettiView viewKonfetti;
    private TextView mainTextView;
    private FrameLayout textViewFrame;
    private TextView scoreTextView;
    private TextView highScoreTextView;


    // Music Stuff
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

    private ArrayList<ModularCube> cubes = new ArrayList<>();

    public static SecondFragment newInstance() {
        return new SecondFragment();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = (MainActivity) getActivity();
        fragment = (SecondFragment) this;
        adapter = new MyRecyclerViewAdapter(activity, cubes);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        musicStuff();
        animateMainTextView("", 0);
        addScore(0);
        checkHighScore();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_second, container, false);
        testButton = (Button) rootView.findViewById(R.id.testButton);
        testButton.setOnClickListener(this);
        // data to populate the RecyclerView with

        // set up the RecyclerView
        recyclerView = (RecyclerView) rootView.findViewById(R.id.rvNumbers);
        recyclerView.setItemAnimator(new ScaleInAnimator());
        recyclerView.setNestedScrollingEnabled(false);

        viewKonfetti = (KonfettiView) rootView.findViewById(R.id.viewKonfetti);

        mainTextView = (TextView) rootView.findViewById(R.id.mainTextView);
        textViewFrame = (FrameLayout) rootView.findViewById(R.id.textViewFrame);
        scoreTextView = (TextView) rootView.findViewById(R.id.scoreTextView);
        highScoreTextView = (TextView) rootView.findViewById(R.id.highScoreTextView);

        int numberOfColumns = 3;
        recyclerView.setLayoutManager(new GridLayoutManager(activity, numberOfColumns));
        return rootView;
    }

    private void musicStuff() {
        audioManager = (AudioManager) activity.getSystemService(AUDIO_SERVICE);
        float currentVolumeIndex = (float) audioManager.getStreamVolume(streamType);
        float maxVolumeIndex = (float) audioManager.getStreamMaxVolume(streamType);
        this.volume = currentVolumeIndex / maxVolumeIndex;
        activity.setVolumeControlStream(streamType);
        if (Build.VERSION.SDK_INT >= 21) {
            AudioAttributes audioAttrib = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            SoundPool.Builder builder = new SoundPool.Builder();
            builder.setAudioAttributes(audioAttrib).setMaxStreams(MAX_STREAMS);
            this.soundPool = builder.build();
        } else {
            this.soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        }
        this.soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });
        this.positiveSound = this.soundPool.load(activity, R.raw.positive, 1);
        this.negativeSound = this.soundPool.load(activity, R.raw.negative, 1);
        this.endSound = this.soundPool.load(activity, R.raw.end, 1);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (FragmentInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentInterface");
        }
    }

    @Override
    public void onDetach() {
        mCallback = null;
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void updatedData() {
    }


    public void sendMessage(String message) {
        mCallback.sendMessage(message);
    }

    private void sendActivate(long currentCube, boolean b) {
        mCallback.sendActivate(currentCube, b);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.testButton:
                startSimon();
                break;
            default:
                break;

        }
    }

    public void startSimon() {
        if (!playing) {
            if (activity.mData.size() != 0) {
                YoYo.with(Techniques.FadeOutDown).duration(300).playOn(testButton);
                currentCube = 0;
                previousCube = 0;
                activity.getMainFragment().stopAllNodes();
                generateSequence(currentLevel);
                animateMainTextView("Memorize the Pattern!\n\nLevel: " + currentLevel, 1300);
                playSequence();
                addScore(0);
            } else {
                Toast.makeText(getActivity(), "Connect to the Mesh Network", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkHighScore() {
        SharedPreferences prefs = activity.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        long highScore = prefs.getLong("score", 0);

        if (highScore >= score) {
            highScoreTextView.setText("HighScore: " + highScore);
        } else {
            highScore = score;
            animateHighScoreTextView("HighScore: " + highScore, 300);
            prefs.edit().putLong("score", highScore).apply();
        }
    }

    private void animateMainTextView(final String text, final int time) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainTextView.setText(text);
                YoYo.with(Techniques.FadeIn).duration((long) (time / 10)).playOn(textViewFrame);
                YoYo.with(Techniques.FadeOut).duration((long) (time / 2.5)).delay(time).playOn(textViewFrame);

                YoYo.with(Techniques.FadeInLeft).duration((long) (time / 2.5)).playOn(mainTextView);
                YoYo.with(Techniques.FadeOutRight).duration((long) (time / 2.5)).delay(time).playOn(mainTextView);
            }
        });
    }

    private void animateScoreTextView(final String text, final int startDelay, final int time) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                YoYo.with(Techniques.FlipOutY).duration((long) time).delay(startDelay).onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        scoreTextView.setText(text);
                    }
                }).playOn(scoreTextView);
                YoYo.with(Techniques.FlipInY).duration((long) time).delay(time + startDelay).playOn(scoreTextView);
            }
        });
    }

    private void animateHighScoreTextView(final String text, final int time) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                YoYo.with(Techniques.FlipOutY).duration((long) time).onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        highScoreTextView.setText(text);
                    }
                }).playOn(highScoreTextView);
                YoYo.with(Techniques.FlipInY).duration((long) time).delay(time).playOn(highScoreTextView);
            }
        });

    }

    private void runEndKonfettiAnimation() {
        //animateMainTextView("AWEEEESOME!", 800);
        viewKonfetti.build()
                .addColors(FlatColors.YELLOW, Color.CYAN, Color.YELLOW, Color.GREEN)
                .setDirection(0, 180)
                .setSpeed(2f, 8f)
                .setFadeOutEnabled(true)
                .setTimeToLive(3000L)
                .addShapes(Shape.RECT)
                .addSizes(new Size(10, 6f))
                .setPosition(-50f, viewKonfetti.getWidth() + 50f, -50f, -50f)
                .stream(1000, 1000L);
    }

    private void playSequence() {
        sequenceThread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(2300);
                    for (long s : cubesSequence) {
                        sendActivate(s, true);
                        resizeItem(s);
                        sleep(1000);
                        sendActivate(s, false);
                        sleep(1000);
                    }
                    animateMainTextView("GO!", 800);
                    sleep(1200);
                    playing = true;
                    correctMovement();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        sequenceThread.start();
    }

    private void resizeItem(long s) {
        final View cubeView = recyclerView.findViewHolderForAdapterPosition(adapter.getCubeIndexById(s)).itemView;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                YoYo.with(Techniques.Flash)
                        .duration(800)
                        .playOn(cubeView);
                //cubeView.animate().scaleX(1).setDuration(500).setStartDelay(600).start();
                //cubeView.animate().scaleY(1).setDuration(500).setStartDelay(600).start();
            }
        });
    }

    public void endSimon() {
        YoYo.with(Techniques.FadeInUp).duration(500).playOn(testButton);
        if (currentLevel > 1)
            testButton.setText("Continue - Level " + currentLevel);
        else testButton.setText("Start Simon Says");
        currentCube = 0;
        previousCube = 0;
        playing = false;
    }

    public void correctMovement() {
        if (movementsLeft != 0) {
            previousCube = currentCube;
            currentCube = cubesSequence.get(0);
            cubesSequence.remove(0);
            movementsLeft = cubesSequence.size();
            //sendActivate(currentCube, true);
        } else {
            currentLevel++;
            playEndSound();
            runEndKonfettiAnimation();
            endSimon();
        }
    }

    private void addScore(int score) {
        this.score += score;
        checkHighScore();
        animateScoreTextView("Score: " + this.score, 0, 250);
    }


    public void incorrectMovement() {
        currentLevel = 1;
        checkHighScore();
        score = 0;
        animateScoreTextView("GameOver!", 0, 500);
        //animateMainTextView("GameOver...", 800);
        playNegativeSound();
        endSimon();
    }

    public void updatedCube(ModularCube cube) {
        Log.d("UpdateCube", cube.toString());
        if (playing)
            if (activity.mData.containsKey(currentCube) && currentCube != 0) {
                if (currentCube == cube.getDeviceId()) {
                    int receivedCubeO = cube.getCurrentOrientation();
                    int previousOrientation = activity.mData.get(currentCube).getCurrentOrientation();
                    if (receivedCubeO != previousOrientation) {
                        //sendActivate(currentCube, false);
                        playPositiveSound();
                        addScore(1);
                        YoYo.with(Techniques.Pulse).duration(600)
                                .playOn(recyclerView.findViewHolderForAdapterPosition(adapter.getCubeIndexById(currentCube)).itemView);
                        correctMovement();
                    }
                } else {
                    if (cube.getDeviceId() != previousCube) {
                        incorrectMovement();
                        YoYo.with(Techniques.Shake).duration(600)
                                .playOn(recyclerView.findViewHolderForAdapterPosition(adapter.getCubeIndexById(cube.getDeviceId())).itemView);

                    }
                }
            }
    }

    public void addCube(ModularCube cube) {
        saveToArray(cube);
    }

    public void removeCube(ModularCube cube) {
        adapter.removeItem(cube);
    }

    private void saveToArray(ModularCube cube) {
        if (!arrayContains(cube)) {
            adapter.addItem(cube);
        }
    }

    private boolean arrayContains(ModularCube cube) {
        for (ModularCube c : cubes) {
            if (c.getDeviceId() == cube.getDeviceId()) return true;
        }
        return false;
    }

    public void generateSequence(int number) {
        this.movementsLeft = number;
        ArrayList<Long> ids = new ArrayList<Long>();
        ids.clear();
        if (activity.mData != null && activity.mData.size() != 0) {
            for (Map.Entry<Long, ModularCube> entry : activity.mData.entrySet()) {
                ids.add(entry.getKey());
            }

            int[] numbers = new int[number];
            for (int i = 0; i < numbers.length; i++) {
                numbers[i] = (int) ((Math.random() * ids.size()));
                if (i != 0 && ids.size() != 1) {
                    while (numbers[i] == numbers[i - 1]) {
                        numbers[i] = (int) ((Math.random() * ids.size()));
                    }
                }

            }
            cubesSequence = new ArrayList<Long>();
            cubesSequence.clear();
            for (int i = 0; i < numbers.length; i++) {
                cubesSequence.add(ids.get(numbers[i]));
            }
            System.out.println("Numbers Generated: " + cubesSequence.toString());
        }
    }

    public void playPositiveSound() {
        if (loaded) {
            float leftVolumn = volume;
            float rightVolumn = volume;
            int streamId = this.soundPool.play(this.positiveSound, leftVolumn, rightVolumn, 1, 0, 1f);
        }
    }

    public void playEndSound() {
        if (loaded) {
            float leftVolumn = volume;
            float rightVolumn = volume;
            int streamId = this.soundPool.play(this.endSound, leftVolumn, rightVolumn, 1, 0, 1f);
        }
    }

    public void playNegativeSound() {
        if (loaded) {
            float leftVolumn = volume;
            float rightVolumn = volume;
            int streamId = this.soundPool.play(this.negativeSound, leftVolumn, rightVolumn, 1, 0, 1f);
        }
    }


    @Override
    public void onItemClick(View view, int position) {
        Log.i("TAG", "You clicked number " + adapter.getItem(position) + ", which is at cell position " + position);
    }
}