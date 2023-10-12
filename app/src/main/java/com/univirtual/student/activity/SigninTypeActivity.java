package com.univirtual.student.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.univirtual.student.R;
import com.univirtual.student.realm.RealmUser;


public class SigninTypeActivity extends Activity {

    private static final String EMAIL_ALREADY_REGISTERED = "0";
    private static RealmUser realmUser = new RealmUser();
    ImageView passwordIcon, confirmPasswordIcon;
    boolean passwordShow, confirmPasswordShow = false;
    Context context;
    private RelativeLayout registeredonplatform, notregisteredonplatform;
    private Button login;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin_type);

        context = getApplicationContext();
        mProgress = new ProgressDialog(this);
        mProgress.setTitle("Processing...");
        mProgress.setMessage("Please wait...");
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);

        registeredonplatform = findViewById(R.id.registeredonplatform);

        notregisteredonplatform = findViewById(R.id.notregisteredonplatform);
        notregisteredonplatform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), SigninActivity.class));
            }
        });


    }

    /*public void Register(String email, String password) {
        String URL = null;
        try {
            URL = API_URL + "users";
            JSONObject request = new JSONObject();
            request.put("email", email);
            request.put("password", password);
            request.put("role", "student");

            mProgress.show();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    URL,
                    request,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("Cyrilll", response.toString());
                            mProgress.dismiss();
                            if (response != null) {

                                try {
                                    int status = response.getInt("status");
                                    switch (status) {
                                        case EMAIL_ALREADY_EXISTS:
                                            Toast.makeText(context, "This email is already registered on School Direct.", Toast.LENGTH_SHORT).show();
                                            break;
                                        case SUCCESSFUL:
                                            String apitoken = response.getString("api_token");
                                            String userid = response.getString("userid");
                                            PreferenceManager
                                                    .getDefaultSharedPreferences(context)
                                                    .edit()
                                                    .putString(MYUSERID, userid)
                                                    .putString(APITOKEN, apitoken)
                                                    .apply();
                                            fetchUserData(context);
                                            break;
                                    }

                                } catch (Throwable t) {
                                    Toast.makeText(context, t.toString(), Toast.LENGTH_LONG).show();
                                    Log.d("My App", "Could not parse malformed JSON: " + t.toString());
                                }
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            mProgress.dismiss();
                            myVolleyError(context, error);
                        }
                    }
            );
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(jsonObjectRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}