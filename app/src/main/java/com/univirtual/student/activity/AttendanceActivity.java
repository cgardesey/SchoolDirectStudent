package com.univirtual.student.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.univirtual.student.R;
import com.univirtual.student.adapter.AttendanceAdapter;
import com.univirtual.student.other.InitApplication;
import com.univirtual.student.realm.RealmAttendance;
import com.univirtual.student.realm.RealmAudio;
import com.univirtual.student.realm.RealmCourse;
import com.univirtual.student.realm.RealmInstructor;
import com.univirtual.student.realm.RealmInstructorCourse;
import com.univirtual.student.realm.RealmRecordedAudioStream;
import com.univirtual.student.realm.RealmRecordedVideoStream;
import com.univirtual.student.util.RealmUtility;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.univirtual.student.activity.HomeActivity.APITOKEN;
import static com.univirtual.student.activity.HomeActivity.MYUSERID;
import static com.univirtual.student.constants.keyConst.API_URL;
import static com.univirtual.student.constants.Const.myVolleyError;

public class AttendanceActivity extends AppCompatActivity {
    protected static Typeface mTfLight;
    private ImageView backbtn, refresh;
    Button backbtn1, retrybtn;
    private String api_token, userid;
    TextView noattendancetext;
    RecyclerView recyclerview;
    AttendanceAdapter attendanceAdapter;
    private static final String MY_LOGIN_ID = "MY_LOGIN_ID";
    ArrayList<RealmAttendance> attendanceArrayList = new ArrayList<>(), newAttendances = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        recyclerview = findViewById(R.id.recyclerview);
        noattendancetext = findViewById(R.id.noattendancetext);
        backbtn = findViewById(R.id.search);
        refresh = findViewById(R.id.refresh);
        retrybtn = findViewById(R.id.retrybtn);
        backbtn1 = findViewById(R.id.backbtn1);
        backbtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh();
            }
        });
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        attendanceAdapter = new AttendanceAdapter(attendanceArrayList, 0);
        recyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerview.setNestedScrollingEnabled(false);
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        recyclerview.setAdapter(attendanceAdapter);

        populateAttendance(getApplicationContext());
    }

    void populateAttendance(final Context context) {
        Realm.init(context);
        Realm.getInstance(RealmUtility.getDefaultConfig(context)).executeTransaction(realm -> {
            RealmResults<RealmAttendance> results;
            results = realm.where(RealmAttendance.class).equalTo("studentid", PreferenceManager.getDefaultSharedPreferences(context).getString(MYUSERID, "")).findAll();
            newAttendances.clear();
            for (RealmAttendance realmAttendance : results) {

                RealmInstructorCourse realmInstructorCourse;
                if (realmAttendance.getType().toLowerCase().equals("video")) {
                    RealmRecordedVideoStream realmRecordedVideoStream = Realm.getInstance(RealmUtility.getDefaultConfig(context)).where(RealmRecordedVideoStream.class).equalTo("sessionid", realmAttendance.getSessionid()).findFirst();

                    if (realmRecordedVideoStream == null) continue;

                    realmAttendance.setTitle(realmRecordedVideoStream.getTitle());
                    realmAttendance.setUrl(realmRecordedVideoStream.getUrl());
                    realmAttendance.setTitle(realmRecordedVideoStream.getTitle());
                    realmAttendance.setDescription(realmRecordedVideoStream.getDescription());
                    realmInstructorCourse = Realm.getInstance(RealmUtility.getDefaultConfig(context)).where(RealmInstructorCourse.class).equalTo("instructorcourseid", realmRecordedVideoStream.getInstructorcourseid()).findFirst();
                } else {
                    RealmRecordedAudioStream realmRecordedAudioStream = Realm.getInstance(RealmUtility.getDefaultConfig(context)).where(RealmRecordedAudioStream.class).equalTo("sessionid", realmAttendance.getSessionid()).findFirst();

                    if (realmRecordedAudioStream == null) continue;

                    realmAttendance.setTitle(realmRecordedAudioStream.getTitle());
                    realmAttendance.setUrl(realmRecordedAudioStream.getUrl());
                    realmAttendance.setTitle(realmRecordedAudioStream.getTitle());
                    realmAttendance.setDescription(realmRecordedAudioStream.getDescription());
                    realmInstructorCourse = Realm.getInstance(RealmUtility.getDefaultConfig(context)).where(RealmInstructorCourse.class).equalTo("instructorcourseid", realmRecordedAudioStream.getInstructorcourseid()).findFirst();
                }
                RealmInstructor realmInstructor = Realm.getInstance(RealmUtility.getDefaultConfig(context)).where(RealmInstructor.class).equalTo("infoid", realmInstructorCourse.getInstructorid()).findFirst();
                RealmCourse realmCourse = Realm.getInstance(RealmUtility.getDefaultConfig(context)).where(RealmCourse.class).equalTo("courseid", realmInstructorCourse.getCourseid()).findFirst();

                realmAttendance.setInstructorname(realmInstructor.getTitle() + " " + realmInstructor.getFirstname() + " " + realmInstructor.getOthername() + " " + realmInstructor.getLastname());
                realmAttendance.setCoursepath(realmCourse.getCoursepath());
                newAttendances.add(realmAttendance);
            }
            attendanceArrayList.clear();
            attendanceArrayList.addAll(newAttendances);
            attendanceAdapter.notifyDataSetChanged();
            if (attendanceArrayList.size() > 0) {
                noattendancetext.setVisibility(View.GONE);
                recyclerview.setVisibility(View.VISIBLE);
            }
            else {
                noattendancetext.setVisibility(View.VISIBLE);
                recyclerview.setVisibility(View.GONE);
            }
        });
    }

    public void refresh() {
        try {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage("Refreshing attendance");
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.show();
            JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    API_URL + "attendance-refresh-data",
                    null,
                    response -> {
                        if (response != null) {
                            dialog.dismiss();
                            Realm.init(getApplicationContext());
                            Realm.getInstance(RealmUtility.getDefaultConfig(AttendanceActivity.this)).executeTransaction(realm -> {
                                RealmResults<RealmAttendance> realmAttendances = realm.where(RealmAttendance.class).findAll();
                                RealmResults<RealmAudio> realmAudios = realm.where(RealmAudio.class).findAll();
                                RealmResults<RealmRecordedAudioStream> realmRecordedAudioStreams = realm.where(RealmRecordedAudioStream.class).findAll();
                                RealmResults<RealmRecordedVideoStream> realmRecordedVideoStreams = realm.where(RealmRecordedVideoStream.class).findAll();

                                realmAttendances.deleteAllFromRealm();
                                realmAudios.deleteAllFromRealm();
                                realmRecordedAudioStreams.deleteAllFromRealm();
                                realmRecordedVideoStreams.deleteAllFromRealm();
                                try {
                                    realm.createOrUpdateAllFromJson(RealmAttendance.class, response.getJSONArray("attendances"));
                                    realm.createOrUpdateAllFromJson(RealmAudio.class, response.getJSONArray("audio"));
                                    realm.createOrUpdateAllFromJson(RealmRecordedAudioStream.class, response.getJSONArray("recorded_audio_streams"));
                                    realm.createOrUpdateAllFromJson(RealmRecordedVideoStream.class, response.getJSONArray("recorded_video_streams"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            });
                            populateAttendance(getApplicationContext());
                            attendanceAdapter.notifyDataSetChanged();
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
                    +
                            0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(jsonArrayRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
