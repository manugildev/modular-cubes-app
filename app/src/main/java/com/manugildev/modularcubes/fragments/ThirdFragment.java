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

import com.manugildev.modularcubes.MainActivity;
import com.manugildev.modularcubes.R;
import com.manugildev.modularcubes.data.models.ModularCube;
import com.manugildev.modularcubes.data.models.Player;
import com.manugildev.modularcubes.ui.third.ThirdRecyclerViewAdapter;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.animators.ScaleInAnimator;


public class ThirdFragment extends Fragment implements ThirdRecyclerViewAdapter.ItemClickListener {

    private FragmentInterface mCallback;
    private MainActivity activity;

    // General Variables
    private int generalColorIndex = 0;
    private ArrayList<Player> players = new ArrayList<>();

    // Views
    private RecyclerView recyclerView;
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
        this.adapter = new ThirdRecyclerViewAdapter(activity, players);
        this.adapter.setClickListener(this);
        this.recyclerView.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_third, container, false);
        this.recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
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
    public void addPlayer() {

    }

    public void removePlayer() {
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
