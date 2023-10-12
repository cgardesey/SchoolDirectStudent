package com.univirtual.student.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.greysonparrelli.permiso.PermisoActivity;
import com.univirtual.student.R;
import com.univirtual.student.adapter.RecordedVideosAdapter;
import com.univirtual.student.other.InitApplication;
import com.univirtual.student.realm.RealmEnrolment;
import com.univirtual.student.realm.RealmInstructorCourse;
import com.univirtual.student.realm.RealmRecordedVideo;
import com.univirtual.student.receiver.NetworkReceiver;
import com.univirtual.student.util.DownloadFilesAsync;
import com.univirtual.student.util.RealmUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import static com.univirtual.student.activity.HomeActivity.APITOKEN;
import static com.univirtual.student.constants.keyConst.API_URL;
import static com.univirtual.student.constants.Const.myVolleyError;
import static com.univirtual.student.constants.Const.showToast;
import static com.univirtual.student.constants.Const.storeFileInRealm;
import static com.univirtual.student.constants.Const.toTitleCase;
import static com.univirtual.student.receiver.NetworkReceiver.activeActivity;

public class RecordedVideosActivity extends PermisoActivity {

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
    TextView norecordedvideos, classtitle, coursepath;
    RecyclerView recyclerview;
    RecordedVideosAdapter recordedVideosAdapter;
    GridLayoutManager gridLayoutManager;
    ArrayList<RealmRecordedVideo> realmRecordedVideos = new ArrayList<>(), newRealmRecordedVideos = new ArrayList<>();
    public static Activity recordedVideosActivity;
    public static RealmRecordedVideo clickedRealmRecordedVideo;
    NetworkReceiver networkReceiver;

    public static HashMap<String, AsyncTask<String, Integer, String>> downFileAsyncMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        setContentView(R.layout.activity_recorded_videos);
        recyclerview = findViewById(R.id.recyclerview);
        recordedVideosActivity = this;
        norecordedvideos = findViewById(R.id.nostream);
        backbtn = findViewById(R.id.search);
        refresh = findViewById(R.id.refresh);
        retrybtn = findViewById(R.id.retrybtn);
        backbtn = findViewById(R.id.backbtn1);

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

        recordedVideosAdapter = new RecordedVideosAdapter((realmRecordedVideos, position, holder) -> {
            RealmRecordedVideo realmRecordedVideo = realmRecordedVideos.get(position);
            final String videoPath = getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Recorded-videos/" + URLUtil.guessFileName(realmRecordedVideo.getUrl(), null, null);
            File file = new File(videoPath);
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            holder.downloadStatusWrapper.setVisibility(View.VISIBLE);
            holder.pbar.setVisibility(View.VISIBLE);
            holder.download.setVisibility(View.GONE);
            holder.pbar.animate();
//                                                                                                                                  Toast.makeText(FileListActivity.this, getString(R.string.downloading), Toast.LENGTH_SHORT).show();



            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    API_URL + "check-subscription",
                    response -> {
                        if (response != null) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                Realm.init(RecordedVideosActivity.this);
                                Realm.getInstance(RealmUtility.getDefaultConfig(RecordedVideosActivity.this)).executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        try {
                                            realm.createOrUpdateObjectFromJson(RealmInstructorCourse.class, jsonObject.getJSONObject("intructorcourse"));
                                            realm.createOrUpdateObjectFromJson(RealmEnrolment.class, jsonObject.getJSONObject("enrolment"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                if (jsonObject.getBoolean("eligible")) {
                                    Map<String, String> filepathMap = new HashMap<>();
                                    filepathMap.put(realmRecordedVideo.getUrl(), videoPath);
                                    downFileAsyncMap.put(realmRecordedVideo.getUrl(), new DownloadFilesAsync(file_url -> {
                                        if (file_url != null) {
                                            Log.d("981sdfs", file_url);
                                            holder.downloadStatusWrapper.setVisibility(View.VISIBLE);
                                            holder.pbar.setVisibility(View.GONE);
                                            holder.download.setVisibility(View.VISIBLE);

                                            if (file_url.contains("java.io.FileNotFoundException")) {
                                                Log.d("ds90", file_url);
                                                new AlertDialog.Builder(RecordedVideosActivity.this)
                                                        .setTitle(toTitleCase(getApplicationContext().getString(R.string.download_failed)))
                                                        .setMessage(RecordedVideosActivity.this.getString(R.string.file_no_longer_available_for_download))

                                                        // Specifying a listener allows you to take an action before dismissing the dialog.
                                                        // The dialog is automatically dismissed when a dialog button is clicked.
                                                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {

                                                        })
                                                        .show();

                                            } else {
                                                Toast.makeText(getApplicationContext(), "Error occured.", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            try {
                                                try {
                                                    storeFileInRealm(RecordedVideosActivity.this, realmRecordedVideo.getUrl(), videoPath);
                                                    new File(videoPath).delete();
                                                    recordedVideosAdapter.notifyDataSetChanged();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                holder.downloadStatusWrapper.setVisibility(View.GONE);
                                                holder.removelayout.setVisibility(View.VISIBLE);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    },
                                            new DownloadFilesAsync.OnTaskProgressUpdateInterface() {
                                                @Override
                                                public void onTaskProgressUpdate(Integer[] progress) {
                                                    AsyncTask<String, Integer, String> downloadFileAsync = (AsyncTask<String, Integer, String>) downFileAsyncMap.get(realmRecordedVideo.getUrl());
                                                    boolean fileDownloading = downloadFileAsync != null && downloadFileAsync.getStatus() != AsyncTask.Status.FINISHED;
                                                    if (fileDownloading) {
                                                        holder.downloadStatusWrapper.setVisibility(View.VISIBLE);
                                                        holder.pbar.setVisibility(View.VISIBLE);
                                                        holder.download.setVisibility(View.GONE);
                                                        holder.pbar.animate();
                                                    }
                                                }
                                            },
                                            new DownloadFilesAsync.OnTaskCancelledInterface() {
                                                @Override
                                                public void onTaskCancelled() {
                                                        showToast(RecordedVideosActivity.this, "Download cancelled.");
                                                    holder.pbar.setVisibility(View.GONE);
                                                    holder.download.setVisibility(View.VISIBLE);

                                                    Iterator it = filepathMap.entrySet().iterator();
                                                    while (it.hasNext()) {
                                                        Map.Entry pair = (Map.Entry) it.next();

                                                        File file = new File((String) pair.getValue());
                                                        if (file.exists()) {
                                                            file.delete();
                                                        }

                                                        it.remove(); // avoids a ConcurrentModificationException
                                                    }
                                                }
                                            }, filepathMap).execute());
                                } else {
                                    holder.pbar.setVisibility(View.GONE);
                                    holder.download.setVisibility(View.VISIBLE);
                                    Toast.makeText(getApplicationContext(), getString(R.string.your_subscription_has_expired), Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        holder.pbar.setVisibility(View.GONE);
                        holder.download.setVisibility(View.VISIBLE);
                        myVolleyError(getApplicationContext(), error);
                    }
            )
            {
                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<>();
                    params.put("enrolmentid", ENROLMENTID);
                    params.put("instructorcourseid", INSTRUCTORCOURSEID);
                    return params;
                }

                @Override
                public Map getHeaders() throws AuthFailureError {
                    HashMap headers = new HashMap();
                    headers.put("accept", "application/json");
                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(APITOKEN, ""));
                    return headers;
                }
            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(stringRequest);
        }, RecordedVideosActivity.this, realmRecordedVideos);
        gridLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        recyclerview.setLayoutManager(gridLayoutManager);
        recyclerview.setNestedScrollingEnabled(false);
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        recyclerview.setAdapter(recordedVideosAdapter);

        populateRecordedVideos(getApplicationContext());
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

    @Override
    protected void onStop() {
        for (Map.Entry me : downFileAsyncMap.entrySet()) {
            AsyncTask<String, Integer, String> downloadFileAsync = (AsyncTask<String, Integer, String>) me.getValue();
            if (downloadFileAsync != null && downloadFileAsync.getStatus() != AsyncTask.Status.FINISHED) {
                // This would not cancel downloading from httpClient
                //  we have do handle that manually in onCancelled event inside AsyncTask
                downloadFileAsync.cancel(true);
            }
        }
        super.onStop();
    }

    void populateRecordedVideos(final Context context) {
        Realm.init(context);
        Realm.getInstance(RealmUtility.getDefaultConfig(context)).executeTransaction(realm -> {
            RealmResults<RealmRecordedVideo> results = realm.where(RealmRecordedVideo.class)
                    .equalTo("instructorcourseid", INSTRUCTORCOURSEID)
                    .equalTo("source", "schooldirect")
//                    .equalTo("active", 1)
                    .sort("id", Sort.DESCENDING)
                    .findAll();
            newRealmRecordedVideos.clear();
            for (RealmRecordedVideo realmRecordedVideo : results) {
                newRealmRecordedVideos.add(realmRecordedVideo);
            }
            realmRecordedVideos.clear();
            realmRecordedVideos.addAll(newRealmRecordedVideos);
            recordedVideosAdapter.notifyDataSetChanged();
            if (realmRecordedVideos.size() > 0) {
                norecordedvideos.setVisibility(View.GONE);
                recyclerview.setVisibility(View.VISIBLE);
            }
            else {
                norecordedvideos.setVisibility(View.VISIBLE);
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
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                    Request.Method.GET,
                    API_URL + "recorded-videos",
                    null,
                    response -> {
                        if (response != null) {
                            dialog.dismiss();
                            Realm.init(getApplicationContext());
                            Realm.getInstance(RealmUtility.getDefaultConfig(RecordedVideosActivity.this)).executeTransaction(realm -> {
                                RealmResults<RealmRecordedVideo> result = realm.where(RealmRecordedVideo.class).findAll();
                                result.deleteAllFromRealm();
                                realm.createOrUpdateAllFromJson(RealmRecordedVideo.class, response);
                            });
                            populateRecordedVideos(getApplicationContext());
                            recordedVideosAdapter.notifyDataSetChanged();
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
