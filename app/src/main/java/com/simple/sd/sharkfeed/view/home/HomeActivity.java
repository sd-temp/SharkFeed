package com.simple.sd.sharkfeed.view.home;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.simple.sd.sharkfeed.R;
import com.simple.sd.sharkfeed.data.DataManager;
import com.simple.sd.sharkfeed.view.fragment.GridFragment;
import com.simple.sd.sharkfeed.view.pager.CustomViewPager;
import com.simple.sd.sharkfeed.view.pager.ScreenSlidePagerAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends AppCompatActivity {

    @BindView(R.id.pager)
    CustomViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        initPager();
    }

    private void initPager() {
        PagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                if (i == 1) mPager.setPagingEnabled(false);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
    }

    public Fragment getGridFragment() {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof GridFragment)
                return fragment;
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getGridFragment();
        if (fragment != null && fragment.isVisible()) {
            if (((GridFragment) fragment).allowBackPressed()) {
                super.onBackPressed();
                DataManager.getInstance().refreshData();
            }
        } else {
            super.onBackPressed();
        }
    }
}
