package com.manugildev.modularcubes;

import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.manugildev.modularcubes.data.models.ModularCube;
import com.manugildev.modularcubes.network.FetchDataTask;
import com.manugildev.modularcubes.network.SendActivateTask;
import com.manugildev.modularcubes.ui.FlatColors;

import java.util.Map;
import java.util.TreeMap;

import static com.manugildev.modularcubes.R.drawable.activate_off;
import static com.manugildev.modularcubes.R.drawable.activate_on;
import static com.manugildev.modularcubes.R.id.gridlayout;


// TODO: Connection lost
public class MainActivityFragment extends Fragment
        implements CompoundButton.OnCheckedChangeListener {

    TreeMap<Integer, ModularCube> mData;
    GridLayout gridLayout;
    final MainActivityFragment fragment;
    ProgressBar progressBar;
    CountDownTimer mCountDownTimer;
    FetchDataTask fetchDataTask;
    Switch switchButton;

    public MainActivityFragment() {
        fragment = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBarTimer);
        switchButton = (Switch) rootView.findViewById(R.id.switchButton);
        switchButton.setAlpha(0);
        switchButton.setOnCheckedChangeListener(this);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mData = new TreeMap<Integer, ModularCube>();
        gridLayout = (GridLayout) getActivity().findViewById(gridlayout);

        gridLayout.removeAllViews();
        callAsynchronousTask(0);
    }

    public void callAsynchronousTask(int milliseconds) {
        final int[] i = {0};
        mCountDownTimer = new CountDownTimer(milliseconds, milliseconds / (progressBar.getMax() + 10)) {
            @Override
            public void onTick(long l) {
                i[0] += 1;
                progressBar.setProgress(i[0]);
            }

            @Override
            public void onFinish() {
                progressBar.setProgress(progressBar.getMax());
                fetchDataTask = new FetchDataTask(fragment);
                fetchDataTask.execute();
            }
        };
        mCountDownTimer.start();
    }

    public void refreshData(TreeMap<Integer, ModularCube> modularCubes) {
        if (modularCubes.size() < mData.size()) {
            mData.clear();
            gridLayout.removeAllViews();
        }
        for (Map.Entry<Integer, ModularCube> entry : modularCubes.entrySet()) {
            Integer key = entry.getKey();
            ModularCube cube = entry.getValue();
            if ((mData == null || !mData.containsKey(key)) && fragment.getActivity() != null) {
                cube.setActivity(this);
                createViewForCube(cube);
                mData.put(key, cube);
                gridLayout.addView(cube.getView(), new LayoutParams(0, 0));
                refreshGridLayout();

            } else {
                if (mData.get(cube.getDeviceId()).updateCube(cube))
                    refreshGridLayout();
            }
        }
    }

    private void createViewForCube(final ModularCube cube) {
        View viewCube = LayoutInflater.from(fragment.getActivity()).inflate(R.layout.view_modular_cube, gridLayout, false);
        TextView textViewOrientation = (TextView) viewCube.findViewById(R.id.textViewOrientation);
        TextView textViewID = (TextView) viewCube.findViewById(R.id.textViewID);
        viewCube.setId(cube.getDeviceId());
        viewCube.setBackgroundColor(FlatColors.allColors.get(mData.size()));
        textViewOrientation.setText(String.valueOf(cube.getCurrentOrientation()));
        textViewID.setText(String.valueOf(cube.getDeviceId()));
        textViewOrientation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SendActivateTask(fragment, cube).execute();
            }
        });
        ImageView imageViewLight = (ImageView) viewCube.findViewById(R.id.imageViewLight);
        if (cube.isActivated()) imageViewLight.setImageResource(activate_on);
        else imageViewLight.setImageResource(activate_off);
        cube.setView(viewCube);
    }

    // TODO: Try to simplify this as much as possible and no hardcode it
    public void refreshGridLayout() {
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
            animateCubeOnCreate(v);

        }
        switchButton.animate().alpha(1).setDuration(300).setStartDelay(500);
    }

    private void animateCubeOnCreate(View v) {
        v.setScaleX(0.0f);
        v.setScaleY(0.0f);
        v.animate().scaleX(1).scaleY(1).setDuration(300).setStartDelay((int) (Math.random() * 200 + 30));
    }

    public void changeTextInButton(ModularCube cube) {
        int id = cube.getDeviceId();
        if (getActivity() != null && getActivity().findViewById(id) != null) {
            TextView tV = (TextView) getActivity().findViewById(id).findViewById(R.id.textViewOrientation);
            TextView tV_id = (TextView) getActivity().findViewById(id).findViewById(R.id.textViewID);
            tV.setText(String.valueOf(cube.getCurrentOrientation()));
            tV_id.setText(String.valueOf(cube.getDeviceId()));
        }
    }

    public void changeActivatedLight(int id, Boolean activated) {
        if (getActivity() != null && getActivity().findViewById(id) != null) {
            ImageView light = (ImageView) getActivity().findViewById(id).findViewById(R.id.imageViewLight);
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
            for (Map.Entry<Integer, ModularCube> entry : mData.entrySet()) {
                final ModularCube cube = entry.getValue();
                cube.setActivated(false);
                new SendActivateTask(fragment, cube).execute();
            }
        } else {
            for (Map.Entry<Integer, ModularCube> entry : mData.entrySet()) {
                final ModularCube cube = entry.getValue();
                cube.setActivated(true);
                new SendActivateTask(fragment, cube).execute();
            }
        }

    }
}
