package com.univirtual.student.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.greysonparrelli.permiso.PermisoActivity;
import com.univirtual.student.R;
import com.univirtual.student.adapter.DistinctRecordedStreamAdapter;
import com.univirtual.student.other.InitApplication;
import com.univirtual.student.realm.RealmAudio;
import com.univirtual.student.realm.RealmRecordedAudioStream;
import com.univirtual.student.realm.RealmRecordedVideoStream;
import com.univirtual.student.receiver.NetworkReceiver;
import com.univirtual.student.util.RealmUtility;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.univirtual.student.activity.HomeActivity.APITOKEN;
import static com.univirtual.student.constants.keyConst.API_URL;
import static com.univirtual.student.constants.Const.myVolleyError;
import static com.univirtual.student.receiver.NetworkReceiver.activeActivity;

public class DistinctRecordedStreamActivity extends PermisoActivity {

    String TITLE;
    String INSTRUCTORCOURSEID;
    String COURSEPATH;
    String ENROLMENTID;
    String PROFILEIMGURL;
    String ROOMID;
    String NODESERVER;
    String INSTRUCTORNAME;
    String SESSIONID;

    RecyclerView recyclerview;
    ImageView backbtn, refresh;
    TextView norecordingtext, coursepath;
    static public ProgressDialog mProgress;
    DistinctRecordedStreamAdapter distinctRecordedStreamAdapter;
    ArrayList<Object> objects = new ArrayList<>(), newObjects = new ArrayList<>();
    ProgressDialog dialog;
    NetworkReceiver networkReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distinct_recorded_streams);

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

        mProgress = new ProgressDialog(this);
        mProgress.setMessage(getString(R.string.pls_wait));
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
        refresh = findViewById(R.id.refresh);
        norecordingtext = findViewById(R.id.norecordingtext);

        coursepath = findViewById(R.id.coursepath);

        coursepath.setText(COURSEPATH);

        refresh.setOnClickListener(v -> refresh());
        objects = new ArrayList<>();
        recyclerview = findViewById(R.id.recyclerview);
        objects.addAll(newObjects);
        distinctRecordedStreamAdapter = new DistinctRecordedStreamAdapter((objects, position, holder) -> {
            Object object = objects.get(position);
            if (objects.get(position) instanceof RealmAudio) {
                RealmAudio realmAudio = (RealmAudio) object;
                TITLE = realmAudio.getTitle();
            } else if (objects.get(position) instanceof RealmRecordedAudioStream) {
                RealmRecordedAudioStream recordedAudioStream = (RealmRecordedAudioStream) object;
                TITLE = recordedAudioStream.getTitle();
            } else if (objects.get(position) instanceof RealmRecordedVideoStream) {
                RealmRecordedVideoStream realmRecordedVideoStream = (RealmRecordedVideoStream) object;
                TITLE = realmRecordedVideoStream.getTitle();
            }
            startActivity(new Intent(DistinctRecordedStreamActivity.this, ClassReplaysActivity.class)
                    .putExtra("INSTRUCTORCOURSEID", INSTRUCTORCOURSEID)
                    .putExtra("COURSEPATH", COURSEPATH)
                    .putExtra("ENROLMENTID", ENROLMENTID)
                    .putExtra("PROFILEIMGURL", PROFILEIMGURL)
                    .putExtra("INSTRUCTORNAME", INSTRUCTORNAME)
                    .putExtra("NODESERVER", NODESERVER)
                    .putExtra("ROOMID", ROOMID)

                    .putExtra("TITLE", TITLE)
            );
        }, objects);
        populateObjects(getApplicationContext());
        recyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerview.setNestedScrollingEnabled(false);
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        recyclerview.setAdapter(distinctRecordedStreamAdapter);
        backbtn = findViewById(R.id.search);
        backbtn.setOnClickListener(view -> {
            clickview(view);
            finish();
        });
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

    private void clickview(View v) {
        Animation animation1 = AnimationUtils.loadAnimation(v.getContext(), R.anim.click);
        v.startAnimation(animation1);
    }

    void populateObjects(final Context context) {
        newObjects.clear();

        Realm.init(context);
        Realm.getInstance(RealmUtility.getDefaultConfig(context)).executeTransaction(realm -> {
            RealmResults<RealmAudio> realmAudios = realm.where(RealmAudio.class)
                    .distinct("title")
                    .equalTo("instructorcourseid", INSTRUCTORCOURSEID)
                    .isNotNull("audiourl")
                    .notEqualTo("audiourl", "")
                    .sort("id", Sort.DESCENDING)
                    .findAll();
            RealmResults<RealmRecordedVideoStream> realmRecordedVideoStreams = realm.where(RealmRecordedVideoStream.class)
                    .distinct("title")
                    .equalTo("instructorcourseid", INSTRUCTORCOURSEID)
                    .isNotNull("url")
                    .notEqualTo("url", "")
                    .sort("id", Sort.DESCENDING)
                    .findAll();
            RealmResults<RealmRecordedAudioStream> realmRecordedAudioStreams = realm.where(RealmRecordedAudioStream.class)
                        .distinct("title")
                        .equalTo("instructorcourseid", INSTRUCTORCOURSEID)
                        .isNotNull("url")
                        .notEqualTo("url", "")
                        .sort("id", Sort.DESCENDING)
                    .findAll();

            for (RealmAudio realmAudio : realmAudios) {
                newObjects.add(realmAudio);
            }
            for (RealmRecordedAudioStream realmRecordedAudioStream : realmRecordedAudioStreams) {
                if (realm.where(RealmAudio.class)
                        .distinct("title")
                        .equalTo("instructorcourseid", INSTRUCTORCOURSEID)
                        .isNotNull("audiourl")
                        .notEqualTo("url", "")
                        .equalTo("title", realmRecordedAudioStream.getTitle()).findFirst() == null) {
                    newObjects.add(realmRecordedAudioStream);
                }
            }
            for (RealmRecordedVideoStream realmRecordedVideoStream : realmRecordedVideoStreams) {
                if (realm.where(RealmRecordedAudioStream.class)
                        .distinct("title")
                        .equalTo("instructorcourseid", INSTRUCTORCOURSEID)
                        .isNotNull("url")
                        .notEqualTo("url", "")
                        .equalTo("title", realmRecordedVideoStream.getTitle()).findFirst() == null
                        &&
                        realm.where(RealmAudio.class)
                        .distinct("title")
                        .equalTo("instructorcourseid", INSTRUCTORCOURSEID)
                        .isNotNull("audiourl")
                        .notEqualTo("url", "")
                        .equalTo("title", realmRecordedVideoStream.getTitle()).findFirst() == null) {
                    newObjects.add(realmRecordedVideoStream);
                }
            }
            objects.clear();
            objects.addAll(newObjects);

            distinctRecordedStreamAdapter.notifyDataSetChanged();
            if (objects.size() > 0) {
                norecordingtext.setVisibility(View.GONE);
                recyclerview.setVisibility(View.VISIBLE);
            } else {
                norecordingtext.setVisibility(View.VISIBLE);
                recyclerview.setVisibility(View.GONE);
            }
        });
    }

    public void refresh() {
        try {
            dialog = new ProgressDialog(this);
            dialog.setMessage(getString(R.string.refreshing));
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.show();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    API_URL + "recorded-streams-refresh-data",
                    null,
                    response -> {
                        if (response != null) {
                            dialog.dismiss();
                            Realm.init(getApplicationContext());
                            Realm.getInstance(RealmUtility.getDefaultConfig(DistinctRecordedStreamActivity.this)).executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    RealmResults<RealmAudio> realmAudios = realm.where(RealmAudio.class).findAll();
                                    RealmResults<RealmRecordedAudioStream> realmRecordedAudioStreams = realm.where(RealmRecordedAudioStream.class).findAll();
                                    RealmResults<RealmRecordedVideoStream> realmRecordedVideoStreams = realm.where(RealmRecordedVideoStream.class).findAll();

                                realmAudios.deleteAllFromRealm();
                                realmRecordedAudioStreams.deleteAllFromRealm();
                                realmRecordedVideoStreams.deleteAllFromRealm();

                                    try {
                                        realm.createOrUpdateAllFromJson(RealmAudio.class, response.getJSONArray("audios"));
                                        realm.createOrUpdateAllFromJson(RealmRecordedAudioStream.class, response.getJSONArray("recorded_audio_streams"));
                                        realm.createOrUpdateAllFromJson(RealmRecordedVideoStream.class, response.getJSONArray("recorded_video_streams"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            objects.clear();
                            newObjects.clear();
                            populateObjects(getApplicationContext());
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
