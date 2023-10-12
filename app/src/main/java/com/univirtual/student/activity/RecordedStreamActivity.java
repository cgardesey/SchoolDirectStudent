package com.univirtual.student.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.greysonparrelli.permiso.PermisoActivity;
import com.univirtual.student.R;
import com.univirtual.student.adapter.RecordedStreamAdapter;
import com.univirtual.student.other.InitApplication;
import com.univirtual.student.realm.RealmAudio;
import com.univirtual.student.realm.RealmRecordedAudioStream;
import com.univirtual.student.realm.RealmRecordedVideoStream;
import com.univirtual.student.util.RealmUtility;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.univirtual.student.activity.HomeActivity.APITOKEN;
import static com.univirtual.student.activity.VideoActivity.VIDEO_URL;
import static com.univirtual.student.constants.keyConst.API_URL;
import static com.univirtual.student.constants.Const.isNetworkAvailable;
import static com.univirtual.student.constants.Const.myVolleyError;


public class RecordedStreamActivity extends PermisoActivity {

    String TITLE;
    String INSTRUCTORCOURSEID;
    String COURSEPATH;
    String ENROLMENTID;
    String PROFILEIMGURL;
    String ROOMID;
    String NODESERVER;
    String INSTRUCTORNAME;
    String SESSIONID;

    protected static Typeface mTfLight;
    private static final int REQUEST_MEDIA = 1002;
    private ImageView backbtn, refresh;
    Button retrybtn;
    TextView nostream;
    RecyclerView recyclerview;
    RecordedStreamAdapter streamAdapter;
    GridLayoutManager gridLayoutManager;
    TextView titletextview, coursepath;
    ArrayList<Object> objects = new ArrayList<>(), newObjects = new ArrayList<>();
    public static Activity recordedConferenceCallActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String instructorcourseid = getIntent().getStringExtra("INSTRUCTORCOURSEID");
        String coursepathstring = getIntent().getStringExtra("COURSEPATH");
        String enrolmentid = getIntent().getStringExtra("ENROLMENTID");
        String profileimgurl = getIntent().getStringExtra("PROFILEIMGURL");
        String nodeserver = getIntent().getStringExtra("NODESERVER");
        String roomid = getIntent().getStringExtra("ROOMID");
        String title = getIntent().getStringExtra("TITLE");


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
        if (title != null && !title.equals("")) {
            TITLE = title;
        }

        setContentView(R.layout.activity_recorded_videos);
        recyclerview = findViewById(R.id.recyclerview);
        recordedConferenceCallActivity = this;
        nostream = findViewById(R.id.nostream);
        backbtn = findViewById(R.id.search);
        refresh = findViewById(R.id.refresh);
        retrybtn = findViewById(R.id.retrybtn);
        backbtn = findViewById(R.id.backbtn1);
        titletextview = findViewById(R.id.title);


        titletextview.setText(TITLE);

        coursepath = findViewById(R.id.coursepath);

        coursepath.setText(COURSEPATH);

        backbtn.setOnClickListener(new View.OnClickListener() {
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

        streamAdapter = new RecordedStreamAdapter((objects, position, holder) -> {
            if (objects.get(position) instanceof RealmAudio) {
                RealmAudio realmAudio = (RealmAudio) objects.get(position);
                Realm.init(getApplicationContext());
                Realm.getInstance(RealmUtility.getDefaultConfig(RecordedStreamActivity.this)).executeTransaction(realm -> {

                    if (realmAudio.getAudiourl().endsWith(";")) {
                        VIDEO_URL = realmAudio.getAudiourl().substring(0, realmAudio.getAudiourl().length() - 1);
                    } else {
                        VIDEO_URL = realmAudio.getAudiourl();
                    }
                });
            }
            else if (objects.get(position) instanceof RealmRecordedAudioStream) {
                RealmRecordedAudioStream realmRecordedAudioStream = (RealmRecordedAudioStream) objects.get(position);
                Realm.init(getApplicationContext());
                Realm.getInstance(RealmUtility.getDefaultConfig(RecordedStreamActivity.this)).executeTransaction(realm -> {

                    if (realmRecordedAudioStream.getUrl().endsWith(";")) {
                        VIDEO_URL = realmRecordedAudioStream.getUrl().substring(0, realmRecordedAudioStream.getUrl().length() - 1);
                    } else {
                        VIDEO_URL = realmRecordedAudioStream.getUrl();
                    }
                });
            }
            else if (objects.get(position) instanceof RealmRecordedVideoStream) {
                RealmRecordedVideoStream realmRecordedVideoStream = (RealmRecordedVideoStream) objects.get(position);
                Realm.init(getApplicationContext());
                Realm.getInstance(RealmUtility.getDefaultConfig(RecordedStreamActivity.this)).executeTransaction(realm -> {

                    if (realmRecordedVideoStream.getUrl().endsWith(";")) {
                        VIDEO_URL = realmRecordedVideoStream.getUrl().substring(0, realmRecordedVideoStream.getUrl().length() - 1);
                    } else {
                        VIDEO_URL = realmRecordedVideoStream.getUrl();
                    }
                });
            }
            if (isNetworkAvailable(RecordedStreamActivity.this)) {
                startActivity(new Intent(this, VideoActivity.class)
                        .putExtra("INSTRUCTORCOURSEID", INSTRUCTORCOURSEID)
                        .putExtra("COURSEPATH", COURSEPATH)
                        .putExtra("ENROLMENTID", ENROLMENTID)
                        .putExtra("PROFILEIMGURL", PROFILEIMGURL)
                        .putExtra("INSTRUCTORNAME", INSTRUCTORNAME)
                        .putExtra("NODESERVER", NODESERVER)
                        .putExtra("ROOMID", ROOMID)
                );
            } else {
                Toast.makeText(RecordedStreamActivity.this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
            }
        }, RecordedStreamActivity.this, objects, "all");
        if (isTablet(getApplicationContext())) {
            gridLayoutManager = new GridLayoutManager(getApplicationContext(), 4);
        } else {
            gridLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        }
        recyclerview.setLayoutManager(gridLayoutManager);
        recyclerview.setNestedScrollingEnabled(false);
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        recyclerview.setAdapter(streamAdapter);

        populateRecordedStreams(getApplicationContext());
    }

    void populateRecordedStreams(final Context context) {
        newObjects.clear();

        Realm.init(context);
        Realm.getInstance(RealmUtility.getDefaultConfig(context)).executeTransaction(realm -> {
            RealmResults<RealmAudio> realmAudios = realm.where(RealmAudio.class)
                    .equalTo("instructorcourseid", INSTRUCTORCOURSEID)
                    .equalTo("title", TITLE)
                    .isNotNull("audiourl")
                    .notEqualTo("audiourl", "")
                    .sort("id", Sort.DESCENDING)
                    .findAll();
            RealmResults<RealmRecordedVideoStream> realmRecordedVideoStreams = realm.where(RealmRecordedVideoStream.class)
                    .equalTo("instructorcourseid", INSTRUCTORCOURSEID)
                    .equalTo("title", TITLE)
                    .isNotNull("url")
                    .notEqualTo("url", "")
                    .sort("id", Sort.DESCENDING)
                    .findAll();
            RealmResults<RealmRecordedAudioStream> realmRecordedAudioStreams = realm.where(RealmRecordedAudioStream.class)
                    .equalTo("instructorcourseid", INSTRUCTORCOURSEID)
                    .equalTo("title", TITLE)
                    .isNotNull("url")
                    .notEqualTo("url", "")
                    .sort("id", Sort.DESCENDING)
                    .findAll();

            for (RealmAudio realmAudio : realmAudios) {
                newObjects.add(realmAudio);
            }
            for (RealmRecordedAudioStream realmRecordedAudioStream : realmRecordedAudioStreams) {
                newObjects.add(realmRecordedAudioStream);
            }
            for (RealmRecordedVideoStream realmRecordedVideoStream : realmRecordedVideoStreams) {
                newObjects.add(realmRecordedVideoStream);
            }
            objects.clear();
            objects.addAll(newObjects);

            streamAdapter.notifyDataSetChanged();
            if (objects.size() > 0) {
                nostream.setVisibility(View.GONE);
                recyclerview.setVisibility(View.VISIBLE);
            } else {
                nostream.setVisibility(View.VISIBLE);
                recyclerview.setVisibility(View.GONE);
            }
        });
    }

    public void refresh() {
        try {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage("Refreshing...");
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
                            Realm.getInstance(RealmUtility.getDefaultConfig(RecordedStreamActivity.this)).executeTransaction(realm -> {
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
                            });
                            populateRecordedStreams(getApplicationContext());
                            streamAdapter.notifyDataSetChanged();
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
                    +
                            0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(jsonObjectRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isTablet(Context context) {
        boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
        boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
        return (xlarge || large);
    }
}
