package com.univirtual.student.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.univirtual.student.R;
import com.univirtual.student.adapter.ClassReplayAdapter;
import com.univirtual.student.other.InitApplication;
import com.univirtual.student.realm.RealmAudio;
import com.univirtual.student.realm.RealmBase64File;
import com.univirtual.student.realm.RealmEnrolment;
import com.univirtual.student.realm.RealmInstructorCourse;
import com.univirtual.student.realm.RealmRecordedAudioStream;
import com.univirtual.student.realm.RealmRecordedVideoStream;
import com.univirtual.student.receiver.NetworkReceiver;
import com.univirtual.student.util.DownloadFilesAsync;
import com.univirtual.student.util.RealmUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.univirtual.student.activity.HomeActivity.APITOKEN;
import static com.univirtual.student.constants.keyConst.API_URL;
import static com.univirtual.student.constants.Const.isNetworkAvailable;
import static com.univirtual.student.constants.Const.myVolleyError;
import static com.univirtual.student.constants.Const.showToast;
import static com.univirtual.student.constants.Const.storeFileInRealm;
import static com.univirtual.student.constants.Const.toTitleCase;
import static com.univirtual.student.constants.Const.writeToFile;
import static com.univirtual.student.receiver.NetworkReceiver.activeActivity;

public class ClassReplaysActivity extends AppCompatActivity {

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
    RecyclerView recyclerview;
    ImageView backbtn, refresh;
    ArrayList<Object> objects = new ArrayList<>(), newObjects = new ArrayList<>();
    TextView norecordingtext, titletextview;
    TextView coursepath;
    static public ProgressDialog mProgress;
    ClassReplayAdapter classReplayAdapter;
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

        networkReceiver = new NetworkReceiver();

        /*String[] split = splitStringEvery("12345", 1);

        Realm.init(ClassReplaysActivity.this);
        Realm.getInstance(RealmUtility.getDefaultConfig(ClassReplaysActivity.this)).executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (int i = 0; i < split.length; i++) {
                    realm.copyToRealmOrUpdate(new RealmBase64File(UUID.randomUUID().toString(), i, "test", split[i]));
                }
                RealmResults<RealmBase64File> realmBase64Files = realm.where(RealmBase64File.class).equalTo("url", "test").findAll().sort("id");
                for (RealmBase64File realmBase64File : realmBase64Files) {
                    Log.d("fds43", realmBase64File.getBase64String());
                    Toast.makeText(ClassReplaysActivity.this, realmBase64File.getBase64String(), Toast.LENGTH_SHORT).show();
                }
            }
        });*/

        mProgress = new ProgressDialog(this);
        mProgress.setMessage(getString(R.string.pls_wait));
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
        refresh = findViewById(R.id.refresh);
        norecordingtext = findViewById(R.id.norecordingtext);

        coursepath = findViewById(R.id.coursepath);
        titletextview = findViewById(R.id.title);

        titletextview.setText(TITLE);

        coursepath.setText(COURSEPATH);

        refresh.setOnClickListener(v -> refresh());

        backbtn = findViewById(R.id.search);

        backbtn.setOnClickListener(view -> {
            clickview(view);
            finish();
        });

        recyclerview = findViewById(R.id.recyclerview);
        classReplayAdapter = new ClassReplayAdapter((objects, position, holder) -> {
            if (objects.get(position) instanceof RealmAudio) {
                RealmAudio realmAudio = (RealmAudio) objects.get(position);
                downloadUndownloadedFiles(holder, realmAudio.getAudioid(), realmAudio.getAudiourl(), realmAudio.getUrl(), realmAudio.getSessionid());
            } else if (objects.get(position) instanceof RealmRecordedAudioStream) {
                RealmRecordedAudioStream realmRecordedAudioStream = (RealmRecordedAudioStream) objects.get(position);
                downloadUndownloadedFiles(holder, realmRecordedAudioStream.getRecordedaudiostreamid(), realmRecordedAudioStream.getUrl(), realmRecordedAudioStream.getDocurl(), realmRecordedAudioStream.getSessionid());
            } else if (objects.get(position) instanceof RealmRecordedVideoStream) {
                RealmRecordedVideoStream realmRecordedVideoStream = (RealmRecordedVideoStream) objects.get(position);
                downloadUndownloadedFiles(holder, realmRecordedVideoStream.getRecordedvideostreamid(), realmRecordedVideoStream.getUrl(), realmRecordedVideoStream.getDocurl(), realmRecordedVideoStream.getSessionid());
            }
        }, ClassReplaysActivity.this, objects);
        recyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerview.setNestedScrollingEnabled(false);
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        recyclerview.setAdapter(classReplayAdapter);

        populateRecordedStreams(getApplicationContext());
    }

    private void downloadUndownloadedFiles(ClassReplayAdapter.ViewHolder holder, String recordedResourceId, String audioUrl, String pdfs, String sessionid) {
        if (!isNetworkAvailable(ClassReplaysActivity.this)) {
            Toast.makeText(this, "Connection error.", Toast.LENGTH_SHORT).show();
            return;
        }
        holder.downloadStatusWrapper.setVisibility(View.VISIBLE);
        holder.pbar.setVisibility(View.VISIBLE);
        holder.download.setVisibility(View.GONE);
        holder.pbar.animate();
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                API_URL + "check-subscription",
                response -> {
                    if (response != null) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Realm.init(ClassReplaysActivity.this);
                            Realm.getInstance(RealmUtility.getDefaultConfig(ClassReplaysActivity.this)).executeTransaction(new Realm.Transaction() {
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

                                JSONArray pdfpaths = new JSONArray();
                                final boolean[] coordinatesStored = new boolean[1];
                                final String[] coordinatesFilePah = new String[1];
                                Realm.init(ClassReplaysActivity.this);
                                Realm.getInstance(RealmUtility.getDefaultConfig(ClassReplaysActivity.this)).executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        if (realm.where(RealmBase64File.class).equalTo("url", audioUrl).findFirst() == null) {
                                            File audioFile = new File(getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Class-sessions/Documents/" + URLUtil.guessFileName(audioUrl, null, null));
                                            filepathMap.put(audioUrl, audioFile.getAbsolutePath());
                                            pdfpaths.put(audioUrl);
                                        }
                                        coordinatesFilePah[0] = getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Class-sessions/Documents/" + URLUtil.guessFileName(audioUrl, null, null).replace(".mp4", ".txt");
                                        coordinatesStored[0] = realm.where(RealmBase64File.class).equalTo("url", audioUrl.replace(".mp4", ".txt")).findFirst() != null;
                                        if (pdfs != null && !pdfs.equals("")) {
                                            for (String pdf : pdfs.split(";")) {
                                                if (realm.where(RealmBase64File.class).equalTo("url", pdf).findFirst() == null) {
                                                    File pdfFile = new File(getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Class-sessions/Documents/" + URLUtil.guessFileName(pdf, null, null));
                                                    filepathMap.put(pdf, pdfFile.getAbsolutePath());
                                                    pdfpaths.put(pdf);
                                                }
                                            }
                                        }
                                    }
                                });

                                if (filepathMap.size() > 0) {
                                    downFileAsyncMap.put(recordedResourceId, new DownloadFilesAsync(file_url -> {
                                        if (file_url != null) {
                                            Log.d("981sdfs", file_url);
                                            holder.downloadStatusWrapper.setVisibility(View.VISIBLE);
                                            holder.pbar.setVisibility(View.GONE);
                                            holder.download.setVisibility(View.VISIBLE);

                                            if (file_url.contains("java.io.FileNotFoundException")) {
                                                Log.d("ds90", file_url);
                                                new AlertDialog.Builder(ClassReplaysActivity.this)
                                                        .setTitle(toTitleCase(getApplicationContext().getString(R.string.download_failed)))
                                                        .setMessage(ClassReplaysActivity.this.getString(R.string.file_no_longer_available_for_download))

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
                                                for (int i = 0; i < pdfpaths.length(); i++) {
                                                    storeFileInRealm(ClassReplaysActivity.this, pdfpaths.getString(i), getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Class-sessions/Documents/" + URLUtil.guessFileName(pdfpaths.getString(i), null, null));
                                                    new File(getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Class-sessions/Documents/" + URLUtil.guessFileName(pdfpaths.getString(i), null, null)).delete();
                                                }
                                                if (!coordinatesStored[0]) {
                                                    StringRequest drawingCoordinatestringRequest = new StringRequest(
                                                            Request.Method.POST,
                                                            API_URL + "session-drawing-coordinates",
                                                            new Response.Listener<String>() {
                                                                @Override
                                                                public void onResponse(String response) {
                                                                    if (response != null) {
                                                                        holder.downloadStatusWrapper.setVisibility(View.GONE);
                                                                        holder.removelayout.setVisibility(View.VISIBLE);
                                                                        try {
                                                                            writeToFile(response, coordinatesFilePah[0]);
                                                                            storeFileInRealm(ClassReplaysActivity.this, audioUrl.replace(".mp4", ".txt"), coordinatesFilePah[0]);
                                                                            new File(coordinatesFilePah[0]).delete();
                                                                            classReplayAdapter.notifyDataSetChanged();
                                                                        } catch (FileNotFoundException e) {
                                                                            e.printStackTrace();
                                                                        } catch (UnsupportedEncodingException e) {
                                                                            e.printStackTrace();
                                                                        } catch (Exception e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                    }
                                                                }
                                                            },
                                                            new Response.ErrorListener() {
                                                                @Override
                                                                public void onErrorResponse(VolleyError error) {
                                                                    holder.downloadStatusWrapper.setVisibility(View.VISIBLE);
                                                                    holder.removelayout.setVisibility(View.GONE);
                                                                    holder.pbar.setVisibility(View.GONE);
                                                                    holder.download.setVisibility(View.VISIBLE);
//                                                myVolleyError(ClassReplaysActivity.this, error);
                                                                }
                                                            }
                                                    ) {
                                                        @Override
                                                        public Map<String, String> getParams() throws AuthFailureError {
                                                            Map<String, String> params = new HashMap<>();
                                                            params.put("sessionid", sessionid);
                                                            return params;
                                                        }

                                                        @Override
                                                        public Map getHeaders() throws AuthFailureError {
                                                            HashMap headers = new HashMap();
                                                            headers.put("accept", "application/json");
                                                            headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(ClassReplaysActivity.this).getString(APITOKEN, ""));
                                                            return headers;
                                                        }
                                                    };
                                                    drawingCoordinatestringRequest.setRetryPolicy(new DefaultRetryPolicy(
                                                            0,
                                                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                                    InitApplication.getInstance().addToRequestQueue(drawingCoordinatestringRequest);
                                                }
                                                else {
                                                    holder.downloadStatusWrapper.setVisibility(View.GONE);
                                                    holder.removelayout.setVisibility(View.VISIBLE);
                                                    classReplayAdapter.notifyDataSetChanged();
                                                }

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    },
                                            new DownloadFilesAsync.OnTaskProgressUpdateInterface() {
                                                @Override
                                                public void onTaskProgressUpdate(Integer[] progress) {
                                                    AsyncTask<String, Integer, String> downloadFileAsync = (AsyncTask<String, Integer, String>) downFileAsyncMap.get(recordedResourceId);
                                                    boolean fileDownloading = downloadFileAsync != null && downloadFileAsync.getStatus() != AsyncTask.Status.FINISHED;
                                                    /*if (fileDownloading) {
                                                        holder.downloadStatusWrapper.setVisibility(View.VISIBLE);
                                                        holder.pbar.setVisibility(View.VISIBLE);
                                                        holder.download.setVisibility(View.GONE);
                                                        holder.pbar.animate();
                                                    }*/
                                                }
                                            },
                                            new DownloadFilesAsync.OnTaskCancelledInterface() {
                                                @Override
                                                public void onTaskCancelled() {
                                                    showToast(ClassReplaysActivity.this, "Download cancelled.");
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
                                }
                                else {
                                    holder.downloadStatusWrapper.setVisibility(View.GONE);
                                    holder.removelayout.setVisibility(View.VISIBLE);
                                    classReplayAdapter.notifyDataSetChanged();
                                }
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

    private void clickview(View v) {
        Animation animation1 = AnimationUtils.loadAnimation(v.getContext(), R.anim.click);
        v.startAnimation(animation1);
    }

    void populateRecordedStreams(final Context context) {
        newObjects.clear();

        Realm.init(context);
        Realm.getInstance(RealmUtility.getDefaultConfig(ClassReplaysActivity.this)).executeTransaction(realm -> {
            RealmResults<RealmAudio> realmAudios = realm.where(RealmAudio.class)
                    .equalTo("instructorcourseid", INSTRUCTORCOURSEID)
                    .equalTo("title", titletextview.getText().toString())
                    .isNotNull("audiourl")
                    .notEqualTo("audiourl", "")
                    .sort("id", Sort.DESCENDING)
                    .findAll();
            RealmResults<RealmRecordedVideoStream> realmRecordedVideoStreams = realm.where(RealmRecordedVideoStream.class)
                    .equalTo("instructorcourseid", INSTRUCTORCOURSEID)
                    .equalTo("title", titletextview.getText().toString())
                    .isNotNull("url")
                    .notEqualTo("url", "")
                    .sort("id", Sort.DESCENDING)
                    .findAll();
            RealmResults<RealmRecordedAudioStream> realmRecordedAudioStreams = realm.where(RealmRecordedAudioStream.class)
                    .equalTo("instructorcourseid", INSTRUCTORCOURSEID)
                    .equalTo("title", titletextview.getText().toString())
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

            classReplayAdapter.notifyDataSetChanged();
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
                            Realm.getInstance(RealmUtility.getDefaultConfig(ClassReplaysActivity.this)).executeTransaction(realm -> {
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
                            classReplayAdapter.notifyDataSetChanged();
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
}
