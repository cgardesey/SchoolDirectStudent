package com.univirtual.student.fragment;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.univirtual.student.R;
import com.univirtual.student.adapter.TimetableAdapter;
import com.univirtual.student.realm.RealmCourse;
import com.univirtual.student.realm.RealmEnrolment;
import com.univirtual.student.realm.RealmInstructor;
import com.univirtual.student.realm.RealmInstructorCourse;
import com.univirtual.student.realm.RealmPeriod;
import com.univirtual.student.realm.RealmTimetable;
import com.univirtual.student.util.RealmUtility;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.univirtual.student.activity.HomeActivity.MYUSERID;
/**
 * Created by 2CLearning on 12/13/2017.
 */

public class TimetableFragment extends Fragment {
    RecyclerView recyclerView;
    ArrayList<RealmTimetable> newTimetables = new ArrayList<>();
    public ArrayList<RealmTimetable> timetables = new ArrayList<>();
    TimetableAdapter timetableAdapter;
    String title = "";
    Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getContext();

        final View rootView = inflater.inflate(R.layout.fragment_timetable, container, false);

        title = getArguments().getString("title");

        if (getResources().getString(R.string.monday).equals(title)) {
            title = "Monday";
        } else if (getResources().getString(R.string.tuesday).equals(title)) {
            title = "Tuesday";
        } else if (getResources().getString(R.string.wednesday).equals(title)) {
            title = "Wednesday";
        } else if (getResources().getString(R.string.thursday).equals(title)) {
            title = "Thursday";
        } else if (getResources().getString(R.string.friday).equals(title)) {
            title = "Friday";
        } else if (getResources().getString(R.string.saturday).equals(title)) {
            title = "Saturday";
        } else if (getResources().getString(R.string.sunday).equals(title)) {
            title = "Sunday";
        }

        recyclerView = rootView.findViewById(R.id.timetablerecyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        timetables.clear();
        populateMyTimetable(mContext, title);
        timetables.addAll(newTimetables);
        timetableAdapter = new TimetableAdapter(timetables);

        recyclerView.setAdapter(timetableAdapter);

        return rootView;
    }

    void populateMyTimetable(final Context context, final String title) {
        Realm.init(context);
        Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
            RealmResults<RealmTimetable> results;
            String UCtitle = title.substring(0, 1).toUpperCase() + title.substring(1).toLowerCase();
            Log.d("gardesey", UCtitle);
            results = realm.where(RealmTimetable.class).equalTo("dow", UCtitle).findAll().sort("dow");
            newTimetables.clear();
            for (RealmTimetable realmTimetable : results) {

                RealmPeriod realmPeriod = Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).where(RealmPeriod.class).equalTo("id", realmTimetable.getPeriod_id()).findFirst();
                realmTimetable.setStarttime(realmPeriod.getStarttime());
                realmTimetable.setEndtime(realmPeriod.getEndtime());

                RealmInstructorCourse realmInstructorCourse = Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).where(RealmInstructorCourse.class).equalTo("instructorcourseid", realmTimetable.getInstructorcourseid()).findFirst();
                RealmEnrolment realmEnrolment = Realm.getInstance(RealmUtility.getDefaultConfig(getActivity()))
                        .where(RealmEnrolment.class)
                        .equalTo("instructorcourseid", realmTimetable.getInstructorcourseid())
                        .equalTo("studentid", PreferenceManager.getDefaultSharedPreferences(context).getString(MYUSERID, ""))
                        .findFirst();

                if (realmEnrolment == null) {
                    continue;
                }


                if (realmEnrolment.getEnrolled() == 1) {
                    RealmInstructor realmInstructor = Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).where(RealmInstructor.class).equalTo("infoid", realmInstructorCourse.getInstructorid()).findFirst();
                    realmTimetable.setInstructorname((realmInstructor.getTitle() + " " + realmInstructor.getFirstname() + " " + realmInstructor.getOthername() + realmInstructor.getLastname()).replace("null", ""));
                    realmTimetable.setAbout(realmInstructor.getEdubackground());
                    realmTimetable.setInstructorpic(realmInstructor.getProfilepicurl());

                    RealmCourse realmCourse = Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).where(RealmCourse.class).equalTo("courseid", realmInstructorCourse.getCourseid()).findFirst();
                    String[] split = realmCourse.getCoursepath().split(">>");
                    realmTimetable.setCourse(split[split.length - 1]);

                    newTimetables.add(realmTimetable);
                }
            }
        });
    }
}