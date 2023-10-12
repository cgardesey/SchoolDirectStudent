package com.univirtual.student.adapter;

import android.Manifest;
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
import com.greysonparrelli.permiso.Permiso;
import com.univirtual.student.R;
import com.univirtual.student.activity.AudioStreamActivity;
import com.univirtual.student.activity.PdfActivity;
import com.univirtual.student.activity.RecordedStreamActivity;
import com.univirtual.student.activity.VideoActivity;
import com.univirtual.student.realm.RealmAudio;
import com.univirtual.student.realm.RealmBase64File;
import com.univirtual.student.realm.RealmRecordedAudioStream;
import com.univirtual.student.realm.RealmRecordedVideoStream;
import com.univirtual.student.util.RealmUtility;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.univirtual.student.activity.ClassReplaysActivity.downFileAsyncMap;
import static com.univirtual.student.activity.SelectResourceActivity.gotonextActivity;
import static com.univirtual.student.activity.VideoActivity.VIDEO_URL;
import static com.univirtual.student.constants.Const.fileSize;
import static com.univirtual.student.constants.Const.isNetworkAvailable;
import static com.univirtual.student.constants.Const.retrieveFileFromRealm;
import static com.univirtual.student.constants.Const.retrieveJsonCoordinatesFromRealm;

public class RecordedStreamAdapter extends RecyclerView.Adapter<RecordedStreamAdapter.ViewHolder> implements Filterable {

    String TITLE;
    String INSTRUCTORCOURSEID;
    String COURSEPATH;
    String ENROLMENTID;
    String PROFILEIMGURL;
    String ROOMID;
    String NODESERVER;
    String INSTRUCTORNAME;
    String SESSIONID;

    ArrayList<Object> objects;
    private Activity activity;
    private String type;
    RecordedStreamAdapterInterface recordedStreamAdapterInterface;

    public RecordedStreamAdapter(RecordedStreamAdapterInterface recordedStreamAdapterInterface, Activity activity, ArrayList<Object> objects, String type) {
        this.recordedStreamAdapterInterface = recordedStreamAdapterInterface;
        this.activity = activity;
        this.objects = objects;
        this.type = type;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_video, parent, false);
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

        Object object = objects.get(position);
        String thumbnail = "";
        if (objects.get(position) instanceof RealmAudio) {
            RealmAudio realmAudio = (RealmAudio) object;
            thumbnail = realmAudio.getThumbnail();
            holder.icon.setImageDrawable(activity.getResources().getDrawable(R.drawable.phone));

            if (relatedResourcesExist(realmAudio.getAudiourl())) {
                holder.removelayout.setVisibility(View.VISIBLE);
                holder.downloadStatusWrapper.setVisibility(View.GONE);
                holder.size.setText(fileSize(getFilesSize(realmAudio.getAudiourl())));
            } else {
                holder.removelayout.setVisibility(View.GONE);
                holder.downloadStatusWrapper.setVisibility(View.VISIBLE);

                if (filesDownloading(realmAudio.getAudiourl())) {
                    holder.pbar.setVisibility(View.VISIBLE);
                    holder.pbar.animate();
                    holder.downloadStatusWrapper.setVisibility(View.GONE);
                } else {
                    holder.pbar.setVisibility(View.GONE);
                    holder.downloadStatusWrapper.setVisibility(View.VISIBLE);
                }
            }
            holder.removelayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeResources(realmAudio.getAudiourl());
                    if (relatedResourcesExist(realmAudio.getAudiourl())) {
                        Toast.makeText(activity, activity.getString(R.string.error_deleting_file_from_device), Toast.LENGTH_SHORT).show();
                    } else {
                        holder.downloadStatusWrapper.setVisibility(View.VISIBLE);
                        holder.pbar.setVisibility(View.GONE);
                        holder.download.setVisibility(View.VISIBLE);
                        holder.removelayout.setVisibility(View.GONE);
                    }
                }
            });
            holder.play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.removelayout.getVisibility() == View.VISIBLE) {
                        if (objects.get(position) instanceof RealmAudio) {
                            VIDEO_URL = activity.getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Recorded-videos/" + URLUtil.guessFileName(((RealmAudio) objects.get(position)).getAudiourl(), null, null);
                        } else if (objects.get(position) instanceof RealmRecordedAudioStream) {
                            VIDEO_URL = activity.getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Recorded-videos/" + URLUtil.guessFileName(((RealmRecordedAudioStream) objects.get(position)).getUrl(), null, null);
                        } else if (objects.get(position) instanceof RealmRecordedVideoStream) {
                            VIDEO_URL = activity.getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Recorded-videos/" + URLUtil.guessFileName(((RealmRecordedVideoStream) objects.get(position)).getUrl(), null, null);
                        }
                        activity.startActivity(new Intent(activity, VideoActivity.class));
                    } else {
                        VIDEO_URL = realmAudio.getAudiourl();
                        if (isNetworkAvailable(activity)) {
                            activity.startActivity(new Intent(activity, VideoActivity.class));
                        } else {
                            Toast.makeText(activity, activity.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        } else if (objects.get(position) instanceof RealmRecordedAudioStream) {
            RealmRecordedAudioStream recordedAudioStream = (RealmRecordedAudioStream) object;
            thumbnail = recordedAudioStream.getThumbnail();
            holder.icon.setImageDrawable(activity.getResources().getDrawable(R.drawable.audio));

            if (relatedResourcesExist(recordedAudioStream.getUrl())) {
                holder.removelayout.setVisibility(View.VISIBLE);
                holder.downloadStatusWrapper.setVisibility(View.GONE);
                holder.size.setText(fileSize(getFilesSize(recordedAudioStream.getUrl())));
            } else {
                holder.removelayout.setVisibility(View.GONE);
                holder.downloadStatusWrapper.setVisibility(View.VISIBLE);

                if (filesDownloading(recordedAudioStream.getUrl())) {
                    holder.pbar.setVisibility(View.VISIBLE);
                    holder.pbar.animate();
                    holder.downloadStatusWrapper.setVisibility(View.GONE);
                } else {
                    holder.pbar.setVisibility(View.GONE);
                    holder.downloadStatusWrapper.setVisibility(View.VISIBLE);
                }
            }
            holder.removelayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeResources(recordedAudioStream.getUrl());
                    if (relatedResourcesExist(recordedAudioStream.getUrl())) {
                        Toast.makeText(activity, activity.getString(R.string.error_deleting_file_from_device), Toast.LENGTH_SHORT).show();
                    } else {
                        holder.downloadStatusWrapper.setVisibility(View.VISIBLE);
                        holder.pbar.setVisibility(View.GONE);
                        holder.download.setVisibility(View.VISIBLE);
                        holder.removelayout.setVisibility(View.GONE);
                    }
                }
            });
            holder.play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.removelayout.getVisibility() == View.VISIBLE) {
                        if (objects.get(position) instanceof RealmAudio) {
                            VIDEO_URL = activity.getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Recorded-videos/" + URLUtil.guessFileName(((RealmAudio) objects.get(position)).getAudiourl(), null, null);
                        } else if (objects.get(position) instanceof RealmRecordedAudioStream) {
                            VIDEO_URL = activity.getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Recorded-videos/" + URLUtil.guessFileName(((RealmRecordedAudioStream) objects.get(position)).getUrl(), null, null);
                        } else if (objects.get(position) instanceof RealmRecordedVideoStream) {
                            VIDEO_URL = activity.getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Recorded-videos/" + URLUtil.guessFileName(((RealmRecordedVideoStream) objects.get(position)).getUrl(), null, null);
                        }
                        activity.startActivity(new Intent(activity, VideoActivity.class));
                    } else {
                        VIDEO_URL = recordedAudioStream.getUrl();
                        if (isNetworkAvailable(activity)) {
                            activity.startActivity(new Intent(activity, VideoActivity.class));
                        } else {
                            Toast.makeText(activity, activity.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        } else if (objects.get(position) instanceof RealmRecordedVideoStream) {
            RealmRecordedVideoStream realmRecordedVideoStream = (RealmRecordedVideoStream) object;
            thumbnail = realmRecordedVideoStream.getThumbnail();
            holder.icon.setImageDrawable(activity.getResources().getDrawable(R.drawable.video));

            if (relatedResourcesExist(realmRecordedVideoStream.getUrl())) {
                holder.removelayout.setVisibility(View.VISIBLE);
                holder.downloadStatusWrapper.setVisibility(View.GONE);
                holder.size.setText(fileSize(getFilesSize(realmRecordedVideoStream.getUrl())));
            } else {
                holder.removelayout.setVisibility(View.GONE);
                holder.downloadStatusWrapper.setVisibility(View.VISIBLE);

                if (filesDownloading(realmRecordedVideoStream.getUrl())) {
                    holder.pbar.setVisibility(View.VISIBLE);
                    holder.pbar.animate();
                    holder.downloadStatusWrapper.setVisibility(View.GONE);
                } else {
                    holder.pbar.setVisibility(View.GONE);
                    holder.downloadStatusWrapper.setVisibility(View.VISIBLE);
                }
            }
            holder.removelayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeResources(realmRecordedVideoStream.getUrl());
                    if (relatedResourcesExist(realmRecordedVideoStream.getUrl())) {
                        Toast.makeText(activity, activity.getString(R.string.error_deleting_file_from_device), Toast.LENGTH_SHORT).show();
                    } else {
                        holder.downloadStatusWrapper.setVisibility(View.VISIBLE);
                        holder.pbar.setVisibility(View.GONE);
                        holder.download.setVisibility(View.VISIBLE);
                        holder.removelayout.setVisibility(View.GONE);
                    }
                }
            });
            holder.play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.removelayout.getVisibility() == View.VISIBLE) {
                        if (objects.get(position) instanceof RealmAudio) {
                            VIDEO_URL = activity.getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Recorded-videos/" + URLUtil.guessFileName(((RealmAudio) objects.get(position)).getAudiourl(), null, null);
                        } else if (objects.get(position) instanceof RealmRecordedAudioStream) {
                            VIDEO_URL = activity.getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Recorded-videos/" + URLUtil.guessFileName(((RealmRecordedAudioStream) objects.get(position)).getUrl(), null, null);
                        } else if (objects.get(position) instanceof RealmRecordedVideoStream) {
                            VIDEO_URL = activity.getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Recorded-videos/" + URLUtil.guessFileName(((RealmRecordedVideoStream) objects.get(position)).getUrl(), null, null);
                        }
                        activity.startActivity(new Intent(activity, VideoActivity.class));
                    } else {
                        VIDEO_URL = realmRecordedVideoStream.getUrl();
                        if (isNetworkAvailable(activity)) {
                            activity.startActivity(new Intent(activity, VideoActivity.class));
                        } else {
                            Toast.makeText(activity, activity.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
        Glide.with(activity).load(thumbnail).apply(new RequestOptions().centerCrop()).into(holder.photoImageView);
        holder.play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordedStreamAdapterInterface.onListItemClick(objects, position, holder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    @Override
    public Filter getFilter() {
        return null;
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

    public void reload(ArrayList<Object> resourceArrayList) {
        this.objects = resourceArrayList;
        notifyDataSetChanged();
    }

    private Bitmap convertToBitmap(byte[] b) {

        return BitmapFactory.decodeByteArray(b, 0, b.length);

    }
    //get bitmap image from byte array

    public class ViewHolder extends RecyclerView.ViewHolder {
        CardView play;
        ImageView photoImageView, icon;

        public TextView size;
        public RelativeLayout downloadStatusWrapper, removelayout;
        public ProgressBar pbar;
        public ImageView download;
        public ImageView remove;

        public ViewHolder(View view) {
            super(view);

            photoImageView = view.findViewById(R.id.photoImageView);
            icon = view.findViewById(R.id.imagetype);
            play = view.findViewById(R.id.play);

            downloadStatusWrapper = view.findViewById(R.id.downloadStatusWrapper);
            removelayout = view.findViewById(R.id.removelayout);
            pbar = view.findViewById(R.id.pbar);
            download = view.findViewById(R.id.download);
            remove = view.findViewById(R.id.remove);
            size = view.findViewById(R.id.size);
        }
    }

    public interface RecordedStreamAdapterInterface {
        void onListItemClick(ArrayList<Object> objects, int position, ViewHolder holder);
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

