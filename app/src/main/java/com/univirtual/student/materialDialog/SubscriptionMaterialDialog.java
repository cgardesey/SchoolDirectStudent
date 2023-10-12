package com.univirtual.student.materialDialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.univirtual.student.R;
import com.univirtual.student.activity.SelectResourceActivity;
import com.univirtual.student.other.InitApplication;
import com.univirtual.student.realm.RealmEnrolment;
import com.univirtual.student.realm.RealmInstitution;
import com.univirtual.student.realm.RealmPayment;
import com.univirtual.student.util.RealmUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;

import static com.univirtual.student.activity.HomeActivity.APITOKEN;
import static com.univirtual.student.activity.HomeActivity.MYUSERID;
import static com.univirtual.student.constants.keyConst.API_URL;
import static com.univirtual.student.constants.Const.myVolleyError;
import static com.univirtual.student.fragment.EnrolmentParentFragment.fragmentManager;
import static com.univirtual.student.fragment.EnrolmentParentFragment.initEnrolmentParentFragment;

/**
 * Created by Nana on 10/22/2017.
 */

public class SubscriptionMaterialDialog extends DialogFragment {

    private static final String TAG = "SubscriptionMaterialDialog";


    Context context;
    public static Spinner course_fees_duration_spinner, app_usage_fees_duration_spinner;
    TextView pay, course_fees_currency_text, course_fees_amount_text, app_usage_fees_currency_text, app_usage_fees_amount_text, total_fees_currency_text, total_amount_text;
    LinearLayout coursefeeslayout, appusagefeeslayout;
    EditText number;
    Double coursepriceperday = -0.1;
    Double coursepriceperweek = -0.1;
    Double coursepricepermonth = -0.1;
    String coursecurrency;

    Double appusagepriceperday = -0.1;
    Double appusagepriceperweek = -0.1;
    Double appusagepricepermonth = -0.1;

    String appusagecurrency;

    String description;
    String appuserfeedescription;
    String enrolmentid;
    String institutionid;
    String feetype;

    boolean markpreviouspaymentexpired;

    ProgressDialog progressDialog;

    SubscriptionMaterialDialog subscriptionMaterialDialog;

    public Double getCoursepriceperday() {
        return coursepriceperday;
    }

    public void setCoursepriceperday(Double coursepriceperday) {
        this.coursepriceperday = coursepriceperday;
    }

    public Double getCoursepriceperweek() {
        return coursepriceperweek;
    }

    public void setCoursepriceperweek(Double coursepriceperweek) {
        this.coursepriceperweek = coursepriceperweek;
    }

    public Double getCoursepricepermonth() {
        return coursepricepermonth;
    }

    public void setCoursepricepermonth(Double coursepricepermonth) {
        this.coursepricepermonth = coursepricepermonth;
    }

    public String getCoursecurrency() {
        return coursecurrency;
    }

    public static String getTAG() {
        return TAG;
    }

    public Double getAppusagepriceperday() {
        return appusagepriceperday;
    }

    public void setAppusagepriceperday(Double appusagepriceperday) {
        this.appusagepriceperday = appusagepriceperday;
    }

    public Double getAppusagepriceperweek() {
        return appusagepriceperweek;
    }

    public void setAppusagepriceperweek(Double appusagepriceperweek) {
        this.appusagepriceperweek = appusagepriceperweek;
    }

    public Double getAppusagepricepermonth() {
        return appusagepricepermonth;
    }

    public void setAppusagepricepermonth(Double appusagepricepermonth) {
        this.appusagepricepermonth = appusagepricepermonth;
    }

    public String getAppusagecurrency() {
        return appusagecurrency;
    }

    public void setAppusagecurrency(String appusagecurrency) {
        this.appusagecurrency = appusagecurrency;
    }

    public void setCoursecurrency(String coursecurrency) {
        this.coursecurrency = coursecurrency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAppuserfeedescription() {
        return appuserfeedescription;
    }

    public void setAppuserfeedescription(String appuserfeedescription) {
        this.appuserfeedescription = appuserfeedescription;
    }

    public String getEnrolmentid() {
        return enrolmentid;
    }

    public void setEnrolmentid(String enrolmentid) {
        this.enrolmentid = enrolmentid;
    }

    public String getInstitutionid() {
        return institutionid;
    }

    public void setInstitutionid(String institutionid) {
        this.institutionid = institutionid;
    }

    public String getFeetype() {
        return feetype;
    }

    public void setFeetype(String feetype) {
        this.feetype = feetype;
    }

    public boolean isMarkpreviouspaymentexpired() {
        return markpreviouspaymentexpired;
    }

    public void setMarkpreviouspaymentexpired(boolean markpreviouspaymentexpired) {
        this.markpreviouspaymentexpired = markpreviouspaymentexpired;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.list_item_subscription, null);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(getString(R.string.processing));
        progressDialog.setMessage(getString(R.string.pls_wait));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);

        course_fees_duration_spinner = view.findViewById(R.id.course_fees_duration_spinner);
        app_usage_fees_duration_spinner = view.findViewById(R.id.app_usage_fees_duration_spinner);
        course_fees_amount_text = view.findViewById(R.id.course_fees_amount_text);
        app_usage_fees_amount_text = view.findViewById(R.id.app_usage_fees_amount_text);
        total_amount_text = view.findViewById(R.id.total_amount_text);
        course_fees_currency_text = view.findViewById(R.id.course_fees_currency_text);
        app_usage_fees_currency_text = view.findViewById(R.id.app_usage_fees_currency_text);
        total_fees_currency_text = view.findViewById(R.id.total_fees_currency_text);
        pay = view.findViewById(R.id.pay);
        number = view.findViewById(R.id.number);
        appusagefeeslayout = view.findViewById(R.id.appusagefeeslayout);
        coursefeeslayout = view.findViewById(R.id.coursefeeslayout);

        if (coursepriceperday == -0.1) {
            coursefeeslayout.setVisibility(View.GONE);
            total_amount_text.setText(String.valueOf(appusagepriceperday));
            description = "";
        } else {
            course_fees_amount_text.setText(String.valueOf(getCoursepriceperday()));
        }
        if (appusagepriceperday == -0.1) {
            appusagefeeslayout.setVisibility(View.GONE);
            total_amount_text.setText(String.valueOf(coursepriceperday));
            appuserfeedescription = "";
        } else {
            app_usage_fees_amount_text.setText(String.valueOf(getAppusagepriceperday()));
        }
        total_amount_text.setText(String.valueOf(Double.parseDouble(course_fees_amount_text.getText().toString()) + Double.parseDouble(app_usage_fees_amount_text.getText().toString())));

        subscriptionMaterialDialog = SubscriptionMaterialDialog.this;

        pay.setOnClickListener(v -> {
            /*if (duration.getSelectedItemPosition() == 0) {
                TextView errorText = (TextView) duration.getSelectedView();
                errorText.setError("");
                errorText.setTextColor(Color.RED);
            }*/


            String mobileno = number.getText().toString();
            if (TextUtils.isEmpty(mobileno)) {
                number.setError(getString(R.string.error_field_required));
            } else if (mobileno.length() != 12) {
                Toast.makeText(getActivity(), getString(R.string.invalid_number), Toast.LENGTH_LONG).show();
            } else {
                savePayment();
            }
        });

        course_fees_duration_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                switch (position) {
                    case 0:
                        course_fees_amount_text.setText(String.format("%.2f", getCoursepriceperday()));
                        description = getCoursecurrency() + String.format("%.2f", getCoursepriceperday()) + " " + "per day";
                        break;
                    case 1:
                        course_fees_amount_text.setText(String.format("%.2f", getCoursepriceperweek()));
                        description = getCoursecurrency() + String.format("%.2f", getCoursepriceperweek()) + " " + "per week";
                        break;
                    case 2:
                        course_fees_amount_text.setText(String.format("%.2f", getCoursepricepermonth()));
                        description = getCoursecurrency() + String.format("%.2f", getCoursepricepermonth()) + " " + "per month";
                        break;
                }
                if (appusagefeeslayout.getVisibility() == View.GONE) {
                    total_amount_text.setText(String.format("%.2f", Double.parseDouble(course_fees_amount_text.getText().toString())));
                } else {
                    total_amount_text.setText(String.format("%.2f", Double.parseDouble(course_fees_amount_text.getText().toString()) + Double.parseDouble(app_usage_fees_amount_text.getText().toString())));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });

        app_usage_fees_duration_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                switch (position) {
                    case 0:
                        app_usage_fees_amount_text.setText(String.format("%.2f", getAppusagepriceperday()));
                        appuserfeedescription = getAppusagecurrency() + String.format("%.2f", getAppusagepriceperday()) + " " + "per day";
                        break;
                    case 1:
                        app_usage_fees_amount_text.setText(String.format("%.2f", getAppusagepriceperweek()));
                        appuserfeedescription = getAppusagecurrency() + String.format("%.2f", getAppusagepriceperweek()) + " " + "per week";
                        break;
                    case 2:
                        app_usage_fees_amount_text.setText(String.format("%.2f", getAppusagepricepermonth()));
                        appuserfeedescription = getAppusagecurrency() + String.format("%.2f", getAppusagepricepermonth()) + " " + "per month";
                        break;
                }
                if (coursefeeslayout.getVisibility() == View.GONE) {
                    total_amount_text.setText(String.format("%.2f", Double.parseDouble(app_usage_fees_amount_text.getText().toString())));
                } else {
                    total_amount_text.setText(String.format("%.2f", Double.parseDouble(course_fees_amount_text.getText().toString()) + Double.parseDouble(app_usage_fees_amount_text.getText().toString())));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
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
                getActivity().runOnUiThread(() -> getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)));
            }
        }, 5);

        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getActivity() instanceof SelectResourceActivity) {
            getActivity().finish();
        }
        else {
            dialog.dismiss();
        }
    }

    private void savePayment() {
        try {
            JSONObject request = new JSONObject();
            request.put("msisdn", number.getText().toString());
            request.put("countrycode", "GH");
            request.put("network", "MTNGHANA");
            request.put("currency", "GHS");
            request.put("amount", Double.parseDouble(total_amount_text.getText().toString()));
            request.put("description", description);
            request.put("appuserfeedescription", appuserfeedescription);
            request.put("payerid", PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(MYUSERID, ""));
            request.put("markpreviouspaymentexpired", appuserfeedescription);
            if (enrolmentid != null && !enrolmentid.equals("")) {
                request.put("enrolmentid", enrolmentid);
            }
            if (institutionid != null && !institutionid.equals("")) {
                request.put("institutionid", institutionid);
            }
            if (feetype != null && !feetype.equals("")) {
                request.put("feetype", feetype);
            }
            progressDialog.show();

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    API_URL + "payments",
                    request,
                    response -> {
                        if (response != null) {
                            if (response.has("not_registered")) {
                                progressDialog.dismiss();
                                new AlertDialog.Builder(getActivity())
                                        .setTitle(getString(R.string.not_registered))
                                        .setMessage(getString(R.string.number_is_not_registered_for_mobile_money))
                                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {

                                        })
                                        .show();
                            }
                            else if (response.has("already_subscribed")) {
                                progressDialog.dismiss();
                                Realm.init(getActivity());
                                Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
                                    try {
                                        if (!response.getJSONObject("payment").isNull("paymentref") && response.getJSONObject("payment").getString("paymentref") != "null") {
                                            realm.createOrUpdateObjectFromJson(RealmPayment.class, response.getJSONObject("payment"));
                                        }
                                        if (response.has("enrolment")) {
                                            realm.createOrUpdateObjectFromJson(RealmEnrolment.class, response.getJSONObject("enrolment"));
                                        } else if (response.has("institution")) {
                                            realm.createOrUpdateObjectFromJson(RealmInstitution.class, response.getJSONObject("institution"));
                                            realm.createOrUpdateAllFromJson(RealmEnrolment.class, response.getJSONArray("institution_enrolments"));
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                });
                                if (fragmentManager != null) {
                                    initEnrolmentParentFragment();
                                }
                                new AlertDialog.Builder(getActivity())
                                        .setTitle(getString(R.string.already_subscribed))
                                        .setMessage(enrolmentid != null && !enrolmentid.equals("") ? getString(R.string.your_are_already_subscribed) : "You are already subscribed to this institution.")
                                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                            subscriptionMaterialDialog.dismiss();
                                        })
                                        .show();
                            }else if (response.has("wait_time")) {
                                progressDialog.dismiss();
                                new AlertDialog.Builder(getActivity())
                                        .setTitle(getString(R.string.pending_payment))
//                                            .setMessage(getWaitTimeMsg(response.getInt("wait_time")))
                                        .setMessage(getString(R.string.try_again_later))
                                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {

                                        })
                                        .show();
                            } else if (response.has("current_payment")) {
                                Realm.init(getActivity());
                                Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
                                    try {
                                        if (!response.getJSONObject("current_payment").isNull("paymentref") && response.getJSONObject("current_payment").getString("paymentref") != "null") {
                                            realm.createOrUpdateObjectFromJson(RealmPayment.class, response.getJSONObject("current_payment"));
                                        }
                                        if (!response.getJSONObject("prev_payment").isNull("paymentref") && response.getJSONObject("prev_payment").getString("paymentref") != "null") {
                                            realm.createOrUpdateObjectFromJson(RealmPayment.class, response.getJSONObject("prev_payment"));
                                        }
                                        pay(getActivity(), response.getJSONObject("current_payment").getString("paymentid"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                });
                                if (fragmentManager != null) {
                                    initEnrolmentParentFragment();
                                }
                            } else {
                                Realm.init(getActivity());
                                Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
                                    try {
                                        if (!response.getJSONObject("stored_payment").isNull("paymentref") && response.getJSONObject("stored_payment").getString("paymentref") != "null") {
                                            realm.createOrUpdateObjectFromJson(RealmPayment.class, response.getJSONObject("stored_payment"));
                                        }
                                        pay(getActivity(), response.getJSONObject("stored_payment").getString("paymentid"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                });
                                /*if (fragmentManager != null) {
                                    initEnrolmentParentFragment();
                                }*/
                            }
                        }
                    },
                    error -> {
                        progressDialog.dismiss();
                        myVolleyError(getActivity(), error);
                    }
            ) {
                /** Passing some request headers* */
                @Override
                public Map getHeaders() throws AuthFailureError {
                    HashMap headers = new HashMap();
                    headers.put("accept", "application/json");
                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(APITOKEN, ""));
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
            Log.e("My error", e.toString());
        }
    }

    private String getWaitTimeMsg(int wait_time) {
        if (wait_time < 1) {
            return getString(R.string.already_have_a_processing_payment);
        } else {
            return getString(R.string.already_have_a_processing_payment) + " " + wait_time + " " + getMinutesString(wait_time) + " " + getString(R.string.before_trying_again);
        }
    }

    private String getMinutesString(int wait_time) {
        return wait_time == 1 ? getString(R.string.minute) : getString(R.string.minutes);
    }


    private void pay(Activity activity, String paymentid) {
        progressDialog.dismiss();
        new AlertDialog.Builder(activity)
                .setTitle(getString(R.string.success))
                .setMessage(getString(R.string.dial_170))

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton("Pay", (dialog, which) -> {
                    try {
                        JSONObject request = new JSONObject();
                        request.put("paymentid", paymentid);

                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                                Request.Method.POST,
                                API_URL + "payments/pay",
                                request,
                                response -> {
                                    if (response != null) {
                                        Realm.init(activity);
                                        Realm.getInstance(RealmUtility.getDefaultConfig(activity)).executeTransaction(realm -> {
                                            realm.createOrUpdateObjectFromJson(RealmPayment.class, response);
                                        });
                                        if (activity instanceof SelectResourceActivity) {
                                            activity.finish();
                                        }
                                    }
                                },
                                error -> {
                                    progressDialog.dismiss();
                                    myVolleyError(getActivity(), error);
                                }
                        ) {
                            /** Passing some request headers* */
                            @Override
                            public Map getHeaders() throws AuthFailureError {
                                HashMap headers = new HashMap();
                                headers.put("accept", "application/json");
                                headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(APITOKEN, ""));
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
                        Log.e("My error", e.toString());
                    }
                    dismiss();
                    subscriptionMaterialDialog.dismiss();
                    if (fragmentManager != null) {
                        initEnrolmentParentFragment();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
//                                        .setNegativeButton(android.R.string.no, null)
//                                        .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private String getUpdatedPrice(String price) {
        switch (course_fees_duration_spinner.getSelectedItemPosition()) {
            case 0:
                return price + " per week";
            case 1:
                return String.valueOf(Double.parseDouble(price) * 4) + " per month";
        }
        return null;
    }
}