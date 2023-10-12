package com.univirtual.student.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.univirtual.student.R;
import com.univirtual.student.realm.RealmEnrolment;

import java.util.ArrayList;

public class ChooseEnrolmentAdapter extends RecyclerView.Adapter<ChooseEnrolmentAdapter.ViewHolder> implements Filterable {
    ChooseEnrolmentAdapterInterface chooseEnrolmentAdapterInterface;
    ArrayList<RealmEnrolment> realmEnrolments;
    private Context mContext;

    public ChooseEnrolmentAdapter(ChooseEnrolmentAdapterInterface chooseEnrolmentAdapterInterface, ArrayList<RealmEnrolment> realmEnrolments) {
        this.chooseEnrolmentAdapterInterface = chooseEnrolmentAdapterInterface;
        this.realmEnrolments = realmEnrolments;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycle_choose_enrolment, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        RealmEnrolment realmEnrolment = realmEnrolments.get(position);
        holder.intructorname.setText(realmEnrolment.getInstructorname());
        holder.coursepath.setText(realmEnrolment.getCoursepath());

        holder.checkBox.setOnClickListener(view -> {
            chooseEnrolmentAdapterInterface.onListItemClick(realmEnrolments, position, holder);
        });
    }

    public interface ChooseEnrolmentAdapterInterface {
        void onListItemClick(ArrayList<RealmEnrolment> files, int position, ViewHolder holder);
    }

    @Override
    public int getItemCount() {
        return realmEnrolments.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public void reload(ArrayList<RealmEnrolment> realmEnrolments) {
        this.realmEnrolments = realmEnrolments;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public CheckBox checkBox;
        public TextView intructorname, coursepath;
        public CardView cardview;

        public ViewHolder(View view) {
            super(view);
            checkBox = view.findViewById(R.id.mycheckbox);
            intructorname = view.findViewById(R.id.intructorname);
            coursepath = view.findViewById(R.id.coursepath);
            cardview = view.findViewById(R.id.cardview);

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

