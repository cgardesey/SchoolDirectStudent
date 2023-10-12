package com.univirtual.student.fragment;

        import android.app.Activity;
        import android.app.ProgressDialog;
        import android.content.Context;
        import android.os.Bundle;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageView;
        import android.widget.TextView;

        import androidx.fragment.app.Fragment;
        import androidx.recyclerview.widget.DefaultItemAnimator;
        import androidx.recyclerview.widget.LinearLayoutManager;
        import androidx.recyclerview.widget.RecyclerView;

        import com.univirtual.student.R;
        import com.univirtual.student.activity.QuizzesActivity;
        import com.univirtual.student.adapter.QuizzesAdapter;
        import com.univirtual.student.constants.Const;
        import com.univirtual.student.realm.RealmQuiz;
        import com.univirtual.student.realm.RealmSubmittedQuiz;
        import com.univirtual.student.util.RealmUtility;

        import java.text.ParseException;
        import java.util.ArrayList;
        import java.util.Calendar;
        import java.util.Date;

        import io.realm.Realm;
        import io.realm.RealmResults;




/**
 * Created by Nana on 11/26/2017.
 */

public class UnsubmittedQuizzesFragment extends Fragment {

    static String TITLE;
    static String INSTRUCTORCOURSEID;
    static String COURSEPATH;
    static String ENROLMENTID;
    static String PROFILEIMGURL;
    static String ROOMID;
    static String NODESERVER;
    static String INSTRUCTORNAME;
    static String SESSIONID;

    static final int
            ALL = 1,
            PENDING = 2,
            PAST = 3;

    static int LISTTYPE = ALL;
    static QuizzesAdapter quizAdapter;
    static ArrayList<RealmQuiz> realmQuizzes = new ArrayList<>();
    ArrayList<RealmQuiz> allquizzes = new ArrayList<>();
    ArrayList<RealmQuiz> pendingPayments = new ArrayList<>();
    ArrayList<RealmQuiz> pastquizzes = new ArrayList<>();
    static RecyclerView recyclerview_quizzes;
    ImageView backbtn, refresh;
    static TextView noquizzestext;
    ProgressDialog dialog;
    public static String enrolmentid;
    static Context context;
    static Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_quizzes, container, false);

        activity = getActivity();
        context = getContext();

        String instructorcourseid = getActivity().getIntent().getStringExtra("INSTRUCTORCOURSEID");
        String coursepathstring = getActivity().getIntent().getStringExtra("COURSEPATH");
        String enrolmentid = getActivity().getIntent().getStringExtra("ENROLMENTID");
        String profileimgurl = getActivity().getIntent().getStringExtra("PROFILEIMGURL");
        String nodeserver = getActivity().getIntent().getStringExtra("NODESERVER");
        String roomid = getActivity().getIntent().getStringExtra("ROOMID");


        if (instructorcourseid != null && !instructorcourseid.equals("")) {
            INSTRUCTORCOURSEID = instructorcourseid;
        }
        if (coursepathstring != null && !coursepathstring.equals("")) {
            COURSEPATH = coursepathstring;
        }
        if (enrolmentid != null && !enrolmentid.equals("")) {
            ENROLMENTID = enrolmentid;
        }
        if (profileimgurl != null && !profileimgurl.equals("")) {
            PROFILEIMGURL = profileimgurl;
        }
        if (nodeserver != null && !nodeserver.equals("")) {
            NODESERVER = nodeserver;
        }
        if (roomid != null && !roomid.equals("")) {
            ROOMID = roomid;
        }
        noquizzestext = rootView.findViewById(R.id.nosubmittedquizzestext);
        realmQuizzes = new ArrayList<>();
        recyclerview_quizzes = rootView.findViewById(R.id.recyclerview_quizzes);
        backbtn = rootView.findViewById(R.id.search);
        refresh = rootView.findViewById(R.id.refresh);
        initUnsubmittedQuizzes(getActivity());

        return rootView;
    }

    public static void initUnsubmittedQuizzes(Context context) {
        populateUnsubmittedQuizzes(context);
        quizAdapter = new QuizzesAdapter(realmQuizzes);
        recyclerview_quizzes.setLayoutManager(new LinearLayoutManager(context));
//        recyclerview_quizzes.setHasFixedSize(true);
        recyclerview_quizzes.setNestedScrollingEnabled(false);
        recyclerview_quizzes.setItemAnimator(new DefaultItemAnimator());
        recyclerview_quizzes.setAdapter(quizAdapter);
    }

    public static void populateUnsubmittedQuizzes(Context context) {
        LISTTYPE = ALL;
        Realm.init(context);
        Realm.getInstance(RealmUtility.getDefaultConfig(context)).executeTransaction(realm -> {

            RealmResults<RealmQuiz> results = realm.where(RealmQuiz.class).equalTo("instructorcourseid", INSTRUCTORCOURSEID).findAll();
            noquizzestext.setVisibility(View.VISIBLE);
            recyclerview_quizzes.setVisibility(View.GONE);
            realmQuizzes.clear();
            for (RealmQuiz RealmQuiz : results) {
                RealmSubmittedQuiz realmSubmittedQuiz = realm.where(RealmSubmittedQuiz.class).equalTo("quizid", RealmQuiz.getQuizid()).findFirst();
                if (realmSubmittedQuiz == null) {
                    realmQuizzes.add(RealmQuiz);
                    noquizzestext.setVisibility(View.GONE);
                    recyclerview_quizzes.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void populatePendingQuizzes(Context context) {
        LISTTYPE = PENDING;
        Realm.init(getContext());
        Realm.getInstance(RealmUtility.getDefaultConfig(context)).executeTransaction(realm -> {

            RealmResults<RealmQuiz> results = realm.where(RealmQuiz.class).equalTo("instructorcourseid", INSTRUCTORCOURSEID).findAll();
            pendingPayments.clear();

            Date dateNow = Calendar.getInstance().getTime();
            for (RealmQuiz realmQuiz : results) {
                try {
                    Date quizDate = Const.dateFormat.parse(realmQuiz.getDate());
                    if (quizDate.after(dateNow)) {
                        pendingPayments.add(realmQuiz);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void populatePastQuizzes(Context context) {
        LISTTYPE = PAST;
        Realm.init(getContext());
        Realm.getInstance(RealmUtility.getDefaultConfig(context)).executeTransaction(realm -> {

            RealmResults<RealmQuiz> results = realm.where(RealmQuiz.class).equalTo("instructorcourseid", INSTRUCTORCOURSEID).findAll();
            pendingPayments.clear();

            Date dateNow = Calendar.getInstance().getTime();
            for (RealmQuiz realmQuiz : results) {
                try {
                    Date quizDate = Const.dateFormat.parse(realmQuiz.getDate());
                    if (quizDate.before(dateNow)) {
                        pendingPayments.add(realmQuiz);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
