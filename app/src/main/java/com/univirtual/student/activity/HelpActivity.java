package com.univirtual.student.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.univirtual.student.R;
import com.univirtual.student.adapter.FaqAdapter;
import com.univirtual.student.other.InitApplication;
import com.univirtual.student.realm.RealmFaq;
import com.univirtual.student.util.LocaleHelper;
import com.univirtual.student.util.RealmUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.univirtual.student.activity.HomeActivity.APITOKEN;
import static com.univirtual.student.constants.keyConst.API_URL;
import static com.univirtual.student.constants.Const.myVolleyError;


public class HelpActivity extends AppCompatActivity {

    private ImageView teacher_image, backbtn;
    RecyclerView faqsrecyclerview;
    private static final String MY_LOGIN_ID = "MY_LOGIN_ID";
    private String api_token;

    ArrayList<RealmFaq> faqArrayList;
    ProgressDialog dialog;
    FaqAdapter faqAdapter;
    TextView nofaqstext;
    ImageView refresh;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    public void setLanguage() {
        SharedPreferences prefs = getSharedPreferences(MY_LOGIN_ID, MODE_PRIVATE);
        String language = prefs.getString("language", "");
        // Toast.makeText(conferenceCallActivity, language, Toast.LENGTH_SHORT).show();
        if (language.contains("French")) {
//use constructor with country
            Locale locale = new Locale("fr", "BE");

            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        } else {
            Locale locale = new Locale("en", "GB");

            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        faqsrecyclerview = findViewById(R.id.helperview);
        backbtn = findViewById(R.id.search);
        nofaqstext = findViewById(R.id.nofaqstext);
        refresh = findViewById(R.id.refresh);
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        refresh.setOnClickListener(v -> refresh());
        faqArrayList = new ArrayList<>();

        populateFaqs();
        faqAdapter = new FaqAdapter(faqArrayList);


        faqsrecyclerview.setLayoutManager(new LinearLayoutManager(this));
        faqsrecyclerview.setNestedScrollingEnabled(false);
        faqsrecyclerview.setItemAnimator(new DefaultItemAnimator());
        faqsrecyclerview.setAdapter(faqAdapter);
    }

    public void populateFaqs() {
        Realm.init(getApplicationContext());
        Realm.getInstance(RealmUtility.getDefaultConfig(HelpActivity.this)).executeTransaction(realm -> {
            RealmResults<RealmFaq> results = realm.where(RealmFaq.class).findAll();
            if (results.size() > 0) {
                nofaqstext.setVisibility(View.GONE);
                faqsrecyclerview.setVisibility(View.VISIBLE);
            } else {
                nofaqstext.setVisibility(View.VISIBLE);
                faqsrecyclerview.setVisibility(View.GONE);
            }
            faqArrayList.clear();
            for (RealmFaq realmFaq : results) {
//                    boolean attended = realm.where(RealmAttendance.class).equalTo("audioid", realmPayment.getAudioid()).findFirst() != null;
//                    realmPayment.setAttended(attended);
                faqArrayList.add(realmFaq);
            }
        });
    }

    public void refresh() {
        try {
            dialog = new ProgressDialog(this);
            dialog.setMessage("Checking for new data...");
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.show();
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                    Request.Method.GET,
                    API_URL + "faqs",
                    null,
                    response -> {
                        if (response != null) {
                            dialog.dismiss();
                            Realm.init(getApplicationContext());
                            Realm.getInstance(RealmUtility.getDefaultConfig(HelpActivity.this)).executeTransaction(realm -> {
                                RealmResults<RealmFaq> result = realm.where(RealmFaq.class).findAll();
                                result.deleteAllFromRealm();
                                realm.createOrUpdateAllFromJson(RealmFaq.class, response);
                            });
                            populateFaqs();
                            faqAdapter.notifyDataSetChanged();
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

