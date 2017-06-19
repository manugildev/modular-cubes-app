package com.manugildev.modularcubes.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.manugildev.modularcubes.MainActivity;
import com.manugildev.modularcubes.R;

public class SecondFragment extends Fragment {

    MainActivityFragment mainFragment;

    public SecondFragment() {
    }

    public static Fragment newInstance() {
        return new SecondFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainFragment = ((MainActivity) getActivity()).getMainFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_second, container, false);
        ToggleButton toggle = (ToggleButton) rootView.findViewById(R.id.turnAllB);

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                }else{}
            }
        });
        return inflater.inflate(R.layout.fragment_second, container, false);
    }

    // Container Activity must implement this interface
    public interface OnButtonClicked {
        public void OnUDPButtonClicked();
    }
}
