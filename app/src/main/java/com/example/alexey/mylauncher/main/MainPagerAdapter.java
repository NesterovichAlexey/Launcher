package com.example.alexey.mylauncher.main;

import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


public class MainPagerAdapter extends FragmentPagerAdapter {
    private static int PAGE_COUNT;

    public MainPagerAdapter(FragmentManager fm, boolean isShow) {
        super(fm);
        PAGE_COUNT = isShow ? 2 : 1;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }


    @Override
    public Fragment getItem(int position) {
        return MainFragment.newInstance(position);
    }
}
