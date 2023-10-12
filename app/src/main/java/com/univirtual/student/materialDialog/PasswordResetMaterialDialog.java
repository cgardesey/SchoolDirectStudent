package com.univirtual.student.materialDialog;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.univirtual.student.R;
import com.univirtual.student.constants.Const;
import com.univirtual.student.other.InitApplication;
import com.univirtual.student.realm.RealmUser;
import com.univirtual.student.util.RealmUtility;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;

import static com.univirtual.student.activity.HomeActivity.MYUSERID;
import static com.univirtual.student.activity.SigninActivity.isEmailValid;
import static com.univirtual.student.constants.keyConst.BASE_URL;
import static com.univirtual.student.constants.Const.SUCCESSFUL;
import static com.univirtual.student.constants.Const.myVolleyError;

/**
 * Created by Nana on 10/22/2017.
 */

public class PasswordResetMaterialDialog extends DialogFragment {
    private static final String TAG = "PasswordResetMaterialDialog";

    Button cancel, continu;
    EditText emailField;
    private ProgressDialog mProgress;

    boolean initiatedBySettingsFrag;

    public boolean isInitiatedBySettingsFrag() {
        return initiatedBySettingsFrag;
    }

    public void setInitiatedBySettingsFrag(boolean initiatedBySettingsFrag) {
        this.initiatedBySettingsFrag = initiatedBySettingsFrag;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.list_item_reset_password, null);

        mProgress = new ProgressDialog(getContext());
        mProgress.setTitle("Processing...");
        mProgress.setMessage("Please wait...");
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);

        cancel = view.findViewById(R.id.cancel);
        continu = view.findViewById(R.id.continu);
        emailField = view.findViewById(R.id.email);

        Realm.getInstance(RealmUtility.getDefaultConfig(getContext())).executeTransaction(realm -> {
            RealmUser realmUser = realm.where(RealmUser.class).equalTo("userid", PreferenceManager.getDefaultSharedPreferences(getContext()).getString(MYUSERID, "")).findFirst();
            if (realmUser != null) {
                emailField.setText(realmUser.getEmail());
                emailField.setEnabled(false);
            }
            else {
                emailField.setEnabled(true);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        continu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRequestResetLink();
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        //  builder.setCancelable(false);
        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // If you want to modify a view in your Activity
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    }
                });
            }
        }, 5);
        return builder.create();
    }

    public void attemptRequestResetLink() {
        String email = emailField.getText().toString();
        boolean canrequestresetlink = true;

        if (TextUtils.isEmpty(email)) {
            emailField.setError(getString(R.string.error_field_required));
            canrequestresetlink = false;
        } else if (!isEmailValid(email)) {
            emailField.setError("Invalid email!");
            canrequestresetlink = false;
        } else {
            emailField.setError(null);
        }

        if (canrequestresetlink) {
            RequestPasswordReset(email);
        }
    }

    public void RequestPasswordReset(final String email) {
        String URL = null;
        try {
            URL = BASE_URL + "password/reset";
            JSONObject request = new JSONObject();
            request.put("email", email);

            mProgress.show();
            Log.d("Cyrilll", URL);
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
                                        case Const.USER_NOT_FOUND:
                                            Toast.makeText(getContext(), "This email is not registered on School Direct.", Toast.LENGTH_SHORT).show();
                                            break;
                                        case SUCCESSFUL:
                                            new AlertDialog.Builder(getContext())
                                                    .setTitle("Success!")
                                                    .setMessage("We just sent a password reset email to " + email + ". When you receive the email, click on the link to reset your password.")
                                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            dismiss();
                                                            /*if (isInitiatedBySettingsFrag()) {
                                                                PreferenceManager
                                                                        .getDefaultSharedPreferences(getActivity())
                                                                        .edit()
                                                                        .putString(MYUSERID, "")
                                                                        .putString(APITOKEN, "")
                                                                        .putString(GUID, "")
                                                                        .apply();
                                                                Realm.init(getContext());
                                                                Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> realm.deleteAll());
                                                                startActivity(new Intent(getActivity(), SigninActivity.class));
                                                                getActivity().finish();
                                                            }*/
                                                        }
                                                    })
                                                    .show();
                                            break;
                                    }

                                } catch (Throwable t) {
                                    Toast.makeText(getContext(), t.toString(), Toast.LENGTH_LONG).show();
                                    Log.d("My App", "Could not parse malformed JSON: " + t.toString());
                                }
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            mProgress.dismiss();
                            myVolleyError(getContext(), error);
                        }
                    }
            );
            InitApplication.getInstance().addToRequestQueue(jsonObjectRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}