package com.univirtual.student.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.takusemba.spotlight.OnSpotlightStartedListener;
import com.takusemba.spotlight.SimpleTarget;
import com.takusemba.spotlight.Spotlight;
import com.univirtual.student.R;
import com.univirtual.student.constants.Const;
import com.univirtual.student.realm.RealmPayment;

import org.joda.time.DateTime;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import static com.univirtual.student.activity.PaymentActivity.refresh;

public class PaymentsAdapter extends RecyclerView.Adapter<PaymentsAdapter.ViewHolder> implements Filterable {
    ArrayList<RealmPayment> paymentArrayList;
    private Context mContext;

    public PaymentsAdapter(ArrayList<RealmPayment> paymentArrayList) {
        this.paymentArrayList = paymentArrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycle_payment, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final RealmPayment realmPayment = paymentArrayList.get(position);
        Date date = null;
        try {
            date = Const.dateTimeFormat.parse(realmPayment.getCreated_at());

        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.day.setText(String.valueOf(new DateTime(date).getDayOfMonth()));
        holder.month.setText(Const.months[date.getMonth()]);
        holder.year.setText(String.valueOf(new DateTime(date).getYear()));
        holder.amt.setText("GHC" + realmPayment.getAmount());
        holder.status.setText(realmPayment.getStatus());
        holder.number.setText(realmPayment.getMsisdn());
        if (realmPayment.getCoursepath() != null && !realmPayment.getCoursepath().equals("")) {
            holder.coursepath.setText(realmPayment.getCoursepath());
            holder.coursename.setText("Course");
        }
        else {
            holder.coursepath.setText(realmPayment.getInstitutionname());
            holder.coursename.setText("Institution");
        }

        /*if (realmPayment.getStatus() != null && !realmPayment.getStatus().equals("FAILED")) {
            holder.statusreason.setVisibility(View.VISIBLE);
            holder.statusreason.setText(realmPayment.getTransactionstatusreason());
        }
        else {
            holder.statusreason.setVisibility(View.GONE);
        }*/

        if (position == 0 && !PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("PAYMENT_ACTIVITY_TIPS_DISMISSED", false)) {
            ViewTreeObserver vto = holder.cardview.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        holder.cardview.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        holder.cardview.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }

                    SimpleTarget firstTarget = new SimpleTarget.Builder((Activity) mContext).setPoint(refresh)
                            .setRadius(150F)
//                        .setTitle("Tip")
                            .setDescription(mContext.getString(R.string.refresh_payment_tip) + mContext.getString(R.string.click_anywhere_to_dismiss))
                            .build();

                    // make an target
                    SimpleTarget secondTarget = new SimpleTarget.Builder((Activity) mContext).setPoint(holder.cardview)
                            .setRadius(150F)
//                        .setTitle("Account Information")
                            .setDescription(mContext.getString(R.string.click_on_a_row_to_view_additional_payment_details) + mContext.getString(R.string.click_anywhere_to_dismiss))
                            .build();

                    Spotlight.with((Activity) mContext)
//                .setOverlayColor(ContextCompat.getColor(getActivity(), R.color.background))
                            .setDuration(250L)
                            .setAnimation(new DecelerateInterpolator(2f))
                            .setTargets(firstTarget, secondTarget)
                            .setClosedOnTouchedOutside(true)
                            .setOnSpotlightStartedListener(new OnSpotlightStartedListener() {
                                @Override
                                public void onStarted() {
                                    PreferenceManager
                                            .getDefaultSharedPreferences(mContext.getApplicationContext())
                                            .edit()
                                            .putBoolean("PAYMENT_ACTIVITY_TIPS_DISMISSED", true)
                                            .apply();
                                }
                            })
                            .start();

                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return paymentArrayList.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public void reload(ArrayList<RealmPayment> paymentArrayList) {
        this.paymentArrayList = paymentArrayList;
        notifyDataSetChanged();
    }

    public void setFilter(ArrayList<RealmPayment> arrayList) {
        paymentArrayList = new ArrayList<>();
        paymentArrayList.addAll(arrayList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView month, day, year, amt, status, number, coursepath, coursename, statusreason;
        LinearLayout details;
        CardView cardview;

        public ViewHolder(View view) {
            super(view);
            month = view.findViewById(R.id.month);
            day = view.findViewById(R.id.day);
            year = view.findViewById(R.id.year);
            amt = view.findViewById(R.id.amt);
            status = view.findViewById(R.id.status);
            number = view.findViewById(R.id.number);
            coursepath = view.findViewById(R.id.coursepath);
            coursename = view.findViewById(R.id.coursename);
            statusreason = view.findViewById(R.id.statusreason);
            details = view.findViewById(R.id.details);
            cardview = view.findViewById(R.id.cardview);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (details.getVisibility() == View.VISIBLE) {
                details.setVisibility(View.GONE);
            } else {
                details.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            return false;
        }

    }
}

