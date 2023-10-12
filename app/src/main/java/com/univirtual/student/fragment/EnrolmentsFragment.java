package com.univirtual.student.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.univirtual.student.R;
import com.univirtual.student.adapter.EnrolmentFragmentAdapter;
import com.univirtual.student.constants.Const;
import com.univirtual.student.materialDialog.ChooseEnrolmentMaterialDialog;
import com.univirtual.student.materialDialog.EnrolallMaterialDialog;
import com.univirtual.student.other.InitApplication;
import com.univirtual.student.realm.RealmCourse;
import com.univirtual.student.realm.RealmEnrolment;
import com.univirtual.student.realm.RealmInstitution;
import com.univirtual.student.realm.RealmInstructor;
import com.univirtual.student.realm.RealmInstructorCourse;
import com.univirtual.student.realm.RealmInstructorCourseRating;
import com.univirtual.student.realm.RealmPayment;
import com.univirtual.student.realm.RealmPeriod;
import com.univirtual.student.realm.RealmTimetable;
import com.univirtual.student.util.RealmUtility;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.univirtual.student.activity.HomeActivity.APITOKEN;
import static com.univirtual.student.activity.HomeActivity.MYUSERID;
import static com.univirtual.student.constants.keyConst.API_URL;
import static com.univirtual.student.constants.Const.myVolleyError;
import static com.univirtual.student.constants.Const.toTitleCase;
import static io.realm.Sort.ASCENDING;
import static io.realm.Sort.DESCENDING;


public class EnrolmentsFragment extends Fragment {

    public final int
            LIVE = 1,
            UPCOMING = 2,
            ALL = 3,
            EXPIRED = 4,
            ACTIVE = 5;
    public int LISTTYPE = ALL;
    public ArrayList<RealmEnrolment> enrolments = new ArrayList<>(), liveClasses = new ArrayList<>(), upcomingClasses = new ArrayList<>(), allClasses = new ArrayList<>(), expiredClasses = new ArrayList<>(), activeClasses = new ArrayList<>();
    public Context enrolmentFragmentContext;
    RecyclerView enrolments_recyclerview;
    ImageView backbtn;
    TextView nodatatextview;

    public TextView dialcode, feeexpirydate, feeexpitydescription;
    public LinearLayout feeexpirylayout;
    public Button renewsubscriptionbtn;

    String institution = "";
    final RealmInstructorCourse[] realmInstructorCourse = {null};
    final RealmInstitution[] realmInstitution = {null};

    public EnrolmentFragmentAdapter enrolmentFragmentAdapter;
    ProgressDialog dialog;
    Activity mHostActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_enrolments, container, false);
        enrolmentFragmentContext = getContext();
        enrolments_recyclerview = rootView.findViewById(R.id.enrolments_recyclerview);
        renewsubscriptionbtn = rootView.findViewById(R.id.renewsubscriptionbtn);
        nodatatextview = rootView.findViewById(R.id.nodatatext);
        feeexpirylayout = rootView.findViewById(R.id.feeexpirylayout);
        dialcode = rootView.findViewById(R.id.dialcode);
        feeexpirydate = rootView.findViewById(R.id.feeexpirydate);
        feeexpitydescription = rootView.findViewById(R.id.feeexpitydescription);
        enrolments = new ArrayList<>();
        liveClasses = new ArrayList<>();
        upcomingClasses = new ArrayList<>();

        institution = getArguments().getString("institution");

        allClasses.clear();
        enrolments.clear();
        populateAllClasses(institution);
        enrolments.addAll(allClasses);
        enrolmentFragmentAdapter = new EnrolmentFragmentAdapter(enrolments, mHostActivity, getChildFragmentManager());
        enrolments_recyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        enrolments_recyclerview.setNestedScrollingEnabled(false);
        enrolments_recyclerview.setItemAnimator(new DefaultItemAnimator());
        enrolments_recyclerview.setAdapter(enrolmentFragmentAdapter);

        if (enrolments.size() > 0) {
            Realm.init(enrolmentFragmentContext);
            Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realmInstructorCourse[0] = realm.where(RealmInstructorCourse.class)
                            .equalTo("instructorcourseid", enrolments.get(0).getInstructorcourseid())
                            .findFirst();

                    realmInstitution[0] = realm.where(RealmInstitution.class)
                            .equalTo("institutionid", realmInstructorCourse[0].getInstitutionid())
                            .findFirst();
                }
            });

            if (realmInstructorCourse[0].getInstitutionid() != null &&
                    !realmInstructorCourse[0].getInstitutionid().equals("") &&
                    realmInstitution[0]
                            .getInternalinstitution() == 1) {
                if (!enrolments.get(0).isInstitutionfeeexpired() && enrolments.get(0).getInstitutionfeeexpirydate() != null && !enrolments.get(0).getInstitutionfeeexpirydate().equals("")) {
                    Date date = null;
                    try {
                        feeexpirylayout.setVisibility(View.VISIBLE);
                        date = Const.dateFormat.parse(enrolments.get(0).getInstitutionfeeexpirydate());
                        DateTime dateTime = new DateTime(date);
                        if (new Date().after(date)) {
                            feeexpirylayout.setVisibility(View.GONE);
                        } else {
                            String day = String.valueOf(new DateTime(date).getDayOfMonth());
                            String month = Const.months[date.getMonth()];
                            String year = String.valueOf(new DateTime(date).getYear());
                            feeexpitydescription.setText("Institution fees expires on:");
                            feeexpirydate.setText(month + " " + day + ", " + year);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    renewsubscriptionbtn.setVisibility(View.VISIBLE);
                    feeexpirylayout.setVisibility(View.GONE);
                    enrolments_recyclerview.setVisibility(View.GONE);
                    nodatatextview.setVisibility(View.GONE);
                }
            }
            else {
                RealmEnrolment realmEnrolment = null;

                for (RealmEnrolment enrolment : enrolments) {
                    if (enrolment.getFee_type_id() == 1) {
                        realmEnrolment = enrolment;
                        break;
                    }
                }

                if (realmEnrolment != null && realmEnrolment.isAppuserfeeexpired() && realmEnrolment.getAppuserfeeexpirydate() != null && !realmEnrolment.getAppuserfeeexpirydate().equals("")) {
                    Date date = null;
                    try {
                        feeexpirylayout.setVisibility(View.VISIBLE);
                        date = Const.dateFormat.parse(realmEnrolment.getAppuserfeeexpirydate());
                        DateTime dateTime = new DateTime(date);
                        if (new Date().after(date)) {
                            feeexpirylayout.setVisibility(View.GONE);
                        } else {
                            String day = String.valueOf(new DateTime(date).getDayOfMonth());
                            String month = Const.months[date.getMonth()];
                            String year = String
                                    .valueOf(new DateTime(date).getYear());
                            feeexpitydescription.setText("App usage subscription expires on:");
                            feeexpirydate.setText(month + " " + day + ", " + year);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                } else {
                    feeexpirylayout.setVisibility(View.GONE);
                }
            }
        }

        if (realmInstitution[0] == null || realmInstitution[0].getInternalinstitution() == 0) {
            renewsubscriptionbtn.setVisibility(View.GONE);
        }
        else if (enrolments.size() > 0 && !enrolments.get(0).isInstitutionfeeexpired()) {
            renewsubscriptionbtn.setVisibility(View.GONE);

            final RealmPayment[] latestInstitutionPayment = new RealmPayment[1];
            Realm.init(enrolmentFragmentContext);
            Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    latestInstitutionPayment[0] = realm.where(RealmPayment.class)
                            .equalTo("institutionid", realmInstitution[0].getInstitutionid())
                            .sort("id", DESCENDING)
                            .findFirst();
                }
            });

            if (latestInstitutionPayment[0] != null && latestInstitutionPayment[0].getFeetype().equals("3_courses_max") && enrolments.size() > 3) {
                ChooseEnrolmentMaterialDialog chooseEnrolmentMaterialDialog = new ChooseEnrolmentMaterialDialog();
                if(chooseEnrolmentMaterialDialog != null && chooseEnrolmentMaterialDialog.isAdded()) {

                } else {
                    chooseEnrolmentMaterialDialog.setRealmEnrolments(enrolments);
                    chooseEnrolmentMaterialDialog.setCancelable(false);
                    chooseEnrolmentMaterialDialog.show(getFragmentManager(), "ChooseEnrolmentMaterialDialog");
                }
            }
        }

        renewsubscriptionbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ProgressDialog progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setMessage(getActivity().getString(R.string.please_wait));
                    progressDialog.setCancelable(false);
                    progressDialog.setIndeterminate(true);
                    progressDialog.show();

                    String institutionid = realmInstitution[0].getInstitutionid();

                    StringRequest stringRequest = new StringRequest(
                            Request.Method.POST,
                            API_URL + "renew-internal-institution-subscription",
                            response -> {
                                progressDialog.dismiss();
                                if (response != null) {
                                    try {
                                        JSONObject jsonObject = new JSONObject(response);
                                        JSONArray institution_fees = jsonObject.getJSONArray("institution_fees");
                                        EnrolallMaterialDialog enrolallMaterialDialog = new EnrolallMaterialDialog();
                                        if(enrolallMaterialDialog != null && enrolallMaterialDialog.isAdded()) {

                                        } else {
                                            enrolallMaterialDialog.setInstitutionid(institutionid);
                                            enrolallMaterialDialog.setPreviouslyon8coursesmax(!jsonObject.isNull("updated_payment") && jsonObject.getJSONObject("updated_payment").getString("feetype").equals("8_courses_max"));
                                            enrolallMaterialDialog.setSubscriptionchangerequestapproved(jsonObject.getInt("subscription_change_request_approved") == 1);
                                            enrolallMaterialDialog.setInstitution_fees(institution_fees);
                                            enrolallMaterialDialog.setCancelable(false);
                                            enrolallMaterialDialog.show(getFragmentManager(), "EnrolallMaterialDialog");
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            error -> {
                                progressDialog.dismiss();
                                myVolleyError(getActivity(), error);
                            }
                    ) {
                        @Override
                        public Map getHeaders() throws AuthFailureError {
                            HashMap headers = new HashMap();
                            headers.put("accept", "application/json");
                            headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(APITOKEN, ""));
                            return headers;
                        }

                        @Override
                        public Map<String, String> getParams() throws AuthFailureError {

                            Map<String, String> params = new HashMap<>();
                            params.put("institutionid", institutionid);
                            params.put("userid", PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(MYUSERID, ""));
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
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
//        initEnrolmentParentFragment();



    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //save the activity to a member of this fragment
        mHostActivity = activity;
    }

    public void populateAllClasses(String institution) {
        initRealmTimetables();
        initRealmPeriods();
        LISTTYPE = ALL;

        HashMap<String, String> weeks = new HashMap<String, String>();
        weeks.put("Monday", "1");
        weeks.put("Tuesday", "2");
        weeks.put("Wednesday", "3");
        weeks.put("Thursday", "4");
        weeks.put("Friday", "5");
        weeks.put("Saturday", "6");
        weeks.put("Sunday", "7");
        Realm.init(enrolmentFragmentContext);
        Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<RealmEnrolment> results = realm.where(RealmEnrolment.class)
                        .equalTo("studentid", PreferenceManager.getDefaultSharedPreferences(enrolmentFragmentContext).getString(MYUSERID, ""))
                        .equalTo("enrolled", 1)
                        .findAll();
                for (RealmEnrolment realmEnrolment : results) {

                    RealmInstructorCourse realmInstructorCourse = Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).where(RealmInstructorCourse.class).equalTo("instructorcourseid", realmEnrolment.getInstructorcourseid()).findFirst();
                    RealmCourse realmCourse = Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).where(RealmCourse.class).equalTo("courseid", realmInstructorCourse.getCourseid()).findFirst();
                    realmEnrolment.setCurrency(realmInstructorCourse.getCurrency());
                    realmEnrolment.setRoom_number(realmInstructorCourse.getRoom_number());
                    realmEnrolment.setNodeserver(realmInstructorCourse.getNodeserver());
                    realmEnrolment.setPrice_day(String.valueOf(realmInstructorCourse.getPrice_day()));
                    realmEnrolment.setPrice_week(String.valueOf(realmInstructorCourse.getPrice_week()));
                    realmEnrolment.setPrice(String.valueOf(realmInstructorCourse.getPrice()));
                    realmEnrolment.setFee_type_id(realmInstructorCourse.getFee_type_id());
                    realmEnrolment.setConferenceid(realmInstructorCourse.getRoom_number());

                    realmEnrolment.setCoursepath(realmCourse.getCoursepath());

                    realmEnrolment.setTotalrating(realmInstructorCourse.getTotal_ratings());
                    realmEnrolment.setRating(realmInstructorCourse.getRating());

                    Date currentTime = Calendar.getInstance().getTime();
                    String currentDow = String.valueOf(new DateTime(currentTime).getDayOfWeek());
                    String currentHod = String.valueOf(new DateTime(currentTime).getHourOfDay());
                    RealmTimetable realmTimetable = null;
                    String[] fieldNames = {"downum", "period_id"};
                    Sort[] sorts = {ASCENDING, ASCENDING};

                    long min = (long) realm.where(RealmPeriod.class).min("order");
                    long max = (long) realm.where(RealmPeriod.class).max("order");

                    int period_id_now;

                    DateTime dateTime = new DateTime(Calendar.getInstance().getTime());
                    long order = dateTime.getHourOfDay();
                    if (order >= min && order < max) {
                        period_id_now = realm.where(RealmPeriod.class)
                                .greaterThanOrEqualTo("order", order)
                                .lessThan("order", max)
                                .findFirst()
                                .getId();
                    } else {
                        period_id_now = realm.where(RealmPeriod.class)
                                .equalTo("order", min)
                                .findFirst()
                                .getId();
                    }


                    int downum_now;
                    if (order >= max) {
                        downum_now = Integer.parseInt(currentDow) + 1;
                    } else {
                        downum_now = Integer.parseInt(currentDow);
                    }
                    RealmTimetable timetable_query = Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).where(RealmTimetable.class)
                            .equalTo("instructorcourseid", realmEnrolment.getInstructorcourseid())
                            .equalTo("downum", downum_now)
                            .findFirst();
                    if (timetable_query == null) {
                        realmTimetable = Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).where(RealmTimetable.class)
                                .equalTo("instructorcourseid", realmEnrolment.getInstructorcourseid())
                                .greaterThan("downum", downum_now)
                                .sort(fieldNames, sorts)
                                .findFirst();
                    } else {
                        realmTimetable = Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).where(RealmTimetable.class)
                                .equalTo("instructorcourseid", realmEnrolment.getInstructorcourseid())
                                .equalTo("downum", downum_now)
                                .sort(fieldNames, sorts)
                                .greaterThanOrEqualTo("period_id", period_id_now)
                                .findFirst();
                    }
                    if (realmTimetable == null) {
                        realmTimetable = Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).where(RealmTimetable.class)
                                .equalTo("instructorcourseid", realmEnrolment.getInstructorcourseid())
                                .sort(fieldNames, sorts)
                                .findFirst();
                    }

                    if (realmTimetable != null) {
                        RealmPeriod realmPeriod = Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).where(RealmPeriod.class).equalTo("id", realmTimetable.getPeriod_id()).findFirst();
                        realmTimetable.setStarttime(realmPeriod.getStarttime());

                        realmEnrolment.setDow(realmTimetable.getDow());
                        realmEnrolment.setDownum(weeks.get(realmTimetable.getDow()));
                        String dow_un = null;
                        if (realmEnrolment.getDownum().equals("1")) {
                            dow_un = enrolmentFragmentContext.getResources().getString(R.string.monday);
                        } else if (realmEnrolment.getDownum().equals("2")) {
                            dow_un = enrolmentFragmentContext.getResources().getString(R.string.tuesday);
                        } else if (realmEnrolment.getDownum().equals("3")) {
                            dow_un = enrolmentFragmentContext.getResources().getString(R.string.wednesday);
                        } else if (realmEnrolment.getDownum().equals("4")) {
                            dow_un = enrolmentFragmentContext.getResources().getString(R.string.thursday);
                        } else if (realmEnrolment.getDownum().equals("5")) {
                            dow_un = enrolmentFragmentContext.getResources().getString(R.string.friday);
                        } else if (realmEnrolment.getDownum().equals("6")) {
                            dow_un = enrolmentFragmentContext.getResources().getString(R.string.saturday);
                        } else if (realmEnrolment.getDownum().equals("7")) {
                            dow_un = enrolmentFragmentContext.getResources().getString(R.string.sunday);
                        }
                        realmEnrolment.setStarttime(realmPeriod.getStarttime());
                        realmEnrolment.setEndtime(realmPeriod.getEndtime());
                        realmEnrolment.setTime(enrolmentFragmentContext.getString(R.string.next_class_on) + " " + toTitleCase(dow_un) + " @ " + realmPeriod.getStarttime());

                        String dow = weeks.get(realmEnrolment.getDow());
                        String startH = realmEnrolment.getStarttime().split(":")[0];
                        String endH = realmEnrolment.getEndtime().split(":")[0];
                        boolean isLive = currentDow.equals(dow) && Integer.parseInt(currentHod) >= Integer.parseInt(startH) && Integer.parseInt(currentHod) < Integer.parseInt(endH);
                        boolean isUpComing = currentDow.equals(realmEnrolment.getDownum()) && Integer.parseInt(realmEnrolment.getStarttime().split(":")[0]) > Integer.parseInt(currentHod);

                        realmEnrolment.setLive(isLive);

                        realmEnrolment.setUpcoming(isUpComing);
                    }

                    RealmInstructor realmInstructor = realm.where(RealmInstructor.class).equalTo("infoid", realmInstructorCourse.getInstructorid()).findFirst();
                    realmEnrolment.setProfilepicurl(realmInstructor.getProfilepicurl());
                    realmEnrolment.setInstructorname((StringUtils.normalizeSpace(realmInstructor.getTitle() + " " + realmInstructor.getFirstname() + " " + realmInstructor.getOthername() + " " + realmInstructor.getLastname())).replace("null", ""));


                    RealmInstructorCourseRating realmInstructorCourseRating = realm.where(RealmInstructorCourseRating.class)
                            .equalTo("instructorcourseid", realmInstructorCourse.getInstructorcourseid())
                            .equalTo("studentid", PreferenceManager.getDefaultSharedPreferences(enrolmentFragmentContext).getString(MYUSERID, ""))
                            .findFirst();
                    realmEnrolment.setRatedbyme(realmInstructorCourseRating != null);

                    allClasses.add(realmEnrolment);
                }

                allClasses.clear();
                RealmResults<RealmEnrolment> sortedResults = realm.where(RealmEnrolment.class)
                        .equalTo("studentid", PreferenceManager.getDefaultSharedPreferences(enrolmentFragmentContext).getString(MYUSERID, ""))
                        .equalTo("enrolled", 1)
                        .findAll().sort("starttime", ASCENDING)
                        .sort("downum", ASCENDING);
                for (RealmEnrolment sortedEnrolment : sortedResults) {
                    if (!sortedEnrolment.getCoursepath().contains(" >> " + institution)) {
                        Log.d("37654", "asd");
                        continue;
                    }
                    allClasses.add(sortedEnrolment);
                }
                noDataCheck(allClasses.size(), enrolmentFragmentContext.getString(R.string.enrolments_will_show_here));
            }
        });
    }

    private String getHourWithLeadingZero(int h) {
        if (h < 12) {
            return "0" + String.valueOf(h);
        }
        return String.valueOf(h);
    }

    public void populateUpcomingClasses() {
        LISTTYPE = UPCOMING;
        Realm.init(enrolmentFragmentContext);
        Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
            upcomingClasses.clear();
            for (RealmEnrolment realmEnrolment : allClasses) {

                if (realmEnrolment.isUpcoming()) {
                    realmEnrolment.setUpcoming(true);
                    realmEnrolment.setTime(enrolmentFragmentContext.getString(R.string.today_at) + " " + realmEnrolment.getStarttime());

                    upcomingClasses.add(realmEnrolment);
                }
            }
            noDataCheck(upcomingClasses.size(), enrolmentFragmentContext.getString(R.string.no_upcoming_course));
        });
    }

    public void populateLiveClasses() {
        LISTTYPE = LIVE;
        Realm.init(enrolmentFragmentContext);
        Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
            liveClasses.clear();
            for (RealmEnrolment realmEnrolment : allClasses) {
                if (realmEnrolment.isLive()) {
                    realmEnrolment.setTime(realmEnrolment.getStarttime());
                    liveClasses.add(realmEnrolment);
                }
            }
            int size = liveClasses.size();
            noDataCheck(liveClasses.size(), enrolmentFragmentContext.getString(R.string.you_currently_have_no_live_classes));
        });
    }

    public void populateExpired() {
        LISTTYPE = EXPIRED;
        Realm.init(enrolmentFragmentContext);
        Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
            expiredClasses.clear();
            for (RealmEnrolment realmEnrolment : allClasses) {
                if (realmEnrolment.isEnrolmentfeeexpired()) {
                    expiredClasses.add(realmEnrolment);
                }
            }
            noDataCheck(expiredClasses.size(), enrolmentFragmentContext.getString(R.string.no_expired_subscriptions));
        });
    }

    public void populateActive() {
        LISTTYPE = ACTIVE;
        Realm.init(enrolmentFragmentContext);
        Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
            activeClasses.clear();
            for (RealmEnrolment realmEnrolment : allClasses) {
                if (!realmEnrolment.isEnrolmentfeeexpired()) {
                    activeClasses.add(realmEnrolment);
                }
            }
            noDataCheck(activeClasses.size(), enrolmentFragmentContext.getString(R.string.no_active_subscriptions));
        });
    }

    public void initRealmTimetables() {
        LISTTYPE = ALL;

        HashMap<String, String> weeks = new HashMap<String, String>();
        weeks.put("Monday", "1");
        weeks.put("Tuesday", "2");
        weeks.put("Wednesday", "3");
        weeks.put("Thursday", "4");
        weeks.put("Friday", "5");
        weeks.put("Saturday", "6");
        weeks.put("Sunday", "7");
        Realm.init(enrolmentFragmentContext);
        Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
            RealmResults<RealmTimetable> timetables = Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).where(RealmTimetable.class).findAll();
            for (RealmTimetable realmTimetable : timetables) {
                realmTimetable.setDownum(Integer.parseInt(weeks.get(realmTimetable.getDow())));
            }
        });
    }

    public void initRealmPeriods() {
        Realm.init(enrolmentFragmentContext);
        Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
            RealmResults<RealmPeriod> realmPeriods = Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).where(RealmPeriod.class).findAll();
            for (RealmPeriod realmPeriod : realmPeriods) {
                String[] split = realmPeriod.getStarttime().split(":");
                int order = 0;
                for (String value : split) {
                    order += Integer.parseInt(value);
                }
                realmPeriod.setOrder(order);
            }
        });
    }

    private void noDataCheck(int size, String nodatatext) {
        nodatatextview.setText(nodatatext);
        if (size > 0) {
            nodatatextview.setVisibility(View.GONE);
            enrolments_recyclerview.setVisibility(View.VISIBLE);
        } else {
            nodatatextview.setVisibility(View.VISIBLE);
            enrolments_recyclerview.setVisibility(View.GONE);
        }
    }

    /*public void initEnrolmentsFragment() {
        enrolments.clear();
        switch (LISTTYPE) {
            case ALL:
                populateAllClasses();
                enrolments.addAll(allClasses);
                break;
            case UPCOMING:
                populateUpcomingClasses();
                enrolments.addAll(upcomingClasses);
                break;
            case LIVE:
                populateLiveClasses();
                enrolments.addAll(liveClasses);
                break;
            case EXPIRED:
                populateExpired();
                enrolments.addAll(expiredClasses);
                break;
            case ACTIVE:
                populateActive();
                enrolments.addAll(activeClasses);
                break;
        }
        enrolmentFragmentAdapter.notifyDataSetChanged();
    }*/
}
