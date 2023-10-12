package com.univirtual.student.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.univirtual.student.R;
import com.univirtual.student.adapter.AssignmentAdapter;
import com.univirtual.student.realm.RealmAssignment;
import com.univirtual.student.realm.RealmCourse;
import com.univirtual.student.realm.RealmEnrolment;
import com.univirtual.student.realm.RealmInstructorCourse;
import com.univirtual.student.realm.RealmSubmittedAssignment;
import com.univirtual.student.util.RealmUtility;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.univirtual.student.activity.HomeActivity.MYUSERID;



/**
 * Created by Nana on 11/26/2017.
 */

public class AssignedFragment extends Fragment {

    static String TITLE;
    static String INSTRUCTORCOURSEID;
    static String COURSEPATH;
    static String ENROLMENTID;
    static String PROFILEIMGURL;
    static String ROOMID;
    static String NODESERVER;
    static String INSTRUCTORNAME;
    static String SESSIONID;

    static ArrayList<RealmAssignment> unsubmittedAssignments = new ArrayList<>();

    static RecyclerView recyclerView;
    static TextView nodatatext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_assignment, container, false);

        String instructorcourseid = getActivity().getIntent().getStringExtra("INSTRUCTORCOURSEID");
        String coursepathstring = getActivity().getIntent().getStringExtra("COURSEPATH");
        String enrolmentid = getActivity().getIntent().getStringExtra("ENROLMENTID");
        String profileimgurl = getActivity().getIntent().getStringExtra("PROFILEIMGURL");
        String nodeserver = getActivity().getIntent().getStringExtra("NODESERVER");
        String roomid = getActivity().getIntent().getStringExtra("ROOMID");


        if (instructorcourseid != null && !instructorcourseid.equals("")) {
            INSTRUCTORCOURSEID = instructorcourseid;
        }
        if (coursepathstring != null && !coursepathstring.equals("")) {
            COURSEPATH = coursepathstring;
        }
        if (enrolmentid != null && !enrolmentid.equals("")) {
            ENROLMENTID = enrolmentid;
        }
        if (profileimgurl != null && !profileimgurl.equals("")) {
            PROFILEIMGURL = profileimgurl;
        }
        if (nodeserver != null && !nodeserver.equals("")) {
            NODESERVER = nodeserver;
        }
        if (roomid != null && !roomid.equals("")) {
            ROOMID = roomid;
        }

        recyclerView = rootView.findViewById(R.id.recyclerview);
        nodatatext = rootView.findViewById(R.id.nodatatext);
        initUnsubmittedFragment(getContext());

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        initUnsubmittedFragment(getContext());
    }

    public static void populateUnsubmittedAssignments(Context context) {

        Realm.init(context);
        Realm.getInstance(RealmUtility.getDefaultConfig(context)).executeTransaction(realm -> {
            RealmResults<RealmAssignment> results = realm.where(RealmAssignment.class)
                    .equalTo("instructorcourseid", INSTRUCTORCOURSEID)
                    .findAll();

            unsubmittedAssignments.clear();
            for (RealmAssignment realmAssignment : results) {
                String enrolmentid = Realm.getInstance(RealmUtility.getDefaultConfig(context))
                        .where(RealmEnrolment.class)
                        .equalTo("instructorcourseid", realmAssignment.getInstructorcourseid())
                        .equalTo("studentid", PreferenceManager.getDefaultSharedPreferences(context).getString(MYUSERID, ""))
                        .findFirst()
                        .getEnrolmentid();
                realmAssignment.setEnrolmentid(enrolmentid);
                RealmInstructorCourse realmInstructorCourse = Realm.getInstance(RealmUtility.getDefaultConfig(context)).where(RealmInstructorCourse.class).equalTo("instructorcourseid", realmAssignment.getInstructorcourseid()).findFirst();
                RealmCourse realmCourse = Realm.getInstance(RealmUtility.getDefaultConfig(context)).where(RealmCourse.class).equalTo("courseid", realmInstructorCourse.getCourseid()).findFirst();

                realmAssignment.setCoursepath(realmCourse.getCoursepath());
                RealmSubmittedAssignment realmSubmittedAssignment = Realm.getInstance(RealmUtility.getDefaultConfig(context)).where(RealmSubmittedAssignment.class).equalTo("assignmentid", realmAssignment.getAssignmentid()).findFirst();
                realmAssignment.setSubmitted(realmSubmittedAssignment == null ? 0 : 1);
                if (realmAssignment.getSubmitted() == 0) {
                    unsubmittedAssignments.add(realmAssignment);
                }
            }
        });
    }

    public static void initUnsubmittedFragment(Context context) {
        populateUnsubmittedAssignments(context);
        if (unsubmittedAssignments.size() == 0) {
            nodatatext.setVisibility(View.VISIBLE);
            nodatatext.setText(context.getString(R.string.no_unsubmitted_assignments));
        } else {
            nodatatext.setVisibility(View.GONE);
        }
        GridLayoutManager layoutManager
                = new GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false);
        AssignmentAdapter assignmentAdapter = new AssignmentAdapter(unsubmittedAssignments);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(assignmentAdapter);
    }
}
