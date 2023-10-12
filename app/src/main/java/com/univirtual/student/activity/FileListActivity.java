package com.univirtual.student.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.greysonparrelli.permiso.PermisoActivity;
import com.univirtual.student.R;
import com.univirtual.student.adapter.FileListAdapter;
import com.univirtual.student.other.InitApplication;
import com.univirtual.student.pojo.MyFile;
import com.univirtual.student.realm.RealmAudio;
import com.univirtual.student.realm.RealmClassSessionDoc;
import com.univirtual.student.realm.RealmEnrolment;
import com.univirtual.student.realm.RealmInstructorCourse;
import com.univirtual.student.realm.RealmRecommendedDoc;
import com.univirtual.student.realm.RealmRecordedAudioStream;
import com.univirtual.student.realm.RealmRecordedVideo;
import com.univirtual.student.realm.RealmRecordedVideoStream;
import com.univirtual.student.receiver.NetworkReceiver;
import com.univirtual.student.util.DownloadFilesAsync;
import com.univirtual.student.util.FilenameUtils;
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
import static com.univirtual.student.activity.HomeActivity.getDefaultDialerPackage;
import static com.univirtual.student.constants.keyConst.API_URL;

import static com.univirtual.student.constants.Const.changeDefaultDialer;
import static com.univirtual.student.constants.Const.myVolleyError;
import static com.univirtual.student.constants.Const.showToast;
import static com.univirtual.student.constants.Const.storeFileInRealm;
import static com.univirtual.student.constants.Const.toTitleCase;
import static com.univirtual.student.receiver.NetworkReceiver.activeActivity;

public class FileListActivity extends PermisoActivity {

    String TITLE;
    String INSTRUCTORCOURSEID;
    String COURSEPATH;
    String ENROLMENTID;
    String PROFILEIMGURL;
    String ROOMID;
    String NODESERVER;
    String INSTRUCTORNAME;
    String SESSIONID;

    public static HashMap<String, AsyncTask<String, Integer, String>> downFileAsyncMap = new HashMap<>();
    public static ArrayList<MyFile> myFiles = new ArrayList<>();
    FileListAdapter fileListAdapter;
    RecyclerView recyclerview_files;
    ImageView backbtn, refresh_videos, refresh_docs;
    TextView activitytitle, assignmenttitle, nofile;
    LinearLayout refresh_layout;
    ProgressDialog dialog;
    NetworkReceiver networkReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);

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
        refresh_layout = findViewById(R.id.refresh_layout);

        backbtn.setOnClickListener(v -> finish());
        activitytitle = findViewById(R.id.activitytitle);
        assignmenttitle = findViewById(R.id.assignmenttitle);
        nofile = findViewById(R.id.nodocument);

        refresh_videos = findViewById(R.id.refresh);
        refresh_docs = findViewById(R.id.refresh_docs);

        refresh_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (refresh_videos.getVisibility() == View.VISIBLE) {
                    refreshVideos();
                } else if (refresh_docs.getVisibility() == View.VISIBLE) {
                    refreshDocs();
                }
            }
        });

        activitytitle.setText(getIntent().getStringExtra("activitytitle"));
        assignmenttitle.setText(getIntent().getStringExtra("assignmenttitle"));

        if (getIntent().getStringExtra("assignmenttitle") != null && getIntent().getStringExtra("assignmenttitle").equals(getString(R.string.recordedclassvideos))) {
            refresh_videos.setVisibility(View.VISIBLE);
            refresh_docs.setVisibility(View.GONE);
        } else if (getIntent().getStringExtra("assignmenttitle") != null && getIntent().getStringExtra("assignmenttitle").equals(getString(R.string.classdocs))) {
            refresh_videos.setVisibility(View.GONE);
            refresh_docs.setVisibility(View.VISIBLE);
            populateDocs();
        } else {
            refresh_docs.setVisibility(View.GONE);
            refresh_videos.setVisibility(View.GONE);
        }

        if (getIntent().getBooleanExtra("LAUNCHED_FROM_NOTIFICATION", false)) {
//            makeAppDefaultCallingApp();
        }

        fileListAdapter = new FileListAdapter((myFiles, position, holder) -> {
            MyFile myFile = myFiles.get(position);
            File file = new File(myFile.getPath());
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
                                Realm.init(FileListActivity.this);
                                Realm.getInstance(RealmUtility.getDefaultConfig(FileListActivity.this)).executeTransaction(new Realm.Transaction() {
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
                                    filepathMap.put(myFile.getUrl(), myFile.getPath());
                                    downFileAsyncMap.put(myFile.getUrl(), new DownloadFilesAsync(file_url -> {
                                        if (file_url != null) {
                                            Log.d("981sdfs", file_url);
                                            holder.downloadStatusWrapper.setVisibility(View.VISIBLE);
                                            holder.pbar.setVisibility(View.GONE);
                                            holder.download.setVisibility(View.VISIBLE);

                                            if (file_url.contains("java.io.FileNotFoundException")) {
                                                Log.d("ds90", file_url);
                                                new AlertDialog.Builder(FileListActivity.this)
                                                        .setTitle(toTitleCase(getApplicationContext().getString(R.string.download_failed)))
                                                        .setMessage(FileListActivity.this.getString(R.string.file_no_longer_available_for_download))

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
                                                    storeFileInRealm(FileListActivity.this, myFile.getUrl(), myFile.getPath());
                                                    new File(myFile.getPath()).delete();
                                                    fileListAdapter.notifyDataSetChanged();
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
                                                    AsyncTask<String, Integer, String> downloadFileAsync = (AsyncTask<String, Integer, String>) downFileAsyncMap.get(myFile.getUrl());
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
                                                    showToast(FileListActivity.this, "Download cancelled.");
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
            ) {
                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
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
        }, myFiles);

        recyclerview_files.setLayoutManager(new LinearLayoutManager(this));
        recyclerview_files.setNestedScrollingEnabled(false);
        recyclerview_files.setItemAnimator(new DefaultItemAnimator());
        recyclerview_files.setAdapter(fileListAdapter);

        ShowNoFileAvailableMsg();

        if (refresh_videos.getVisibility() == View.VISIBLE) {
            populateExternalLinks();
        } else if (refresh_docs.getVisibility() == View.VISIBLE) {
            populateDocs();
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

    @Override

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getStringExtra("assignmenttitle") != null && intent.getStringExtra("assignmenttitle").equals(getString(R.string.recordedclassvideos))) {
            refresh_videos.setVisibility(View.VISIBLE);
            refresh_docs.setVisibility(View.GONE);
        } else if (intent.getStringExtra("assignmenttitle") != null && intent.getStringExtra("assignmenttitle").equals(getString(R.string.classdocs))) {
            refresh_videos.setVisibility(View.GONE);
            refresh_docs.setVisibility(View.VISIBLE);
        }

        if (intent.getBooleanExtra("LAUNCHED_FROM_NOTIFICATION", false)) {
//            makeAppDefaultCallingApp();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setSchoolDirectStudentADefaultCallingApp() {
        if (!getDefaultDialerPackage(getApplicationContext()).equals(getPackageName())) {
            changeDefaultDialer(FileListActivity.this, getPackageName());
        }
    }

    public void refreshVideos() {
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
                            Realm.getInstance(RealmUtility.getDefaultConfig(FileListActivity.this)).executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    RealmResults<RealmRecordedVideo> result = realm.where(RealmRecordedVideo.class).findAll();
                                    result.deleteAllFromRealm();
                                    realm.createOrUpdateAllFromJson(RealmRecordedVideo.class, response);
                                }
                            });
                            populateExternalLinks();
                            fileListAdapter.notifyDataSetChanged();
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

    public void refreshDocs() {
        try {
            dialog = new ProgressDialog(this);
            dialog.setMessage(getString(R.string.refreshing));
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.show();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    API_URL + "docs-refresh-data",
                    null,
                    response -> {
                        if (response != null) {
                            dialog.dismiss();
                            Realm.init(getApplicationContext());
                            Realm.getInstance(RealmUtility.getDefaultConfig(FileListActivity.this)).executeTransaction(realm -> {
                                RealmResults<RealmAudio> audios = realm.where(RealmAudio.class).findAll();
                                RealmResults<RealmRecordedAudioStream> recordedAudioStreams = realm.where(RealmRecordedAudioStream.class).findAll();
                                RealmResults<RealmRecordedVideoStream> recordedVideoStreams = realm.where(RealmRecordedVideoStream.class).findAll();
                                RealmResults<RealmRecommendedDoc> realmRecommendedDocs = realm.where(RealmRecommendedDoc.class).findAll();
                                RealmResults<RealmClassSessionDoc> classSessionDocs = realm.where(RealmClassSessionDoc.class).findAll();


                                audios.deleteAllFromRealm();
                                recordedAudioStreams.deleteAllFromRealm();
                                recordedVideoStreams.deleteAllFromRealm();
                                realmRecommendedDocs.deleteAllFromRealm();
                                classSessionDocs.deleteAllFromRealm();
                                try {
                                    realm.createOrUpdateAllFromJson(RealmAudio.class, response.getJSONArray("audios"));
                                    realm.createOrUpdateAllFromJson(RealmRecordedAudioStream.class, response.getJSONArray("recorded_audio_strems"));
                                    realm.createOrUpdateAllFromJson(RealmRecordedVideoStream.class, response.getJSONArray("recorded_video_strems"));
                                    realm.createOrUpdateAllFromJson(RealmRecommendedDoc.class, response.getJSONArray("recommended_docs"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            });
                            populateDocs();
                            fileListAdapter.notifyDataSetChanged();
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

    private void ShowNoFileAvailableMsg() {
        if (myFiles.size() > 0) {
            nofile.setVisibility(View.GONE);
            recyclerview_files.setVisibility(View.VISIBLE);
        } else {
            nofile.setVisibility(View.VISIBLE);
            recyclerview_files.setVisibility(View.GONE);
        }
    }

    public void populateDocs() {
        myFiles.clear();

        Realm.init(getApplicationContext());
        Realm.getInstance(RealmUtility.getDefaultConfig(getApplicationContext())).executeTransaction(realm -> {

            RealmResults<RealmAudio> realmAudios = realm.where(RealmAudio.class)
                    .isNotNull("url")
                    .notEqualTo("url", "")
                    .distinct("url")
                    .equalTo("instructorcourseid", INSTRUCTORCOURSEID)
                    .sort("id", Sort.DESCENDING)
                    .findAll();

            RealmResults<RealmRecordedAudioStream> realmRecordedAudioStreams = realm.where(RealmRecordedAudioStream.class)
                    .isNotNull("docurl")
                    .notEqualTo("docurl", "")
                    .distinct("docurl")
                    .equalTo("instructorcourseid", INSTRUCTORCOURSEID)
                    .sort("id", Sort.DESCENDING)
                    .findAll();

            RealmResults<RealmRecordedVideoStream> realmRecordedVideoStreams = realm.where(RealmRecordedVideoStream.class)
                    .isNotNull("docurl")
                    .notEqualTo("docurl", "")
                    .distinct("docurl")
                    .equalTo("instructorcourseid", INSTRUCTORCOURSEID)
                    .sort("id", Sort.DESCENDING)
                    .findAll();

            RealmResults<RealmRecommendedDoc> realmRecommendedDocs = realm.where(RealmRecommendedDoc.class)
                    .isNotNull("url")
                    .notEqualTo("url", "")
                    .distinct("url")
                    .equalTo("instructorcourseid", INSTRUCTORCOURSEID)
                    .sort("id", Sort.DESCENDING)
                    .findAll();

            for (RealmAudio realmAudio : realmAudios) {
                RealmClassSessionDoc realmClassSessionDoc = new RealmClassSessionDoc(realmAudio.getUrl(), INSTRUCTORCOURSEID);
                realmClassSessionDoc.setSessionid(realmAudio.getSessionid());
                realm.copyToRealmOrUpdate(realmClassSessionDoc);
            }

            for (RealmRecordedAudioStream recordedAudioStream : realmRecordedAudioStreams) {
                RealmClassSessionDoc realmClassSessionDoc = new RealmClassSessionDoc(recordedAudioStream.getDocurl(), INSTRUCTORCOURSEID);
                realmClassSessionDoc.setSessionid(recordedAudioStream.getSessionid());
                realm.copyToRealmOrUpdate(realmClassSessionDoc);
            }

            for (RealmRecordedVideoStream recordedVideoStream : realmRecordedVideoStreams) {
                RealmClassSessionDoc realmClassSessionDoc = new RealmClassSessionDoc(recordedVideoStream.getDocurl(), INSTRUCTORCOURSEID);
                realmClassSessionDoc.setSessionid(recordedVideoStream.getSessionid());
                realm.copyToRealmOrUpdate(realmClassSessionDoc);
            }

            for (RealmRecommendedDoc realmRecommendedDoc : realmRecommendedDocs) {
                RealmClassSessionDoc realmClassSessionDoc = new RealmClassSessionDoc(realmRecommendedDoc.getUrl(), INSTRUCTORCOURSEID);
                realm.copyToRealmOrUpdate(realmClassSessionDoc);
            }

            RealmResults<RealmClassSessionDoc> classSessionDocs = realm.where(RealmClassSessionDoc.class)
                    .equalTo("instructorcourseid", INSTRUCTORCOURSEID)
                    .findAll();

            for (RealmClassSessionDoc classSessionDoc : classSessionDocs) {
                if (classSessionDoc.getSessionid() == null || classSessionDoc.getSessionid() == "") {
                    myFiles.add(new MyFile(getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Recommended-documents/" + URLUtil.guessFileName(classSessionDoc.getUrl(), null, null),
                            classSessionDoc.getUrl(),
                            null));
                } else {
                    myFiles.add(new MyFile(getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Class-sessions/Documents/" + URLUtil.guessFileName(classSessionDoc.getUrl(), null, null),
                            classSessionDoc.getUrl(),
                            null));
                }
            }
        });
    }

    public void populateExternalLinks() {
        myFiles.clear();

        Realm.init(getApplicationContext());
        Realm.getInstance(RealmUtility.getDefaultConfig(getApplicationContext())).executeTransaction(realm -> {
            RealmResults<RealmRecordedVideo> realmRecordedVideos = realm.where(RealmRecordedVideo.class)
                    .equalTo("instructorcourseid", INSTRUCTORCOURSEID)
                    .equalTo("source", "schooldirect")
                    .equalTo("active", 1)
                    .sort("id", Sort.DESCENDING)
                    .findAll();

            for (RealmRecordedVideo realmRecordedVideo : realmRecordedVideos) {
                myFiles.add(new MyFile(getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Videos/" + INSTRUCTORCOURSEID + "/" + FilenameUtils.getName(realmRecordedVideo.getUrl()),
                        realmRecordedVideo.getUrl(),
                        realmRecordedVideo.getGiflink()
                ));
            }
        });
    }
}
