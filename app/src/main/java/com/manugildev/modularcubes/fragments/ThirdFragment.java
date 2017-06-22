package com.manugildev.modularcubes.fragments;

import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.manugildev.modularcubes.MainActivity;
import com.manugildev.modularcubes.R;
import com.manugildev.modularcubes.data.models.ModularCube;
import com.manugildev.modularcubes.data.models.Player;
import com.manugildev.modularcubes.ui.third.ThirdRecyclerViewAdapter;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.ArrayList;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.recyclerview.animators.ScaleInAnimator;


public class ThirdFragment extends Fragment implements ThirdRecyclerViewAdapter.ItemClickListener {

    private FragmentInterface mCallback;
    private MainActivity activity;

    // General Variables
    private int generalColorIndex = 0;
    private ArrayList<Long> usedCubes = new ArrayList<>();
    private ArrayList<Integer> sequence = new ArrayList<Integer>();
    private Thread illuminateCubesThread;

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


    private ThirdRecyclerViewAdapter adapter;

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
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.adapter = new ThirdRecyclerViewAdapter(activity);
        this.adapter.setClickListener(this);
        this.recyclerView.setAdapter(adapter);
        this.gameState = GameState.STOP;
        this.sequence.add(2);
        this.sequence.add(6);
        this.sequence.add(4);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_third, container, false);
        ButterKnife.bind(this, rootView);
        this.recyclerView.setItemAnimator(new ScaleInAnimator());
        this.recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        return rootView;
    }


    // Callback Stuff
    public void onUpdatedCube(ModularCube cube) {
    }

    public void onRemoveCube(ModularCube cube) {
    }

    public void onAddCube(ModularCube cube) {
    }

    // General Stuff for this game
    @OnClick(R.id.playButton)
    public void startGame() {
        if (gameState == GameState.STOP) {
            gameState = GameState.PLAYING;
            mainTextView.setText("Playing");
            setSequenceToAll();

        }
    }

    public void endGame() {
        mainTextView.setText("Good Job!");
    }

    public void setSequenceToAll() {
        for (Player p : adapter.getPlayers()) {
            p.setCubeSequence(sequence);
        }
    }

    @OnClick(R.id.resetButton)
    public void stopGame() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.STOP;
        }
    }

    @OnClick(R.id.addPlayerButton)
    public void addPlayer() {
        ModularCube cube1 = getFreeCube();
        ModularCube cube2 = getFreeCube();

        illuminateTheCubes(cube1, cube2);
        if (cube1 == null || cube2 == null) {
            Toast.makeText(activity, "Not enough cubes...", Toast.LENGTH_SHORT).show();
            return;
        }
        Player p = new Player(adapter.getItemCount(), "5", cube1, cube2, getMatColor("500"));
        adapter.addItem(p);
        mainTextView.setText("Press PLAY Button!");
    }

    private void illuminateTheCubes(ModularCube cube1, ModularCube cube2) {
        activity.getMainFragment().stopAllNodes();
        illuminateCubesThread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(0);
                    sendActivate(cube1.getDeviceId(), true);
                    sendActivate(cube2.getDeviceId(), true);
                    sleep(1000);
                    sendActivate(cube1.getDeviceId(), false);
                    sendActivate(cube2.getDeviceId(), false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        illuminateCubesThread.start();

    }


    private void sendActivate(long deviceId, boolean b) {
        mCallback.sendActivate(deviceId, b);
    }


    public ModularCube getFreeCube() {
        for (TreeMap.Entry<Long, ModularCube> entry : activity.mData.entrySet()) {
            ModularCube cube = entry.getValue();
            if (!usedCubes.contains(cube.getDeviceId())) {
                usedCubes.add(cube.getDeviceId());
                return cube;
            }
        }
        return null;
    }

    @OnClick(R.id.removeAllButton)
    public void removeAllPlayers() {
        mainTextView.setText("Add some players");
        usedCubes.clear();
        adapter.removeAll();

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

}

