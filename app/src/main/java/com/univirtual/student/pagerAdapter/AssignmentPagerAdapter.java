package com.univirtual.student.pagerAdapter;

/**
 * Created by Nana on 11/26/2017.
 */

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import com.univirtual.student.fragment.AssignedFragment;
import com.univirtual.student.fragment.UnmarkedFragment;
import com.univirtual.student.fragment.MarkedFragment;


public class AssignmentPagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public AssignmentPagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                AssignedFragment assignedFragment = new AssignedFragment();
                return assignedFragment;
            case 1:
                UnmarkedFragment unmarkedFragment = new UnmarkedFragment();
                return unmarkedFragment;
            case 2:
                MarkedFragment markedFragment = new MarkedFragment();
                return markedFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
