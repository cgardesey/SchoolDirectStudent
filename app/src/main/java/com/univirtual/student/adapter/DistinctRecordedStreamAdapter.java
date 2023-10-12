package com.univirtual.student.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.univirtual.student.R;
import com.univirtual.student.realm.RealmAudio;
import com.univirtual.student.realm.RealmRecordedAudioStream;
import com.univirtual.student.realm.RealmRecordedVideoStream;

import java.util.ArrayList;

public class DistinctRecordedStreamAdapter extends RecyclerView.Adapter<DistinctRecordedStreamAdapter.ViewHolder> implements Filterable {
    ArrayList<Object> objects;
    DistinctRecordedStreamAdapterInterface distinctRecordedStreamAdapterInterface;
    Context mContext;

    public DistinctRecordedStreamAdapter(DistinctRecordedStreamAdapterInterface distinctRecordedStreamAdapterInterface, ArrayList<Object> objects) {
        this.distinctRecordedStreamAdapterInterface = distinctRecordedStreamAdapterInterface;
        this.objects = objects;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycle_resource, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        Object object = objects.get(position);
        if (objects.get(position) instanceof RealmAudio) {
            RealmAudio realmAudio = (RealmAudio) object;

            holder.title.setText(realmAudio.getTitle());
            holder.date.setText(realmAudio.getCreated_at());
            holder.cardview.setOnClickListener(v -> distinctRecordedStreamAdapterInterface.onListItemClick(objects, position, holder));
        } else if (objects.get(position) instanceof RealmRecordedAudioStream) {
            RealmRecordedAudioStream recordedAudioStream = (RealmRecordedAudioStream) object;

            holder.title.setText(recordedAudioStream.getTitle());
            holder.date.setText(recordedAudioStream.getCreated_at());
            holder.cardview.setOnClickListener(v -> distinctRecordedStreamAdapterInterface.onListItemClick(objects, position, holder));
        } else if (objects.get(position) instanceof RealmRecordedVideoStream) {
            RealmRecordedVideoStream realmRecordedVideoStream = (RealmRecordedVideoStream) object;

            holder.title.setText(realmRecordedVideoStream.getTitle());
            holder.date.setText(realmRecordedVideoStream.getCreated_at());
            holder.cardview.setOnClickListener(v -> distinctRecordedStreamAdapterInterface.onListItemClick(objects, position, holder));
        }
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public void reload(ArrayList<Object> resourceArrayList) {
        this.objects = resourceArrayList;
        notifyDataSetChanged();
    }

    private Bitmap convertToBitmap(byte[] b) {

        return BitmapFactory.decodeByteArray(b, 0, b.length);

    }
    //get bitmap image from byte array

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, date;
        CardView cardview;

        public ViewHolder(View view) {
            super(view);

            title = view.findViewById(R.id.title);
            date = view.findViewById(R.id.date);
            cardview = view.findViewById(R.id.cardview);
        }
    }

    public interface DistinctRecordedStreamAdapterInterface {
        void onListItemClick(ArrayList<Object> objects, int position, ViewHolder holder);
    }
}

