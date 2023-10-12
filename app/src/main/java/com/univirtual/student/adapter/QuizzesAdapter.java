package com.univirtual.student.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.univirtual.student.R;
import com.univirtual.student.activity.PastQuestionsActivity;
import com.univirtual.student.activity.QuizActivity;
import com.univirtual.student.activity.QuizzesActivity;
import com.univirtual.student.constants.Const;
import com.univirtual.student.realm.RealmQuiz;

import org.joda.time.DateTime;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;




public class QuizzesAdapter extends RecyclerView.Adapter<QuizzesAdapter.ViewHolder> implements Filterable {

    String TITLE;
    String INSTRUCTORCOURSEID;
    String COURSEPATH;
    String ENROLMENTID;
    String PROFILEIMGURL;
    String ROOMID;
    String NODESERVER;
    String INSTRUCTORNAME;
    String SESSIONID;

    ArrayList<RealmQuiz> realmQuizzes;
    private Context mContext;

    public QuizzesAdapter(ArrayList<RealmQuiz> realmQuizzes) {
        this.realmQuizzes = realmQuizzes;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycle_quiz, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {


        String instructorcourseid = ((Activity)mContext).getIntent().getStringExtra("INSTRUCTORCOURSEID");
        String coursepathstring = ((Activity)mContext).getIntent().getStringExtra("COURSEPATH");
        String enrolmentid = ((Activity)mContext).getIntent().getStringExtra("ENROLMENTID");
        String profileimgurl = ((Activity)mContext).getIntent().getStringExtra("PROFILEIMGURL");
        String nodeserver = ((Activity)mContext).getIntent().getStringExtra("NODESERVER");
        String roomid = ((Activity)mContext).getIntent().getStringExtra("ROOMID");


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

        final RealmQuiz realmQuiz = realmQuizzes.get(position);
        Date date = null;
        try {

            date = Const.dateFormat.parse(realmQuiz.getDate());
            DateTime dateTime = new DateTime(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.day.setText(String.valueOf(new DateTime(date).getDayOfMonth()));
        holder.month.setText(Const.months[date.getMonth()]);
        holder.year.setText(String.valueOf(new DateTime(date).getYear()));
        holder.starttime.setText(realmQuiz.getStarttime());
        holder.endtime.setText(realmQuiz.getEndtime());
        holder.description.setText(realmQuiz.getDescription());
        holder.title.setText(realmQuiz.getTitle());
        holder.gotoquiz.setOnClickListener(v -> mContext.startActivity(new Intent(mContext, QuizActivity.class)
                .putExtra("INSTRUCTORCOURSEID", INSTRUCTORCOURSEID)
                .putExtra("COURSEPATH", COURSEPATH)
                .putExtra("ENROLMENTID", ENROLMENTID)
                .putExtra("PROFILEIMGURL", PROFILEIMGURL)
                .putExtra("INSTRUCTORNAME", INSTRUCTORNAME)
                .putExtra("NODESERVER", NODESERVER)
                .putExtra("ROOMID", ROOMID)

                .putExtra("quizid", realmQuiz.getQuizid())
                .putExtra("title", realmQuiz.getTitle())
                .putExtra("startime", realmQuiz.getStarttime())
                .putExtra("endtime", realmQuiz.getEndtime())
        ));
        holder.parent.setOnClickListener(v -> {
            if (holder.detailsarea.getVisibility() == View.VISIBLE) {
                holder.detailsarea.setVisibility(View.GONE);
                holder.downbtn.animate().rotation(360).start();
            } else {
                holder.detailsarea.setVisibility(View.VISIBLE);
                holder.downbtn.animate().rotation(-180).start();
                if (realmQuiz.getPercentagescore() == null) {
                    holder.details.setVisibility(View.VISIBLE);
                    holder.scorearea.setVisibility(View.GONE);
                } else {
                    holder.details.setVisibility(View.GONE);
                    holder.scorearea.setVisibility(View.VISIBLE);
                    holder.score.setText(realmQuiz.getPercentagescore());
                }
            }
        });
        /*if (position == 0 && !PreferenceManager.getDefaultSharedPreferences(mActivity).getBoolean("PAYMENT_ACTIVITY_TIPS_DISMISSED", false)) {
            ViewTreeObserver vto = holder.cardview.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        holder.cardview.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        holder.cardview.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    // make an target
                    SimpleTarget firstTarget = new SimpleTarget.Builder((Activity) mActivity).setPoint(holder.cardview)
                            .setRadius(150F)
//                        .setTitle("Account Information")
                            .setDescription("Click on a row to view payment status")
                            .build();

                    Spotlight.with((Activity) mActivity)
//                .setOverlayColor(ContextCompat.getColor(getActivity(), R.color.background))
                            .setDuration(250L)
                            .setAnimation(new DecelerateInterpolator(2f))
                            .setTargets(firstTarget)
                            .setClosedOnTouchedOutside(true)
                            .setOnSpotlightStartedListener(new OnSpotlightStartedListener() {
                                @Override
                                public void onStarted() {
                                }
                            })
                            .setOnSpotlightEndedListener(new OnSpotlightEndedListener() {
                                @Override
                                public void onEnded() {
                                    PreferenceManager
                                            .getDefaultSharedPreferences(mActivity.getApplicationContext())
                                            .edit()
                                            .putBoolean("PAYMENT_ACTIVITY_TIPS_DISMISSED", true)
                                            .apply();
                                }
                            })
                            .start();

                }
            });
        }*/
    }

    @Override
    public int getItemCount() {
        return realmQuizzes.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public void reload(ArrayList<RealmQuiz> quizArrayList) {
        this.realmQuizzes = quizArrayList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView month, day, year, starttime, endtime, description, title, score;
        LinearLayout detailsarea, details, scorearea;
        ImageView downbtn;
        CardView cardview;
        Button gotoquiz;
        RelativeLayout parent;

        public ViewHolder(View view) {
            super(view);

            month = view.findViewById(R.id.month);
            day = view.findViewById(R.id.day);
            year = view.findViewById(R.id.year);
            starttime = view.findViewById(R.id.starttime);
            endtime = view.findViewById(R.id.endtime);
            description = view.findViewById(R.id.description);
            title =  view.findViewById(R.id.response);
            detailsarea = view.findViewById(R.id.detailsarea);
            details = view.findViewById(R.id.details);
            cardview = view.findViewById(R.id.cardview);
            downbtn = view.findViewById(R.id.upbtn);
            gotoquiz = view.findViewById(R.id.gotoquiz);
            scorearea = view.findViewById(R.id.scorearea);
            score = view.findViewById(R.id.score);
            parent = view.findViewById(R.id.parent);

            itemView.setOnClickListener(this);

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

