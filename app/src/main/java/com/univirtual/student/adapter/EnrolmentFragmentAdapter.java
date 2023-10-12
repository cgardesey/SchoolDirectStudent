package com.univirtual.student.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.messaging.FirebaseMessaging;
import com.univirtual.student.R;
import com.univirtual.student.activity.HomeActivity;
import com.univirtual.student.activity.SelectResourceActivity;
import com.univirtual.student.constants.Const;
import com.univirtual.student.materialDialog.RatingMaterialDialog;
import com.univirtual.student.materialDialog.SubscriptionMaterialDialog;
import com.univirtual.student.other.InitApplication;
import com.univirtual.student.realm.RealmAppUserFee;
import com.univirtual.student.realm.RealmEnrolment;
import com.univirtual.student.realm.RealmInstructorCourse;
import com.univirtual.student.util.RealmUtility;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;

import static com.univirtual.student.activity.HomeActivity.APITOKEN;
import static com.univirtual.student.constants.keyConst.API_URL;
import static com.univirtual.student.constants.Const.myVolleyError;

public class EnrolmentFragmentAdapter extends RecyclerView.Adapter<EnrolmentFragmentAdapter.ViewHolder> implements Filterable {
    ArrayList<RealmEnrolment> enrolments;
    Activity activity;
    Context context;
    FragmentManager childFragmentManager;
    private ProgressDialog mProgress;

    public EnrolmentFragmentAdapter(ArrayList<RealmEnrolment> enrolments, Activity activity, FragmentManager childFragmentManager) {
        this.enrolments = enrolments;
        this.activity = activity;
        this.childFragmentManager = childFragmentManager;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_enrolment, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final RealmEnrolment enrolment = enrolments.get(position);

        String level_en = enrolment.getCoursepath().split((" >> "))[0];
        String level = "";
        if (level_en.equals("Pre-School")) {
            level = activity.getResources().getString(R.string.preschool);
        } else if (level_en.equals("Primary School")) {
            level = activity.getResources().getString(R.string.primary_school);
        } else if (level_en.equals("JHS")) {
            level = activity.getResources().getString(R.string.jhs);
        } else if (level_en.equals("SHS")) {
            level = activity.getResources().getString(R.string.shs);
        } else if (level_en.equals("Pre-University")) {
            level = activity.getResources().getString(R.string.preuniversity);
        } else if (level_en.equals("University")) {
            level = activity.getResources().getString(R.string.university);
        } else if (level_en.equals("Professional")) {
            level = activity.getResources().getString(R.string.professional);
        } else if (level_en.equals("Vocational")) {
            level = activity.getResources().getString(R.string.vocational);
        }

        holder.coursename.setText(enrolment.getCoursepath().replaceFirst(level_en, level));
        holder.intructorname.setText(enrolment.getInstructorname().replace("null", ""));
        holder.rating.setText(String.valueOf(enrolment.getRating()));
        String rating_text = "(" + enrolment.getTotalrating() + " " + activity.getString(R.string.rating);
        if (enrolment.getTotalrating() == 1) {
            rating_text += ")";
        } else {
            rating_text += "s)";
        }
        holder.totalrating.setText(rating_text);
        holder.ratingbar.setRating(enrolment.getRating());
        holder.time.setText(enrolment.getTime());
        holder.dialcode.setText(enrolment.getDialcode());
        holder.conferenceid.setText(enrolment.getConferenceid());


        switch (enrolment.getFee_type_id()) {
            case 2:
                holder.price.setText(enrolment.getCurrency() + String.format("%.02f", Float.parseFloat(enrolment.getPrice_week())));
                holder.feesLayout.setVisibility(View.VISIBLE);
                if (!enrolment.isEnrolmentfeeexpired()) {
                    if (Double.valueOf(enrolment.getPrice_day()) != 0 && enrolment.getEnrolmentfeeexpirydate() != null && !enrolment.getEnrolmentfeeexpirydate().equals("")) {
                        holder.expirylayout.setVisibility(View.VISIBLE);
                        try {
                            Date date = Const.dateFormat.parse(enrolment.getEnrolmentfeeexpirydate());
                            String day = String.valueOf(new DateTime(date).getDayOfMonth());
                            String month = Const.months[date.getMonth()];
                            String year = String.valueOf(new DateTime(date).getYear());
                            holder.expirydate.setText(month + " " + day + ", " + year);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        holder.expirylayout.setVisibility(View.GONE);
                    }
                    holder.renewsubscriptionbtn.setVisibility(View.GONE);
                    holder.enterclass.setVisibility(View.VISIBLE);
                } else {
                    holder.expirylayout.setVisibility(View.GONE);
                    if (enrolment.getEnrolmentfeeexpirydate() != null && !enrolment.getEnrolmentfeeexpirydate().equals("")) {
                        holder.renewsubscriptionbtn.setText(activity.getString(R.string.renew_subscription));
                    } else {
                        holder.renewsubscriptionbtn.setText(activity.getString(R.string.subscribe));
                    }

                    holder.renewsubscriptionbtn.setVisibility(View.VISIBLE);
                    holder.enterclass.setVisibility(View.GONE);
                }
                break;
            case 3:
            case 1:
                holder.feesLayout.setVisibility(View.GONE);
                if (!enrolment.isEnrolmentfeeexpired()) {
                    if (enrolment.getEnrolmentfeeexpirydate() != null && !enrolment.getEnrolmentfeeexpirydate().equals("")) {
                        holder.expirylayout.setVisibility(View.VISIBLE);
                        try {
                            Date date = Const.dateFormat.parse(enrolment.getEnrolmentfeeexpirydate());
                            String day = String.valueOf(new DateTime(date).getDayOfMonth());
                            String month = Const.months[date.getMonth()];
                            String year = String.valueOf(new DateTime(date).getYear());
                            holder.expirydate.setText(month + " " + day + ", " + year);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        holder.expirylayout.setVisibility(View.GONE);
                    }
                    holder.renewsubscriptionbtn.setVisibility(View.GONE);
                    holder.enterclass.setVisibility(View.VISIBLE);
                } else {
                    holder.expirylayout.setVisibility(View.GONE);
                    if (enrolment.getEnrolmentfeeexpirydate() != null && !enrolment.getEnrolmentfeeexpirydate().equals("")) {
                        holder.renewsubscriptionbtn.setText(activity.getString(R.string.renew_subscription));
                    } else {
                        holder.renewsubscriptionbtn.setText(activity.getString(R.string.subscribe));
                    }

                    holder.renewsubscriptionbtn.setVisibility(View.VISIBLE);
                    holder.enterclass.setVisibility(View.GONE);
                }
                break;
        }
        holder.renewsubscriptionbtn.setOnClickListener(v -> {
            if (enrolment.getFee_type_id() != 2) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Expired Subscription");
                builder.setMessage("Pay fees to your institution and click the refresh button at the top right corner to see actively subscribed courses.");
                builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    dialog.dismiss();
                });
                builder.show();
            } else {
                try {
                    ProgressDialog progressDialog = new ProgressDialog(activity);
                    progressDialog.setMessage(activity.getString(R.string.please_wait));
                    progressDialog.setCancelable(false);
                    progressDialog.setIndeterminate(true);
                    progressDialog.show();

                    String instructorcourseid = enrolment.getInstructorcourseid();
                    String enrolmentid = enrolment.getEnrolmentid();
                    StringRequest stringRequest = new StringRequest(
                            com.android.volley.Request.Method.POST,
                            API_URL + "payment-refresh-data",
                            response -> {
                                progressDialog.dismiss();
                                if (response != null) {
                                    progressDialog.dismiss();
                                    final RealmAppUserFee[] realmAppUserFee = new RealmAppUserFee[1];
                                    final RealmInstructorCourse[] realmInstructorCourse = new RealmInstructorCourse[1];
                                    final RealmEnrolment[] realmEnrolment = new RealmEnrolment[1];
                                    try {
                                        JSONObject responseJson = new JSONObject(response);
                                        Realm.init(activity);
                                        Realm.getInstance(RealmUtility.getDefaultConfig(activity)).executeTransaction(realm -> {
                                            try {
                                                realmAppUserFee[0] = realm.createOrUpdateObjectFromJson(RealmAppUserFee.class, responseJson.getJSONObject("app_user_fee"));
                                                realmInstructorCourse[0] = realm.createOrUpdateObjectFromJson(RealmInstructorCourse.class, responseJson.getJSONObject("instructor_course"));
                                                realmEnrolment[0] = realm.createOrUpdateObjectFromJson(RealmEnrolment.class, responseJson.getJSONObject("enrolment"));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        });


                                        SubscriptionMaterialDialog subscriptionMaterialDialog = new SubscriptionMaterialDialog();
                                        if(subscriptionMaterialDialog != null && subscriptionMaterialDialog.isAdded()) {

                                        } else {
                                            if (!enrolment.isAppuserfeeexpired() && enrolment.getFee_type_id() == 1) {
                                                subscriptionMaterialDialog.setAppusagecurrency(realmAppUserFee[0].getCurrency());
                                                subscriptionMaterialDialog.setAppusagepriceperday(Double.valueOf(realmAppUserFee[0].getPriceperday()));
                                                subscriptionMaterialDialog.setAppusagepriceperweek(Double.valueOf(realmAppUserFee[0].getPriceperweek()));
                                                subscriptionMaterialDialog.setAppusagepricepermonth(Double.valueOf(realmAppUserFee[0].getPricepermonth()));
                                                subscriptionMaterialDialog.setAppuserfeedescription(realmAppUserFee[0].getCurrency() + realmAppUserFee[0].getPriceperday() + " " + "per day");
                                            }
                                            subscriptionMaterialDialog.setCoursecurrency(realmInstructorCourse[0].getCurrency());
                                            subscriptionMaterialDialog.setCoursepriceperday(Double.valueOf(realmInstructorCourse[0].getPrice_day()));
                                            subscriptionMaterialDialog.setCoursepriceperweek(Double.valueOf(realmInstructorCourse[0].getPrice_week()));
                                            subscriptionMaterialDialog.setCoursepricepermonth(Double.valueOf(realmInstructorCourse[0].getPrice()));
                                            subscriptionMaterialDialog.setDescription(realmInstructorCourse[0].getCurrency() + realmInstructorCourse[0].getPrice_day() + " " + "per day");
                                            subscriptionMaterialDialog.setEnrolmentid(enrolment.getEnrolmentid());
                                            subscriptionMaterialDialog.show(childFragmentManager, "SubscriptionMaterialDialog");
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                            },
                            error -> {
                                error.printStackTrace();
                                Log.d("Cyrilll", error.toString());
                                progressDialog.dismiss();
                                myVolleyError(activity, error);
                            }
                    )
                    {
                        @Override
                        public Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            params.put("instructorcourseid", instructorcourseid);
                            params.put("enrolmentid", enrolmentid);
                            return params;
                        }
                        /** Passing some request headers* */
                        @Override
                        public Map getHeaders() throws AuthFailureError {
                            HashMap headers = new HashMap();
                            headers.put("accept", "application/json");
                            headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(activity).getString(APITOKEN, ""));
                            return headers;
                        }
                    };;
                    stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                            0,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    InitApplication.getInstance().addToRequestQueue(stringRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        holder.enterclass.setOnClickListener(v -> {

            Intent i = new Intent(activity, SelectResourceActivity.class)
                    .putExtra("INSTRUCTORCOURSEID", enrolment.getInstructorcourseid())
                    .putExtra("COURSEPATH", enrolment.getCoursepath())
                    .putExtra("ENROLMENTID", enrolment.getEnrolmentid())
                    .putExtra("PROFILEIMGURL", enrolment.getProfilepicurl())
                    .putExtra("INSTRUCTORNAME", enrolment.getInstructorname())
                    .putExtra("ROOMID", enrolment.getRoom_number())
                    .putExtra("NODESERVER", enrolment.getNodeserver());

            if (enrolment.isLive()) {
                i.putExtra("ISLIVE", true);
            }
            if (enrolment.isUpcoming()) {
                i.putExtra("ISUPCOMING", true);
            }
            activity.startActivity(i);
        });
        holder.more_details.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(activity, holder.more_details);

            popup.inflate(R.menu.enrolment_menu);

            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.ratecourse:
                        RatingMaterialDialog ratingMaterialDialog = new RatingMaterialDialog();
                        if(ratingMaterialDialog != null && ratingMaterialDialog.isAdded()) {

                        } else {
                            ratingMaterialDialog.setInstructorcourseid(enrolment.getInstructorcourseid());
                            ratingMaterialDialog.setCoursepath(enrolment.getCoursepath());
                            ratingMaterialDialog.setProfilepicurl(enrolment.getProfilepicurl());
                            ratingMaterialDialog.setInstructorname(enrolment.getInstructorname());

                            ratingMaterialDialog.show(activity.getFragmentManager(), "RatingMaterialDialog");
                        }
                        return true;
                    case R.id.unenroll:
                        try {
                            JSONObject request = new JSONObject();

                            request.put("enrolled", 0);
                            mProgress = new ProgressDialog(activity);
                            mProgress.setTitle(activity.getString(R.string.unenrolling));
                            mProgress.setMessage(activity.getString(R.string.pls_wait));
                            mProgress.setCancelable(false);
                            mProgress.setIndeterminate(true);
                            mProgress.show();
                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                                    Request.Method.PATCH,
                                    API_URL + "enrolments/" + enrolment.getEnrolmentid(),
                                    request,
                                    response -> {
                                        mProgress.dismiss();
                                        if (response != null) {
                                            Toast.makeText(activity, activity.getString(R.string.successfully_unenrolled), Toast.LENGTH_LONG).show();
                                            Realm.getInstance(RealmUtility.getDefaultConfig(activity)).executeTransaction(realm -> {
                                                realm.createOrUpdateObjectFromJson(RealmEnrolment.class, response);
                                                RealmInstructorCourse realmInstructorCourse = realm.where(RealmInstructorCourse.class).equalTo("instructorcourseid", enrolment.getInstructorcourseid()).findFirst();
//                                                String confirmation_token = realm.where(RealmUser.class).equalTo("userid", realmInstructorCourse.getInstructorid()).findFirst().getConfirmation_token();
//                                                new EnrolmentActivityAdapter.notifyInstructor(mActivity, "unenrolment", confirmation_token, enrolment.getCoursepath()).execute();
                                            });

                                            FirebaseMessaging.getInstance().unsubscribeFromTopic(enrolment.getInstructorcourseid())
                                                    .addOnCompleteListener(task -> {
                                                        String msg = "unsubscription successful";
                                                        if (!task.isSuccessful()) {
                                                            msg = "unsubscription unccessful";
                                                        }
                                                        Log.d("engineer:sub_status:", msg);
                                                    });
                                        }
                                        context.startActivity(new Intent(context, HomeActivity.class));
                                    },
                                    error -> {
                                        mProgress.dismiss();
                                        myVolleyError(activity, error);
                                    }
                            ) {
                                /** Passing some request headers* */
                                @Override
                                public Map getHeaders() throws AuthFailureError {
                                    HashMap headers = new HashMap();
                                    headers.put("accept", "application/json");
                                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(activity).getString(APITOKEN, ""));
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
                        }
                        return true;


                }
                return false;
            });

            MenuItem rateCourseItem = popup.getMenu().findItem(R.id.ratecourse);
            if (enrolment.isRatedbyme()) {
                rateCourseItem.setVisible(false);
            } else {
                rateCourseItem.setVisible(true);
            }

            popup.show();
        });
        /*if (position == 0 && !PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("ENROLMENT_FRAG_TIPS_DISMISSED", false)) {
            ViewTreeObserver vto = holder.more_details.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        holder.more_details.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        holder.more_details.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    // make an target
                    SimpleTarget firstTarget = new SimpleTarget.Builder(activity).setPoint(refresh)
                            .setRadius(150F)
//                        .setTitle("Account Information")
                            .setDescription(activity.getString(R.string.refresh_payment_tip) + activity.getString(R.string.click_anywhere_to_dismiss))
                            .build();

                    SimpleTarget secondTarget = new SimpleTarget.Builder(activity).setPoint(menu)
                            .setRadius(150F)
//                        .setTitle("Account Information")
                            .setDescription(activity.getString(R.string.filter_courses_tip) + activity.getString(R.string.click_anywhere_to_dismiss))
                            .build();

                    SimpleTarget thirdTarget = new SimpleTarget.Builder(activity).setPoint(holder.more_details)
                            .setRadius(150F)
//                        .setTiFtle("Account Information")
                            .setDescription(activity.getString(R.string.unenrol_tip) + activity.getString(R.string.click_anywhere_to_dismiss))
                            .build();

                    Spotlight.with(activity)
                            .setDuration(250L)
                            .setAnimation(new DecelerateInterpolator(2f))
                            .setTargets(firstTarget, secondTarget, thirdTarget)
                            .setClosedOnTouchedOutside(true)
                            .setOnSpotlightStartedListener(() -> {
                            })
                            .setOnSpotlightStartedListener(() -> PreferenceManager
                                    .getDefaultSharedPreferences(activity.getApplicationContext())
                                    .edit()
                                    .putBoolean("ENROLMENT_FRAG_TIPS_DISMISSED", true)
                                    .apply())
                            .start();

                }
            });
        }*/
    }

    @Override
    public int getItemCount() {
        return enrolments.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView time, coursename, statusmessage, price, expirydate, schoolid;
        LinearLayout expirylayout, feesLayout;
        Button enterclass, renewsubscriptionbtn;
        ImageView more_details;
        RatingBar ratingbar;
        TextView totalrating, rating;
        TextView intructorname;
        TextView dialcode;
        TextView conferenceid;

        public ViewHolder(View view) {
            super(view);
            time = view.findViewById(R.id.time);
            ratingbar = view.findViewById(R.id.ratingbar);
            rating = view.findViewById(R.id.rating);
            totalrating = view.findViewById(R.id.totalrating);
            coursename = view.findViewById(R.id.assignmenttitle);
            statusmessage = view.findViewById(R.id.statusmessage);
            price = view.findViewById(R.id.price);
            expirylayout = view.findViewById(R.id.feeexpirylayout);
            feesLayout = view.findViewById(R.id.feesLayout);
            expirydate = view.findViewById(R.id.expirydate);
            schoolid = view.findViewById(R.id.schoolid);
            more_details = view.findViewById(R.id.more_details);
            enterclass = view.findViewById(R.id.enterclass);
            renewsubscriptionbtn = view.findViewById(R.id.renewsubscriptionbtn);
            intructorname = view.findViewById(R.id.intructorname);
            dialcode = view.findViewById(R.id.dialcode);
            conferenceid = view.findViewById(R.id.conferenceid);
        }

        @Override
        public void onClick(View view) {
        }

        @Override
        public boolean onLongClick(View view) {
            return false;
        }


    }
}

