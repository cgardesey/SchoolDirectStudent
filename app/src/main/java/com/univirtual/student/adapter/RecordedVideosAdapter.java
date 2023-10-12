package com.univirtual.student.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.univirtual.student.R;
import com.univirtual.student.activity.PdfActivity;
import com.univirtual.student.activity.VideoActivity;
import com.univirtual.student.realm.RealmBase64File;
import com.univirtual.student.realm.RealmRecordedVideo;
import com.univirtual.student.util.RealmUtility;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.univirtual.student.activity.RecordedVideosActivity.downFileAsyncMap;
import static com.univirtual.student.activity.VideoActivity.VIDEO_URL;
import static com.univirtual.student.constants.Const.fileSize;
import static com.univirtual.student.constants.Const.isNetworkAvailable;
import static com.univirtual.student.constants.Const.retrieveFileFromRealm;

public class RecordedVideosAdapter extends RecyclerView.Adapter<RecordedVideosAdapter.ViewHolder> implements Filterable {
    String TITLE;
    String INSTRUCTORCOURSEID;
    String COURSEPATH;
    String ENROLMENTID;
    String PROFILEIMGURL;
    String ROOMID;
    String NODESERVER;
    String INSTRUCTORNAME;
    String SESSIONID;

    ArrayList<RealmRecordedVideo> realmRecordedVideos;
    private Activity activity;
    RecordedVideoStreamAdapterInterface recordedVideoStreamAdapterInterface;
    Context mContext;

    public RecordedVideosAdapter(RecordedVideoStreamAdapterInterface recordedVideoStreamAdapterInterface, Activity activity, ArrayList<RealmRecordedVideo> realmRecordedVideos) {
        this.recordedVideoStreamAdapterInterface = recordedVideoStreamAdapterInterface;
        this.activity = activity;
        this.realmRecordedVideos = realmRecordedVideos;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycle_video, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        String instructorcourseid = activity.getIntent().getStringExtra("INSTRUCTORCOURSEID");
        String coursepathstring = activity.getIntent().getStringExtra("COURSEPATH");
        String enrolmentid = activity.getIntent().getStringExtra("ENROLMENTID");
        String profileimgurl = activity.getIntent().getStringExtra("PROFILEIMGURL");
        String nodeserver = activity.getIntent().getStringExtra("NODESERVER");
        String roomid = activity.getIntent().getStringExtra("ROOMID");


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

        RealmRecordedVideo realmRecordedVideo = realmRecordedVideos.get(position);
        mContext = holder.photoImageView.getContext();
        Glide.with(mContext).load(realmRecordedVideo.getThumbnail()).apply( new RequestOptions().centerCrop()).into(holder.photoImageView);
        holder.imagetype.setVisibility(View.GONE);
        Glide.with(mContext).asGif().load(realmRecordedVideo.getGiflink()).apply(new RequestOptions()
                .centerCrop()
                .placeholder(null)
                .error(null))
                .into(holder.photoImageView);

        holder.downloadStatusWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (realmRecordedVideo.getUrl().endsWith(";")) {
                    VIDEO_URL = realmRecordedVideo.getUrl().substring(0, realmRecordedVideo.getUrl().length() - 1);
                } else {
                    VIDEO_URL = realmRecordedVideo.getUrl();
                }
                if (isNetworkAvailable(mContext)) {
                    mContext.startActivity(new Intent(mContext, VideoActivity.class));
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (relatedResourcesExist(realmRecordedVideo.getUrl())) {
            holder.downloadStatusWrapper.setVisibility(View.GONE);
            holder.removelayout.setVisibility(View.VISIBLE);

            holder.size.setText(fileSize(getFilesSize(realmRecordedVideo.getUrl())));
        } else {
            holder.downloadStatusWrapper.setVisibility(View.VISIBLE);
            holder.removelayout.setVisibility(View.GONE);

            if (filesDownloading(realmRecordedVideo.getUrl())) {
                holder.download.setVisibility(View.GONE);

                holder.pbar.setVisibility(View.VISIBLE);
                holder.pbar.animate();
            } else {
                holder.download.setVisibility(View.VISIBLE);

                holder.pbar.setVisibility(View.GONE);
            }
        }

        holder.removelayout.setOnClickListener(v -> {
            removeResources(realmRecordedVideo.getUrl());
            if (relatedResourcesExist(realmRecordedVideo.getUrl())) {
                Toast.makeText(mContext, mContext.getString(R.string.error_deleting_file_from_device), Toast.LENGTH_SHORT).show();
            } else {
                holder.downloadStatusWrapper.setVisibility(View.VISIBLE);
                holder.pbar.setVisibility(View.GONE);
                holder.download.setVisibility(View.VISIBLE);
                holder.removelayout.setVisibility(View.GONE);
            }
        });
        holder.downloadStatusWrapper.setOnClickListener(view -> {
            recordedVideoStreamAdapterInterface.onListItemClick(realmRecordedVideos, position, holder);
        });
        holder.play.setOnClickListener(v -> {

            if (holder.downloadStatusWrapper.getVisibility() == View.GONE) {
                String video_path = null;
                try {
                    video_path = mContext.getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Recorded-videos/" + URLUtil.guessFileName(realmRecordedVideo.getUrl(), null, null);
                    retrieveFileFromRealm(mContext, realmRecordedVideo.getUrl(), video_path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                VIDEO_URL = video_path;
                mContext.startActivity(new Intent(mContext, VideoActivity.class));
            }
            else {
                if (isNetworkAvailable(activity)) {
                    VIDEO_URL = realmRecordedVideo.getUrl();
                    activity.startActivity(new Intent(activity, VideoActivity.class));
                } else {
                    Toast.makeText(activity, activity.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return realmRecordedVideos.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public void reload(ArrayList<RealmRecordedVideo> resourceArrayList) {
        this.realmRecordedVideos = resourceArrayList;
        notifyDataSetChanged();
    }

    private Bitmap convertToBitmap(byte[] b) {

        return BitmapFactory.decodeByteArray(b, 0, b.length);

    }
    //get bitmap image from byte array

    public class ViewHolder extends RecyclerView.ViewHolder {
        CardView play;
        ImageView photoImageView, imagetype;

        public TextView size;
        CardView cardview;
        public RelativeLayout downloadStatusWrapper, removelayout;
        public ProgressBar pbar;
        public ImageView download;
        public ImageView remove;

        public ViewHolder(View view) {
            super(view);

            photoImageView = view.findViewById(R.id.photoImageView);
            imagetype = view.findViewById(R.id.imagetype);
            play = view.findViewById(R.id.play);

            cardview = view.findViewById(R.id.cardview);
            downloadStatusWrapper = view.findViewById(R.id.downloadStatusWrapper);
            removelayout = view.findViewById(R.id.removelayout);
            pbar = view.findViewById(R.id.pbar);
            download = view.findViewById(R.id.download);
            remove = view.findViewById(R.id.remove);
            size = view.findViewById(R.id.size);
        }
    }

    public interface RecordedVideoStreamAdapterInterface {
        void onListItemClick(ArrayList<RealmRecordedVideo> realmRecordedVideos, int position, ViewHolder holder);
    }


    private boolean filesDownloading(String url) {
        AsyncTask<String, Integer, String> downloadFileAsync = (AsyncTask<String, Integer, String>) downFileAsyncMap.get(url);
        return downloadFileAsync != null && downloadFileAsync.getStatus() != AsyncTask.Status.FINISHED;
    }

    private void removeResources(String url) {
        Realm.init(activity);
        Realm.getInstance(RealmUtility.getDefaultConfig(activity)).executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<RealmBase64File> videoData = realm.where(RealmBase64File.class).equalTo("url", url).findAll();
                videoData.deleteAllFromRealm();
            }
        });
    }

    private boolean relatedResourcesExist(String url) {
        final boolean[] exist = {true};
        Realm.init(activity);
        Realm.getInstance(RealmUtility.getDefaultConfig(activity)).executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                if (realm.where(RealmBase64File.class).equalTo("url", url).findFirst() == null) {
                    exist[0] = false;
                }
            }
        });

        return exist[0];
    }

    public interface RecordedStreamAdapterInterface {
        void onListItemClick(ArrayList<Object> objects, int position, RecordedStreamAdapter.ViewHolder holder);
    }

    private void retrieveFilesFromRealm(Context context, String url) {
        try {
            retrieveFileFromRealm(context, url, context.getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Recorded-videos/" + URLUtil.guessFileName(url, null, null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getFilesSize(String url) {
        final int[] filesLength = {0};
        Realm.init(activity);
        Realm.getInstance(RealmUtility.getDefaultConfig(activity)).executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                try {
                    RealmResults<RealmBase64File> audioData = realm.where(RealmBase64File.class).equalTo("url", url).findAll();
                    for (RealmBase64File realmBase64File : audioData) {
                        filesLength[0] += realmBase64File.getBase64String().getBytes("UTF-8").length;
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
        return filesLength[0];
    }
}

