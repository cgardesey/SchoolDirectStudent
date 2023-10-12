package com.univirtual.student.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.takusemba.spotlight.SimpleTarget;
import com.takusemba.spotlight.Spotlight;
import com.univirtual.student.R;
import com.univirtual.student.activity.FileListActivity;
import com.univirtual.student.activity.SubmitAssignmentActivity;
import com.univirtual.student.constants.Const;
import com.univirtual.student.pojo.MyFile;
import com.univirtual.student.realm.RealmAssignment;
import com.univirtual.student.realm.RealmSubmittedAssignment;
import com.univirtual.student.util.RealmUtility;

import org.joda.time.DateTime;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.univirtual.student.activity.AssignmentActivity.refreshassignmentsimg;
import static com.univirtual.student.activity.FileListActivity.myFiles;



public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.ViewHolder> implements Filterable {

    String TITLE;
    String INSTRUCTORCOURSEID;
    String COURSEPATH;
    String ENROLMENTID;
    String PROFILEIMGURL;
    String ROOMID;
    String NODESERVER;
    String INSTRUCTORNAME;
    String SESSIONID;


    ArrayList<RealmAssignment> realmAssignments;
    private Context mContext;

    public AssignmentAdapter(ArrayList<RealmAssignment> realmAssignments) {
        this.realmAssignments = realmAssignments;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycle_assignment, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        String instructorcourseid = ((Activity) mContext).getIntent().getStringExtra("INSTRUCTORCOURSEID");
        String coursepathstring = ((Activity) mContext).getIntent().getStringExtra("COURSEPATH");
        String enrolmentid = ((Activity) mContext).getIntent().getStringExtra("ENROLMENTID");
        String profileimgurl = ((Activity) mContext).getIntent().getStringExtra("PROFILEIMGURL");
        String nodeserver = ((Activity) mContext).getIntent().getStringExtra("NODESERVER");
        String roomid = ((Activity) mContext).getIntent().getStringExtra("ROOMID");


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

        final RealmAssignment realmAssignment = realmAssignments.get(position);
        Date date = null;
        try {

            date = Const.dateFormat.parse(realmAssignment.getSubmitdate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.day.setText(String.valueOf(new DateTime(date).getDayOfMonth()));
        holder.month.setText(Const.months[date.getMonth()]);
        holder.year.setText(String.valueOf(new DateTime(date).getYear()));
        if (realmAssignment.getSubmitted() == 0) {
            holder.submitassignmentbtn.setVisibility(View.VISIBLE);

            holder.scorearea.setVisibility(View.GONE);
            holder.submittedassignmentbtn.setVisibility(View.GONE);
            holder.markedassignmentbtn.setVisibility(View.GONE);
        } else if (realmAssignment.getMarkedassignmenturl() == null || realmAssignment.getMarkedassignmenturl().equals("")) {
            holder.submitassignmentbtn.setVisibility(View.GONE);

            holder.scorearea.setVisibility(View.GONE);
            holder.submittedassignmentbtn.setVisibility(View.VISIBLE);
            holder.markedassignmentbtn.setVisibility(View.GONE);
        }
        else {
            holder.submitassignmentbtn.setVisibility(View.GONE);

            holder.scorearea.setVisibility(View.VISIBLE);
            holder.submittedassignmentbtn.setVisibility(View.GONE);
            holder.markedassignmentbtn.setVisibility(View.VISIBLE);
        }


        holder.assignmenttitle.setText(realmAssignment.getTitle());
        holder.score.setText(realmAssignment.getScore() + "%");
        holder.assignmentbtn.setOnClickListener(view -> {

            RealmResults<RealmAssignment> realmAssignments = Realm.getInstance(RealmUtility.getDefaultConfig(mContext)).where(RealmAssignment.class).equalTo("assignmentid", realmAssignment.getAssignmentid()).findAll();
            myFiles.clear();
            for (RealmAssignment realmAssignment1 : realmAssignments) {
                String url = realmAssignment1.getUrl();
                String[] split = url.split("/");
                myFiles.add(new MyFile(mContext.getFilesDir() + "/SchoolDirectStudent/" + realmAssignment.getCoursepath().replace(" >> ", "/") + "/Assignments/Received/" + split[split.length - 1],
                        url,
                        null
                ));
            }
            view.getContext().startActivity(new Intent(view.getContext(), FileListActivity.class)
                    .putExtra("INSTRUCTORCOURSEID", realmAssignment.getInstructorcourseid())
                    .putExtra("COURSEPATH", COURSEPATH)
                    .putExtra("ENROLMENTID", realmAssignment.getEnrolmentid())
                    .putExtra("PROFILEIMGURL", PROFILEIMGURL)
                    .putExtra("INSTRUCTORNAME", INSTRUCTORNAME)
                    .putExtra("NODESERVER", NODESERVER)
                    .putExtra("ROOMID", ROOMID)

                    .putExtra("activitytitle", "Received Assignment")
                    .putExtra("assignmenttitle", realmAssignment.getTitle())
            );
        });
        holder.submitassignmentbtn.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), SubmitAssignmentActivity.class);
            intent.putExtra("assignmentid", realmAssignment.getAssignmentid())

                    .putExtra("INSTRUCTORCOURSEID", INSTRUCTORCOURSEID)
                    .putExtra("COURSEPATH", realmAssignment.getCoursepath())
                    .putExtra("ENROLMENTID", ENROLMENTID)
                    .putExtra("PROFILEIMGURL", PROFILEIMGURL)
                    .putExtra("INSTRUCTORNAME", INSTRUCTORNAME)
                    .putExtra("NODESERVER", NODESERVER)
                    .putExtra("ROOMID", ROOMID);
            view.getContext().startActivity(intent);
        });
        holder.submittedassignmentbtn.setOnClickListener(view -> {

            RealmResults<RealmSubmittedAssignment> realmSubmittedAssignments = Realm.getInstance(RealmUtility.getDefaultConfig(mContext)).where(RealmSubmittedAssignment.class).sort("id").equalTo("assignmentid", realmAssignment.getAssignmentid()).findAll();
            myFiles.clear();
            for (RealmSubmittedAssignment realmSubmittedAssignment : realmSubmittedAssignments) {
                String url = realmSubmittedAssignment.getUrl();
                String[] split = url.split("/");
                myFiles.add(
                        new MyFile(mContext.getFilesDir() + "/SchoolDirectStudent/" + realmAssignment.getCoursepath().replace(" >> ", "/") + "/Assignments/Submitted/" + split[split.length - 1],
                                url,
                                null
                        ));
            }
            view.getContext().startActivity(new Intent(view.getContext(), FileListActivity.class)
                    .putExtra("INSTRUCTORCOURSEID", realmAssignment.getInstructorcourseid())
                    .putExtra("COURSEPATH", COURSEPATH)
                    .putExtra("ENROLMENTID", realmAssignment.getEnrolmentid())
                    .putExtra("PROFILEIMGURL", PROFILEIMGURL)
                    .putExtra("INSTRUCTORNAME", INSTRUCTORNAME)
                    .putExtra("NODESERVER", NODESERVER)
                    .putExtra("ROOMID", ROOMID)

                    .putExtra("activitytitle", "Submitted Assignment")
                    .putExtra("assignmenttitle", realmSubmittedAssignments.get(0).getTitle())
            );
        });
        holder.markedassignmentbtn.setOnClickListener(view -> {

            RealmResults<RealmSubmittedAssignment> realmSubmittedAssignments = Realm.getInstance(RealmUtility.getDefaultConfig(mContext)).where(RealmSubmittedAssignment.class).sort("id").equalTo("assignmentid", realmAssignment.getAssignmentid()).findAll();
            myFiles.clear();
            for (RealmSubmittedAssignment realmSubmittedAssignment : realmSubmittedAssignments) {
                String url = realmSubmittedAssignment.getMarkedassignmenturl();
                String[] split = url.split("/");
                myFiles.add(
                        new MyFile(mContext.getFilesDir() + "/SchoolDirectStudent/" + realmAssignment.getCoursepath().replace(" >> ", "/") + "/Assignments/Marked/" + split[split.length - 1],
                                url,
                                null
                        ));
            }
            view.getContext().startActivity(new Intent(view.getContext(), FileListActivity.class)
                    .putExtra("INSTRUCTORCOURSEID", realmAssignment.getInstructorcourseid())
                    .putExtra("COURSEPATH", COURSEPATH)
                    .putExtra("ENROLMENTID", realmAssignment.getEnrolmentid())
                    .putExtra("PROFILEIMGURL", PROFILEIMGURL)
                    .putExtra("INSTRUCTORNAME", INSTRUCTORNAME)
                    .putExtra("NODESERVER", NODESERVER)
                    .putExtra("ROOMID", ROOMID)

                    .putExtra("activitytitle", "Marked Assignment")
                    .putExtra("assignmenttitle", realmSubmittedAssignments.get(0).getTitle())
            );
        });

        if (position == 0 && !PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("ASSIGNMENTS_FRAG_TIPS_DISMISSED", false) && realmAssignment.getSubmitted() == 0) {
            ViewTreeObserver vto = holder.downbtn.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        holder.downbtn.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        holder.downbtn.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    // make an target
                    SimpleTarget firstTarget = new SimpleTarget.Builder((Activity) mContext).setPoint(refreshassignmentsimg)
                            .setRadius(150F)
//                        .setTitle("Account Information")
                            .setDescription(mContext.getString(R.string.check_for_new_assignment_tip) + mContext.getString(R.string.click_anywhere_to_dismiss))
                            .build();

                    SimpleTarget secondTarget = new SimpleTarget.Builder((Activity) mContext).setPoint(holder.downbtn)
                            .setRadius(150F)
//                        .setTitle("Account Information")
                            .setDescription(mContext.getString(R.string.information_about_assignment_tip) + mContext.getString(R.string.click_anywhere_to_dismiss))
                            .build();

                    Spotlight.with((Activity) mContext)
//                .setOverlayColor(ContextCompat.getColor(getActivity(), R.color.background))
                            .setDuration(250L)
                            .setAnimation(new DecelerateInterpolator(2f))
                            .setTargets(firstTarget, secondTarget)
                            .setClosedOnTouchedOutside(true)
                            .setOnSpotlightStartedListener(() -> {
                            })
                            .setOnSpotlightStartedListener(() -> PreferenceManager
                                    .getDefaultSharedPreferences(mContext.getApplicationContext())
                                    .edit()
                                    .putBoolean("ASSIGNMENTS_FRAG_TIPS_DISMISSED", true)
                                    .apply())
                            .start();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return realmAssignments.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public void reload(ArrayList<RealmAssignment> realmAssignments) {
        this.realmAssignments = realmAssignments;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        LinearLayout details, scorearea;
        TextView score, day, month, year, assignmenttitle, submitdate;
        View statuscolor;
        ImageView downbtn;
        Button assignmentbtn, submitassignmentbtn, submittedassignmentbtn,markedassignmentbtn;

        public ViewHolder(View view) {
            super(view);
            score = view.findViewById(R.id.score);
            details = view.findViewById(R.id.details);
            year = view.findViewById(R.id.year);
            month = view.findViewById(R.id.month);
            day = view.findViewById(R.id.day);
            assignmenttitle = view.findViewById(R.id.assignmenttitle);
            submitdate = view.findViewById(R.id.status);
            statuscolor = view.findViewById(R.id.statuscolor);
            downbtn = view.findViewById(R.id.upbtn);
            assignmentbtn = view.findViewById(R.id.assignmentbtn);
            submitassignmentbtn = view.findViewById(R.id.submitassignmentbtn);
            submittedassignmentbtn = view.findViewById(R.id.submittedassignmentbtn);
            markedassignmentbtn = view.findViewById(R.id.markedassignmentbtn);
            scorearea = view.findViewById(R.id.scorearea);


            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            if (details.getVisibility() == View.VISIBLE) {
                details.setVisibility(View.GONE);
                downbtn.animate().rotation(360).start();
            } else {
                details.setVisibility(View.VISIBLE);
                downbtn.animate().rotation(-180).start();
            }
        }

        @Override
        public boolean onLongClick(View view) {
            return false;
        }


    }
}

