package com.univirtual.student.activity;

import android.app.ProgressDialog;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.tabs.TabLayout;
import com.takusemba.spotlight.SimpleTarget;
import com.takusemba.spotlight.Spotlight;
import com.univirtual.student.R;
import com.univirtual.student.other.InitApplication;
import com.univirtual.student.pagerAdapter.QuizPagerAdapter;
import com.univirtual.student.realm.RealmQuiz;
import com.univirtual.student.realm.RealmSubmittedQuiz;
import com.univirtual.student.receiver.NetworkReceiver;
import com.univirtual.student.util.RealmUtility;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.univirtual.student.activity.HomeActivity.APITOKEN;
import static com.univirtual.student.constants.keyConst.API_URL;
import static com.univirtual.student.constants.Const.myVolleyError;
import static com.univirtual.student.fragment.SubmittedQuizzesFragment.populateSubmittedQuizzes;
import static com.univirtual.student.fragment.UnsubmittedQuizzesFragment.populateUnsubmittedQuizzes;
import static com.univirtual.student.receiver.NetworkReceiver.activeActivity;

public class QuizzesActivity extends AppCompatActivity {

    String TITLE;
    String INSTRUCTORCOURSEID;
    String COURSEPATH;
    String ENROLMENTID;
    String PROFILEIMGURL;
    String ROOMID;
    String NODESERVER;
    String INSTRUCTORNAME;
    String SESSIONID;

    static final int
            ALL = 1,
            PENDING = 2,
            PAST = 3;
    static int LISTTYPE = ALL;

    ImageView backbtn, refresh;
    ProgressDialog dialog;
    TextView classtitle;
    NetworkReceiver networkReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quizzes);

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

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        backbtn = findViewById(R.id.search);
        refresh = findViewById(R.id.refresh);
        classtitle = findViewById(R.id.classtitle);

        classtitle.setText(COURSEPATH);

        refresh.setOnClickListener(v -> refresh());

        tabLayout.setSelectedTabIndicatorHeight(5);
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.white));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.unsubmitted)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.submitted)));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        final ViewPager viewPager = findViewById(R.id.pager);
        final QuizPagerAdapter quizPagerAdapter = new QuizPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setAdapter(quizPagerAdapter);
        tabLayout.getTabAt(0).setText(getResources().getString(R.string.unsubmitted));
        tabLayout.getTabAt(1).setText(getString(R.string.submitted));

        backbtn.setOnClickListener(v -> finish());

        if (!PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("QUIZZES_ACTIVITY_TIPS_DISMISSED", false)) {
            ViewTreeObserver vto = refresh.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        refresh.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        refresh.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    // make an target
                    // make an target
                    SimpleTarget firstTarget = new SimpleTarget.Builder(QuizzesActivity.this).setPoint(refresh)
                            .setRadius(150F)
//                        .setTitle("Account Information")
                            .setDescription(getString(R.string.check_for_new_quizzes_tip) + getString(R.string.click_anywhere_to_dismiss))
                            .build();

                    Spotlight.with(QuizzesActivity.this)
                            .setDuration(250L)
                            .setAnimation(new DecelerateInterpolator(2f))
                            .setTargets(firstTarget)
                            .setClosedOnTouchedOutside(true)
                            .setOnSpotlightStartedListener(() -> {
                            })
                            .setOnSpotlightStartedListener(() -> PreferenceManager
                                    .getDefaultSharedPreferences(getApplicationContext())
                                    .edit()
                                    .putBoolean("QUIZZES_ACTIVITY_TIPS_DISMISSED", true)
                                    .apply())
                            .start();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        activeActivity = this;
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkReceiver);
    }

    public void refresh() {
        try {
            dialog = new ProgressDialog(this);
            dialog.setMessage(getString(R.string.checking_for_new_quizzes));
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.show();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    API_URL + "quizzes-refresh-data",
                    null,
                    response -> {
                        if (response != null) {
                            dialog.dismiss();
                            Realm.init(getApplicationContext());
                            Realm.getInstance(RealmUtility.getDefaultConfig(QuizzesActivity.this)).executeTransaction(realm -> {
                                try {
                                    RealmResults<RealmQuiz> realmQuizzes = realm.where(RealmQuiz.class).findAll();
                                    realmQuizzes.deleteAllFromRealm();

                                    RealmResults<RealmSubmittedQuiz> realmSubmittedQuizzes = realm.where(RealmSubmittedQuiz.class).findAll();
                                    realmSubmittedQuizzes.deleteAllFromRealm();

                                    realm.createOrUpdateAllFromJson(RealmQuiz.class, response.getJSONArray("quizzes"));
                                    realm.createOrUpdateAllFromJson(RealmSubmittedQuiz.class, response.getJSONArray("submitted_quizzes"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            });
                            populateUnsubmittedQuizzes(QuizzesActivity.this);
                            populateSubmittedQuizzes(QuizzesActivity.this);
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
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(jsonObjectRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
