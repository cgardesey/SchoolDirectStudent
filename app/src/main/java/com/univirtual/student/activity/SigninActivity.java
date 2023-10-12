package com.univirtual.student.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.univirtual.student.R;
import com.univirtual.student.materialDialog.PasswordResetMaterialDialog;
import com.univirtual.student.other.InitApplication;
import com.univirtual.student.util.RealmUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;

import static com.univirtual.student.activity.HomeActivity.APITOKEN;
import static com.univirtual.student.activity.HomeActivity.GUID;
import static com.univirtual.student.activity.HomeActivity.MYUSERID;
import static com.univirtual.student.activity.HomeActivity.persistAll;
import static com.univirtual.student.constants.keyConst.API_URL;
import static com.univirtual.student.constants.keyConst.APP_HASH;
import static com.univirtual.student.constants.Const.isNetworkAvailable;
import static com.univirtual.student.constants.Const.myVolleyError;


public class SigninActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "LogMeIn";
    static int modelsfetched = 0;
    ImageView passwordIcon;
    TextView register, forgotpassword;
    boolean passwordShow = false;
    Context context;
    private EditText emailField, passwordField;
    private Button login;
    private ProgressDialog mProgress;
    private static final int RC_SIGN_IN = 110;
    SignInButton google_button;
    RelativeLayout facebook_button;
    private GoogleApiClient mGoogleApiClient;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        context = getApplicationContext();

        FragmentManager fm = getSupportFragmentManager();
        final PasswordResetMaterialDialog passwordResetMaterialDialog = new PasswordResetMaterialDialog();

        mProgress = new ProgressDialog(this);
        mProgress.setTitle("Signing in...");
        mProgress.setMessage("Please wait...");
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);

        google_button = findViewById(R.id.google_button);
        facebook_button = findViewById(R.id.facebook_button);
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        passwordIcon = findViewById(R.id.passwordIcon);


        // Add code to print out the key hash
        String appHash;
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                appHash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.d("KeyHash:", appHash);
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {


        }


        login = findViewById(R.id.login);
        register = findViewById(R.id.register);
        forgotpassword = findViewById(R.id.forgotpassword);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignin();
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });
        forgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (passwordResetMaterialDialog != null && passwordResetMaterialDialog.isAdded()) {

                } else {
                    passwordResetMaterialDialog.show(getSupportFragmentManager(), "PasswordResetMaterialDialog");
                }
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        RequestData();
                    }

                    @Override
                    public void onCancel() {
                        // App code
                        mProgress.dismiss();
                        Log.d("sdf42ty", "onCancel");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                        mProgress.dismiss();

                        if (exception instanceof FacebookAuthorizationException) {
                            if (AccessToken.getCurrentAccessToken() != null) {
                                LoginManager.getInstance().logOut();

                                if (isNetworkAvailable(context)) {
                                    mProgress.show();
                                    LoginManager.getInstance().logInWithReadPermissions(SigninActivity.this, Arrays.asList("public_profile", "email"));
                                } else {
                                    Toast.makeText(context, context.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                                }
                            }
                            return;
                        }



                        AlertDialog.Builder builder = new AlertDialog.Builder(SigninActivity.this);
                        builder.setTitle("Error.");
                        builder.setMessage(exception.getLocalizedMessage());
                        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            dialog.dismiss();
                        }).show();
                        Log.d("sdf42ty", "facebook onError: " + exception.getLocalizedMessage());
                    }
                });

        facebook_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNetworkAvailable(context)) {
                    mProgress.show();
                    LoginManager.getInstance().logInWithReadPermissions(SigninActivity.this, Arrays.asList("public_profile", "email"));
                } else {
                    Toast.makeText(context, context.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                }
            }
        });

        google_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNetworkAvailable(context)) {
                    view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click));
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                    mProgress.show();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                } else {
                    Toast.makeText(context, context.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                }
            }
        });

        setGooglePlusButtonText(google_button, "Sign in with Google");
        /*boolean signedIn = !PreferenceManager.getDefaultSharedPreferences(this).getString(MYUSERID, "").equals("");
        if (signedIn) {
            onSignInInit();
        }*/
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
    }

    private void RequestData() {
        AccessToken currentAccessToken = AccessToken.getCurrentAccessToken();
        GraphRequest request = GraphRequest.newMeRequest(currentAccessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {

                final JSONObject json = response.getJSONObject();
                try {
                    if (json != null) {
                        if (!json.isNull("email")) {
                            Signin(json.getString("email"), true, json.getString("id"));
                        } else {
                            mProgress.dismiss();
                            AlertDialog.Builder builder = new AlertDialog.Builder(SigninActivity.this);
                            builder.setTitle("Error Retrieving email");
                            builder.setMessage(Html.fromHtml("Unable to retrieve the email address associated with your facebook account.<br><br> <u>Possible Fixes:</u><br><br> 1. Link your facebook account to an email address. <br>2. Allow access to you email address in facebook settings."));
                            builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                dialog.dismiss();
                            }).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(TAG, e.getLocalizedMessage());
                    mProgress.dismiss();
                }
            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,email,picture");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void attemptSignin() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        boolean canLogin = true;

        if (TextUtils.isEmpty(email)) {
            emailField.setError(getString(R.string.error_field_required));
            canLogin = false;
        } else if (!isEmailValid(email)) {
            emailField.setError("Invalid email!");
            canLogin = false;
        } else {
            emailField.setError(null);
        }
        if (TextUtils.isEmpty(password)) {
            passwordField.setError(getString(R.string.error_field_required));
            canLogin = false;
        } else {
            passwordField.setError(null);
        }

        if (canLogin) {
            mProgress.show();
            Signin(email, false, password);
        }
    }

    public void Signin(String email, boolean external_login, String password) {
        try {
            JSONObject request = new JSONObject();

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int heightPixels = displayMetrics.heightPixels;
            int widthPixels = displayMetrics.widthPixels;
            request.put("apphash", APP_HASH);
            request.put("osversion", System.getProperty("os.version"));
            request.put("sdkversion", android.os.Build.VERSION.SDK_INT);
            request.put("device", android.os.Build.DEVICE);
            request.put("devicemodel", android.os.Build.MODEL);
            request.put("deviceproduct", android.os.Build.PRODUCT);
            request.put("manufacturer", android.os.Build.MANUFACTURER);
            request.put("androidid", Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));
            request.put("versionrelease", Build.VERSION.RELEASE);
            request.put("deviceheight", String.valueOf(heightPixels));
            request.put("devicewidth", String.valueOf(widthPixels));
            request.put("email", email);
            if (external_login) {
                request.put("external_login", true);
            }
            request.put("password", password);
            String guid = UUID.randomUUID().toString();
            request.put("guid", guid);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    API_URL + "login",
                    request,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
//                            Toast.makeText(context, "Testing toast", Toast.LENGTH_SHORT).show();
                            //mProgress.dismiss();
                            if (response != null) {

                                try {
                                    if (response.has("user_not_found")) {
                                        mProgress.dismiss();
                                        Toast.makeText(context, "This email is not registered on School Direct.", Toast.LENGTH_SHORT).show();
                                    } else if (response.has("linked_to_instructor_account")) {
                                        mProgress.dismiss();
                                        Toast.makeText(context, "This email cannot be used because it is linked to an instructor account.", Toast.LENGTH_LONG).show();
                                    } else if (response.has("email_not_verified")) {
                                        mProgress.dismiss();
//                                        Toast.makeText(context, "This email is not verified.", Toast.LENGTH_SHORT).show();
                                        new AlertDialog.Builder(SigninActivity.this)
                                                .setTitle("Email not verified!")
                                                .setMessage("Resend verification link?")
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        new SendEmailVerificationLinkAsync(SigninActivity.this).execute(email, password);
                                                        dialogInterface.dismiss();
                                                    }
                                                })
                                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                        dialogInterface.dismiss();
                                                    }
                                                })
                                                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                    @Override
                                                    public void onDismiss(DialogInterface dialogInterface) {

                                                    }
                                                })
                                                .show();
                                    } else if (response.has("incorrect_password")) {
                                        mProgress.dismiss();
                                        Toast.makeText(context, "Incorrect password!", Toast.LENGTH_SHORT).show();
                                    } else if (response.has("successful")) {

                                        Realm.init(getApplicationContext());
                                        Realm.getInstance(RealmUtility.getDefaultConfig(SigninActivity.this)).executeTransaction(realm -> {
                                            try {
                                                persistAll(realm, response);

                                                PreferenceManager
                                                        .getDefaultSharedPreferences(getApplicationContext())
                                                        .edit()
                                                        .putString(MYUSERID, response.getString("userid"))
                                                        .putString(APITOKEN, response.getString("api_token"))
                                                        .putString(GUID, guid)
                                                        .apply();

                                                mProgress.dismiss();
                                                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                                finish();
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        });
                                    }
                                } catch (Throwable t) {
//                                    Toast.makeText(context, t.toString(), Toast.LENGTH_LONG).show();
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
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(jsonObjectRequest);

        } catch (Exception e) {
            mProgress.dismiss();
            e.printStackTrace();
        }
    }

    private void onSignInInit() {
        boolean isFromEnrolment = getIntent().getBooleanExtra("isFromEnrolment", false);

        if (isFromEnrolment) {
            Intent data = new Intent();
            setResult(Activity.RESULT_OK, data);
        } else {
            startActivity(new Intent(SigninActivity.this, HomeActivity.class));
        }
        finish();
    }

    public void register() {
        startActivity(new Intent(SigninActivity.this, RegisterActivity.class));
    }

    public static boolean isEmailValid(String email) {
        String regExpn =
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                        + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                        + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        return matcher.matches();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("bbbbb", "onConnectionFailed:" + connectionResult);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            GoogleSignInAccount acct = result.getSignInAccount();
            if (acct == null) {
                mProgress.dismiss();
                Log.d("sdf42ty", "Google onError: " + result);
                Toast.makeText(context, "Signin error!", Toast.LENGTH_SHORT).show();
            } else {
                Signin(acct.getEmail(), true, acct.getId());
            }
        }
    }

    protected void setGooglePlusButtonText(SignInButton signInButton, String buttonText) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                return;
            }
        }
    }

    public class SendEmailVerificationLinkAsync extends AsyncTask<String, Void, String> {

        private Context context;

        public SendEmailVerificationLinkAsync(Context context) {
            this.context = context;
        }

        protected void onPreExecute() {
            mProgress.setMessage("Please wait...");
            mProgress.show();
        }

        @Override
        protected String doInBackground(String... arg0) {
            mProgress.dismiss();
            try {
                String email = arg0[0];

                String link = API_URL + "resend-verification-link";
                String data = URLEncoder.encode("email", "UTF-8") + "=" +
                        URLEncoder.encode(email, "UTF-8");


                URL url = new URL(link);
                URLConnection conn = url.openConnection();

                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                wr.write(data);
                wr.flush();

                BufferedReader reader = new BufferedReader(new
                        InputStreamReader(conn.getInputStream()));

                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    break;
                }

                return sb.toString();
            } catch (Exception e) {
                return "" + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            mProgress.dismiss();
            if (result != null) {
                if (result.equals("0")) {
                    Toast.makeText(context, "Email verification link successfully sent.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}