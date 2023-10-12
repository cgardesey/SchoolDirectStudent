package com.univirtual.student.pagerAdapter;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.univirtual.student.fragment.EnrolmentsFragment;

import java.util.ArrayList;

public class EnrolmentPagerAdapter extends FragmentPagerAdapter {
    ArrayList<String> institutions;

    public EnrolmentPagerAdapter(FragmentManager fm, ArrayList<String> institutions) {
        super(fm);
        this.institutions = institutions;
    }

    @Override
    public Fragment getItem(int position) {
        EnrolmentsFragment enrolmentsFragment = new EnrolmentsFragment();


        Bundle bundle = new Bundle(institutions.size());
        bundle.putString("institution", getPageTitle(position).toString());
        enrolmentsFragment.setArguments(bundle);

        return enrolmentsFragment;
    }

    @Override
    public int getCount() {
        return institutions.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return institutions.get(position);
    }
}