package com.manugildev.modularcubes;

import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.GridLayout;

import com.manugildev.modularcubes.data.models.ModularCube;
import com.manugildev.modularcubes.network.FetchDataTask;
import com.manugildev.modularcubes.network.SendActivateTask;

import java.util.Map;
import java.util.TreeMap;

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
        new FetchDataTask(this).execute();

    }

    public void refreshData(TreeMap<Integer, ModularCube> modularCubes) {
        for (Map.Entry<Integer, ModularCube> entry : modularCubes.entrySet()) {
            Integer key = entry.getKey();
            final ModularCube cube = entry.getValue();
            //TODO if size is less, remove;
            if (data == null || !data.containsKey(key)) {
                // TODO: Move this to other function
                View view = LayoutInflater.from(getActivity())
                                          .inflate(R.layout.view_modular_cube, gridLayout, false);
                view.setId(cube.getDeviceId());
                Button b = (Button) view.findViewById(R.id.button);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new SendActivateTask(fragment, cube).execute();
                    }
                });
                b.setText(cube.getCurrentOrientation());
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

    public void changeTextInButton(int id, String text) {
        if ((getActivity().findViewById(id)) != null)
            ((Button) getActivity().findViewById(id).findViewById(R.id.button)).setText(text);
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
}
