package com.example.runningtracker.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private final List<Fragment> fragmentsList = new ArrayList<>();
    private final List<String> fragmentsTitleList = new ArrayList<>();


    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentsList.get(position);

    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return fragmentsTitleList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentsList.size();

    }

    public void addFragment(Fragment fragment, String title){
        fragmentsList.add(fragment);
        fragmentsTitleList.add(title);
    }
}