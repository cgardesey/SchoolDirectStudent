package com.univirtual.student.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.greysonparrelli.permiso.PermisoActivity;
import com.univirtual.student.R;
import com.univirtual.student.adapter.ExternalLinksAdapter;
import com.univirtual.student.other.InitApplication;
import com.univirtual.student.realm.RealmRecordedVideo;
import com.univirtual.student.receiver.NetworkReceiver;
import com.univirtual.student.util.RealmUtility;

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

public class ExternalLinksActivity extends PermisoActivity {

    String TITLE;
    String INSTRUCTORCOURSEID;
    String COURSEPATH;
    String ENROLMENTID;
    String PROFILEIMGURL;
    String ROOMID;
    String NODESERVER;
    String INSTRUCTORNAME;
    String SESSIONID;

    private HashMap<String, AsyncTask<String, Integer, String>> downFileAsyncMap = new HashMap<>();
    public static ArrayList<RealmRecordedVideo> externalLinks = new ArrayList<>();
    ExternalLinksAdapter externalLinksAdapter;
    RecyclerView recyclerview_files;
    ImageView backbtn, refresh;
    TextView coursepath, nofile;
    ProgressDialog dialog;
    NetworkReceiver networkReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_external_links);

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

        recyclerview_files = findViewById(R.id.recyclerview_files);
        backbtn = findViewById(R.id.search);
        refresh = findViewById(R.id.refresh);

        backbtn.setOnClickListener(v -> finish());
        coursepath = findViewById(R.id.coursepath);
        nofile = findViewById(R.id.nodocument);

        refresh = findViewById(R.id.refresh);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh();
            }
        });

        coursepath.setText(COURSEPATH);

        if (getIntent().getBooleanExtra("LAUNCHED_FROM_NOTIFICATION", false)) {
//            makeAppDefaultCallingApp();
        }

        externalLinksAdapter = new ExternalLinksAdapter((externalLinks, position, holder) -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(externalLinks.get(position).getUrl()));
            startActivity(i);
        }, externalLinks);

        recyclerview_files.setLayoutManager(new LinearLayoutManager(this));
        recyclerview_files.setNestedScrollingEnabled(false);
        recyclerview_files.setItemAnimator(new DefaultItemAnimator());
        recyclerview_files.setAdapter(externalLinksAdapter);

        populateExternallinks(this);
        externalLinksAdapter.notifyDataSetChanged();
        ShowNoFileAvailableMsg();
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
            dialog.setMessage(getString(R.string.checking_for_new_videos));
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.show();
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                    Request.Method.GET,
                    API_URL + "recorded-videos",
                    null,
                    response -> {
                        if (response != null) {
                            dialog.dismiss();
                            Realm.init(getApplicationContext());
                            Realm.getInstance(RealmUtility.getDefaultConfig(ExternalLinksActivity.this)).executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    RealmResults<RealmRecordedVideo> result = realm.where(RealmRecordedVideo.class).findAll();
                                    result.deleteAllFromRealm();
                                    realm.createOrUpdateAllFromJson(RealmRecordedVideo.class, response);
                                }
                            });
                            populateExternallinks(this);
                            externalLinksAdapter.notifyDataSetChanged();
                            ShowNoFileAvailableMsg();
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

    private void ShowNoFileAvailableMsg() {
        if (externalLinks.size() > 0) {
            nofile.setVisibility(View.GONE);
            recyclerview_files.setVisibility(View.VISIBLE);
        }
        else {
            nofile.setVisibility(View.VISIBLE);
            recyclerview_files.setVisibility(View.GONE);
        }
    }

    public void populateExternallinks(Activity activity) {
        externalLinks.clear();

        Realm.init(activity);
        Realm.getInstance(RealmUtility.getDefaultConfig(activity)).executeTransaction(realm -> {
            RealmResults<RealmRecordedVideo> realmRecordedVideos = realm.where(RealmRecordedVideo.class)
                    .equalTo("instructorcourseid", INSTRUCTORCOURSEID)
                    .notEqualTo("source", "schooldirect")
//                    .equalTo("active", 1)
                    .sort("id", Sort.DESCENDING)
                    .findAll();

            for (RealmRecordedVideo externallink : realmRecordedVideos) {
                externalLinks.add(externallink);
            }
        });
    }
}
