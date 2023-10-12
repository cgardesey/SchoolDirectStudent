package com.univirtual.student.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.univirtual.student.R;
import com.univirtual.student.constants.Const;
import com.univirtual.student.realm.RealmAttendance;
import com.univirtual.student.realm.RealmPayment;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;


/**
 * Created by Andy-Obeng on 4/3/2018.
 */

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> implements Filterable {
    ArrayList<RealmAttendance> participantArrayList;
    Context context;
    int type;


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_attendance, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    public AttendanceAdapter(ArrayList<RealmAttendance> participantArrayList, int type) {
        this.participantArrayList = participantArrayList;
        this.type = type;

    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        RealmAttendance realmAttendance = participantArrayList.get(position);
        context = holder.coursepath.getContext();
        Date date = null;
        try {
            date = Const.dateTimeFormat.parse(realmAttendance.getCreated_at());
            holder.date.setText(Const.months[date.getMonth()] + " " + String.valueOf(new DateTime(date).getDayOfMonth()) + ", " + String.valueOf(new DateTime(date).getYear()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.coursepath.setText(realmAttendance.getCoursepath());
        holder.instructorname.setText(realmAttendance.getInstructorname());
        int[] componentTimes = splitToComponentTimes(new BigDecimal(realmAttendance.getDuration()));
        holder.type.setText(realmAttendance.getType().toLowerCase().equals("video") ? context.getString(R.string.video_stream) : context.getString(R.string.audio_stream));
        holder.duration.setText(String.valueOf( componentTimes[0] + ":" + componentTimes[1] + ":" + componentTimes[2]));
        holder.title.setText(realmAttendance.getTitle());
        holder.description.setText(realmAttendance.getDescription());
    }

    @Override
    public int getItemCount() {
        return participantArrayList.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public void reload(ArrayList<RealmAttendance> participantArrayList) {
        this.participantArrayList = participantArrayList;
        notifyDataSetChanged();
    }

    public void setFilter(ArrayList<RealmAttendance> arrayList) {
        participantArrayList = new ArrayList<>();
        participantArrayList.addAll(arrayList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        LinearLayout details;
        CardView cardview;
        TextView coursepath, date, instructorname, type, duration, title, description;
        ImageView downbtn;


        public ViewHolder(View view) {
            super(view);
            details = view.findViewById(R.id.details);
            cardview = view.findViewById(R.id.cardview);
            coursepath = view.findViewById(R.id.coursepath);
            date = view.findViewById(R.id.date);
            instructorname = view.findViewById(R.id.instructorname);
            type = view.findViewById(R.id.type);
            duration = view.findViewById(R.id.duration);
            title = view.findViewById(R.id.title);
            description = view.findViewById(R.id.description);
            downbtn = view.findViewById(R.id.upbtn);

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

    public static int[] splitToComponentTimes(BigDecimal biggy)
    {
        long longVal = biggy.longValue();
        int hours = (int) longVal / 3600;
        int remainder = (int) longVal - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        int[] ints = {hours , mins , secs};
        return ints;
    }
}

