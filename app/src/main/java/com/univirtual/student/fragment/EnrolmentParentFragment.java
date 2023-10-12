package com.univirtual.student.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.flyco.tablayout.SlidingTabLayout;
import com.univirtual.student.R;
import com.univirtual.student.other.InitApplication;
import com.univirtual.student.pagerAdapter.EnrolmentPagerAdapter;
import com.univirtual.student.realm.RealmAppUserFee;
import com.univirtual.student.realm.RealmCourse;
import com.univirtual.student.realm.RealmDialcode;
import com.univirtual.student.realm.RealmEnrolment;
import com.univirtual.student.realm.RealmInstitution;
import com.univirtual.student.realm.RealmInstructor;
import com.univirtual.student.realm.RealmInstructorCourse;
import com.univirtual.student.realm.RealmPayment;
import com.univirtual.student.realm.RealmStudent;
import com.univirtual.student.realm.RealmTimetable;
import com.univirtual.student.realm.RealmUser;
import com.univirtual.student.util.RealmUtility;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.univirtual.student.activity.HomeActivity.APITOKEN;
import static com.univirtual.student.activity.HomeActivity.MYUSERID;
import static com.univirtual.student.activity.HomeActivity.persistAll;
import static com.univirtual.student.constants.keyConst.API_URL;
import static com.univirtual.student.constants.Const.myVolleyError;

public class EnrolmentParentFragment extends Fragment {

    static ViewPager mViewPager;
    static SlidingTabLayout mTabLayout;
    static ArrayList<String> institutions = new ArrayList<>();
    ImageView backbtn;
    public static ImageView refresh;
    ProgressDialog dialog;
    public TextView dialcode;
    public static ImageView menu;
    PopupMenu popup;
    public static FragmentManager fragmentManager;
    static FragmentManager childFragmentManager;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_parent_enrolment, container, false);

        childFragmentManager = getChildFragmentManager();
        fragmentManager = childFragmentManager;
        backbtn = rootView.findViewById(R.id.search);

        backbtn.setOnClickListener(v -> getActivity().finish());

        dialcode = rootView.findViewById(R.id.dialcode);

        refresh = rootView.findViewById(R.id.refresh);

        menu = rootView.findViewById(R.id.menu);

        menu.setOnClickListener(v -> {
            EnrolmentsFragment enrolmentsFragment = (EnrolmentsFragment) childFragmentManager.getFragments().get(mViewPager.getCurrentItem());
            if(enrolmentsFragment.renewsubscriptionbtn.getVisibility() == View.VISIBLE) {
                return;
            }
            popup = new PopupMenu(getContext(), menu);

            popup.inflate(R.menu.course_menu);

            if(enrolmentsFragment.institution.trim().toLowerCase().equals("other")) {
                popup.getMenu().findItem(R.id.expiredsubscription).setVisible(true);
                popup.getMenu().findItem(R.id.activesubscriptions).setVisible(true);
            }
            else {
                popup.getMenu().findItem(R.id.expiredsubscription).setVisible(false);
                popup.getMenu().findItem(R.id.activesubscriptions).setVisible(false);
            }

            popup.setOnMenuItemClickListener(item -> {

                enrolmentsFragment.enrolments.clear();
                switch (item.getItemId()) {
                    case R.id.upcoming:
                        enrolmentsFragment.populateUpcomingClasses();
                        enrolmentsFragment.enrolments.addAll(enrolmentsFragment.upcomingClasses);
                        enrolmentsFragment.enrolmentFragmentAdapter.notifyDataSetChanged();
                        return true;

                    case R.id.allmyclasses:
                        enrolmentsFragment.populateAllClasses(enrolmentsFragment.institution);
                        enrolmentsFragment.enrolments.addAll(enrolmentsFragment.allClasses);
                        enrolmentsFragment.enrolmentFragmentAdapter.notifyDataSetChanged();
                        return true;
                    case R.id.expiredsubscription:
                        enrolmentsFragment.populateExpired();
                        enrolmentsFragment.enrolments.addAll(enrolmentsFragment.expiredClasses);
                        enrolmentsFragment.enrolmentFragmentAdapter.notifyDataSetChanged();
                        return true;
                    case R.id.activesubscriptions:
                        enrolmentsFragment.populateActive();
                        enrolmentsFragment.enrolments.addAll(enrolmentsFragment.activeClasses);
                        enrolmentsFragment.enrolmentFragmentAdapter.notifyDataSetChanged();
                        return true;
                }
                return false;
            });

            popup.show();
        });

        refresh.setOnClickListener(v -> refresh());

        Realm.init(getActivity());
        Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmDialcode realmDialcode = Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).where(RealmDialcode.class).findFirst();
                dialcode.setText(realmDialcode.getDialcode());
            }
        });
        institutions.clear();
        Realm.init(getActivity());
        Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
            RealmResults<RealmEnrolment> results = realm.where(RealmEnrolment.class)
                    .equalTo("studentid", PreferenceManager.getDefaultSharedPreferences(getContext()).getString(MYUSERID, ""))
                    .equalTo("enrolled", 1)
                    .findAll();
            for (RealmEnrolment realmEnrolment : results) {

                RealmInstructorCourse realmInstructorCourse = Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).where(RealmInstructorCourse.class).equalTo("instructorcourseid", realmEnrolment.getInstructorcourseid()).findFirst();
                RealmCourse realmCourse = Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).where(RealmCourse.class).equalTo("courseid", realmInstructorCourse.getCourseid()).findFirst();
                String institution = realmCourse.getCoursepath().split(" >> ")[1];
                if (!institutions.contains(StringUtils.normalizeSpace(institution))) {
                    institutions.add(institution);
                }
            }
        });

        mViewPager = rootView.findViewById(R.id.viewPager);
        mTabLayout = rootView.findViewById(R.id.tabLayout);
        initEnrolmentParentFragment();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
//        initEnrolmentParentFragment();
    }

    public void refresh() {
        try {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage(getString(R.string.refreshing_enrolment_status));
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.show();
            JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    API_URL + "enrolment-refresh-data",
                    null,
                    responseJson -> {
                        if (responseJson != null) {
                            dialog.dismiss();
                            Realm.init(getActivity());
                            Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    try {
                                        RealmResults<RealmCourse> realmCourses = realm.where(RealmCourse.class).findAll();
                                        realmCourses.deleteAllFromRealm();

                                        RealmResults<RealmStudent> realmStudents = realm.where(RealmStudent.class).findAll();
                                        realmStudents.deleteAllFromRealm();

                                        RealmResults<RealmUser> realmUsers = realm.where(RealmUser.class).findAll();
                                        realmUsers.deleteAllFromRealm();

                                        /*RealmResults<RealmEnrolment> realmEnrolments = realm.where(RealmEnrolment.class).findAll();
                                        realmEnrolments.deleteAllFromRealm();*/

                                        RealmResults<RealmTimetable> realmTimetables = realm.where(RealmTimetable.class).findAll();
                                        realmTimetables.deleteAllFromRealm();

                                        RealmResults<RealmInstructor> realmInstructors = realm.where(RealmInstructor.class).findAll();
                                        realmInstructors.deleteAllFromRealm();

                                        RealmResults<RealmInstructorCourse> realmInstructorCourses = realm.where(RealmInstructorCourse.class).findAll();
                                        realmInstructorCourses.deleteAllFromRealm();

                                        RealmResults<RealmPayment> realmPayments = realm.where(RealmPayment.class).findAll();
                                        realmPayments.deleteAllFromRealm();

                                        RealmResults<RealmInstitution> realmInstitutions = realm.where(RealmInstitution.class).findAll();
                                        realmInstitutions.deleteAllFromRealm();

                                        RealmResults<RealmAppUserFee> realmAppUserFees = realm.where(RealmAppUserFee.class).findAll();
                                        realmAppUserFees.deleteAllFromRealm();

                                        RealmResults<RealmDialcode> realmDialcodes = realm.where(RealmDialcode.class).findAll();
                                        realmDialcodes.deleteAllFromRealm();

                                        persistAll(realm, responseJson);

                                        RealmDialcode realmDialcode = Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).where(RealmDialcode.class).findFirst();
                                        dialcode.setText(realmDialcode.getDialcode());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            initEnrolmentParentFragment();
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Log.d("Cyrilll", error.toString());
                        dialog.dismiss();
                        myVolleyError(getActivity(), error);
                    }
            ) {
                /** Passing some request headers* */
                @Override
                public Map getHeaders() throws AuthFailureError {
                    HashMap headers = new HashMap();
                    headers.put("accept", "application/json");
                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(APITOKEN, ""));
                    return headers;
                }
            };
            jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(jsonArrayRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void initEnrolmentParentFragment() {
        mViewPager.setAdapter(new EnrolmentPagerAdapter(fragmentManager, institutions));
        mTabLayout.setViewPager(mViewPager);
    }
}
