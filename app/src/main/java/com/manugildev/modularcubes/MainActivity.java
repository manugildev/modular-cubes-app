package com.manugildev.modularcubes;

import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.manugildev.modularcubes.data.models.ModularCube;
import com.manugildev.modularcubes.fragments.FragmentInterface;
import com.manugildev.modularcubes.fragments.MainActivityFragment;
import com.manugildev.modularcubes.fragments.MessageFragment;
import com.manugildev.modularcubes.fragments.SecondFragment;
import com.manugildev.modularcubes.fragments.ThirdFragment;
import com.manugildev.modularcubes.ui.MyPagerAdapter;

import java.util.TreeMap;

public class MainActivity extends AppCompatActivity implements FragmentInterface {
    FragmentPagerAdapter adapterViewPager;
    public TreeMap<Long, ModularCube> mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewPager vpPager = (ViewPager) findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);
        vpPager.setOffscreenPageLimit(4);
        vpPager.setCurrentItem(3);
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

    public ThirdFragment getThirdFragment() {
        return (ThirdFragment) adapterViewPager.getItem(3);
    }

    @Override
    public void communicateToFragment2() {
        getSecondFragment().updatedData();
    }

    @Override
    public void removeItem(ModularCube modularCube) {
        getSecondFragment().onRemoveCube(modularCube);
        getThirdFragment().onRemoveCube(modularCube);
    }

    @Override
    public void addItem(ModularCube modularCube) {
        getSecondFragment().onAddcube(modularCube);
        getThirdFragment().onAddCube(modularCube);
    }

    @Override
    public void sendMessage(String message) {
        getMainFragment().sendMessage(message);
    }

    @Override
    public void updatedCube(ModularCube cube) {
        getSecondFragment().onUpdatedCube(cube);
        getThirdFragment().onUpdatedCube(cube);
    }

    @Override
    public void sendActivate(long currentCube, boolean b, boolean r) {
        getMainFragment().sendActivate(currentCube, b, r);
    }
}
