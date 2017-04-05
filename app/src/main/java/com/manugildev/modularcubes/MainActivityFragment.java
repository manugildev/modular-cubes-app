package com.manugildev.modularcubes;

import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.manugildev.modularcubes.data.models.ModularCube;
import com.manugildev.modularcubes.network.FetchDataTask;
import com.manugildev.modularcubes.network.SendActivateTask;
import com.manugildev.modularcubes.ui.FlatColors;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import static com.manugildev.modularcubes.R.drawable.activate_off;
import static com.manugildev.modularcubes.R.drawable.activate_on;

/**
 * A placeholder fragment containing a simple view.
 */

// TODO: Connection lost
public class MainActivityFragment extends Fragment {

    TreeMap<Integer, ModularCube> data;
    GridLayout gridLayout;
    final MainActivityFragment fragment;

    public MainActivityFragment() {
        fragment = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Initializers
        data = new TreeMap<Integer, ModularCube>();
        gridLayout = (GridLayout) getActivity().findViewById(R.id.gridlayout);

        gridLayout.removeAllViews();
        callAsynchronousTask();
    }

    public void callAsynchronousTask() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            FetchDataTask fetchDataTask = new FetchDataTask(fragment);
                            fetchDataTask.execute();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 5000); //execute in every 50000 ms
    }

    public void refreshData(TreeMap<Integer, ModularCube> modularCubes) {
        if (modularCubes.size() < data.size()) {
            data.clear();
            gridLayout.removeAllViews();
        }
        for (Map.Entry<Integer, ModularCube> entry : modularCubes.entrySet()) {
            Integer key = entry.getKey();
            final ModularCube cube = entry.getValue();
            //TODO if size is less, remove;
            if ((data == null || !data.containsKey(key)) && fragment.getActivity() != null) {
                // TODO: Move this to other function
                View view = LayoutInflater.from(fragment.getActivity())
                                          .inflate(R.layout.view_modular_cube, gridLayout, false);
                view.setId(cube.getDeviceId());
                TextView tV = (TextView) view.findViewById(R.id.textView);
                TextView tV_id = (TextView) view.findViewById(R.id.textView_id);
                view.setBackgroundColor(FlatColors.allColors.get(data.size()));
                tV.setText(String.valueOf(cube.getCurrentOrientation()));
                tV_id.setText(String.valueOf(cube.getDeviceId()));
                tV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (cube.isActivated()) cube.setActivated(false);
                        else cube.setActivated(true);
                        new SendActivateTask(fragment, cube).execute();
                    }
                });
                cube.setActivity(this);
                cube.setView(view);
                gridLayout.addView(cube.getView(), new LayoutParams(0, 0));
                this.data.put(key, cube);
                refreshGridLayout();
            } else {
                if (data.get(cube.getDeviceId()).updateCube(cube))
                    refreshGridLayout();
            }
        }
    }

    // TODO: Try to simplify this as much as possible and no hardcode it
    public void refreshGridLayout() {
        GridLayout gridLayout = (GridLayout) getActivity().findViewById(R.id.gridlayout);
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16,
                                             r.getDisplayMetrics());
        int width = (int) (size.x - px * 2);
        int d = gridLayout.getChildCount();
        int columns = (int) Math.sqrt(d);
        int rows = Math.sqrt(d) % 1 != 0 ? columns + 1 : columns;
        Log.d("CR", columns + " " + rows);
        if (Math.abs(rows * rows - d) < Math.abs(columns * columns - d)) {
            columns = rows;
        }
        // Special cases for when the columns or rows are just 1
        columns = d == 2 ? rows-- : columns;
        columns = d == 1 ? 1 : columns;
        rows = d == 1 ? 1 : rows;
        gridLayout.setColumnCount(columns);
        gridLayout.setRowCount(rows);
        ViewGroup.LayoutParams params = gridLayout.getLayoutParams();
        int maxSize = width / 2;
        int height = d == 1 ? maxSize : (width / columns) * rows;
        params.width = d == 1 ? maxSize : width;
        params.height = height;
        int cubeSize = height / rows;
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View b = gridLayout.getChildAt(i);
            LayoutParams l = b.getLayoutParams();
            l.height = cubeSize;
            l.width = cubeSize;
        }

        gridLayout.setLayoutParams(params);
    }

    public void changeTextInButton(ModularCube cube) {
        int id = cube.getDeviceId();
        if (getActivity() != null && getActivity().findViewById(id) != null) {
            TextView tV = (TextView) getActivity().findViewById(id).findViewById(R.id.textView);
            TextView tV_id = (TextView) getActivity().findViewById(id)
                                                     .findViewById(R.id.textView_id);
            tV.setText(String.valueOf(cube.getCurrentOrientation()));
            tV_id.setText(String.valueOf(cube.getDeviceId()));
        }
    }

    public void changeActivatedLight(int id, Boolean activated) {
        if (getActivity() != null && getActivity().findViewById(id) != null) {
            ImageView light = (ImageView) getActivity().findViewById(id)
                                                       .findViewById(R.id.imageView);
            if (activated) {
                light.setImageResource(activate_on);
            } else {
                light.setImageResource(activate_off);
            }
        }

    }
}
