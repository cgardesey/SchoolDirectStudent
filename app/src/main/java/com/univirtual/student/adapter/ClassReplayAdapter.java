package com.univirtual.student.adapter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.greysonparrelli.permiso.Permiso;
import com.univirtual.student.R;
import com.univirtual.student.activity.AudioStreamActivity;
import com.univirtual.student.activity.VideoActivity;
import com.univirtual.student.realm.RealmAudio;
import com.univirtual.student.realm.RealmBase64File;
import com.univirtual.student.realm.RealmRecordedAudioStream;
import com.univirtual.student.realm.RealmRecordedVideoStream;
import com.univirtual.student.util.RealmUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.univirtual.student.activity.AudioStreamActivity.audioStreamActivity;
import static com.univirtual.student.activity.ClassReplaysActivity.downFileAsyncMap;
import static com.univirtual.student.activity.SelectResourceActivity.gotonextActivity;
import static com.univirtual.student.activity.VideoActivity.VIDEO_URL;
import static com.univirtual.student.constants.Const.fileSize;
import static com.univirtual.student.constants.Const.isNetworkAvailable;
import static com.univirtual.student.constants.Const.retrieveFileFromRealm;
import static com.univirtual.student.constants.Const.retrieveJsonCoordinatesFromRealm;

public class ClassReplayAdapter extends RecyclerView.Adapter<ClassReplayAdapter.ViewHolder> implements Filterable {

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
    ClassReplayAdapterInterface audioAdapterInterface;
    Context mContext;

    public ClassReplayAdapter(ClassReplayAdapterInterface classReplayAdapterInterface, Activity activity, ArrayList<Object> objects) {
        this.audioAdapterInterface = classReplayAdapterInterface;
        this.activity = activity;
        this.objects = objects;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycle_replay, parent, false);
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

        final Object object = objects.get(position);

        if (objects.get(position) instanceof RealmAudio) {
            RealmAudio realmAudio = (RealmAudio) object;
            holder.title.setText(realmAudio.getTitle());
            holder.icon.setImageDrawable(activity.getResources().getDrawable(R.drawable.phone));
            holder.date.setText(realmAudio.getCreated_at());

            if (relatedResourcesExist(realmAudio.getAudiourl(), realmAudio.getUrl())) {
                holder.removelayout.setVisibility(View.VISIBLE);
                holder.downloadStatusWrapper.setVisibility(View.GONE);

                holder.size.setText(fileSize(getFilesSize(realmAudio.getUrl(), realmAudio.getUrl())));
            } else {
                holder.removelayout.setVisibility(View.GONE);
                holder.downloadStatusWrapper.setVisibility(View.VISIBLE);
                if (filesDownloading(realmAudio.getAudioid())) {
                    holder.download.setVisibility(View.GONE);

                    holder.pbar.setVisibility(View.VISIBLE);
                    holder.pbar.animate();
                } else {
                    holder.download.setVisibility(View.VISIBLE);

                    holder.pbar.setVisibility(View.GONE);
                }
            }

            holder.removelayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeResources(realmAudio.getAudiourl(), realmAudio.getUrl());
                    if (relatedResourcesExist(realmAudio.getUrl(), realmAudio.getUrl())) {
                        Toast.makeText(mContext, mContext.getString(R.string.error_deleting_file_from_device), Toast.LENGTH_SHORT).show();
                    } else {
                        holder.downloadStatusWrapper.setVisibility(View.VISIBLE);
                        holder.pbar.setVisibility(View.GONE);
                        holder.download.setVisibility(View.VISIBLE);
                        holder.removelayout.setVisibility(View.GONE);
                    }
                }
            });
            holder.playLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AsyncTask<String, Integer, String> downloadFileAsync = (AsyncTask<String, Integer, String>) downFileAsyncMap.get(realmAudio.getAudioid());
                    boolean fileDownloading = downloadFileAsync != null && downloadFileAsync.getStatus() != AsyncTask.Status.FINISHED;
                    if (!fileDownloading) {
                        AudioStreamActivity.ISRECORDED = true;
                        if (holder.removelayout.getVisibility() == View.VISIBLE) {
                            Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
                                                                         @Override
                                                                         public void onPermissionResult(Permiso.ResultSet resultSet) {
                                                                             if (resultSet.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE) && resultSet.isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                                                                 try {
                                                                                     retrieveFilesFromRealm(mContext, realmAudio.getAudiourl(), realmAudio.getUrl());
                                                                                     launchNext(realmAudio.getSessionid(), mContext, mContext.getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Class-sessions/Documents/" + URLUtil.guessFileName(realmAudio.getAudiourl(), null, null));
                                                                                 } catch (JSONException e) {
                                                                                     e.printStackTrace();
                                                                                 } catch (FileNotFoundException e) {
                                                                                     e.printStackTrace();
                                                                                 } catch (UnsupportedEncodingException e) {
                                                                                     e.printStackTrace();
                                                                                 }
                                                                             }
                                                                         }

                                                                         @Override
                                                                         public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                                                                             Permiso.getInstance().showRationaleInDialog(mContext.getString(R.string.permissions), mContext.getString(R.string.this_permission_is_mandatory_pls_allow_access), null, callback);
                                                                         }
                                                                     },
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

                        } else {
                            gotonextActivity(mContext, "call", realmAudio.getAudiourl(), realmAudio.getUrl(), realmAudio.getSessionid());
                        }
                    }
                }
            });
        } else if (objects.get(position) instanceof RealmRecordedAudioStream) {
            RealmRecordedAudioStream recordedAudioStream = (RealmRecordedAudioStream) object;
            holder.title.setText(recordedAudioStream.getTitle());
            holder.icon.setImageDrawable(activity.getResources().getDrawable(R.drawable.audio));
            holder.date.setText(recordedAudioStream.getCreated_at());

            if (relatedResourcesExist(recordedAudioStream.getUrl(), recordedAudioStream.getDocurl())) {
                holder.removelayout.setVisibility(View.VISIBLE);
                holder.downloadStatusWrapper.setVisibility(View.GONE);

                holder.size.setText(fileSize(getFilesSize(recordedAudioStream.getUrl(), recordedAudioStream.getDocurl())));
            } else {
                holder.removelayout.setVisibility(View.GONE);
                holder.downloadStatusWrapper.setVisibility(View.VISIBLE);
                if (filesDownloading(recordedAudioStream.getRecordedaudiostreamid())) {
                    holder.download.setVisibility(View.GONE);

                    holder.pbar.setVisibility(View.VISIBLE);
                    holder.pbar.animate();
                } else {
                    holder.download.setVisibility(View.VISIBLE);

                    holder.pbar.setVisibility(View.GONE);
                }
            }

            holder.removelayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeResources(recordedAudioStream.getUrl(), recordedAudioStream.getDocurl());
                    if (relatedResourcesExist(recordedAudioStream.getUrl(), recordedAudioStream.getDocurl())) {
                        Toast.makeText(mContext, mContext.getString(R.string.error_deleting_file_from_device), Toast.LENGTH_SHORT).show();
                    } else {
                        holder.downloadStatusWrapper.setVisibility(View.VISIBLE);
                        holder.pbar.setVisibility(View.GONE);
                        holder.download.setVisibility(View.VISIBLE);
                        holder.removelayout.setVisibility(View.GONE);
                    }
                }
            });
            holder.playLayout.setOnClickListener(new View.OnClickListener() {
                @Override

                public void onClick(View view) {
                    AsyncTask<String, Integer, String> downloadFileAsync = (AsyncTask<String, Integer, String>) downFileAsyncMap.get(recordedAudioStream.getRecordedaudiostreamid());
                    boolean fileDownloading = downloadFileAsync != null && downloadFileAsync.getStatus() != AsyncTask.Status.FINISHED;
                    if (!fileDownloading) {
                        AudioStreamActivity.ISRECORDED = true;
                        if (holder.removelayout.getVisibility() == View.VISIBLE) {
                            //                            Toast.makeText(activity, Boolean.toString(new File(mContext.getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Class-sessions/Documents/" + URLUtil.guessFileName(recordedAudioStream.getUrl(), null, null).replace(".mp4", ".txt")).exists()), Toast.LENGTH_SHORT).show();
                            Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
                                                                         @Override
                                                                         public void onPermissionResult(Permiso.ResultSet resultSet) {
                                                                             if (resultSet.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE) && resultSet.isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                                                                 try {
                                                                                     retrieveFilesFromRealm(mContext, recordedAudioStream.getUrl(), recordedAudioStream.getDocurl());
                                                                                     launchNext(recordedAudioStream.getSessionid(), mContext, mContext.getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Class-sessions/Documents/" + URLUtil.guessFileName(recordedAudioStream.getUrl(), null, null));
                                                                                 } catch (JSONException e) {
                                                                                     e.printStackTrace();
                                                                                 } catch (FileNotFoundException e) {
                                                                                     e.printStackTrace();
                                                                                 } catch (UnsupportedEncodingException e) {
                                                                                     e.printStackTrace();
                                                                                 }
                                                                             }
                                                                         }

                                                                         @Override
                                                                         public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                                                                             Permiso.getInstance().showRationaleInDialog(mContext.getString(R.string.permissions), mContext.getString(R.string.this_permission_is_mandatory_pls_allow_access), null, callback);
                                                                         }
                                                                     },
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        } else {
                            gotonextActivity(mContext, "audio", recordedAudioStream.getUrl(), recordedAudioStream.getDocurl(), recordedAudioStream.getSessionid());
                        }
                    }
                }
            });
        } else if (objects.get(position) instanceof RealmRecordedVideoStream) {
            RealmRecordedVideoStream realmRecordedVideoStream = (RealmRecordedVideoStream) object;
            holder.title.setText(realmRecordedVideoStream.getTitle());
            holder.icon.setImageDrawable(activity.getResources().getDrawable(R.drawable.video));
            holder.date.setText(realmRecordedVideoStream.getCreated_at());

            if (relatedResourcesExist(realmRecordedVideoStream.getUrl(), realmRecordedVideoStream.getDocurl())) {
                holder.removelayout.setVisibility(View.VISIBLE);
                holder.downloadStatusWrapper.setVisibility(View.GONE);

                holder.size.setText(fileSize(getFilesSize(realmRecordedVideoStream.getUrl(), realmRecordedVideoStream.getDocurl())));
            } else {
                holder.removelayout.setVisibility(View.GONE);
                holder.downloadStatusWrapper.setVisibility(View.VISIBLE);

                if (filesDownloading(realmRecordedVideoStream.getRecordedvideostreamid())) {
                    holder.download.setVisibility(View.GONE);

                    holder.pbar.setVisibility(View.VISIBLE);
                    holder.pbar.animate();
                } else {
                    holder.download.setVisibility(View.VISIBLE);

                    holder.pbar.setVisibility(View.GONE);
                }
            }

            holder.removelayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeResources(realmRecordedVideoStream.getUrl(), realmRecordedVideoStream.getDocurl());
                    if (relatedResourcesExist(realmRecordedVideoStream.getUrl(), realmRecordedVideoStream.getDocurl())) {
                        Toast.makeText(mContext, mContext.getString(R.string.error_deleting_file_from_device), Toast.LENGTH_SHORT).show();
                    } else {
                        holder.downloadStatusWrapper.setVisibility(View.VISIBLE);
                        holder.pbar.setVisibility(View.GONE);
                        holder.download.setVisibility(View.VISIBLE);
                        holder.removelayout.setVisibility(View.GONE);
                    }
                }
            });
            holder.playLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AsyncTask<String, Integer, String> downloadFileAsync = (AsyncTask<String, Integer, String>) downFileAsyncMap.get(realmRecordedVideoStream.getRecordedvideostreamid());
                    boolean fileDownloading = downloadFileAsync != null && downloadFileAsync.getStatus() != AsyncTask.Status.FINISHED;
                    if (!fileDownloading) {
                        AudioStreamActivity.ISRECORDED = true;
                        if (holder.removelayout.getVisibility() == View.VISIBLE) {
                            Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
                                                                         @Override
                                                                         public void onPermissionResult(Permiso.ResultSet resultSet) {
                                                                             if (resultSet.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE) && resultSet.isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                                                                 try {
                                                                                     VIDEO_URL = activity.getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Class-sessions/Documents/" + URLUtil.guessFileName(realmRecordedVideoStream.getUrl(), null, null);
                                                                                     retrieveFileFromRealm(activity, realmRecordedVideoStream.getUrl(), VIDEO_URL);
                                                                                     activity.startActivity(new Intent(activity, VideoActivity.class));
                                                                                 } catch (JSONException e) {
                                                                                     e.printStackTrace();
                                                                                 } catch (FileNotFoundException e) {
                                                                                     e.printStackTrace();
                                                                                 } catch (Exception e) {
                                                                                     e.printStackTrace();
                                                                                 }
                                                                             }
                                                                         }

                                                                         @Override
                                                                         public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                                                                             Permiso.getInstance().showRationaleInDialog(mContext.getString(R.string.permissions), mContext.getString(R.string.this_permission_is_mandatory_pls_allow_access), null, callback);
                                                                         }
                                                                     },
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

                        } else {
                            VIDEO_URL = realmRecordedVideoStream.getUrl();
                            if (isNetworkAvailable(activity)) {
                                activity.startActivity(new Intent(activity, VideoActivity.class));
                            } else {
                                Toast.makeText(activity, activity.getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            });
        }
        holder.title.setVisibility(View.GONE);
        holder.downloadStatusWrapper.setOnClickListener(v -> audioAdapterInterface.onListItemClick(objects, position, holder));
    }

    private boolean filesDownloading(String recourseid) {
        AsyncTask<String, Integer, String> downloadFileAsync = (AsyncTask<String, Integer, String>) downFileAsyncMap.get(recourseid);
        return downloadFileAsync != null && downloadFileAsync.getStatus() != AsyncTask.Status.FINISHED;
    }

    private void removeResources(String url, String pdfs) {
        if (pdfs.endsWith(";")) {
            pdfs = pdfs.substring(0, pdfs.length() - 1);
        }

        Realm.init(mContext);
        String finalPdfs = pdfs;
        Realm.getInstance(RealmUtility.getDefaultConfig(mContext)).executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<RealmBase64File> audioData = realm.where(RealmBase64File.class).equalTo("url", url).findAll();
                audioData.deleteAllFromRealm();
                RealmResults<RealmBase64File> coordinatedData = realm.where(RealmBase64File.class).equalTo("url", url.replace(".mp4", ".txt")).findAll();
                coordinatedData.deleteAllFromRealm();
                if (finalPdfs != null && !finalPdfs.equals("")) {
                    for (String pdf : finalPdfs.split(";")) {
                        RealmResults<RealmBase64File> pdfData = realm.where(RealmBase64File.class).equalTo("url", pdf).findAll();
                        pdfData.deleteAllFromRealm();
                    }
                }
            }
        });
    }

    private boolean relatedResourcesExist(String url, String pdfs) {
        if (pdfs.endsWith(";")) {
            pdfs = pdfs.substring(0, pdfs.length() - 1);
        }

        final boolean[] exist = {true};
        Realm.init(mContext);
        String finalPdfs = pdfs;
        Realm.getInstance(RealmUtility.getDefaultConfig(mContext)).executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                if (realm.where(RealmBase64File.class).equalTo("url", url).findFirst() == null) {
                    exist[0] = false;
                }
                if (realm.where(RealmBase64File.class).equalTo("url", url.replace(".mp4", ".txt")).findFirst() == null) {
                    exist[0] = false;
                }
                if (finalPdfs != null && !finalPdfs.equals("")) {
                    for (String pdf : finalPdfs.split(";")) {
                        if (realm.where(RealmBase64File.class).equalTo("url", pdf).findFirst() == null) {
                            exist[0] = false;
                        }
                    }
                }
            }
        });

        return exist[0];
    }

    public void launchNext(String sessionid, Context context, String streamurl) throws JSONException, FileNotFoundException, UnsupportedEncodingException {
        SESSIONID = sessionid;
        AudioStreamActivity.AUDIO_URL = streamurl;
        if (audioStreamActivity != null) {
            audioStreamActivity.finish();
        }
        context.startActivity(new Intent(context, AudioStreamActivity.class)
                .putExtra("INSTRUCTORCOURSEID", INSTRUCTORCOURSEID)
                .putExtra("COURSEPATH", COURSEPATH)
                .putExtra("ENROLMENTID", ENROLMENTID)
                .putExtra("PROFILEIMGURL", PROFILEIMGURL)
                .putExtra("INSTRUCTORNAME", INSTRUCTORNAME)
                .putExtra("NODESERVER", NODESERVER)
                .putExtra("ROOMID", ROOMID)

                .putExtra("SESSIONID", sessionid));
    }

    private int getFilesSize(String url, String pdfs) {
        if (pdfs.endsWith(";")) {
            pdfs = pdfs.substring(0, pdfs.length() - 1);
        }

        final int[] filesLength = {0};
        Realm.init(mContext);
        String finalPdfs = pdfs;
        Realm.getInstance(RealmUtility.getDefaultConfig(mContext)).executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                try {
                    RealmResults<RealmBase64File> audioData = realm.where(RealmBase64File.class).equalTo("url", url).findAll();
                    for (RealmBase64File realmBase64File : audioData) {
                        filesLength[0] += realmBase64File.getBase64String().getBytes("UTF-8").length;
                    }
                    RealmResults<RealmBase64File> coordinatesData = realm.where(RealmBase64File.class).equalTo("url", url.replace(".mp4", ".txt")).findAll();
                    for (RealmBase64File realmBase64File : coordinatesData) {
                        filesLength[0] += realmBase64File.getBase64String().getBytes("UTF-8").length;
                    }
                    if (finalPdfs != null && !finalPdfs.equals("")) {
                        for (String pdf : finalPdfs.split(";")) {
                            RealmResults<RealmBase64File> pdfData = realm.where(RealmBase64File.class).equalTo("url", pdf).findAll();
                            for (RealmBase64File realmBase64File : pdfData) {
                                filesLength[0] += realmBase64File.getBase64String().getBytes("UTF-8").length;
                            }
                        }
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
        return filesLength[0];
    }

    private void retrieveFilesFromRealm(Context context, String url, String pdfs) {
        if (pdfs.endsWith(";")) {
            pdfs = pdfs.substring(0, pdfs.length() - 1);
        }

        try {
            retrieveJsonCoordinatesFromRealm(context, url.replace(".mp4", ".txt"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (pdfs != null && !pdfs.equals("")) {
            for (String pdf : pdfs.split(";")) {
                try {
                    retrieveFileFromRealm(context, pdf, context.getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Class-sessions/Documents/" + URLUtil.guessFileName(pdf, null, null));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            retrieveFileFromRealm(context, url, context.getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Class-sessions/Documents/" + URLUtil.guessFileName(url, null, null));
        } catch (Exception e) {
            e.printStackTrace();
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView size;
        public TextView title, date, duration;
        public CardView cardview;
        public ImageView icon;
        public RelativeLayout downloadStatusWrapper, removelayout;
        public ProgressBar pbar;
        public ImageView download;
        public ImageView remove;
        public LinearLayout playLayout;

        public ViewHolder(View view) {
            super(view);

            title = view.findViewById(R.id.title);
            date = view.findViewById(R.id.date);
            cardview = view.findViewById(R.id.cardview);
            icon = view.findViewById(R.id.icon);
            downloadStatusWrapper = view.findViewById(R.id.downloadStatusWrapper);
            pbar = view.findViewById(R.id.pbar);
            download = view.findViewById(R.id.download);
            remove = view.findViewById(R.id.remove);
            removelayout = view.findViewById(R.id.removelayout);
            playLayout = view.findViewById(R.id.playLayout);
            size = view.findViewById(R.id.size);
        }
    }

    public interface ClassReplayAdapterInterface {
        void onListItemClick(ArrayList<Object> objects, int position, ViewHolder holder);
    }
}

