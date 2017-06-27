package com.manugildev.modularcubes.fragments;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.manugildev.modularcubes.MainActivity;
import com.manugildev.modularcubes.R;
import com.manugildev.modularcubes.data.models.ModularCube;
import com.manugildev.modularcubes.data.models.Player;
import com.manugildev.modularcubes.ui.FlatColors;
import com.manugildev.modularcubes.ui.third.ThirdRecyclerViewAdapter;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.recyclerview.animators.SlideInDownAnimator;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;


public class ThirdFragment extends Fragment implements ThirdRecyclerViewAdapter.ItemClickListener {

    private FragmentInterface mCallback;
    private MainActivity activity;

    // General Variables
    private int generalColorIndex = 0;
    private ArrayList<Long> usedCubes = new ArrayList<>();
    private ArrayList<Point> sequence = new ArrayList<>();
    private ArrayList<Player> winners = new ArrayList<>();
    private ArrayList<String> names = new ArrayList<>();

    //private Thread illuminateCubesThread;
    private CountDownTimer mCountDownTimer;
    private long timeLeft;
    private int totalTime = 61000;

    public enum GameState {PLAYING, PAUSE, STOP}

    public GameState gameState;

    // Views
    @BindView(R.id.recyclerView)
    public RecyclerView recyclerView;
    @BindView(R.id.mainTextView)
    public TextView mainTextView;
    @BindView(R.id.timeTextView)
    public TextView timeTextView;
    @BindView(R.id.progressBar)
    public CircularProgressBar progressBar;
    @BindView(R.id.addPlayerButton)
    public Button addPlayerButton;
    @BindView(R.id.removeAllButton)
    public Button removeAllButton;
    @BindView(R.id.playButton)
    public Button playButton;
    @BindView(R.id.resetButton)
    public Button resetButton;
    @BindView(R.id.viewKonfetti)
    public KonfettiView viewKonfetti;
    @BindView(R.id.chartFrame)
    public FrameLayout chartFrame;
    @BindView(R.id.chartCardView)
    public CardView chartCardView;
    @BindView(R.id.chartView)
    public LineChartView chartView;

    private ThirdRecyclerViewAdapter adapter;
    private LinearLayoutManager layoutManager;

    public ThirdFragment() {
    }

    public static ThirdFragment newInstance() {
        ThirdFragment fragment = new ThirdFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.activity = (MainActivity) getActivity();
        names.add("Max");
        names.add("Manu");
        names.add("Henrik");
        names.add("Chirstina");
        names.add("Markus");
        names.add("Claire");
        names.add("Conrad");
        names.add("Jessica");
        names.add("Adriana");
        names.add("Nacho");
        names.add("Luis");
        names.add("Joseff");
        shuffleArray(names);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.adapter = new ThirdRecyclerViewAdapter(activity);
        this.adapter.setClickListener(this);
        this.recyclerView.setAdapter(adapter);
        this.gameState = GameState.STOP;
        this.sequence.add(new Point(6, 6));
        this.sequence.add(new Point(5, 2));
        this.sequence.add(new Point(4, 4));
        this.sequence.add(new Point(6, 1));
        setChartData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_third, container, false);
        ButterKnife.bind(this, rootView);
        this.recyclerView.setItemAnimator(new SlideInDownAnimator());
        layoutManager = new LinearLayoutManager(activity);
        this.recyclerView.setLayoutManager(layoutManager);
        return rootView;
    }


    // Callback Stuff
    public void onUpdatedCube(ModularCube cube) {
        if (gameState == GameState.PLAYING) {
            Player cPlayer = adapter.getPlayerByCube(cube);
            if (cPlayer != null) {
                if (cPlayer.areBothOnSequence() && cPlayer.getProgress() != 100) {
                    int id = cPlayer.getId();
                    activity.getSecondFragment().playPositiveSound();
                    cPlayer.addTime(totalTime, timeLeft);
                    int progress = (int) ((1 - ((float) cPlayer.getCubeSequence().size() / (float) sequence.size())) * 100);
                    if (cPlayer.getCubeSequence().size() > 0) {
                        cPlayer.setProgress(progress);
                    } else {
                        winners.add(cPlayer);
                        cPlayer.setProgress(0);
                        if (winners.size() == adapter.getPlayers().size()) endGame();
                        else activity.getSecondFragment().playPowerUpSound();
                        animatePlayerON(cPlayer.getId(), "AWESOME!", 1200);
                        //recyclerView.scrollToPosition(id);


                    }
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    public void startTimer(long milliseconds) {
        if (mCountDownTimer != null) mCountDownTimer.cancel();
        mCountDownTimer = new CountDownTimer(milliseconds, 1) {
            @Override
            public void onTick(long l) {
                float percent = (float) l / (float) totalTime;
                progressBar.setProgress(percent * 100);
                updateTimeTextView(l);
                timeLeft = l;
            }

            @Override
            public void onFinish() {
                endGame();
            }
        };
        mCountDownTimer.start();
    }

    private void updateTimeTextView(long l) {
        animateTimeView(String.valueOf((int) (l / 1000)), 200);
    }

    public void onRemoveCube(ModularCube cube) {
    }

    public void onAddCube(ModularCube cube) {
    }

    // General Stuff for this game
    @OnClick(R.id.playButton)
    public void startGame() {
        if (gameState == GameState.STOP) {
            if (adapter.getPlayers().size() > 0) {
                winners.clear();
                playButton.setBackgroundResource(R.drawable.pause_button);
                progressBar.setProgressWithAnimation(100, 500);
                gameState = GameState.PLAYING;
                animateMainTextView("Playing", 250);
                setSequenceToAll();
                adapter.notifyDataSetChanged();/**/
                startTimer(totalTime);
            } else {
                Toast.makeText(activity, "Add some Players!", Toast.LENGTH_SHORT).show();
            }
        } else if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSE;
            playButton.setBackgroundResource(R.drawable.play_button);
            animateMainTextView("Pause", 250);
            mCountDownTimer.cancel();
        } else if (gameState == GameState.PAUSE) {
            gameState = GameState.PLAYING;
            playButton.setBackgroundResource(R.drawable.pause_button);
            animateMainTextView("Playing", 250);
            startTimer(timeLeft);
        }
    }

    public void endGame() {
        animateMainTextView("Again?", 250);
        playButton.setBackgroundResource(R.drawable.play_button);
        gameState = GameState.STOP;
        mCountDownTimer.cancel();
        mCountDownTimer.cancel();
        updateTimeTextView(0);
        timeLeft = 0;
        runEndKonfettiAnimation();
        activity.getSecondFragment().playEndSound();
        launchChart(1000, 8000);
        setSequenceToAll();
        progressBar.setProgressWithAnimation(100, 800);
        mCountDownTimer.cancel();
        for (Player p : adapter.getPlayers())
            animatePlayerOFF(p.getId(), 1300, 2000);

    }

    public void setSequenceToAll() {
        for (Player p : adapter.getPlayers()) {
            p.setCubeSequence(sequence);
        }
    }

    @OnClick(R.id.resetButton)
    public void stopGame() {
        if (gameState == GameState.PLAYING || gameState == GameState.PAUSE) {
            gameState = GameState.STOP;
            playButton.setBackgroundResource(R.drawable.play_button);
            animateTimeView(String.valueOf((totalTime / 1000)), 100);
            animateMainTextView("Stopped", 250);
            for (Player p : adapter.getPlayers())
                animatePlayerOFF(p.getId(), 500, 0);
            progressBar.setProgressWithAnimation(100, 800);
            mCountDownTimer.cancel();
        }
    }

    @OnClick(R.id.addPlayerButton)
    public void addPlayer() {
        if (gameState == GameState.PLAYING) return;
        if (adapter.getPlayers().size() == 0) {
            recyclerView.removeAllViewsInLayout();
            activity.getMainFragment().stopAllNodes();
        }
        ModularCube cube1 = getFreeCube();
        ModularCube cube2 = getFreeCube();
        if (cube1 == null || cube2 == null) {
            Toast.makeText(activity, "Not enough cubes...", Toast.LENGTH_SHORT).show();
            return;
        }
        illuminateTheCubes(cube1, cube2);
        Player p = new Player(this, moveArray(names), adapter.getItemCount(), 0, cube1, cube2, getMatColor("500"));
        //Toast.makeText(activity, "ID: " + p.getId(), Toast.LENGTH_SHORT).show();
        p.setProgress(100);
        adapter.addItem(p);
        animateMainTextView("Press the PLAY Button!", 250);
    }

    private void illuminateTheCubes(ModularCube cube1, ModularCube cube2) {
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(0);
                    sendActivate(cube1.getDeviceId(), true, false);
                    sendActivate(cube2.getDeviceId(), true, false);
                    sleep(1000);
                    sendActivate(cube1.getDeviceId(), false, false);
                    sendActivate(cube2.getDeviceId(), false, false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }


    private void sendActivate(long deviceId, boolean b, boolean r) {
        mCallback.sendActivate(deviceId, b, r);
    }


    public ModularCube getFreeCube() {
        for (TreeMap.Entry<Long, ModularCube> entry : activity.mData.entrySet()) {
            ModularCube cube = entry.getValue();
            if (!usedCubes.contains(cube.getDeviceId())) {
                usedCubes.add(0, cube.getDeviceId());
                return cube;
            }
        }
        return null;
    }

    @OnClick(R.id.removeAllButton)
    public void removeAllPlayers() {
        if (gameState == GameState.STOP) {
            animateMainTextView("Add some players.", 250);
            usedCubes.clear();
            adapter.removeAll();
            generalColorIndex = 0;
        }
    }

    private void setChartData() {
        List<Line> lines = new ArrayList<>();
        for (Player p : adapter.getPlayers()) {
            List<PointValue> values = new ArrayList<>();
            for (int i = 0; i < p.getTimes().size(); i++)
                values.add(new PointValue(i, p.getTimes().get(i) / 1000));

            System.out.println("ChartValues: " + values.toString());
            Line line = new Line(values).setColor(Color.parseColor(p.getColor())).setCubic(true);
            //line.setHasLabels(true);
            line.setHasPoints(true);
            lines.add(line);
        }
        LineChartData data = new LineChartData();
        ArrayList<AxisValue> xValues = new ArrayList<>();
        for (int i = 0; i < sequence.size(); i++) {
            xValues.add(new AxisValue(i));
        }
        Axis axisX = new Axis().setValues(xValues);
        Axis axisY = new Axis();
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);
        data.setLines(lines);
        chartView.setLineChartData(data);

        final Viewport v = new Viewport(chartView.getMaximumViewport());
        v.bottom = 0;
        v.top = v.top + 2;
        v.left = -0.1f;
        v.right = v.right + 0.1f;
        chartView.setMaximumViewport(v);
        chartView.setCurrentViewport(v);
    }

    // Utils
    private String getMatColor(String typeColor) {
        String returnColor = "#FFFFFFF";
        int arrayId = getResources().getIdentifier("mdcolor_" + typeColor, "array", activity.getApplicationContext().getPackageName());
        if (arrayId != 0) {
            TypedArray colors = getResources().obtainTypedArray(arrayId);
            returnColor = colors.getString(generalColorIndex);
            generalColorIndex++;
            if (generalColorIndex == colors.length() - 1) generalColorIndex = 0;
            colors.recycle();

        }
        return returnColor;
    }

    // Overrides
    @Override
    public void onItemClick(View view, int position) {
        if (view.getId() == recyclerView.findViewHolderForAdapterPosition(position).itemView.findViewById(R.id.tile1).getId()) {
            System.out.println("Press " + position + " tile1");
        }

        if (view.getId() == recyclerView.findViewHolderForAdapterPosition(position).itemView.findViewById(R.id.tile2).getId()) {
            System.out.println("Press " + position + " tile2");
        }


    }

    @Override
    public void onDetach() {
        mCallback = null;
        super.onDetach();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (FragmentInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement FragmentInterface");
        }
    }

    // ANIMATIONS
    private void animateMainTextView(final String text, final int time) {
        activity.runOnUiThread(() -> {
            YoYo.with(Techniques.FlipOutY).duration((long) time).onEnd(animator -> mainTextView.setText(text)).playOn(mainTextView);
            YoYo.with(Techniques.FlipInY).duration((long) time).delay((long) (time / 1.1)).playOn(mainTextView);
        });
    }

    private void animatePlayerON(int id, final String text, final int time) {
        //Toast.makeText(activity, "ID: " + id, Toast.LENGTH_SHORT).show();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    if (recyclerView.findViewHolderForAdapterPosition(id) != null) {
                        View playerView = recyclerView.findViewHolderForAdapterPosition(id).itemView;
                        FrameLayout playerFrame = (FrameLayout) playerView.findViewById(R.id.playerFrameLayout);
                        TextView playerTextView = (TextView) playerView.findViewById(R.id.playerTextView);
                        activity.runOnUiThread(() -> {
                            playerTextView.setText(text);
                            YoYo.with(Techniques.FadeIn).duration((long) (time / 10)).playOn(playerFrame);
                            YoYo.with(Techniques.FadeInLeft).duration((long) (time / 2.5)).playOn(playerTextView);
                        });
                    }
                    recyclerView.removeOnScrollListener(this);
                }
            }
        });

        recyclerView.smoothScrollToPosition(id);


    }

    private void animatePlayerOFF(int id, final int time, int delay) {
        // Toast.makeText(activity, "ID: " + id, Toast.LENGTH_SHORT).show();
        if (recyclerView.findViewHolderForAdapterPosition(id) != null) {
            View playerView = recyclerView.findViewHolderForAdapterPosition(id).itemView;
            FrameLayout playerFrame = (FrameLayout) playerView.findViewById(R.id.playerFrameLayout);
            TextView playerTextView = (TextView) playerView.findViewById(R.id.playerTextView);
            activity.runOnUiThread(() -> {
                YoYo.with(Techniques.FadeOut).duration((long) (time / 2.5)).delay(delay).playOn(playerFrame);
                YoYo.with(Techniques.FadeOutRight).duration((long) (time / 2.5)).delay(time).delay(delay).playOn(playerTextView);
            });
        }
    }

    private void animateTimeView(final String text, final int time) {
        if (!text.equals(timeTextView.getText().toString()))
            activity.runOnUiThread(() -> {
                YoYo.with(Techniques.RubberBand).duration(0).onEnd(animator -> timeTextView.setText(text)).playOn(timeTextView);
            });
    }

    private void runEndKonfettiAnimation() {
        viewKonfetti.build()
                .addColors(FlatColors.YELLOW, Color.MAGENTA, Color.YELLOW, Color.GREEN)
                .setDirection(0, 180)
                .setSpeed(2f, 8f)
                .setFadeOutEnabled(true)
                .setTimeToLive(3000L)
                .addShapes(Shape.RECT)
                .addSizes(new Size(10, 6f))
                .setPosition(-50f, viewKonfetti.getWidth() + 50f, -50f, -50f)
                .stream(2000, 2000L);
    }

    private void launchChart(int startAnimation, int stayFor) {
        setChartData();
        activity.runOnUiThread(() -> {
            YoYo.with(Techniques.FadeIn).duration((long) (startAnimation / 5)).playOn(chartFrame);
            YoYo.with(Techniques.FadeOut).duration((long) (startAnimation)).delay(stayFor).playOn(chartFrame);
            YoYo.with(Techniques.FadeInLeft).duration((long) (startAnimation)).playOn(chartCardView);
            YoYo.with(Techniques.FadeOutRight).duration((long) (startAnimation)).delay(stayFor).playOn(chartCardView);
        });
    }

    private static void shuffleArray(ArrayList<String> array) {
        long seed = System.nanoTime();
        Collections.shuffle(array, new Random(seed));
    }

    private static String moveArray(ArrayList<String> array) {
        String temp = array.remove(0);
        array.add(temp);
        return temp;

    }

}

