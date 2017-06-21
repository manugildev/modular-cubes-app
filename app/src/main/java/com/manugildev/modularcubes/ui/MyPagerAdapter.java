package com.manugildev.modularcubes.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.manugildev.modularcubes.fragments.MainActivityFragment;
import com.manugildev.modularcubes.fragments.MessageFragment;
import com.manugildev.modularcubes.fragments.SecondFragment;

public class MyPagerAdapter extends FragmentPagerAdapter {
    private static int NUM_ITEMS = 3;
    public static MessageFragment messageFragment;
    public static MainActivityFragment mainActivityFragment;
    public static SecondFragment secondFragment;

    public MyPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
        messageFragment = MessageFragment.newInstance();
        mainActivityFragment = MainActivityFragment.newInstance();
        secondFragment = SecondFragment.newInstance();
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