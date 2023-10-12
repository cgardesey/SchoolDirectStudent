package com.univirtual.student.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.univirtual.student.R;
import com.univirtual.student.constants.keyConst;
import com.univirtual.student.other.InitApplication;
import com.univirtual.student.realm.RealmUser;
import com.univirtual.student.util.RealmUtility;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;

import static com.univirtual.student.activity.HomeActivity.APITOKEN;
import static com.univirtual.student.activity.HomeActivity.MYUSERID;
import static com.univirtual.student.activity.SigninActivity.isEmailValid;
import static com.univirtual.student.constants.keyConst.API_URL;
import static com.univirtual.student.constants.Const.myVolleyError;


public class RegisterActivity extends Activity {

    private static final String EMAIL_ALREADY_REGISTERED = "0";
    private static RealmUser realmUser = new RealmUser();
    ImageView passwordIcon, confirmPasswordIcon;
    boolean passwordShow, confirmPasswordShow = false;
    Context context;
    private EditText emailField, passwordField, confirmPasswordField;
    private Button login;
    private ProgressDialog mProgress;
    private TextView help;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        context = getApplicationContext();
        mProgress = new ProgressDialog(this);
        mProgress.setTitle("Processing...");
        mProgress.setMessage("Please wait...");
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);

        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        confirmPasswordField = findViewById(R.id.confirmPassword);
        passwordIcon = findViewById(R.id.passwordIcon);
        confirmPasswordIcon = findViewById(R.id.confirmPasswordIcon);
        help = findViewById(R.id.help);

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(RegisterActivity.this)
//                        .setTitle("")
                .setMessage(Html.fromHtml("For help, call <font color='#C01930'>1850</font> on an MTN number and dial <font color='#C01930'>1</font>"))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();

            }
        });

        login = findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        boolean signedIn = !PreferenceManager.getDefaultSharedPreferences(this).getString(MYUSERID, "").equals("");
        if (signedIn) {
            onSignInInit();
        }
        passwordIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passwordShow = !passwordShow;
                if (passwordShow) {
                    passwordField.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hide_password);
                    passwordIcon.setImageBitmap(bitmap);
                } else {
                    passwordField.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.see_password);
                    passwordIcon.setImageBitmap(bitmap);
                }
            }
        });

        confirmPasswordIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmPasswordShow = !confirmPasswordShow;
                if (confirmPasswordShow) {
                    confirmPasswordField.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hide_password);
                    confirmPasswordIcon.setImageBitmap(bitmap);
                } else {
                    confirmPasswordField.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.see_password);
                    confirmPasswordIcon.setImageBitmap(bitmap);
                }
            }
        });
    }

    private void onSignInInit() {
        startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
        finish();
    }

    public void attemptRegister() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        String confirmPassword = confirmPasswordField.getText().toString();
        boolean canRegister = true;

        if (TextUtils.isEmpty(email)) {
            emailField.setError(getString(R.string.error_field_required));
            canRegister = false;
        } else if (!isEmailValid(email)) {
            emailField.setError("Invalid email!");
            canRegister = false;
        } else {
            emailField.setError(null);
//            realmUser.setEmail(email);
        }
        if (TextUtils.isEmpty(password)) {
            passwordField.setError(getString(R.string.error_field_required));
            canRegister = false;
        } else if (!isPasswordValid(password)) {
            passwordField.setError(getString(R.string.error_invalid_password));
            canRegister = false;
        } else if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordField.setError(getString(R.string.error_field_required));
            canRegister = false;
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordField.setError("Passwords do not match!");
            canRegister = false;
        } else {
            passwordField.setError(null);
//            realmUser.setPassword(password);
        }

        if (canRegister) {
            register(email, password, "student");
        }
    }

    public void register(String email, String password, String role) {
        String URL = null;
        try {
            mProgress.show();
            StringRequest stringRequest = new StringRequest(
                    com.android.volley.Request.Method.POST,
                    API_URL + "register",
                    response -> {
                        mProgress.dismiss();
                        if (response != null) {
                            if (response.equals("0")) {
                                Toast.makeText(context, "This email is already registered on School Direct!", Toast.LENGTH_SHORT).show();
                            } else if (response.equals("1")) {
                                new AlertDialog.Builder(RegisterActivity.this)
                                        .setTitle("Success!")
                                        .setMessage("We just sent an email confirmation link to " + emailField.getText() + ". When you receive the email, click on the link to confirm your email. Then you can proceed to login")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                dialogInterface.dismiss();
                                            }
                                        })
                                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                            @Override
                                            public void onDismiss(DialogInterface dialogInterface) {
                                                emailField.getText().clear();
                                                passwordField.getText().clear();
                                                confirmPasswordField.getText().clear();
                                            }
                                        })
                                        .show();
                            }
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Log.d("Cyrilll", error.toString());
                        mProgress.dismiss();
                        myVolleyError(getApplicationContext(), error);
                    }
            ) {
                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("email", email);
                    params.put("password", password);
                    params.put("role", role);
                    return params;
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

    private void fetchUserData(final Context context) {
        String URL;
        try {
            URL = keyConst.API_URL + "users";
//            JSONObject jsonBody = new JSONObject();
            // RequestQueue requestQueue = Volley.newRequestQueue(mContext);

            // Initialize a new JsonArrayRequest instance
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                    Request.Method.GET,
                    URL,
                    null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(final JSONArray response) {
                            Realm.init(context);
                            Realm.getInstance(RealmUtility.getDefaultConfig(RegisterActivity.this)).executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    realm.createOrUpdateAllFromJson(RealmUser.class, response);
                                    Toast.makeText(context, "Successfully registered!", Toast.LENGTH_SHORT).show();
                                    onRegisterInit();
                                }
                            });
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Cyrilll error", error.toString());
                        }
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
            InitApplication.getInstance().addToRequestQueue(jsonArrayRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onRegisterInit() {
        startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
        finish();
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 5;
    }
}