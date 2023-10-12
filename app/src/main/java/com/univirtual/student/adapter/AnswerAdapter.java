package com.univirtual.student.adapter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.univirtual.student.fragment.AnswerFragment;
import com.univirtual.student.realm.RealmQuestion;

import java.util.ArrayList;

/**
 * Created by Nana on 10/23/2017.
 */

public class AnswerAdapter extends FragmentStatePagerAdapter {

    protected Context mContext;

    ArrayList<RealmQuestion> realmQuestionArrayList;



    public AnswerAdapter(FragmentManager fm, Context context, ArrayList<RealmQuestion> realmQuestionArrayList1) {
        super(fm);
        mContext = context;
       this.realmQuestionArrayList = realmQuestionArrayList1;
    }

    @Override

    public Fragment getItem(int position) {
        Fragment fragment = new AnswerFragment();
        Bundle args = new Bundle();
        args.putInt("page_position", position + 1);
        args.putString("questionid", realmQuestionArrayList.get(position).getQuestionid());
        args.putString("question", realmQuestionArrayList.get(position).getQuestion());
        args.putString("url", realmQuestionArrayList.get(position).getUrl());
        args.putString("answer", realmQuestionArrayList.get(position).getAnswer());
        args.putString("optionA", realmQuestionArrayList.get(position).getOptionA());
        args.putString("optionB", realmQuestionArrayList.get(position).getOptionB());
        args.putString("optionC", realmQuestionArrayList.get(position).getOptionC());
        args.putString("optionD", realmQuestionArrayList.get(position).getOptionD());
        args.putString("optionE", realmQuestionArrayList.get(position).getOptionE());
        args.putString("optionC", realmQuestionArrayList.get(position).getOptionC());
       Log.d("qus", realmQuestionArrayList.get(position).toString());
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getCount() {
        return realmQuestionArrayList.size();
    }
}