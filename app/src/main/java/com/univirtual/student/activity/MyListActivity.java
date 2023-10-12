package com.univirtual.student.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.univirtual.student.R;
import com.univirtual.student.adapter.ListAdapter;
import com.univirtual.student.materialDialog.EnrolallMaterialDialog;
import com.univirtual.student.materialDialog.InstitutionLoginMaterialDialog;
import com.univirtual.student.other.InitApplication;
import com.univirtual.student.realm.RealmPayment;
import com.univirtual.student.util.RealmUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;

import static com.univirtual.student.activity.HomeActivity.APITOKEN;
import static com.univirtual.student.activity.HomeActivity.MYUSERID;
import static com.univirtual.student.constants.keyConst.API_URL;
import static com.univirtual.student.constants.Const.myVolleyError;

public class MyListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageView loadinggif;
    ImageView backbtn;
    Button retrybtn;
    LinearLayout retry_layout;
    TextView titleTextView, text;
    ArrayList<String> newList = new ArrayList<>();
    ArrayList<String> list = new ArrayList<>();
    ListAdapter listAdapter;
    String title = "";
    Context mContext;
    ArrayList<String> courses = new ArrayList<>();

    static InstitutionLoginMaterialDialog institutionLoginMaterialDialog = new InstitutionLoginMaterialDialog();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_list);

        mContext = getApplicationContext();
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
        backbtn = findViewById(R.id.search);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        title = getIntent().getStringExtra("title");
        titleTextView.setText(title);

        courses.clear();
        populateNames(title.concat(" ").concat(">>").concat(" "));
        backbtn.setOnClickListener(v -> finish());
        retrybtn.setOnClickListener(v -> populateNames(title.concat(" ").concat(">>").concat(" ")));
    }


    private void populateNames(String search) {

        try {
            loadinggif.setVisibility(View.VISIBLE);
            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    API_URL + "sub-courses",
                    response -> {
                        loadinggif.setVisibility(View.GONE);
                        retry_layout.setVisibility(View.GONE);
                        if (response != null) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray jsonArray = jsonObject.getJSONArray("course_paths");
                                int length = jsonArray.length();
                                if (length == 0) {
                                    startActivity(new Intent(mContext, EnrolmentActivity.class).putExtra("coursepath", title));
                                    finish();
                                }
                                JSONObject institution = null;
                                JSONArray institution_fees = null;
                                if (jsonObject.has("institution") && !jsonObject.isNull("institution")) {
                                    institution = jsonObject.getJSONObject("institution");
                                    if (search.split((" >> ")).length == 2 && !search.contains(" >> Other >> ")) {
                                        if (institution.getInt("internalinstitution") == 0) {
                                            if (institutionLoginMaterialDialog != null && institutionLoginMaterialDialog.isAdded()) {

                                            } else {
                                                try {

                                                    institutionLoginMaterialDialog.setInstitutionid(institution.getString("institutionid"));
                                                    institutionLoginMaterialDialog.setSchol_name(institution.getString("name"));
                                                    institutionLoginMaterialDialog.setLogo_url(institution.getString("logourl"));

                                                    institutionLoginMaterialDialog.setTitle(title);
                                                    institutionLoginMaterialDialog.setCourses(courses);
                                                    institutionLoginMaterialDialog.setJsonArray(jsonArray);
                                                    institutionLoginMaterialDialog.setRecyclerView(recyclerView);

                                                    institutionLoginMaterialDialog.setCancelable(false);
                                                    institutionLoginMaterialDialog.show(getSupportFragmentManager(), "InstitutionLoginMaterialDialog");
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        } else if (jsonObject.has("pending_payment")) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(MyListActivity.this);
                                            builder.setTitle("Processing Payment");
                                            builder.setMessage("You have a processing payment. \n\nPlease try again later");
                                            builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                                finish();
                                            });
                                            builder.setCancelable(false).show();
                                        } else {
                                            if (jsonObject.has("institution_fees")) {
                                                if (jsonObject.has("updated_payment")) {
                                                    Realm.init(getApplicationContext());
                                                    Realm.getInstance(RealmUtility.getDefaultConfig(MyListActivity.this)).executeTransaction(realm -> {
                                                        try {
                                                            realm.createOrUpdateObjectFromJson(RealmPayment.class, jsonObject.getJSONObject("updated_payment"));
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    });
                                                }
                                                institution_fees = jsonObject.getJSONArray("institution_fees");
                                                JSONObject finalInstitution = institution;
                                                EnrolallMaterialDialog enrolallMaterialDialog = new EnrolallMaterialDialog();
                                                if (enrolallMaterialDialog != null && enrolallMaterialDialog.isAdded()) {

                                                } else {
                                                    enrolallMaterialDialog.setInstitutionid(finalInstitution.getString("institutionid"));
                                                    if (jsonObject.has("updated_payment")) {
                                                        enrolallMaterialDialog.setPreviouslyon8coursesmax(!jsonObject.isNull("updated_payment") && jsonObject.getJSONObject("updated_payment").getString("feetype").equals("8_courses_max"));
                                                        enrolallMaterialDialog.setSubscriptionchangerequestapproved(jsonObject.getInt("subscription_change_request_approved") == 1);
                                                    }
                                                    enrolallMaterialDialog.setInstitution_fees(institution_fees);
                                                    enrolallMaterialDialog.setCancelable(false);
                                                    enrolallMaterialDialog.show(getSupportFragmentManager(), "EnrolallMaterialDialog");
                                                }
                                            }
                                        }
                                    }
                                }
                                courses.clear();
                                for (int i = 0; i < length; i++) {
                                    try {
                                        courses.add(jsonArray.getString(i));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                listAdapter = new ListAdapter((names, position, holder) -> {
                                    String textViewText = names.get(position);
                                    startActivity(new Intent(mContext, MyListActivity.class).putExtra("title", title.concat(" ").concat(">>").concat(" ") + textViewText));
                                }, this, courses, title);

                                recyclerView.setAdapter(listAdapter);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
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
                    params.put("userid", PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(MYUSERID, ""));
                    params.put("search", search.replaceFirst(level, level_en));
                    return params;
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

    public static void institutionLogin(Activity activity, String studentidno, String password, String institutionid, String title, ArrayList<String> courses, JSONArray jsonArray, RecyclerView recyclerView) {
        try {


            ProgressDialog mProgress = new ProgressDialog(activity);
            mProgress.setTitle("Signig in...");
            mProgress.setMessage("Please wait...");
            mProgress.setCancelable(false);
            mProgress.setIndeterminate(true);

            mProgress.show();

            JSONObject request = new JSONObject();
            request.put("studentid", PreferenceManager.getDefaultSharedPreferences(activity).getString(MYUSERID, ""));
            request.put("studentno", studentidno);
            request.put("password", password);
            request.put("institutionid", institutionid);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    API_URL + "student-institution-login",
                    request,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            mProgress.dismiss();
                            if (response != null) {

                                try {
                                    if (response.has("user_not_found")) {
                                        mProgress.dismiss();
                                        Toast.makeText(activity, "Student id number not found for your student account.", Toast.LENGTH_SHORT).show();
                                    } else if (response.has("incorrect_password")) {
                                        mProgress.dismiss();
                                        Toast.makeText(activity, "Incorrect password!", Toast.LENGTH_SHORT).show();
                                    } else if (response.has("login_successful")) {
                                        institutionLoginMaterialDialog.dismiss();

                                        int length = jsonArray.length();
                                        courses.clear();
                                        for (int i = 0; i < length; i++) {
                                            try {
                                                courses.add(jsonArray.getString(i));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        recyclerView.setAdapter(new ListAdapter((names, position, holder) -> {
                                            String textViewText = names.get(position);
                                            activity.startActivity(new Intent(activity, MyListActivity.class).putExtra("title", title.concat(" ").concat(">>").concat(" ") + textViewText));
                                        }, activity, courses, title));
                                    }
                                } catch (Throwable t) {
                                    Log.d("My App", "Could not parse malformed JSON: " + t.toString());
                                }
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("hmmm eii", "error");
                            mProgress.dismiss();
                            myVolleyError(activity, error);
                        }
                    }
            ) {
                @Override
                public Map getHeaders() throws AuthFailureError {
                    HashMap headers = new HashMap();
                    headers.put("accept", "application/json");
                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(activity).getString(APITOKEN, ""));
                    return headers;
                }
            };
            ;
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
