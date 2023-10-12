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

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.flyco.tablayout.SlidingTabLayout;
import com.takusemba.spotlight.SimpleTarget;
import com.takusemba.spotlight.Spotlight;
import com.univirtual.student.R;
import com.univirtual.student.other.InitApplication;
import com.univirtual.student.pagerAdapter.TimetablePagerAdapter;
import com.univirtual.student.realm.RealmTimetable;
import com.univirtual.student.receiver.NetworkReceiver;
import com.univirtual.student.util.RealmUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.univirtual.student.activity.HomeActivity.APITOKEN;
import static com.univirtual.student.constants.keyConst.API_URL;
import static com.univirtual.student.constants.Const.myVolleyError;
import static com.univirtual.student.receiver.NetworkReceiver.activeActivity;

public class TimetableActivity extends AppCompatActivity {

    ViewPager mViewPager;
    SlidingTabLayout mTabLayout;
    ArrayList<String> titles;
    ImageView backbtn, refresh;
    ProgressDialog dialog;
    NetworkReceiver networkReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);

        networkReceiver = new NetworkReceiver();

        backbtn = findViewById(R.id.search);

        backbtn.setOnClickListener(v -> finish());

        refresh = findViewById(R.id.refresh);

        refresh.setOnClickListener(v -> refresh());

        if (!PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("TIMETABLE_ACTIVITY_TIPS_DISMISSED", false)) {
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
                    SimpleTarget firstTarget = new SimpleTarget.Builder(TimetableActivity.this).setPoint(refresh)
                            .setRadius(150F)
//                        .setTitle("Tip")
                            .setDescription(getString(R.string.refresh_timetable_tip) + getString(R.string.click_anywhere_to_dismiss))
                            .build();

                    /*SimpleTarget secondTarget = new SimpleTarget.Builder(PaymentActivity.this).setPoint(menu)
                            .setRadius(150F)
//                        .setTitle("Account Information")
                            .setDescription("Click the highlighted area to filter payments")
                            .build();*/

                    Spotlight.with(TimetableActivity.this)
//                .setOverlayColor(ContextCompat.getColor(getActivity(), R.color.background))
                            .setDuration(250L)
                            .setAnimation(new DecelerateInterpolator(2f))
                            .setTargets(firstTarget)
                            .setClosedOnTouchedOutside(true)
                            .setOnSpotlightStartedListener(() -> {
                            })
                            .setOnSpotlightStartedListener(() -> PreferenceManager
                                    .getDefaultSharedPreferences(getApplicationContext())
                                    .edit()
                                    .putBoolean("TIMETABLE_ACTIVITY_TIPS_DISMISSED", true)
                                    .apply())
                            .start();

                }
            });
        }

        titles = new ArrayList<String>() {{
            add(getResources().getString(R.string.monday));
            add(getResources().getString(R.string.tuesday));
            add(getResources().getString(R.string.wednesday));
            add(getResources().getString(R.string.thursday));
            add(getResources().getString(R.string.friday));
            add(getResources().getString(R.string.saturday));
            add(getResources().getString(R.string.sunday));
        }};

        mViewPager = findViewById(R.id.viewPager);
        mTabLayout = findViewById(R.id.tabLayout);
        mViewPager.setAdapter(new TimetablePagerAdapter(getSupportFragmentManager(), titles));
        mTabLayout.setViewPager(mViewPager);
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
            dialog.setMessage(getString(R.string.refreshing_timetable));
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.show();
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                    Request.Method.GET,
                    API_URL + "timetables",
                    null,
                    response -> {
                        if (response != null) {
                            dialog.dismiss();
                            Realm.init(getApplicationContext());
                            Realm.getInstance(RealmUtility.getDefaultConfig(TimetableActivity.this)).executeTransaction(realm -> {
                                RealmResults<RealmTimetable> realmTimetables = realm.where(RealmTimetable.class).findAll();
                                realmTimetables.deleteAllFromRealm();
                                realm.createOrUpdateAllFromJson(RealmTimetable.class, response);
                            });
                            mViewPager.setAdapter(new TimetablePagerAdapter(getSupportFragmentManager(), titles));
                            mTabLayout.setViewPager(mViewPager);
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
