package com.manugildev.modularcubes;

import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.manugildev.modularcubes.data.models.ModularCube;
import com.manugildev.modularcubes.fragments.FragmentInterface;
import com.manugildev.modularcubes.fragments.MainActivityFragment;
import com.manugildev.modularcubes.fragments.MessageFragment;
import com.manugildev.modularcubes.fragments.SecondFragment;
import com.manugildev.modularcubes.ui.MyPagerAdapter;

import java.util.TreeMap;

public class MainActivity extends AppCompatActivity implements FragmentInterface {
    FragmentPagerAdapter adapterViewPager;
    public TreeMap<Long, ModularCube> mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ViewPager vpPager = (ViewPager) findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);
        vpPager.setCurrentItem(2);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }


    public MessageFragment getMessagesFragment() {
        return (MessageFragment) adapterViewPager.getItem(0);
    }

    public MainActivityFragment getMainFragment() {
        return (MainActivityFragment) adapterViewPager.getItem(1);
    }

    public SecondFragment getSecondFragment() {
        return (SecondFragment) adapterViewPager.getItem(2);
    }

    @Override
    public void communicateToFragment2() {
        getSecondFragment().updatedData();
    }

    @Override
    public void removeItem(ModularCube modularCube) {
        getSecondFragment().removeCube(modularCube);
    }

    @Override
    public void addItem(ModularCube modularCube) {
        getSecondFragment().addCube(modularCube);
    }

    @Override
    public void sendMessage(String message) {
        getMainFragment().sendMessage(message);
    }

    @Override
    public void updatedCube(ModularCube cube) {
        getSecondFragment().updatedCube(cube);
    }

    @Override
    public void sendActivate(long currentCube, boolean b) {
        getMainFragment().sendActivate(currentCube, b);
    }
}
