package com.univirtual.student.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.univirtual.student.R;
import com.univirtual.student.adapter.ListAdapter;
import com.univirtual.student.other.InitApplication;
import com.univirtual.student.receiver.NetworkReceiver;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.univirtual.student.activity.PastQuestionsActivity.VIDEOVISIBILITY;
import static com.univirtual.student.activity.PastQuestionsActivity.mQuestionNumber;
import static com.univirtual.student.constants.keyConst.API_URL;
import static com.univirtual.student.constants.Const.myVolleyError;
import static com.univirtual.student.receiver.NetworkReceiver.activeActivity;

public class PastQuestionYearsActivity extends AppCompatActivity {

    String TITLE;
    String INSTRUCTORCOURSEID;
    String COURSEPATH;
    String ENROLMENTID;
    String PROFILEIMGURL;
    String ROOMID;
    String NODESERVER;
    String INSTRUCTORNAME;
    String SESSIONID;

    RecyclerView recyclerView;
    ImageView loadinggif;
    ImageView backbtn;
    Button retrybtn;
    LinearLayout retry_layout;
    TextView titleTextView, text, nodata;
    ArrayList<String> newList = new ArrayList<>();
    ArrayList<String> list = new ArrayList<>();
    ListAdapter listAdapter;
    String title = "";
    Context mContext;
    ArrayList<String> years = new ArrayList<>();
    NetworkReceiver networkReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_question_years);

        mContext = getApplicationContext();

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

        loadinggif = findViewById(R.id.loadinggif);
        retry_layout = findViewById(R.id.retry_layout);
        retrybtn = findViewById(R.id.retrybtn);
        Glide.with(getApplicationContext()).asGif().load(R.drawable.spinner).apply(new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.spinner)
                .error(R.drawable.error)).into(loadinggif);
        text = findViewById(R.id.text);
        recyclerView = findViewById(R.id.recyclerView);
        titleTextView = findViewById(R.id.title);
        nodata = findViewById(R.id.nodata);
        backbtn = findViewById(R.id.search);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        title = COURSEPATH;
        titleTextView.setText(title);

        years.clear();
        populateYears(title.concat(" ").concat(">>").concat(" "));
        backbtn.setOnClickListener(v -> finish());
        retrybtn.setOnClickListener(v -> populateYears(title.concat(" ").concat(">>").concat(" ")));
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

    private void populateYears(String search) {

        try {
            loadinggif.setVisibility(View.VISIBLE);
            StringRequest jsonArrayRequest = new StringRequest(
                    Request.Method.POST,
                    API_URL + "unique-years",
                    response -> {
                        loadinggif.setVisibility(View.GONE);
                        retry_layout.setVisibility(View.GONE);
                        if (response != null) {
                            try {
                                JSONArray jsonArray = new JSONArray(response);
                                int length = jsonArray.length();
                                if (length == 0) {
                                    nodata.setVisibility(View.VISIBLE);
                                    recyclerView.setVisibility(View.GONE);
                                }
                                else {
                                    nodata.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                }
                                years.clear();
                                for (int i = 0; i < length; i++) {
                                    try {
                                        years.add(jsonArray.getString(i));

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        listAdapter = new ListAdapter((names, position, holder) -> {
                            String textViewText = names.get(position);

                            VIDEOVISIBILITY = View.GONE;
                            mQuestionNumber = 1;
                            startActivity(new Intent(mContext, PastQuestionsActivity.class).putExtra("title", title.concat(" ").concat(">>").concat(" ") + textViewText));
                        }, this, years, title);

                        recyclerView.setAdapter(listAdapter);
                    },
                    error -> {
                        loadinggif.setVisibility(View.GONE);
                        retry_layout.setVisibility(View.VISIBLE);
                        myVolleyError(mContext, error);
                    }
            ) {
                @Override
                public Map<String, String> getParams() throws AuthFailureError {

                    String level = search.split((" >> "))[0];
                    String level_en = "";
                    if (level.equals(mContext.getResources().getString(R.string.preschool))) {
                        level_en = "Pre-School";
                    } else if (level.equals(mContext.getResources().getString(R.string.primary_school))) {
                        level_en = "Primary School";
                    } else if (level.equals(mContext.getResources().getString(R.string.jhs))) {
                        level_en = "JHS";
                    } else if (level.equals(mContext.getResources().getString(R.string.shs))) {
                        level_en = "SHS";
                    } else if (level.equals(mContext.getResources().getString(R.string.preuniversity))) {
                        level_en = "Pre-University";
                    } else if (level.equals(mContext.getResources().getString(R.string.university))) {
                        level_en = "University";
                    } else if (level.equals(mContext.getResources().getString(R.string.professional))) {
                        level_en = "Professional";
                    } else if (level.equals(mContext.getResources().getString(R.string.vocational))) {
                        level_en = "Vocational";
                    }

                    Map<String, String> params = new HashMap<>();
                    params.put("coursepath", search.replaceFirst(level, level_en));
                    return params;
                }
            };
            jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(jsonArrayRequest);

        } catch (Exception e) {
            Log.e("My error", e.toString());
        }
    }
}
