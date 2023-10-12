package com.univirtual.student.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.greysonparrelli.permiso.Permiso;
import com.greysonparrelli.permiso.PermisoActivity;
import com.univirtual.student.R;
import com.univirtual.student.materialDialog.PhonenumberMaterialDialog;
import com.univirtual.student.materialDialog.SubscriptionMaterialDialog;
import com.univirtual.student.other.InitApplication;
import com.univirtual.student.realm.RealmAppUserFee;
import com.univirtual.student.realm.RealmBase64File;
import com.univirtual.student.realm.RealmDialcode;
import com.univirtual.student.realm.RealmEnrolment;
import com.univirtual.student.realm.RealmInstructorCourse;
import com.univirtual.student.realm.RealmStudent;
import com.univirtual.student.receiver.NetworkReceiver;
import com.univirtual.student.util.DownloadFilesAsync;
import com.univirtual.student.util.RealmUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.univirtual.student.activity.AudioStreamActivity.audioStreamActivity;
import static com.univirtual.student.activity.ChatActivity.chatActivity;
import static com.univirtual.student.activity.ConferenceCallActivity.conferenceCallActivity;
import static com.univirtual.student.activity.HomeActivity.APITOKEN;
import static com.univirtual.student.activity.HomeActivity.MYUSERID;
import static com.univirtual.student.activity.VideoStreamActivity.videoStreamActivity;
import static com.univirtual.student.constants.keyConst.API_URL;
import static com.univirtual.student.constants.Const.myVolleyError;
import static com.univirtual.student.constants.Const.retrieveFileFromRealm;
import static com.univirtual.student.constants.Const.storeFileInRealm;
import static com.univirtual.student.receiver.NetworkReceiver.activeActivity;

import io.realm.Realm;
import io.realm.RealmResults;

public class SelectResourceActivity extends PermisoActivity {

    public static final int REQUEST_CODE_SET_DEFAULT_DIALER = 100;
    static public Context context;
    static public Activity selectresourceactivity;
    LinearLayout quiz, assignment, pastquestions, classchat, classdocs, liveclassstream, recordedclassvideos, classreplay, externalLinks;
    ImageView backbtn;
    public static AsyncTask<String, Integer, String> downloadFileAsync;

    public static JSONObject classSessionJsonObj, classvideosessionJsonObj, classaudiosessionJsonObj;

    static public File docFile;

    static TextView coursepath;
    NetworkReceiver networkReceiver;

    public static String TITLE;
    public static String INSTRUCTORCOURSEID;
    public static String COURSEPATH;
    public static String ENROLMENTID;
    public static String PROFILEIMGURL;
    public static String ROOMID;
    public static String NODESERVER;
    public static String INSTRUCTORNAME;
    public static String SESSIONID;

    public static ImageView live_menu, recorded_menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_resource);
        context = getApplicationContext();
        selectresourceactivity = this;

        networkReceiver = new NetworkReceiver();

        coursepath = findViewById(R.id.coursepath);

        quiz = findViewById(R.id.quiz);
        assignment = findViewById(R.id.assignment);
        recordedclassvideos = findViewById(R.id.recordedclassvideos);
        pastquestions = findViewById(R.id.pastquestions);
        classdocs = findViewById(R.id.classdocs);
        liveclassstream = findViewById(R.id.liveclassstream);
        classreplay = findViewById(R.id.classreplay);
        classchat = findViewById(R.id.classchat);
        backbtn = findViewById(R.id.search);
        externalLinks = findViewById(R.id.externalLinks);

        backbtn.setOnClickListener(view -> {
            clickview(view);
            finish();
        });

        pastquestions.setOnClickListener(view -> {
            clickview(view);
            startActivity(new Intent(getApplicationContext(), PastQuestionYearsActivity.class).putExtra("INSTRUCTORCOURSEID", INSTRUCTORCOURSEID)
                    .putExtra("INSTRUCTORCOURSEID", INSTRUCTORCOURSEID)
                    .putExtra("COURSEPATH", COURSEPATH)
                    .putExtra("ENROLMENTID", ENROLMENTID)
                    .putExtra("PROFILEIMGURL", PROFILEIMGURL)
                    .putExtra("INSTRUCTORNAME", INSTRUCTORNAME)
                    .putExtra("NODESERVER", NODESERVER)
                    .putExtra("ROOMID", ROOMID)
            );
        });

        classdocs.setOnClickListener(view -> {
            clickview(view);

            startActivity(new Intent(getApplicationContext(), FileListActivity.class)
                    .putExtra("INSTRUCTORCOURSEID", INSTRUCTORCOURSEID)
                    .putExtra("COURSEPATH", COURSEPATH)
                    .putExtra("ENROLMENTID", ENROLMENTID)
                    .putExtra("PROFILEIMGURL", PROFILEIMGURL)
                    .putExtra("INSTRUCTORNAME", INSTRUCTORNAME)
                    .putExtra("NODESERVER", NODESERVER)
                    .putExtra("ROOMID", ROOMID)

                    .putExtra("activitytitle", COURSEPATH)
                    .putExtra("assignmenttitle", context.getString(R.string.classdocs))
            );
        });

        externalLinks.setOnClickListener(view -> {
            clickview(view);
            startActivity(new Intent(getApplicationContext(), ExternalLinksActivity.class)
                    .putExtra("INSTRUCTORCOURSEID", INSTRUCTORCOURSEID)
                    .putExtra("COURSEPATH", COURSEPATH)
                    .putExtra("ENROLMENTID", ENROLMENTID)
                    .putExtra("PROFILEIMGURL", PROFILEIMGURL)
                    .putExtra("INSTRUCTORNAME", INSTRUCTORNAME)
                    .putExtra("NODESERVER", NODESERVER)
                    .putExtra("ROOMID", ROOMID)
            );
        });

        liveclassstream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(SelectResourceActivity.this, liveclassstream);
                popupMenu.getMenuInflater().inflate(R.menu.live_menu, popupMenu.getMenu());
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();
                        String type = "";
                        switch (itemId) {
                            case R.id.conferencecall:
                                type = "conferencecall";
                                break;
                            case R.id.audio:
                                type = "audio";
                                break;
                            case R.id.video:
                                type = "video";
                                break;
                        }
                        final String[] phonenumber = {""};
                        Realm.init(SelectResourceActivity.this);
                        Realm.getInstance(RealmUtility.getDefaultConfig(SelectResourceActivity.this)).executeTransaction(realm -> {
                            phonenumber[0] = realm.where(RealmStudent.class).equalTo("infoid", PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(MYUSERID, "")).findFirst().getPrimarycontact();
                        });
                        PhonenumberMaterialDialog phonenumberMaterialDialog = new PhonenumberMaterialDialog();
                        if (phonenumberMaterialDialog != null && phonenumberMaterialDialog.isAdded()) {

                        } else {
                            phonenumberMaterialDialog.setType(type);
                            phonenumberMaterialDialog.setPhonenumber(phonenumber[0]);
                            phonenumberMaterialDialog.show(getSupportFragmentManager(), "");
                        }
                        return true;
                    }
                });
            }
        });

        classreplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SelectResourceActivity.this, DistinctRecordedStreamActivity.class)
                        .putExtra("INSTRUCTORCOURSEID", INSTRUCTORCOURSEID)
                        .putExtra("COURSEPATH", COURSEPATH)
                        .putExtra("ENROLMENTID", ENROLMENTID)
                        .putExtra("PROFILEIMGURL", PROFILEIMGURL)
                        .putExtra("INSTRUCTORNAME", INSTRUCTORNAME)
                        .putExtra("NODESERVER", NODESERVER)
                        .putExtra("ROOMID", ROOMID)
                );
            }
        });

        recordedclassvideos.setOnClickListener(view -> {
            clickview(view);
            startActivity(new Intent(SelectResourceActivity.this, RecordedVideosActivity.class)
                    .putExtra("INSTRUCTORCOURSEID", INSTRUCTORCOURSEID)
                    .putExtra("COURSEPATH", COURSEPATH)
                    .putExtra("ENROLMENTID", ENROLMENTID)
                    .putExtra("PROFILEIMGURL", PROFILEIMGURL)
                    .putExtra("INSTRUCTORNAME", INSTRUCTORNAME)
                    .putExtra("NODESERVER", NODESERVER)
                    .putExtra("ROOMID", ROOMID)
            );
        });

        quiz.setOnClickListener(view -> {
            clickview(view);
            startActivity(new Intent(getApplicationContext(), QuizzesActivity.class)
                    .putExtra("INSTRUCTORCOURSEID", INSTRUCTORCOURSEID)
                    .putExtra("COURSEPATH", COURSEPATH)
                    .putExtra("ENROLMENTID", ENROLMENTID)
                    .putExtra("PROFILEIMGURL", PROFILEIMGURL)
                    .putExtra("INSTRUCTORNAME", INSTRUCTORNAME)
                    .putExtra("NODESERVER", NODESERVER)
                    .putExtra("ROOMID", ROOMID)
            );
        });

        assignment.setOnClickListener(view -> {
            clickview(view);
            startActivity(new Intent(getApplicationContext(), AssignmentActivity.class)
                    .putExtra("INSTRUCTORCOURSEID", INSTRUCTORCOURSEID)
                    .putExtra("COURSEPATH", COURSEPATH)
                    .putExtra("ENROLMENTID", ENROLMENTID)
                    .putExtra("PROFILEIMGURL", PROFILEIMGURL)
                    .putExtra("INSTRUCTORNAME", INSTRUCTORNAME)
                    .putExtra("NODESERVER", NODESERVER)
                    .putExtra("ROOMID", ROOMID)
            );
        });

        classchat.setOnClickListener(view -> {
            clickview(view);
            if (chatActivity != null) {
                chatActivity.finish();
            }
            Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
                                                         @Override
                                                         public void onPermissionResult(Permiso.ResultSet resultSet) {
                                                             if (resultSet.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE) && resultSet.isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                                                 startActivity(new Intent(context, ChatActivity.class)
                                                                         .putExtra("INSTRUCTORCOURSEID", INSTRUCTORCOURSEID)
                                                                         .putExtra("COURSEPATH", COURSEPATH)
                                                                         .putExtra("ENROLMENTID", ENROLMENTID)
                                                                         .putExtra("PROFILEIMGURL", PROFILEIMGURL)
                                                                         .putExtra("INSTRUCTORNAME", INSTRUCTORNAME)
                                                                         .putExtra("NODESERVER", NODESERVER)
                                                                         .putExtra("ROOMID", ROOMID)
                                                                 );
                                                             }
                                                         }

                                                         @Override
                                                         public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                                                             Permiso.getInstance().showRationaleInDialog(context.getString(R.string.permissions), context.getString(R.string.this_permission_is_mandatory_pls_allow_access), null, callback);
                                                         }
                                                     },
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
        });

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

        if (getIntent().getBooleanExtra("LAUNCHED_FROM_NOTIFICATION", false)) {
            String type = "";
            switch (getIntent().getStringExtra("TYPE")) {
                case "class":
                    type = "conferencecall";
                    break;
                case "audio_stream":
                    type = "audio";
                    break;
                case "video_stream":
                    type = "video";
                    break;
            }

            final String[] phonenumber = {""};
            Realm.init(SelectResourceActivity.this);
            Realm.getInstance(RealmUtility.getDefaultConfig(SelectResourceActivity.this)).executeTransaction(realm -> {
                phonenumber[0] = realm.where(RealmStudent.class).equalTo("infoid", PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(MYUSERID, "")).findFirst().getPrimarycontact();
            });


            PhonenumberMaterialDialog phonenumberMaterialDialog = new PhonenumberMaterialDialog();
            if (phonenumberMaterialDialog != null && phonenumberMaterialDialog.isAdded()) {

            } else {

                phonenumberMaterialDialog.setType(type);
                phonenumberMaterialDialog.setPhonenumber(phonenumber[0]);
                phonenumberMaterialDialog.show(getSupportFragmentManager(), "");
            }
        }

        final RealmEnrolment[] realmEnrolment = new RealmEnrolment[1];
        Realm.init(SelectResourceActivity.this);
        Realm.getInstance(RealmUtility.getDefaultConfig(SelectResourceActivity.this)).executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realmEnrolment[0] = Realm.getInstance(RealmUtility.getDefaultConfig(SelectResourceActivity.this))
                        .where(RealmEnrolment.class)
                        .equalTo("enrolmentid", ENROLMENTID)
                        .findFirst();
            }
        });


        if (realmEnrolment[0].isAppuserfeeexpired()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SelectResourceActivity.this);
            if (realmEnrolment[0].getAppuserfeeexpirydate() != null && !realmEnrolment[0].getAppuserfeeexpirydate().equals("")) {
                builder.setTitle("Expired Subscription");
                builder.setMessage("Your app usage subscription has expired.\n\nRenew subscription?");
            } else {
                builder.setTitle("Not Subscribed.");
                builder.setMessage("Your app usage subscription fee has not been paid.\n\nSubscribe?");
            }
            builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                try {
                    ProgressDialog progressDialog = new ProgressDialog(SelectResourceActivity.this);
                    progressDialog.setMessage(SelectResourceActivity.this.getString(R.string.please_wait));
                    progressDialog.setCancelable(false);
                    progressDialog.setIndeterminate(true);
                    progressDialog.show();

                    StringRequest stringRequest = new StringRequest(
                            com.android.volley.Request.Method.POST,
                            API_URL + "payment-refresh-data",
                            response -> {
                                progressDialog.dismiss();
                                if (response != null) {
                                    progressDialog.dismiss();
                                    final RealmAppUserFee[] newRealmAppUserFee = new RealmAppUserFee[1];
                                    try {
                                        JSONObject responseJson = new JSONObject(response);
                                        Realm.init(SelectResourceActivity.this);
                                        Realm.getInstance(RealmUtility.getDefaultConfig(SelectResourceActivity.this)).executeTransaction(realm -> {
                                            try {
                                                newRealmAppUserFee[0] = realm.createOrUpdateObjectFromJson(RealmAppUserFee.class, responseJson.getJSONObject("app_user_fee"));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        });


                                        SubscriptionMaterialDialog subscriptionMaterialDialog = new SubscriptionMaterialDialog();
                                        if (subscriptionMaterialDialog != null && subscriptionMaterialDialog.isAdded()) {

                                        } else {
                                            subscriptionMaterialDialog.setAppusagecurrency(newRealmAppUserFee[0].getCurrency());
                                            subscriptionMaterialDialog.setAppusagepriceperday(Double.valueOf(newRealmAppUserFee[0].getPriceperday()));
                                            subscriptionMaterialDialog.setAppusagepriceperweek(Double.valueOf(newRealmAppUserFee[0].getPriceperweek()));
                                            subscriptionMaterialDialog.setAppusagepricepermonth(Double.valueOf(newRealmAppUserFee[0].getPricepermonth()));
                                            subscriptionMaterialDialog.setAppuserfeedescription(newRealmAppUserFee[0].getCurrency() + newRealmAppUserFee[0].getPriceperday() + " " + "per day");
                                            subscriptionMaterialDialog.setEnrolmentid(ENROLMENTID);
                                            subscriptionMaterialDialog.show(getSupportFragmentManager(), "SubscriptionMaterialDialog");
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                            },
                            error -> {
                                error.printStackTrace();
                                Log.d("Cyrilll", error.toString());
                                progressDialog.dismiss();
                                myVolleyError(SelectResourceActivity.this, error);
                                finish();
                            }
                    ) {
                        @Override
                        public Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            params.put("instructorcourseid", INSTRUCTORCOURSEID);
                            return params;
                        }

                        /** Passing some request headers* */
                        @Override
                        public Map getHeaders() throws AuthFailureError {
                            HashMap headers = new HashMap();
                            headers.put("accept", "application/json");
                            headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(SelectResourceActivity.this).getString(APITOKEN, ""));
                            return headers;
                        }
                    };
                    ;
                    stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                            0,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    InitApplication.getInstance().addToRequestQueue(stringRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                dialog.dismiss();
                finish();
            });
            builder.setCancelable(false).show();
        }
    }

    public static void conferenceCallClass(Context context, String mobileno) {
        try {
            ProgressDialog mProgress = new ProgressDialog(context);
            mProgress.setCancelable(false);
            mProgress.setIndeterminate(true);

            mProgress.setTitle("Checking for class conference call...");
            mProgress.show();
            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    API_URL + "live-classes",
                    response -> {
                        mProgress.dismiss();
                        if (response != null) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (jsonObject.getBoolean("eligible")) {
                                    Realm.init(context);
                                    Realm.getInstance(RealmUtility.getDefaultConfig(context)).executeTransaction(new Realm.Transaction() {
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
                                    if (!jsonObject.isNull("classsession")) {
                                        classSessionJsonObj = jsonObject.getJSONObject("classsession");
                                        if (classSessionJsonObj.getInt("islive") == 0 || !jsonObject.getBoolean("no_participants_exist")) {
                                            gotonextActivity(context, "call", null, classSessionJsonObj.getString("docurl"), classSessionJsonObj.getString("sessionid"));
                                        } else {
                                            Toast.makeText(context, context.getString(R.string.your_instructor_is_yet_to_start_class), Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(context, context.getString(R.string.no_live_class_at_the_moment), Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(context, context.getString(R.string.your_subscription_has_expired), Toast.LENGTH_LONG).show();
                                    context.startActivity(new Intent(context, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                    ((Activity) context).finish();
                                }


                            } catch (JSONException e) {
//                                Toast.makeText(SelectResourceActivity.this, getString(R.string.no_live_class_at_the_moment), Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }

                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Log.d("Cyrilll", error.toString());
                        mProgress.dismiss();
                        myVolleyError(context, error);
                    }
            ) {
                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("enrolmentid", ENROLMENTID);
                    params.put("instructorcourseid", INSTRUCTORCOURSEID);
                    params.put("mobileno", mobileno);
                    return params;
                }

                @Override
                public Map getHeaders() throws AuthFailureError {
                    HashMap headers = new HashMap();
                    headers.put("accept", "application/json");
                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(context).getString(APITOKEN, ""));
                    return headers;
                }
            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(stringRequest);

        } catch (Exception e) {
            Log.e("My error", e.toString());
        }
    }

    public static void liveVideo(Context context, String mobileno) {
        try {
            ProgressDialog mProgress = new ProgressDialog(context);
            ;
            mProgress.setCancelable(false);
            mProgress.setIndeterminate(true);

            mProgress.setTitle(context.getString(R.string.checking_for_live_class_video_stream));
            mProgress.show();
            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    API_URL + "live-class-videos",
                    response -> {
                        mProgress.dismiss();
                        if (response != null) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                Realm.init(context);
                                Realm.getInstance(RealmUtility.getDefaultConfig(context)).executeTransaction(new Realm.Transaction() {
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
                                    if (!jsonObject.isNull("class_video_session")) {
                                        classvideosessionJsonObj = jsonObject.getJSONObject("class_video_session");
                                        VideoStreamActivity.ISRECORDED = false;
                                        gotonextActivity(context, "video", classvideosessionJsonObj.getString("streamurl"), classvideosessionJsonObj.getString("docurl"), classvideosessionJsonObj.getString("sessionid"));
                                    } else {
                                        Toast.makeText(context, context.getString(R.string.no_live_video_class_at_the_moment), Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(context, context.getString(R.string.your_subscription_has_expired), Toast.LENGTH_LONG).show();
                                    context.startActivity(new Intent(context, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                    ((Activity) context).finish();
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Log.d("Cyrilll", error.toString());
                        mProgress.dismiss();
                        myVolleyError(context, error);
                    }
            ) {
                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("enrolmentid", ENROLMENTID);
                    params.put("instructorcourseid", INSTRUCTORCOURSEID);
                    params.put("mobileno", mobileno);
                    return params;
                }

                @Override
                public Map getHeaders() throws AuthFailureError {
                    HashMap headers = new HashMap();
                    headers.put("accept", "application/json");
                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(context).getString(APITOKEN, ""));
                    return headers;
                }
            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(stringRequest);

        } catch (Exception e) {
            Log.e("My error", e.toString());
        }
    }

    public static void liveAudio(Context context, String mobileno) {
        try {

            ProgressDialog mProgress = new ProgressDialog(context);
            mProgress.setCancelable(false);
            mProgress.setIndeterminate(true);

            mProgress.setTitle(context.getString(R.string.checking_for_live_class_audio_stream));
            mProgress.show();
            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    API_URL + "live-class-audios",
                    response -> {
                        mProgress.dismiss();
                        if (response != null) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                Realm.init(context);
                                Realm.getInstance(RealmUtility.getDefaultConfig(context)).executeTransaction(new Realm.Transaction() {
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
                                    if (!jsonObject.isNull("class_audio_session")) {
                                        classaudiosessionJsonObj = jsonObject.getJSONObject("class_audio_session");
                                        AudioStreamActivity.ISRECORDED = false;
                                        gotonextActivity(context, "audio", classaudiosessionJsonObj.getString("streamurl"), classaudiosessionJsonObj.getString("docurl"), classaudiosessionJsonObj.getString("sessionid"));
                                    } else {
                                        Toast.makeText(context, context.getString(R.string.no_live_audio_class_at_the_moment), Toast.LENGTH_LONG).show();
                                    }
                                    mProgress.setTitle(context.getString(R.string.checking_for_live_class));
                                } else {
                                    Toast.makeText(context, context.getString(R.string.your_subscription_has_expired), Toast.LENGTH_LONG).show();
                                    context.startActivity(new Intent(context, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                    ((Activity) context).finish();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Log.d("Cyrilll", error.toString());
                        mProgress.dismiss();
                        myVolleyError(context, error);
                    }
            ) {
                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("enrolmentid", ENROLMENTID);
                    params.put("instructorcourseid", INSTRUCTORCOURSEID);
                    params.put("mobileno", mobileno);
                    return params;
                }

                @Override
                public Map getHeaders() throws AuthFailureError {
                    HashMap headers = new HashMap();
                    headers.put("accept", "application/json");
                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(context).getString(APITOKEN, ""));
                    return headers;
                }
            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(stringRequest);

        } catch (Exception e) {
            Log.e("My error", e.toString());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        activeActivity = this;
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        coursepath.setText(COURSEPATH);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkReceiver);
    }

    @Override
    protected void onStop() {
        if (downloadFileAsync != null && downloadFileAsync.getStatus() != AsyncTask.Status.FINISHED) {
            // This would not cancel downloading from httpClient
            //  we have do handle that manually in onCancelled event inside AsyncTask
            downloadFileAsync.cancel(true);
        }
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void clickview(View v) {
        Animation animation1 = AnimationUtils.loadAnimation(v.getContext(), R.anim.click);
        v.startAnimation(animation1);

    }

    public static void gotonextActivity(Context context, String type, String streamurl, String docurl, String sessionid) {
        docFile = null;
        Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
                                                     @Override
                                                     public void onPermissionResult(Permiso.ResultSet resultSet) {
                                                         if (resultSet.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE) && resultSet.isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                                             final String[] mydocurl = {""};
                                                             mydocurl[0] = docurl;
                                                             ProgressDialog mProgress = new ProgressDialog(context);
                                                             mProgress.setCancelable(false);
                                                             mProgress.setIndeterminate(true);

                                                             mProgress.setMessage(context.getString(R.string.please_wait));
                                                             mProgress.show();
                                                             StringRequest stringRequest = new StringRequest(
                                                                     Request.Method.POST,
                                                                     API_URL + "session-data",
                                                                     new Response.Listener<String>() {
                                                                         @Override
                                                                         public void onResponse(String sessionDataResponse) {
                                                                             if (sessionDataResponse != null) {
                                                                                 try {
                                                                                     JSONObject sessionDataResponseJson = new JSONObject(sessionDataResponse);

                                                                                     JSONArray pdfpaths = new JSONArray();

                                                                                     if (mydocurl[0] != null && !mydocurl[0].equals("")) {
                                                                                         if (mydocurl[0].endsWith(";")) {
                                                                                             mydocurl[0] = mydocurl[0].substring(0, mydocurl[0].length() - 1);
                                                                                         }
                                                                                         String[] doc_urls = mydocurl[0].split(";");
                                                                                         for (String doc_url : doc_urls) {
                                                                                             if (!pathAlreadyExist(pdfpaths, doc_url)) {
                                                                                                 pdfpaths.put(doc_url);
                                                                                             }
                                                                                         }
                                                                                     }
                                                                                     Map<String, String> filepathMap = new HashMap<>();

                                                                                     for (int i = 0; i < pdfpaths.length(); i++) {
                                                                                         try {
                                                                                             final boolean[] resourceExist = new boolean[1];
                                                                                             int finalI = i;
                                                                                             Realm.getInstance(RealmUtility.getDefaultConfig(context)).executeTransaction(new Realm.Transaction() {
                                                                                                 @Override
                                                                                                 public void execute(Realm realm) {
                                                                                                     try {
                                                                                                         resourceExist[0] = realm.where(RealmBase64File.class).equalTo("url", pdfpaths.getString(finalI)).findFirst() != null;
                                                                                                     } catch (JSONException e) {
                                                                                                         e.printStackTrace();
                                                                                                     }
                                                                                                 }
                                                                                             });
                                                                                             if (!resourceExist[0]) {
                                                                                                 filepathMap.put(pdfpaths.getString(i), context.getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Class-sessions/Documents/" + URLUtil.guessFileName(pdfpaths.getString(i), null, null));
                                                                                             }
                                                                                             else {
                                                                                                 retrieveFileFromRealm(context, pdfpaths.getString(i), context.getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Class-sessions/Documents/" + URLUtil.guessFileName(pdfpaths.getString(i), null, null));
                                                                                             }
                                                                                         } catch (JSONException e) {
                                                                                             e.printStackTrace();
                                                                                         } catch (Exception e) {
                                                                                             e.printStackTrace();
                                                                                         }
                                                                                     }

                                                                                     if (filepathMap.size() > 0) {
                                                                                         File parentDir = new File(context.getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Class-sessions", "Documents");
                                                                                         if (!parentDir.exists()) {
                                                                                             parentDir.mkdirs();
                                                                                         }
                                                                                         String map_size = String.valueOf(filepathMap.size());
                                                                                         downloadFileAsync = new DownloadFilesAsync(file_url -> {
                                                                                             if (file_url == null) {
                                                                                                 try {
                                                                                                     for (int i = 0; i < pdfpaths.length(); i++) {
                                                                                                         storeFileInRealm(context, pdfpaths.getString(i), context.getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Class-sessions/Documents/" + URLUtil.guessFileName(pdfpaths.getString(i), null, null));
                                                                                                     }
                                                                                                     launchNext(sessionDataResponse, type, sessionid, context, streamurl);
                                                                                                     mProgress.dismiss();
                                                                                                 } catch (JSONException e) {
                                                                                                     e.printStackTrace();
                                                                                                 } catch (Exception e) {
                                                                                                     e.printStackTrace();
                                                                                                 }
                                                                                             } else {
                                                                                                 Log.d("981sdfs", file_url);
                                                                                                 Toast.makeText(context, "Error occured.", Toast.LENGTH_SHORT).show();
                                                                                             }
                                                                                             mProgress.dismiss();
                                                                                         },
                                                                                                 new DownloadFilesAsync.OnTaskProgressUpdateInterface() {
                                                                                                     @Override
                                                                                                     public void onTaskProgressUpdate(Integer[] progress) {
                                                                                                         mProgress.setMessage("Downloading class files...\n\n" + String.valueOf(progress[0]) + " of " + map_size + "\n\n" + String.valueOf(progress[1]) + "% complete");
                                                                                                     }
                                                                                                 },
                                                                                                 new DownloadFilesAsync.OnTaskCancelledInterface() {
                                                                                                     @Override
                                                                                                     public void onTaskCancelled() {

                                                                                                         Iterator it = filepathMap.entrySet().iterator();
                                                                                                         while (it.hasNext()) {
                                                                                                             Map.Entry pair = (Map.Entry) it.next();

                                                                                                             File file = new File((String) pair.getValue());
                                                                                                             if (file.exists()) {
                                                                                                                 file.delete();
                                                                                                             }

                                                                                                             it.remove(); // avoids a ConcurrentModificationException
                                                                                                         }
                                                                                                         mProgress.dismiss();
                                                                                                         Toast.makeText(context, context.getString(R.string.download_cancelled), Toast.LENGTH_LONG).show();
                                                                                                     }
                                                                                                 }, filepathMap).execute();
                                                                                     } else {
                                                                                         Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
                                                                                                                                      @Override
                                                                                                                                      public void onPermissionResult(Permiso.ResultSet resultSet) {
                                                                                                                                          if (resultSet.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE) && resultSet.isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                                                                                                                              try {
                                                                                                                                                  launchNext(sessionDataResponse, type, sessionid, context, streamurl);
                                                                                                                                              } catch (JSONException e) {
                                                                                                                                                  e.printStackTrace();
                                                                                                                                              } catch (FileNotFoundException e) {
                                                                                                                                                  e.printStackTrace();
                                                                                                                                              } catch (UnsupportedEncodingException e) {
                                                                                                                                                  e.printStackTrace();
                                                                                                                                              }
                                                                                                                                          }
                                                                                                                                      }

                                                                                                                                      @Override
                                                                                                                                      public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                                                                                                                                          Permiso.getInstance().showRationaleInDialog(context.getString(R.string.permissions), context.getString(R.string.this_permission_is_mandatory_pls_allow_access), null, callback);
                                                                                                                                      }
                                                                                                                                  },
                                                                                                 Manifest.permission.READ_EXTERNAL_STORAGE,
                                                                                                 Manifest.permission.WRITE_EXTERNAL_STORAGE);
                                                                                         mProgress.dismiss();
                                                                                     }
                                                                                 } catch (JSONException e) {
                                                                                     mProgress.dismiss();
                                                                                     e.printStackTrace();
                                                                                 }
                                                                             }
                                                                         }
                                                                     },
                                                                     error -> {
                                                                         error.printStackTrace();
                                                                         Log.d("Cyrilll", error.toString());
                                                                         mProgress.dismiss();
                                                                         myVolleyError(context, error);
                                                                     }
                                                             ) {
                                                                 @Override
                                                                 public Map<String, String> getParams() throws AuthFailureError {
                                                                     Map<String, String> params = new HashMap<>();
                                                                     params.put("sessionid", sessionid);
                                                                     params.put("type", type);
                                                                     return params;
                                                                 }

                                                                 @Override
                                                                 public Map getHeaders() throws AuthFailureError {
                                                                     HashMap headers = new HashMap();
                                                                     headers.put("accept", "application/json");
                                                                     headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(context).getString(APITOKEN, ""));
                                                                     return headers;
                                                                 }
                                                             };
                                                             stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                                                                     0,
                                                                     DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                                                     DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                                             InitApplication.getInstance().addToRequestQueue(stringRequest);
                                                         }
                                                     }

                                                     @Override
                                                     public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                                                         Permiso.getInstance().showRationaleInDialog(context.getString(R.string.permissions), context.getString(R.string.this_permission_is_mandatory_pls_allow_access), null, callback);
                                                     }
                                                 },
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

    }

    private static boolean pathAlreadyExist(JSONArray jsonArray, String path) {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                if (jsonArray.getString(i).equals(path)) {
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void launchNext(String response, String type, String sessionid, Context context, String streamurl) throws JSONException, FileNotFoundException, UnsupportedEncodingException {
        String dialcode = "";
        if (audioStreamActivity != null) {
            audioStreamActivity.finish();
        }
        if (videoStreamActivity != null) {
            videoStreamActivity.finish();
        }
        if (conferenceCallActivity != null) {
            conferenceCallActivity.finish();
        }


        if (type.equals("call")) {
            dialcode = classSessionJsonObj.getString("dialcode");
            ConferenceCallActivity.DIALCODE = dialcode;
            ConferenceCallActivity.CONFERENCEID = ROOMID;
            ConferenceCallActivity.drawingCoordinatesJsonArray = new JSONObject(response).getJSONArray("drawing_coordinates");
            context.startActivity(new Intent(context, ConferenceCallActivity.class)
                    .putExtra("INSTRUCTORCOURSEID", INSTRUCTORCOURSEID)
                    .putExtra("COURSEPATH", COURSEPATH)
                    .putExtra("ENROLMENTID", ENROLMENTID)
                    .putExtra("PROFILEIMGURL", PROFILEIMGURL)
                    .putExtra("INSTRUCTORNAME", INSTRUCTORNAME)
                    .putExtra("NODESERVER", NODESERVER)
                    .putExtra("ROOMID", ROOMID)

                    .putExtra("SESSIONID", sessionid)
            );
        } else if (type.equals("video")) {
            dialcode = classvideosessionJsonObj.getString("dialcode");

            VideoStreamActivity.DIALCODE = dialcode;
            VideoStreamActivity.CONFERENCEID = ROOMID;
            VideoStreamActivity.VIDEO_URL = streamurl;
            VideoStreamActivity.drawingCoordinatesJsonArray = new JSONObject(response).getJSONArray("drawing_coordinates");
            context.startActivity(new Intent(context, VideoStreamActivity.class)
                    .putExtra("INSTRUCTORCOURSEID", INSTRUCTORCOURSEID)
                    .putExtra("COURSEPATH", COURSEPATH)
                    .putExtra("ENROLMENTID", ENROLMENTID)
                    .putExtra("PROFILEIMGURL", PROFILEIMGURL)
                    .putExtra("INSTRUCTORNAME", INSTRUCTORNAME)
                    .putExtra("NODESERVER", NODESERVER)
                    .putExtra("ROOMID", ROOMID)

                    .putExtra("SESSIONID", sessionid)
            );
        } else {
            if (classaudiosessionJsonObj != null) {
                dialcode = classaudiosessionJsonObj.getString("dialcode");
                AudioStreamActivity.DIALCODE = dialcode;
                AudioStreamActivity.CONFERENCEID = ROOMID;
            }
            AudioStreamActivity.AUDIO_URL = streamurl;
            if (response != null) {
                AudioStreamActivity.drawingCoordinatesJsonArray = new JSONObject(response).getJSONArray("drawing_coordinates");
            }
            context.startActivity(new Intent(context, AudioStreamActivity.class)
                    .putExtra("INSTRUCTORCOURSEID", INSTRUCTORCOURSEID)
                    .putExtra("COURSEPATH", COURSEPATH)
                    .putExtra("ENROLMENTID", ENROLMENTID)
                    .putExtra("PROFILEIMGURL", PROFILEIMGURL)
                    .putExtra("INSTRUCTORNAME", INSTRUCTORNAME)
                    .putExtra("NODESERVER", NODESERVER)
                    .putExtra("ROOMID", ROOMID)

                    .putExtra("SESSIONID", sessionid)
            );
        }

        if (dialcode != null && !dialcode.equals("")) {
            Realm.init(context);
            String finalDialcode = dialcode;
            Realm.getInstance(RealmUtility.getDefaultConfig(context)).executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<RealmDialcode> realmDialcodes = Realm.getInstance(RealmUtility.getDefaultConfig(context)).where(RealmDialcode.class).findAll();
                    realmDialcodes.deleteAllFromRealm();
                    realm.copyToRealmOrUpdate(new RealmDialcode(finalDialcode));
                    RealmEnrolment realmEnrolment = Realm.getInstance(RealmUtility.getDefaultConfig(context)).where(RealmEnrolment.class)
                            .equalTo("enrolmentid", ENROLMENTID)
                            .findFirst();
                    realmEnrolment.setConferenceid(ROOMID);
                }
            });
        }
    }
}
