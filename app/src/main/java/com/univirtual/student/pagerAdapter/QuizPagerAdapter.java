package com.univirtual.student.pagerAdapter;

/**
 * Created by Nana on 11/26/2017.
 */

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.univirtual.student.fragment.SubmittedQuizzesFragment;
import com.univirtual.student.fragment.UnsubmittedQuizzesFragment;


public class QuizPagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public QuizPagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                UnsubmittedQuizzesFragment unsubmittedQuizzesFragment = new UnsubmittedQuizzesFragment();
                return unsubmittedQuizzesFragment;
            case 1:
                SubmittedQuizzesFragment submittedQuizFragment = new SubmittedQuizzesFragment();
                return submittedQuizFragment;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
