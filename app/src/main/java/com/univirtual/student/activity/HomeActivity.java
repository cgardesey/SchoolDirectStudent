package com.univirtual.student.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.greysonparrelli.permiso.PermisoActivity;
import com.univirtual.student.R;
import com.univirtual.student.fragment.EnrolmentParentFragment;
import com.univirtual.student.fragment.SearchCourseFragment;
import com.univirtual.student.fragment.SettingsFragment;
import com.univirtual.student.other.InitApplication;
import com.univirtual.student.realm.RealmAppUserFee;
import com.univirtual.student.realm.RealmCourse;
import com.univirtual.student.realm.RealmDialcode;
import com.univirtual.student.realm.RealmEnrolment;
import com.univirtual.student.realm.RealmInstitution;
import com.univirtual.student.realm.RealmInstructor;
import com.univirtual.student.realm.RealmInstructorCourse;
import com.univirtual.student.realm.RealmPayment;
import com.univirtual.student.realm.RealmPeriod;
import com.univirtual.student.realm.RealmStudent;
import com.univirtual.student.realm.RealmTimetable;
import com.univirtual.student.realm.RealmUser;
import com.univirtual.student.receiver.NetworkReceiver;
import com.univirtual.student.util.FCMAsyncTask;
import com.univirtual.student.util.RealmUtility;
import com.univirtual.student.util.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import static com.univirtual.student.activity.SelectResourceActivity.REQUEST_CODE_SET_DEFAULT_DIALER;
import static com.univirtual.student.constants.keyConst.API_URL;

import static com.univirtual.student.constants.keyConst.GUID_WS_URL;
import static com.univirtual.student.constants.Const.clearAppData;
import static com.univirtual.student.constants.Const.isNetworkAvailable;
import static com.univirtual.student.other.InitApplication.versionName;
import static com.univirtual.student.receiver.NetworkReceiver.activeActivity;
import static com.univirtual.student.util.Socket.EVENT_CLOSED;
import static com.univirtual.student.util.Socket.EVENT_OPEN;
import static com.univirtual.student.util.Socket.EVENT_RECONNECT_ATTEMPT;

public class HomeActivity extends PermisoActivity implements SettingsFragment.Callbacks {

    private String TAG;
    public static String MYUSERID = "MYUSERID";
    public static String APITOKEN = "APITOKEN";
    public static String ACCESSTOKEN = "ACCESSTOKEN";
    public static String GUID = "GUID";
    public static String JUSTENROLLED = "JUSTENROLLED";
    public static int RC_ACCOUNT = 435;
    public static final  int FILE_PICKER_REQUEST_CODE = 4389;
    NetworkReceiver networkReceiver;
    static BottomNavigationView navigation;
    FloatingActionButton close;
    public static Context context;
    public static Activity homeactivity;
    private static Socket guidSocket;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        public Fragment fragment;

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fragment = new SearchCourseFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_learn:
                    fragment = new EnrolmentParentFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_settings:
                    fragment = new SettingsFragment();
                    loadFragment(fragment);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        homeactivity = this;
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        if (InitApplication.getInstance().isNightModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_home);

        guidSocket = Socket
                .Builder.with(GUID_WS_URL)
                .build();
        guidSocket.connect();

        guidSocket.onEvent(EVENT_OPEN, new Socket.OnEventListener() {
            @Override
            public void onMessage(String event) {
                Log.d("mywebsocket2", "Connected");

                guidSocket.join("guid:" + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(MYUSERID, ""));

                guidSocket.onEventResponse("guid:" + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(MYUSERID, ""), new Socket.OnEventResponseListener() {
                    @Override
                    public void onMessage(String event, String data) {

                    }
                });

                guidSocket.setMessageListener(new Socket.OnMessageListener() {
                    @Override
                    public void onMessage(String data) {
                        JSONObject jsonObject = null;
                        JSONObject jsonResponse = null;
                        String message = "";
                        try {
                            jsonObject = new JSONObject(data);
                            switch (jsonObject.getInt("t")) {
                                case 0:
                                    break;
                                case 1:
                                    break;
                                case 2:
                                    break;
                                case 3:
                                    break;
                                case 4:
                                    break;
                                case 5:
                                    break;
                                case 6:
                                    break;
                                case 7:
                                    jsonResponse = jsonObject.getJSONObject("d");
                                    Log.d("mywebsocket2", jsonResponse.toString());
                                    Realm.init(activeActivity);
                                    JSONObject finalJsonResponse = jsonResponse;
                                    if (finalJsonResponse.getJSONObject("data").has("guid")) {
                                        String myguid = PreferenceManager.getDefaultSharedPreferences(activeActivity).getString(GUID, "");
                                        if (!myguid.equals("") && !finalJsonResponse.getJSONObject("data").getString("guid").equals(myguid)) {
                                            Log.d("d7410852", "local: " + myguid + "server : " + finalJsonResponse.getJSONObject("data").getString("guid"));
                                            AlertDialog.Builder builder = new AlertDialog.Builder(activeActivity);
                                            builder.setTitle("Duplicate Account Detected");
                                            builder.setMessage("You can only use your account on one device at a time.");
                                            builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                                clearAppData(activeActivity);
                                            });
                                            builder
                                                    .setCancelable(false)
                                                    .show();
                                        }
                                    }
                                    break;
                                case 8:
                                    break;
                                case 9:
                                    break;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                try {
                    JSONObject jsonData = new JSONObject()
                            .put(
                                    "guid", PreferenceManager.getDefaultSharedPreferences(homeactivity).getString(GUID, "")
                            );
                    if (isNetworkAvailable(homeactivity)) {

                        if (guidSocket.getState() == Socket.State.OPEN) {
                            if (guidSocket != null) {
                                guidSocket.send("guid:" + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(MYUSERID, ""), jsonData.toString());
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        guidSocket.onEvent(EVENT_RECONNECT_ATTEMPT, new Socket.OnEventListener() {
            @Override
            public void onMessage(String event) {
                Log.d("mywebsocket2", "reconnecting");
            }
        });
        guidSocket.onEvent(EVENT_CLOSED, new Socket.OnEventListener() {
            @Override
            public void onMessage(String event) {
                Log.d("mywebsocket2", "connection closed");
            }
        });

        close = findViewById(R.id.close);
        close.setOnClickListener(v -> {
//            changeDefaultDialer(HomeActivity.this, getPackagesOfDialerApps(getApplicationContext()).get(0));
            Intent intent = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
//            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            finish();
        });
        new FCMAsyncTask(getApplicationContext()).execute();

        FirebaseMessaging.getInstance().subscribeToTopic(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(MYUSERID, ""))
                .addOnCompleteListener(task -> {
                    String msg = "successfully subscribed";
                    if (!task.isSuccessful()) {
                        msg = "unsuccessfully subscribed";
                    }
                    Log.d("engineer:sub_status:", msg);
                });

        Realm.init(getApplicationContext());
        Realm.getInstance(RealmUtility.getDefaultConfig(HomeActivity.this)).executeTransaction(realm -> {
            RealmResults<RealmEnrolment> enrolments = realm.where(RealmEnrolment.class)
                    .equalTo("studentid", PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(MYUSERID, ""))
                    .equalTo("enrolled", 1)
                    .findAll();
            for (RealmEnrolment enrolment : enrolments) {
                FirebaseMessaging.getInstance().subscribeToTopic(enrolment.getInstructorcourseid())
                        .addOnCompleteListener(task -> {
                            String msg = "successfully subscribed";
                            if (!task.isSuccessful()) {
                                msg = "unsuccessfully unsubscribed";
                            }
                            Log.d("engineer:sub_status:", msg);
                        });
            }
        });


        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "getInstanceId failed", task.getException());
                        return;
                    }

                    // Get new Instance ID token
                    String token = task.getResult().getToken();

                    // Log and toast
                    Log.d("engineer", token);
                    retriev_current_registration_token(getApplicationContext(), token);
                });

        loadFragment(new SearchCourseFragment());
        //navigation.setSelectedItemId(R.id.navigation_home);

        networkReceiver = new NetworkReceiver();


        navigation = findViewById(R.id.navigation);
        //BottomNavigationViewHelper.removeShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (guidSocket != null) {
            guidSocket.leave("guid:" + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(MYUSERID, ""));
            guidSocket.clearListeners();
            guidSocket.close();
            guidSocket.terminate();
            guidSocket = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        activeActivity = this;
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        Realm.init(getApplicationContext());
        Realm.getInstance(RealmUtility.getDefaultConfig(HomeActivity.this)).executeTransaction(realm -> {
            RealmStudent realmStudent = realm.where(RealmStudent.class).equalTo("infoid", PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(MYUSERID, "")).findFirst();
           if (realmStudent == null) {
               clearAppData(getApplicationContext());
           }
            boolean accountInfoIncomplete =
                    realmStudent.getTitle() == null ||
                    realmStudent.getFirstname() == null ||
                    realmStudent.getLastname() == null ||
                    realmStudent.getGender() == null;
            if (accountInfoIncomplete) {
                int requestCode = 435;
                startActivityForResult(new Intent(getApplicationContext(), AccountActivity.class), requestCode);
            }
            else {
                boolean just_enrolled = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(JUSTENROLLED, false);
                if (just_enrolled) {
                    PreferenceManager
                            .getDefaultSharedPreferences(getApplicationContext())
                            .edit()
                            .putBoolean(JUSTENROLLED, false)
                            .apply();
                    navigation.getMenu().getItem(1).setChecked(true);
                    Fragment fragment = new EnrolmentParentFragment();
                    loadFragment(fragment);
                }
            }
        });

        boolean signedIn = !PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(MYUSERID, "").equals("");
        Realm.init(getApplicationContext());
        Realm.getInstance(RealmUtility.getDefaultConfig(HomeActivity.this)).executeTransaction(realm -> {
            int size = realm.where(RealmEnrolment.class)
                    .equalTo("studentid", PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(MYUSERID, ""))
                    .equalTo("enrolled", 1)
                    .findAll().size();

            navigation.getMenu().getItem(0).setEnabled(true);
            navigation.getMenu().getItem(1).setEnabled(true);
            navigation.getMenu().getItem(2).setEnabled(true);

            if (signedIn && size > 0) {
                navigation.getMenu().getItem(1).setChecked(true);
                Fragment fragment = new EnrolmentParentFragment();
                loadFragment(fragment);

            } else {
                navigation.getMenu().getItem(0).setChecked(true);
                navigation.getMenu().getItem(1).setEnabled(false);
                Fragment fragment = new SearchCourseFragment();
                loadFragment(fragment);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_ACCOUNT) {
            Realm.init(getApplicationContext());
            Realm.getInstance(RealmUtility.getDefaultConfig(HomeActivity.this)).executeTransaction(realm -> {
                boolean accountInfoNotSet = realm.where(RealmStudent.class).equalTo("infoid", PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(MYUSERID, "")).findFirst().getFirstname() == null;
                if (accountInfoNotSet) {
                    finish();
                }
            });
        }
        else if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER) {
//            finish();
        }
    }

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.homeframe, fragment);
        transaction.commit();
    }


    @Override
    public void onChangeNightMOde() {
        if (InitApplication.getInstance().isNightModeEnabled()) {
            InitApplication.getInstance().setIsNightModeEnabled(false);
            Intent intent = getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();
            startActivity(intent);

        } else {
            InitApplication.getInstance().setIsNightModeEnabled(true);
            Intent intent = getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();
            startActivity(intent);
        }

    }

    public static void dialog(Context context, boolean value) {

        if (value) {
            //   tv_check_connection.setVisibility(View.VISIBLE);

        } else {
            Snackbar snackbar = Snackbar
                    .make(navigation, context.getString(R.string.no_internet_connection), Snackbar.LENGTH_LONG)
                    .setAction(context.getString(R.string.ok).toUpperCase(), view -> {

                    });

            snackbar.show();
        }
    }

    public static void versionGuidCheck(Context context) {
        try {
            StringRequest stringRequest = new StringRequest(
                    Request.Method.GET,
                    API_URL + "version-guid-check",
                    response -> {
                        if (response != null) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (!jsonObject.getString("version").equals(versionName)) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder((AppCompatActivity)context);
                                    builder.setTitle("Update Available!");
                                    builder.setMessage("Update app to continue using this app.");
                                    builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                        Intent i = new Intent(Intent.ACTION_VIEW);
                                        i.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.univirtual.student"));
                                        context.startActivity(i);
                                    })
                                            .setCancelable(false)
                                            .show();
                                }
                                else if (!jsonObject.getString("guid").equals(PreferenceManager.getDefaultSharedPreferences(context).getString(GUID, ""))) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Duplicate Account Detected");
                                    builder.setMessage("You can only use your account on one device at a time.");
                                    builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                        clearAppData(context);
                                    });
                                    builder
                                            .setCancelable(false)
                                            .show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Log.d("Cyrilll", error.toString());
                        //                                myVolleyError(context, error);
                    }
            ) {
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
            e.printStackTrace();
        }
    }

    public static void fetchAllMyData(Context context) {
        try {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    API_URL + "all-data",
                    null,
                    responseJson -> {
                        if (responseJson != null) {
                            Realm.init(context);
                            Realm.getInstance(RealmUtility.getDefaultConfig(context)).executeTransaction(realm -> {
                                try {
                                    persistAll(realm, responseJson);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    },
                    error -> {

                    }
            ){
                /** Passing some request headers* */
                @Override
                public Map getHeaders() throws AuthFailureError {
                    HashMap headers = new HashMap();
                    headers.put("accept", "application/json");
                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(context).getString(APITOKEN, ""));
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

    public static void persistAll(Realm realm, JSONObject responseJson) throws JSONException {
        realm.createOrUpdateAllFromJson(RealmCourse.class, responseJson.getJSONArray("courses"));
        realm.createOrUpdateAllFromJson(RealmEnrolment.class, responseJson.getJSONArray("enrolments"));
        realm.createOrUpdateAllFromJson(RealmInstructor.class, responseJson.getJSONArray("instructors"));
        realm.createOrUpdateAllFromJson(RealmInstructorCourse.class, responseJson.getJSONArray("instructor_courses"));
        realm.createOrUpdateAllFromJson(RealmPayment.class, responseJson.getJSONArray("payments"));
        realm.createOrUpdateAllFromJson(RealmInstitution.class, responseJson.getJSONArray("institutions"));
        realm.createOrUpdateAllFromJson(RealmStudent.class, responseJson.getJSONArray("students"));
        realm.createOrUpdateAllFromJson(RealmUser.class, responseJson.getJSONArray("users"));
        realm.createOrUpdateAllFromJson(RealmTimetable.class, responseJson.getJSONArray("timetables"));
        realm.createOrUpdateAllFromJson(RealmPeriod.class, responseJson.getJSONArray("periods"));
        realm.createOrUpdateAllFromJson(RealmDialcode.class, responseJson.getJSONArray("dialcodes"));
        realm.createOrUpdateAllFromJson(RealmAppUserFee.class, responseJson.getJSONArray("app_user_fees"));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static String getDefaultDialerPackage(Context context) {
        TelecomManager manger= null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            manger = (TelecomManager) context.getSystemService(TELECOM_SERVICE);
        }
        String name=manger.getDefaultDialerPackage();
        return name;
    }

    public static void retriev_current_registration_token(Context context, String confirmation_token) {
        JSONObject request = new JSONObject();

        try {
            request.put("confirmation_token", confirmation_token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.PATCH,
                API_URL + "update-confirmation-token/" + PreferenceManager.getDefaultSharedPreferences(context).getString(MYUSERID, ""),
                request,
                response -> {
                },
                error -> {
                }
        ) {
            /** Passing some request headers* */

            @Override
            public Map getHeaders() throws AuthFailureError {
                HashMap headers = new HashMap();
                headers.put("accept", "application/json");
                headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(context).getString(APITOKEN, ""));
                return headers;
            }
        };
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        InitApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    public static class broadcastWithFirebase extends AsyncTask<Void, Integer, String> {


        // private ProgressDialog progressDialog;
        private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
        private static final String[] SCOPES = {MESSAGING_SCOPE};

        @Override
        protected String doInBackground(Void... params) {
            JSONObject jsonObject = null;
            try {
                JSONObject dataJsonObject = null;
                dataJsonObject = new JSONObject()
                        .put("guid", PreferenceManager.getDefaultSharedPreferences(context).getString(GUID, ""));

                jsonObject = new JSONObject().put(
                        "message", new JSONObject()
                                .put("topic", PreferenceManager.getDefaultSharedPreferences(homeactivity).getString(MYUSERID, ""))
                                /*.put("notification", new JSONObject()
                                        .put("body", jsonObject.getJSONObject("chat").has("attachmenturl") ? "Attachment" : jsonObject.getJSONObject("chat").getString("text"))
                                        .put("title", COURSEPATH + " Chat")
                                )*/
                                .put("data", dataJsonObject
                                )
                );
            } catch (JSONException e) {
                e.printStackTrace();
            }


            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
            String content = jsonObject.toString();
            RequestBody body = RequestBody.create(mediaType, content);
            okhttp3.Request request = new okhttp3.Request.Builder()

                    .url("https://fcm.googleapis.com/v1/projects/instructorapp-c6c95/messages:send")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(homeactivity).getString(ACCESSTOKEN, ""))
                    .build();
            try {
                okhttp3.Response response = client.newCall(request).execute();
                String s = response.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {

            // Init and show dialog

        }

        @Override
        protected void onPostExecute(String result) {

        }
    }
}
