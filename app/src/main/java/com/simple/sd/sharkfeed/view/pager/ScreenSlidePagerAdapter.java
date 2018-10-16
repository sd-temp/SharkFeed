package com.simple.sd.sharkfeed.view.pager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.simple.sd.sharkfeed.view.fragment.GridFragment;
import com.simple.sd.sharkfeed.view.fragment.SplashFragment;

public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

    private static final int NUM_PAGES = 2;

    public ScreenSlidePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        if ( i==0 ) {
            return new SplashFragment();
        } else {
            return new GridFragment();
        }
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }
}
