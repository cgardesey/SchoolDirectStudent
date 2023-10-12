package com.univirtual.student.adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.univirtual.student.R;
import com.univirtual.student.activity.PaperOnboardingActivity;
import com.univirtual.student.activity.PdfActivity;
import com.univirtual.student.activity.SplashScreenActivity;
import com.univirtual.student.pojo.MyFile;
import com.univirtual.student.realm.RealmBase64File;
import com.univirtual.student.util.RealmUtility;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.univirtual.student.activity.FileListActivity.downFileAsyncMap;
import static com.univirtual.student.constants.Const.fileSize;
import static com.univirtual.student.constants.Const.getMimeType;
import static com.univirtual.student.constants.Const.retrieveFileFromRealm;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.ViewHolder> implements Filterable {
    FileListAdapterInterface fileListAdapterInterface;
    ArrayList<MyFile> myFiles;
    private Context mContext;

    public FileListAdapter(FileListAdapterInterface fileListAdapterInterface, ArrayList<MyFile> myFiles) {
        this.fileListAdapterInterface = fileListAdapterInterface;
        this.myFiles = myFiles;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycle_file_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        MyFile myFile = myFiles.get(position);
        holder.path.setText(URLUtil.guessFileName(myFile.getUrl(), null, null));
        if (relatedResourcesExist(myFile.getUrl())) {
            holder.downloadStatusWrapper.setVisibility(View.GONE);
            holder.removelayout.setVisibility(View.VISIBLE);

            holder.size.setText(fileSize(getFilesSize(myFile.getUrl())));
        } else {
            holder.downloadStatusWrapper.setVisibility(View.VISIBLE);
            holder.removelayout.setVisibility(View.GONE);

            if (filesDownloading(myFile.getUrl())) {
                holder.download.setVisibility(View.GONE);

                holder.pbar.setVisibility(View.VISIBLE);
                holder.pbar.animate();
            } else {
                holder.download.setVisibility(View.VISIBLE);

                holder.pbar.setVisibility(View.GONE);
            }
        }
        if (myFile.getGiflink() != null) {
            Glide.with(mContext).asGif().load(myFile.getGiflink()).apply(new RequestOptions()
                    .centerCrop()
                    .placeholder(null)
                    .error(R.drawable.error))
                    .into(holder.preview);
            holder.preview.setVisibility(View.VISIBLE);
        }
        else {
            holder.preview.setVisibility(View.GONE);
        }
        holder.removelayout.setOnClickListener(v -> {
            removeResources(myFile.getUrl());
            if (relatedResourcesExist(myFile.getUrl())) {
                Toast.makeText(mContext, mContext.getString(R.string.error_deleting_file_from_device), Toast.LENGTH_SHORT).show();
            } else {
                holder.downloadStatusWrapper.setVisibility(View.VISIBLE);
                holder.pbar.setVisibility(View.GONE);
                holder.download.setVisibility(View.VISIBLE);
                holder.removelayout.setVisibility(View.GONE);
            }
        });
        holder.downloadStatusWrapper.setOnClickListener(view -> {
            fileListAdapterInterface.onListItemClick(myFiles, position, holder);
        });
        holder.cardview.setOnClickListener(v -> {

            if (holder.downloadStatusWrapper.getVisibility() == View.GONE) {
                try {
                    retrieveFileFromRealm(mContext, myFile.getUrl(), myFile.getPath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                PdfActivity.filepath = myFile.getPath();
                mContext.startActivity(new Intent(mContext, PdfActivity.class));
            }
        });
    }

    public interface FileListAdapterInterface {
        void onListItemClick(ArrayList<MyFile> files, int position, ViewHolder holder);
    }

    @Override
    public int getItemCount() {
        return myFiles.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public void reload(ArrayList<MyFile> myFiles) {
        this.myFiles = myFiles;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView path;
        public TextView size;
        CardView cardview;
        public RelativeLayout downloadStatusWrapper, removelayout;
        public ProgressBar pbar;
        public ImageView download;
        public ImageView remove, preview;

        public ViewHolder(View view) {
            super(view);
            path = view.findViewById(R.id.path);
            cardview = view.findViewById(R.id.cardview);
            downloadStatusWrapper = view.findViewById(R.id.downloadStatusWrapper);
            removelayout = view.findViewById(R.id.removelayout);
            pbar = view.findViewById(R.id.pbar);
            download = view.findViewById(R.id.download);
            remove = view.findViewById(R.id.remove);
            size = view.findViewById(R.id.size);
            preview = view.findViewById(R.id.preview);

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

    private boolean relatedResourcesExist(String url) {
        final boolean[] exist = {true};
        Realm.init(mContext);
        Realm.getInstance(RealmUtility.getDefaultConfig(mContext)).executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                if (realm.where(RealmBase64File.class).equalTo("url", url).findFirst() == null) {
                    exist[0] = false;
                }
            }
        });
        return exist[0];
    }

    private int getFilesSize(String url) {
        final int[] filesLength = {0};
        Realm.init(mContext);
        Realm.getInstance(RealmUtility.getDefaultConfig(mContext)).executeTransaction(new Realm.Transaction() {
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

    private void removeResources(String url) {
        Realm.init(mContext);
        Realm.getInstance(RealmUtility.getDefaultConfig(mContext)).executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<RealmBase64File> fileData = realm.where(RealmBase64File.class).equalTo("url", url).findAll();
                fileData.deleteAllFromRealm();
            }
        });
    }

    private boolean filesDownloading(String url) {
        AsyncTask<String, Integer, String> downloadFileAsync = (AsyncTask<String, Integer, String>) downFileAsyncMap.get(url);
        return downloadFileAsync != null && downloadFileAsync.getStatus() != AsyncTask.Status.FINISHED;
    }
}

