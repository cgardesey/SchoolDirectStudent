package com.univirtual.student.materialDialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.univirtual.student.R;
import com.univirtual.student.adapter.ChooseEnrolmentAdapter;
import com.univirtual.student.other.InitApplication;
import com.univirtual.student.realm.RealmEnrolment;
import com.univirtual.student.util.RealmUtility;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;

import static com.univirtual.student.activity.HomeActivity.APITOKEN;
import static com.univirtual.student.constants.keyConst.API_URL;
import static com.univirtual.student.constants.Const.myVolleyError;
import static com.univirtual.student.fragment.EnrolmentParentFragment.initEnrolmentParentFragment;

public class ChooseEnrolmentMaterialDialog extends DialogFragment {

    ArrayList<RealmEnrolment> realmEnrolments = new ArrayList<>();

    public ArrayList<RealmEnrolment> getRealmEnrolments() {
        return realmEnrolments;
    }

    public void setRealmEnrolments(ArrayList<RealmEnrolment> realmEnrolments) {
        this.realmEnrolments = realmEnrolments;
    }


    ProgressDialog mProgress;
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.list_item_choose_enrolment,null);

        mProgress = new ProgressDialog(getActivity());
        mProgress.setMessage(getActivity().getString(R.string.pls_wait));

        TextView cancel = view.findViewById(R.id.cancel);
        TextView ok = view.findViewById(R.id.ok);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        ArrayList<String> enrolmentids = new ArrayList<>();
        for (RealmEnrolment realmEnrolment : realmEnrolments) {
            enrolmentids.add(realmEnrolment.getEnrolmentid());
        }
         ChooseEnrolmentAdapter chooseEnrolmentAdapter = new ChooseEnrolmentAdapter((enrolments, position, holder) -> {
             RealmEnrolment realmEnrolment = enrolments.get(position);
                if (holder.checkBox.isChecked()) {
                    if (enrolmentids.contains(realmEnrolment.getEnrolmentid())) {
                        enrolmentids.remove(realmEnrolment.getEnrolmentid());
                    }
                }
                else {
                    if (!enrolmentids.contains(realmEnrolment.getEnrolmentid())) {
                        enrolmentids.add(realmEnrolment.getEnrolmentid());
                    }
                }
         }, realmEnrolments);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(chooseEnrolmentAdapter);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (realmEnrolments.size() - enrolmentids.size() > 3) {
                    Toast.makeText(getActivity(), "You cannot check more than 3 subjects.", Toast.LENGTH_SHORT).show();
                }
                else if (realmEnrolments.size() - enrolmentids.size() == 0) {
                    Toast.makeText(getActivity(), "You must check at least 1 subject.", Toast.LENGTH_SHORT).show();
                }
                else {
                    mProgress.show();
                    StringRequest stringRequest = new StringRequest(
                            com.android.volley.Request.Method.POST,
                            API_URL + "bulk-unenrol",
                            response -> {
                                if (response != null) {
                                    mProgress.dismiss();
                                    try {
                                        JSONArray jsonArray = new JSONArray(response);
                                        Realm.init(getActivity());
                                        Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                realm.createOrUpdateAllFromJson(RealmEnrolment.class, jsonArray);
                                            }
                                        });
                                        dismiss();
                                        initEnrolmentParentFragment();

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            error -> {
                                error.printStackTrace();
                                myVolleyError(getActivity(), error);
                                mProgress.dismiss();
                                Log.d("Cyrilll", error.toString());
                            }
                    ) {
                        @Override
                        public Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params  = new HashMap<>();

                            for (int i = 0; i < enrolmentids.size(); i++) {
                                params.put("enrolmentid" + String.valueOf(i), enrolmentids.get(i));
                            }
                            return params;
                        }

                        @Override
                        public Map getHeaders() throws AuthFailureError {
                            HashMap headers = new HashMap();
                            headers.put("accept", "application/json");
                            headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(APITOKEN, ""));
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
        });

        // doneBtn.setOnClickListener(doneAction);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        /*Timer myTimer = new Timer();
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
        }, 5);*/
        return builder.create();
    }
}