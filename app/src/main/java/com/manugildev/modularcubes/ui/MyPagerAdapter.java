package com.manugildev.modularcubes.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.manugildev.modularcubes.fragments.FourthFragment;
import com.manugildev.modularcubes.fragments.MainActivityFragment;
import com.manugildev.modularcubes.fragments.MessageFragment;
import com.manugildev.modularcubes.fragments.SecondFragment;
import com.manugildev.modularcubes.fragments.ThirdFragment;

public class MyPagerAdapter extends FragmentPagerAdapter {
    private static int NUM_ITEMS = 5;
    public static MessageFragment messageFragment;
    public static MainActivityFragment mainActivityFragment;
    public static SecondFragment secondFragment;
    public static ThirdFragment thirdFragment;
    public static FourthFragment fourthFragment;

    public MyPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
        messageFragment = MessageFragment.newInstance();
        mainActivityFragment = MainActivityFragment.newInstance();
        secondFragment = SecondFragment.newInstance();
        thirdFragment = ThirdFragment.newInstance();
        fourthFragment = FourthFragment.newInstance();
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return messageFragment;
            case 1:
                return mainActivityFragment;
            case 2:
                return secondFragment;
            case 3:
                return thirdFragment;
            case 4:
                return fourthFragment;
            default:
                return null;
        }
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        return "Page " + position;
    }
}