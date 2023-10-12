package com.univirtual.student.activity;

import android.app.ProgressDialog;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.tabs.TabLayout;
import com.univirtual.student.R;
import com.univirtual.student.other.InitApplication;
import com.univirtual.student.pagerAdapter.AssignmentPagerAdapter;
import com.univirtual.student.realm.RealmAssignment;
import com.univirtual.student.realm.RealmCourse;
import com.univirtual.student.realm.RealmSubmittedAssignment;
import com.univirtual.student.receiver.NetworkReceiver;
import com.univirtual.student.util.RealmUtility;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.univirtual.student.activity.HomeActivity.APITOKEN;
import static com.univirtual.student.constants.keyConst.API_URL;
import static com.univirtual.student.constants.Const.myVolleyError;
import static com.univirtual.student.fragment.AssignedFragment.initUnsubmittedFragment;
import static com.univirtual.student.fragment.MarkedFragment.initMarkedFragment;
import static com.univirtual.student.fragment.UnmarkedFragment.initUnmarkedFragment;
import static com.univirtual.student.receiver.NetworkReceiver.activeActivity;

public class AssignmentActivity extends AppCompatActivity {

    String TITLE;
    String INSTRUCTORCOURSEID;
    String COURSEPATH;
    String ENROLMENTID;
    String PROFILEIMGURL;
    String ROOMID;
    String NODESERVER;
    String INSTRUCTORNAME;
    String SESSIONID;

    ArrayList<RealmCourse> courses = new ArrayList<>();
    ImageView backbtn;
    public static ImageView refreshassignmentsimg;
    ProgressDialog dialog;
    TextView classtitle;
    ViewPager viewPager;
    TabLayout tabLayout;
    AssignmentPagerAdapter assignmentPagerAdapter;
    NetworkReceiver networkReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment);

        String instructorcourseid = getIntent().getStringExtra("INSTRUCTORCOURSEID");
        String coursepathstring = getIntent().getStringExtra("COURSEPATH");
        String enrolmentid = getIntent().getStringExtra("ENROLMENTID");
        String profileimgurl = getIntent().getStringExtra("PROFILEIMGURL");
        String nodeserver = getIntent().getStringExtra("NODESERVER");
        String roomid = getIntent().getStringExtra("ROOMID");


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

        networkReceiver = new NetworkReceiver();

        tabLayout = findViewById(R.id.tab_layout);
        backbtn = findViewById(R.id.search);
        classtitle = findViewById(R.id.classtitle);
        refreshassignmentsimg = findViewById(R.id.refresh);
        refreshassignmentsimg.setOnClickListener(v -> refresh());
        viewPager = findViewById(R.id.pager);

        tabLayout.setSelectedTabIndicatorHeight(5);
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.white));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.unsubmitted)));
        tabLayout.addTab(tabLayout.newTab().setText("Unmarked"));
        tabLayout.addTab(tabLayout.newTab().setText("Marked"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        assignmentPagerAdapter = new AssignmentPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setAdapter(assignmentPagerAdapter);
        viewPager.setOffscreenPageLimit(3);
        tabLayout.getTabAt(0).setText(getResources().getString(R.string.unsubmitted));
        tabLayout.getTabAt(1).setText("Unmarked");
        tabLayout.getTabAt(2).setText("Marked");
        backbtn.setOnClickListener(v -> finish());

        if (getIntent().getBooleanExtra("SHOWMARKEDFRAGMENT", false)) {
            tabLayout.selectTab(tabLayout.getTabAt(2), true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        activeActivity = this;
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        classtitle.setText(COURSEPATH);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkReceiver);
    }

    public void refresh() {
        try {
            dialog = new ProgressDialog(this);
            dialog.setMessage(getString(R.string.checking_for_new_assignment));
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.show();
            JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    API_URL + "assignments-refresh-data",
                    null,
                    response -> {
                        if (response != null) {
                            dialog.dismiss();
                            Realm.init(getApplicationContext());
                            Realm.getInstance(RealmUtility.getDefaultConfig(AssignmentActivity.this)).executeTransaction(realm -> {
                                try {
                                    RealmResults<RealmAssignment> realmAssignments = realm.where(RealmAssignment.class).findAll();
                                    realmAssignments.deleteAllFromRealm();

                                    RealmResults<RealmSubmittedAssignment> realmSubmittedAssignments = realm.where(RealmSubmittedAssignment.class).findAll();
                                    realmSubmittedAssignments.deleteAllFromRealm();

                                    realm.createOrUpdateAllFromJson(RealmAssignment.class, response.getJSONArray("assignments"));
                                    realm.createOrUpdateAllFromJson(RealmSubmittedAssignment.class, response.getJSONArray("submitted_assignments"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            });
                            initUnsubmittedFragment(AssignmentActivity.this);
                            initUnmarkedFragment(AssignmentActivity.this);
                            initMarkedFragment(AssignmentActivity.this);
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Log.d("Cyrilll", error.toString());
                        dialog.dismiss();
                        myVolleyError(getApplicationContext(), error);
                    }
            ) {
                /** Passing some request headers* */
                @Override
                public Map getHeaders() throws AuthFailureError {
                    HashMap headers = new HashMap();
                    headers.put("accept", "application/json");
                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(APITOKEN, ""));
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
}
