package com.univirtual.student.materialDialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.univirtual.student.R;
import com.univirtual.student.activity.HomeActivity;
import com.univirtual.student.other.InitApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import static com.univirtual.student.activity.HomeActivity.APITOKEN;
import static com.univirtual.student.activity.HomeActivity.MYUSERID;
import static com.univirtual.student.constants.keyConst.API_URL;

public class EnrolallMaterialDialog extends DialogFragment {
    String institutionid = "";
    JSONArray institution_fees = null;
    boolean previouslyon8coursesmax = false;
    boolean subscriptionchangerequestapproved = false;

    public String getInstitutionid() {
        return institutionid;
    }

    public void setInstitutionid(String institutionid) {
        this.institutionid = institutionid;
    }

    public JSONArray getInstitution_fees() {
        return institution_fees;
    }

    public void setInstitution_fees(JSONArray institution_fees) {
        this.institution_fees = institution_fees;
    }

    public boolean isPreviouslyon8coursesmax() {
        return previouslyon8coursesmax;
    }

    public void setPreviouslyon8coursesmax(boolean previouslyon8coursesmax) {
        this.previouslyon8coursesmax = previouslyon8coursesmax;
    }

    public boolean isSubscriptionchangerequestapproved() {
        return subscriptionchangerequestapproved;
    }

    public void setSubscriptionchangerequestapproved(boolean subscriptionchangerequestapproved) {
        this.subscriptionchangerequestapproved = subscriptionchangerequestapproved;
    }

    ProgressDialog mProgress;

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.list_item_enrol_all,null);
        TextView cancel = view.findViewById(R.id.cancel);
        TextView ok = view.findViewById(R.id.ok);
        Spinner max_courses_spinner = view.findViewById(R.id.max_courses_spinner);

        mProgress = new ProgressDialog(getActivity());
        mProgress.setMessage(getActivity().getString(R.string.pls_wait));
        mProgress.setCancelable(false);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof HomeActivity) {
                    dismiss();
                }
                else {
                    getActivity().finish();
                }
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPreviouslyon8coursesmax() && max_courses_spinner.getSelectedItemPosition() == 0 && !subscriptionchangerequestapproved) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Request Subscription Change.");
                    builder.setMessage(Html.fromHtml("You were previously on the <font color='#0000FF'><i>\"maximum 8 subjects\"</i></font> subscription plan.  <br/><br/>Request to change to the <font color='#0000FF'><i>\"maximum 3 subjects\"</i></font> subscription plan?"));
                    builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                        dialog.dismiss();
                    });
                    builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        mProgress.show();
                        StringRequest stringRequest = new StringRequest(
                                Request.Method.POST,
                                API_URL + "subscription-change-request",
                                response -> {
                                    if (response != null) {
                                        mProgress.dismiss();
                                        try {
                                            JSONObject jsonObject = new JSONObject(response);

                                            if (jsonObject.has("request_already_exist")) {
                                                AlertDialog.Builder innerBuilder = new AlertDialog.Builder(getActivity());
                                                innerBuilder.setTitle("Request Pending Approval");
                                                innerBuilder.setMessage(Html.fromHtml("You previously sent a request to change to the <font color='#0000FF'><i>\"maximum 3 subjects\"</i></font> subscription plan.  <br/><br/>Your request is pending approval."));
                                                innerBuilder.setPositiveButton(android.R.string.yes, (innerDialog, innerWhich) -> {
                                                    innerDialog.dismiss();
                                                })
                                                        .setCancelable(false)
                                                        .show();
                                            }
                                            else if (jsonObject.has("request_successfully_sent")) {
                                                AlertDialog.Builder innerBuilder = new AlertDialog.Builder(getActivity());
                                                innerBuilder.setTitle("Request Successfully Sent.");
                                                innerBuilder.setMessage(Html.fromHtml("Subscription change request successfully sent. \n\nYou will receive a mail informing you whether your request is approved. <br/><br/>Until your request is approved, you may choose to pay for the <font color='#0000FF'><i>\"maximum 8 subjects\"</i></font> subscription plan."));
                                                innerBuilder.setPositiveButton(android.R.string.yes, (innerDialog, innerWhich) -> {
                                                    innerDialog.dismiss();
                                                })
                                                        .setCancelable(false)
                                                        .show();
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                },
                                error -> {
                                    mProgress.dismiss();
                                    error.printStackTrace();
                                    Log.d("Cyrilll", error.toString());
                                    //                                myVolleyError(context, error);
                                }
                        ) {
                            @Override
                            public Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<>();
                                params.put("studentid", PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(MYUSERID, ""));
                                params.put("institutionid", institutionid);

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
                    })
                            .setCancelable(false)
                            .show();
                }
                else {
                    JSONObject institution_fee;
                    try {
                        institution_fee = institution_fees.getJSONObject(max_courses_spinner.getSelectedItemPosition());
                        SubscriptionMaterialDialog subscriptionMaterialDialog = new SubscriptionMaterialDialog();
                        if(subscriptionMaterialDialog != null && subscriptionMaterialDialog.isAdded()) {

                        } else {
                            try {
                                subscriptionMaterialDialog.setCoursecurrency(institution_fee.getString("currency"));
                                subscriptionMaterialDialog.setCoursepriceperday(Double.valueOf(institution_fee.getString("price_day")));
                                subscriptionMaterialDialog.setCoursepriceperweek(Double.valueOf(institution_fee.getString("price_week")));
                                subscriptionMaterialDialog.setCoursepricepermonth(Double.valueOf(institution_fee.getString("price")));
                                subscriptionMaterialDialog.setDescription(institution_fee.getString("currency") + institution_fee.getString("price_day") + " " + "per day");
                                subscriptionMaterialDialog.setInstitutionid(institution_fee.getString("institutionid"));
                                subscriptionMaterialDialog.setFeetype(institution_fee.getString("feetype"));
                                subscriptionMaterialDialog.show(getChildFragmentManager(), "SubscriptionMaterialDialog");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // doneBtn.setOnClickListener(doneAction);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        Timer myTimer = new Timer();
        /*myTimer.schedule(new TimerTask() {
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