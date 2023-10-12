package com.univirtual.student.adapter;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.univirtual.student.R;
import com.univirtual.student.realm.RealmRecordedVideo;

import java.util.ArrayList;

public class ExternalLinksAdapter extends RecyclerView.Adapter<ExternalLinksAdapter.ViewHolder> implements Filterable {
    ExternalLinksAdapterInterface externalLinkstAdapterInterface;
    ArrayList<RealmRecordedVideo> externalLinks;
    private Context mContext;

    public ExternalLinksAdapter(ExternalLinksAdapterInterface externalLinkstAdapterInterface, ArrayList<RealmRecordedVideo> externalLinks) {
        this.externalLinkstAdapterInterface = externalLinkstAdapterInterface;
        this.externalLinks = externalLinks;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycle_external_link, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        RealmRecordedVideo externalLink = externalLinks.get(position);
        holder.description.setText(externalLink.getDescription());

        SpannableString content = new SpannableString(externalLink.getUrl());
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        holder.link.setText(content);

        holder.link.setOnClickListener(v -> {
            externalLinkstAdapterInterface.onListItemClick(externalLinks, position, holder);
        });
    }

    public interface ExternalLinksAdapterInterface {
        void onListItemClick(ArrayList<RealmRecordedVideo> files, int position, ViewHolder holder);
    }

    @Override
    public int getItemCount() {
        return externalLinks.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public void reload(ArrayList<RealmRecordedVideo> externalLinks) {
        this.externalLinks = externalLinks;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView description, link;
        CardView cardview;

        public ViewHolder(View view) {
            super(view);
            description = view.findViewById(R.id.description);
            link = view.findViewById(R.id.link);
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

