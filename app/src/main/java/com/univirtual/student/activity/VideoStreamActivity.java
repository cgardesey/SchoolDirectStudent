
package com.univirtual.student.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.byox.enums.DrawingMode;
import com.byox.views.DrawView;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.exoplayer2.Player;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.gson.Gson;

import com.makeramen.roundedimageview.RoundedImageView;
import com.otaliastudios.zoom.ZoomLayout;
import com.potyvideo.library.AndExoPlayerView;
import com.potyvideo.library.globalInterfaces.ExoPlayerCallBack;
import com.rw.keyboardlistener.KeyboardUtils;
import com.shockwave.pdfium.PdfDocument;
import com.univirtual.student.R;
import com.univirtual.student.adapter.ChatAdapter;
import com.univirtual.student.adapter.ParticipantsAdapter;
import com.univirtual.student.constants.Const;
import com.univirtual.student.constants.keyConst;
import com.univirtual.student.materialDialog.ConferenceCallInfoMaterialDialog;
import com.univirtual.student.other.InitApplication;
import com.univirtual.student.other.MyHttpEntity;
import com.univirtual.student.pojo.DateItem;
import com.univirtual.student.pojo.Participant;
import com.univirtual.student.realm.RealmChat;
import com.univirtual.student.realm.RealmEnrolment;
import com.univirtual.student.realm.RealmInstructor;
import com.univirtual.student.realm.RealmInstructorCourse;
import com.univirtual.student.realm.RealmStudent;
import com.univirtual.student.realm.RealmUser;
import com.univirtual.student.receiver.NetworkReceiver;
import com.univirtual.student.util.AlertDialogHelper;
import com.univirtual.student.util.DownloadFileAsync;
import com.univirtual.student.util.GenerateImageFromPdfAsync;
import com.univirtual.student.util.RealmUtility;
import com.univirtual.student.util.RecyclerItemClickListener;
import com.univirtual.student.util.Socket;
import com.yalantis.ucrop.util.FileUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;
import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import droidninja.filepicker.utils.ContentUriUtils;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import vn.tungdx.mediapicker.MediaItem;
import vn.tungdx.mediapicker.MediaOptions;
import vn.tungdx.mediapicker.activities.MediaPickerActivity;

import com.leocardz.LinkPreviewCallback;
import com.leocardz.SourceContent;
import com.leocardz.TextCrawler;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.android.volley.Request.Method.POST;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;
import static com.facebook.internal.Utility.deleteDirectory;
import static com.univirtual.student.activity.HomeActivity.ACCESSTOKEN;
import static com.univirtual.student.activity.HomeActivity.APITOKEN;
import static com.univirtual.student.activity.HomeActivity.FILE_PICKER_REQUEST_CODE;
import static com.univirtual.student.activity.HomeActivity.MYUSERID;
import static com.univirtual.student.activity.SelectResourceActivity.docFile;
import static com.univirtual.student.adapter.ChatAdapter.downFileAsyncMap;
import static com.univirtual.student.adapter.ChatAdapter.isMultiSelect;
import static com.univirtual.student.constants.keyConst.API_URL;
import static com.univirtual.student.constants.Const.convertDpToPx;
import static com.univirtual.student.constants.Const.dateFormat;
import static com.univirtual.student.constants.Const.efficientPDFPageCount;
import static com.univirtual.student.constants.Const.fileSize;
import static com.univirtual.student.constants.Const.getFormattedDate;
import static com.univirtual.student.constants.Const.isExternalStorageWritable;
import static com.univirtual.student.constants.Const.isNetworkAvailable;
import static com.univirtual.student.constants.Const.myVolleyError;
import static com.univirtual.student.receiver.NetworkReceiver.activeActivity;
import static com.univirtual.student.util.Socket.EVENT_CLOSED;
import static com.univirtual.student.util.Socket.EVENT_OPEN;
import static com.univirtual.student.util.Socket.EVENT_RECONNECT_ATTEMPT;

public class VideoStreamActivity extends AppCompatActivity implements AlertDialogHelper.AlertDialogListener, OnPageChangeListener, OnLoadCompleteListener, OnPageErrorListener {

    String TITLE;
    String INSTRUCTORCOURSEID;
    String COURSEPATH;
    String ENROLMENTID;
    String PROFILEIMGURL;
    String ROOMID;
    String NODESERVER;
    String INSTRUCTORNAME;
    String SESSIONID;

    public static final int REQUEST_MEDIA = 1002;
    public static final int RC_FILE_PICKER_PERM = 321;
    public static final int RC_AUDIO_AND_STORAGE = 1230;
    public static final int RC_AUDIO_PICKER = 0;
    public static final int RC_STORAGE = 322;
    public static final String PREF_MSG_LENGTH_KEY = "pref_msg_length";
    public static final int DEFAULT_MSG_LENGTH = 140;
    public static final int RC_PLACE_PICKER = 102;
    static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));
    static final String NO_CLASS_CHATS = "There are currently no course chats.";
    static final String TAG = "videoStreamActivity";
    public static Context videoStreamContext;
    public static Activity videoStreamActivity;
    public static ArrayList<Object> newObjects = new ArrayList<>();
    public static ArrayList<Object> objects = new ArrayList<>();
    public static ArrayList<Participant> newParticipants = new ArrayList<>();
    public static ArrayList<Participant> participants = new ArrayList<>();
    static RecyclerView recylerView;
    public static RecyclerView recyrlerView_participant;
    public static LinearLayoutManager mLinearLayoutManager;
    static TextView statusMsg, chatdisabledtext;
    LinearLayout chatcontrolslayout;
    static RelativeLayout controls;
    static ChatAdapter chatAdapter;
    public static ParticipantsAdapter participantsAdapter;
    ProgressBar progressBar, horizontalPBar;
    static ImageView imageView;
    TextCrawler textCrawler;
    static Socket chatSocket;
    ImageButton attach;
    EmojiconEditText messageEditText;
    FrameLayout  sendButton;
    ImageView emojiButton;
    Menu context_menu;
    View rootView;
    EmojIconActions emojIcon;
    public static CardView cardview;
    RelativeLayout doc, gal, loc, audio;
    LinearLayout txtMsgFrame;
    EmojiconTextView txtMsg;

    FrameLayout linkPrevFrame;
    ImageView linkImage;
    LinearLayout linkTextArea;
    TextView linkTitle;
    TextView linkDesc;
    ImageView close;

    FrameLayout replyPrevFrame;
    LinearLayout replyTextArea;
    TextView replyName;
    TextView replyBody;
    ImageView replyClose;

    static TextView coursepath, coursepath_white_board;

    public static String DIALCODE, CONFERENCEID;


    private Uri uri;

    ActionMode mActionMode;
    ArrayList<Object> multiselect_list = new ArrayList<>();
    AlertDialogHelper alertDialogHelper;

    static SearchView searchView;

    PDFView pdfView;
    static int pdfpageCount = 1;
    static int padpageCount = 1;
    static boolean padCanvasCleared = true;
    static boolean pdfCanvasCleared = true;
    RoundedImageView profileimg, profileimg_whiteboard;
    ImageView downbtn, upbtn, participantsBtn;
    LinearLayout whiteboardlayout, participantslayout;

    Integer pageNumber = 0;
    String pdfFileName;

    File wavFile;
    String replyChatId = "";

    public static String link = "", linkimgurl = "";

    private List<MediaItem> mMediaSelectedList;

    public static String VIDEO_URL;
    public static boolean ISRECORDED = false;
    public static int CHATOCKED;

    LinkPreviewCallback mLinkPreviewCallback;
    private int MAX_ATTACHMENT_COUNT = 5;
    private ArrayList<Uri> docUris = new ArrayList<>();
    private ImageView chaticon;
    LinearLayout toggleLayout;
    public static TextView gotopage;
    public static JSONArray drawingCoordinatesJsonArray;

    ImageButton brush, ibEraser, ibSave, ibClear;

    private int sampleRate = 48000;
    /**
     * 8000, 16000, 22050, 24000, 32000, 44100, 48000 Choose the best for the device and the bandwidth
     **/
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
    static DrawView pad_canvas, pdf_canvas;
    static ZoomLayout zoom_layout;
    static int padPageNum = 1, pdfPageNum = 1;
    ImageView padPrevBtn, padNextBtn, pdfPrevBtn, pdfNextBtn;
    public static TextView pdfTotalPages, padTotalPages, participantno;
    static EditText padCurpage;
    static EditText pdfCurpage;
    public ProgressDialog mProgress;
    private static AndExoPlayerView player;
    private LinearLayout maximizelayout, canvaslayout;
    private ImageView maximize, minimize;
    private static ImageView pad;
    private static ImageView pdf;
    private static LinearLayout padPagePanel;
    private static LinearLayout pdfPagePanel;
    LinearLayout info, chat;

    static boolean sessionupdated = false;

    static AsyncTask<String, Integer, String> downloadFileAsync;
    static JSONObject usersJson = new JSONObject();

    static Timer myTimer = new Timer();
    static Timer connectTimer = new Timer();
    static final boolean[] backgroundset = {false};
    static int coordinateIndex = 0;
    static long timeDiff = 0;
    NetworkReceiver networkReceiver;
    ImageView imgview;

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_multi_select, menu);
            context_menu = menu;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            MenuItem replyMenuItem = menu.findItem(R.id.action_reply);
            MenuItem deleteMenuItem = menu.findItem(R.id.action_delete);
            if (multiselect_list.size() == 0) {
                if (mActionMode != null) {
                    mActionMode.finish();
                }
            } else if (multiselect_list.size() == 1) {
                deleteMenuItem.setVisible(isDeleteIconShowable());
                replyMenuItem.setVisible(true);
            } else if (multiselect_list.size() > 1) {
                deleteMenuItem.setVisible(isDeleteIconShowable());
                replyMenuItem.setVisible(false);
            }
            return true; // Return false if nothing is done
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    alertDialogHelper.showAlertDialog("", getString(R.string.delete_), getString(R.string.delete).toUpperCase(), getString(R.string.cancel).toUpperCase(), 1, false);
                    return true;
                case R.id.action_reply:
                    sendButton.setVisibility(View.INVISIBLE);
                    RealmChat realmChat = (RealmChat) multiselect_list.get(0);
                    replyPrevFrame.setVisibility(View.VISIBLE);
                    replyChatId = realmChat.getChatid();
                    if (realmChat.getText() == null) {
                        replyBody.setText(realmChat.getAttachmenttitle());
                    } else {
                        replyBody.setText(realmChat.getText());
                    }
                    Realm.init(getApplicationContext());
                    Realm.getInstance(RealmUtility.getDefaultConfig(videoStreamContext)).executeTransaction(realm -> {
                        RealmUser realmUser = Realm.getInstance(RealmUtility.getDefaultConfig(videoStreamContext))
                                .where(RealmUser.class)
                                .equalTo("userid", realmChat.getSenderid())
                                .findFirst();
                        if (realmUser.getRole().equals("student")) {
                            RealmStudent realmStudent = Realm.getInstance(RealmUtility.getDefaultConfig(videoStreamContext))
                                    .where(RealmStudent.class)
                                    .equalTo("infoid", realmChat.getSenderid())
                                    .findFirst();
                            if (realmStudent.getFirstname() != null) {
                                replyName.setText(realmStudent.getFirstname());
                            } else {
                                replyName.setText(realmUser.getPhonenumber());
                            }
                        } else if (realmUser.getRole().equals("instructor")) {
                            RealmInstructor realmInstructor = Realm.getInstance(RealmUtility.getDefaultConfig(videoStreamContext))
                                    .where(RealmInstructor.class)
                                    .equalTo("infoid", realmChat.getSenderid())
                                    .findFirst();
                            if (realmInstructor.getFirstname() != null) {
                                replyName.setText(realmInstructor.getFirstname());
                            } else {
                                replyName.setText(realmUser.getPhonenumber());
                            }
                        }
                    });
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            isMultiSelect = false;
            multiselect_list = new ArrayList<Object>();
            refreshAdapter();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_video_stream);
        videoStreamContext = getApplicationContext();
        videoStreamActivity = this;


        String instructorcourseid = getIntent().getStringExtra("INSTRUCTORCOURSEID");
        String coursepathstring = getIntent().getStringExtra("COURSEPATH");
        String enrolmentid = getIntent().getStringExtra("ENROLMENTID");
        String profileimgurl = getIntent().getStringExtra("PROFILEIMGURL");
        String nodeserver = getIntent().getStringExtra("NODESERVER");
        String roomid = getIntent().getStringExtra("ROOMID");
        String sessionid = getIntent().getStringExtra("SESSIONID");

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
        if (sessionid != null && !sessionid.equals("")) {
            SESSIONID = sessionid;
        }


        networkReceiver = new NetworkReceiver();

        myTimer = new Timer();
        connectTimer = new Timer();

        usersJson = new JSONObject();
        sessionupdated = false;
        backgroundset[0] = false;
        coordinateIndex = 0;
        timeDiff = 0;
        pdfpageCount = 1;
        padpageCount = 1;
        padPageNum = 1;
        pdfPageNum = 1;

        padCanvasCleared = true;
        pdfCanvasCleared = true;

        imgview = findViewById(R.id.imgview);
        player = findViewById(R.id.player);

        player.setSource(VIDEO_URL);
        player.onClick(player);

        player.setExoPlayerCallBack(new ExoPlayerCallBack() {
            @Override
            public void onError() {
                if (ISRECORDED) {
                    if (myTimer != null) {
                        myTimer.cancel();
                    }
                } else {
                    boolean filedownloadinprogress = downloadFileAsync != null && downloadFileAsync.getStatus() != AsyncTask.Status.FINISHED;
                    if (!filedownloadinprogress) {
                        StringRequest stringRequest = new StringRequest(
                                com.android.volley.Request.Method.POST,
                                API_URL + "class-video-session-refresh-data",
                                response -> {
                                    if (response != null) {
                                        try {
                                            JSONObject jsonObject = new JSONObject(response);
                                            Realm.init(videoStreamContext);
                                            Realm.getInstance(RealmUtility.getDefaultConfig(videoStreamContext)).executeTransaction(new Realm.Transaction() {
                                                @Override
                                                public void execute(Realm realm) {
                                                    try {
                                                        realm.createOrUpdateObjectFromJson(RealmInstructorCourse.class, jsonObject.getJSONObject("intructorcourse"));
                                                        realm.createOrUpdateObjectFromJson(RealmEnrolment.class, jsonObject.getJSONObject("enrolment"));
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                            if (jsonObject.getBoolean("eligible")) {
                                                if (jsonObject.getBoolean("eligible")) {
                                                    if (jsonObject.getBoolean("session_changed")) {
                                                        sessionupdated = true;
                                                        drawingCoordinatesJsonArray = new JSONArray(new ArrayList<JSONObject>());

                                                        drawingCoordinatesJsonArray = jsonObject.getJSONArray("drawing_coordinates");
                                                        JSONObject class_video_session = jsonObject.getJSONObject("class_video_session");

                                                        SESSIONID = class_video_session.getString("sessionid");
                                                        VIDEO_URL = class_video_session.getString("streamurl");
                                                        String docurl = class_video_session.getString("docurl");
                                                        DIALCODE = class_video_session.getString("dialcode");
                                                        CONFERENCEID = class_video_session.getString("roomid");
                                                        boolean streaming = jsonObject.getBoolean("streaming");

                                                        if (streaming) {
                                                            player.setSource(VIDEO_URL);
                                                        }
                                                        onPlayInit();
                                                        setPdf(docurl, null);
                                                    }
                                                }
                                            } else {
                                                Toast.makeText(videoStreamContext, getString(R.string.your_subscription_has_expired), Toast.LENGTH_LONG).show();
                                                startActivity(new Intent(videoStreamContext, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                                finish();
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                },
                                error -> {
                                    error.printStackTrace();
                                    Log.d("Cyrilll", error.toString());
                                    //                                myVolleyError(context, error);
                                }
                        ) {
                            @Override
                            public Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<>();
                                params.put("sessionid", SESSIONID);
                                params.put("enrolmentid", ENROLMENTID);
                                params.put("instructorcourseid", INSTRUCTORCOURSEID);
                                return params;
                            }

                            @Override
                            public Map getHeaders() throws AuthFailureError {
                                HashMap headers = new HashMap();
                                headers.put("accept", "application/json");
                                headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(videoStreamContext).getString(APITOKEN, ""));
                                return headers;
                            }
                        };
                        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                                0,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        InitApplication.getInstance().addToRequestQueue(stringRequest);
                    }
                }
            }
        });

        info = findViewById(R.id.info);

        if (ISRECORDED) {
            info.setVisibility(View.GONE);
        }

        info.setOnClickListener(view -> {
            if (CONFERENCEID == null || CONFERENCEID.equals("")) {

            } else {
                ConferenceCallInfoMaterialDialog conferenceCallInfoMaterialDialog = new ConferenceCallInfoMaterialDialog();
                conferenceCallInfoMaterialDialog.setDial_code(DIALCODE);
                conferenceCallInfoMaterialDialog.setConference_id(CONFERENCEID);

                conferenceCallInfoMaterialDialog.show(getSupportFragmentManager(), "");
            }
        });

        maximizelayout = findViewById(R.id.maximizelayout);
        canvaslayout = findViewById(R.id.canvaslayout);
        maximize = findViewById(R.id.maximize);
        minimize = findViewById(R.id.minimize);

        maximizelayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final boolean maximized = maximize.getVisibility() == View.VISIBLE;
                if (maximized) {
                    maximize.setVisibility(View.GONE);
                    minimize.setVisibility(View.VISIBLE);
                    player.setVisibility(View.GONE);
                } else {
                    maximize.setVisibility(View.VISIBLE);
                    minimize.setVisibility(View.GONE);
                    player.setVisibility(View.VISIBLE);
                }

                ViewTreeObserver vto = pad_canvas.getViewTreeObserver();
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            pad_canvas.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            pad_canvas.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                        if (pad.getVisibility() == View.VISIBLE) {
                            pdf_canvas.clearHistory();
                            pdfCanvasCleared = true;
                            redrawOnPdf(pdfPageNum);
                        }
                        else {
                            pad_canvas.clearHistory();
                            padCanvasCleared = true;
                            redrawOnPad(padPageNum);
                        }
                    }
                });

            }
        });

        mProgress = new ProgressDialog(this);
        mProgress.setMessage(getString(R.string.pls_wait));
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);

        alertDialogHelper = new AlertDialogHelper(this);
        textCrawler = new TextCrawler();
        emojiButton = findViewById(R.id.emoji_btn);
        participantno = findViewById(R.id.participantno);
        coursepath = findViewById(R.id.coursepath);
        coursepath_white_board = findViewById(R.id.coursepath_white_board);
        coursepath.setText(COURSEPATH);
        coursepath_white_board.setText(COURSEPATH);

        mLinkPreviewCallback = new LinkPreviewCallback() {
            @Override
            public void onPre() {

            }

            @Override
            public void onPos(SourceContent sourceContent, boolean b) {
                List<String> images = sourceContent.getImages();
                String title = sourceContent.getTitle();
                String description = sourceContent.getDescription();
                if ((images.size() == 0) && (title.length() == 0) && (description.length() == 0)) {
                    linkPrevFrame.setVisibility(View.GONE);
                } else {
                    linkPrevFrame.setVisibility(View.VISIBLE);
                }

                if (sourceContent.getImages() != null && sourceContent.getImages().size() > 0 && sourceContent.getImages().get(0).toLowerCase().startsWith("http")) {
                    linkimgurl = sourceContent.getImages().get(0);
                    Glide.with(videoStreamContext).
                            load(linkimgurl)
                            .into(linkImage);
                    linkImage.setVisibility(View.VISIBLE);

                } else {
                    linkimgurl = "";
                    linkImage.setVisibility(View.GONE);
                }
                if (sourceContent.getTitle() != null && sourceContent.getTitle().length() > 0) {
                    linkTitle.setText(sourceContent.getTitle());
                    linkTitle.setVisibility(View.VISIBLE);
                } else {
                    linkTitle.setVisibility(View.GONE);
                }
                if (sourceContent.getFinalUrl() != null && sourceContent.getFinalUrl().length() > 0) {
                    link = sourceContent.getFinalUrl();
                } else {
                    link = "";
                }
                if (sourceContent.getDescription() != null && sourceContent.getDescription().length() > 0) {
                    linkDesc.setText(sourceContent.getDescription());
                    linkDesc.setVisibility(View.VISIBLE);
                } else {
                    linkDesc.setVisibility(View.GONE);
                }
            }
        };

        pdfView = findViewById(R.id.pdfView);
        profileimg = findViewById(R.id.profileimg);
        profileimg_whiteboard = findViewById(R.id.profileimg_whiteboard);
        pad = findViewById(R.id.pad);
        pdf = findViewById(R.id.pdf);

        if (PROFILEIMGURL != null) {
            Glide.with(getApplicationContext()).load(PROFILEIMGURL).apply(new RequestOptions().centerCrop().placeholder(R.drawable.user_icon_white)).into(profileimg);
            Glide.with(getApplicationContext()).load(PROFILEIMGURL).apply(new RequestOptions().centerCrop().placeholder(R.drawable.user_icon_white)).into(profileimg_whiteboard);
        }
        downbtn = findViewById(R.id.upbtn);
        upbtn = findViewById(R.id.downbtn);
        participantsBtn = findViewById(R.id.participantsBtn);
        imageView = findViewById(R.id.imageView);
        doc = findViewById(R.id.upcomingdoc);
        gal = findViewById(R.id.gal);
        audio = findViewById(R.id.audio);
        loc = findViewById(R.id.loc);
        progressBar = findViewById(R.id.pbar_pic);
        recylerView = findViewById(R.id.recyrlerView);
        recyrlerView_participant = findViewById(R.id.recyrlerView_participant);
        controls = findViewById(R.id.add);
        statusMsg = findViewById(R.id.statusMsg);
        chatdisabledtext = findViewById(R.id.chatdisabledtext);
        chatcontrolslayout = findViewById(R.id.chatcontrolslayout);
        attach = findViewById(R.id.attach);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendMessageButton);
        cardview = findViewById(R.id.card_view);
        whiteboardlayout = findViewById(R.id.whiteboardlayout);
        participantslayout = findViewById(R.id.participantslayout);
        toggleLayout = findViewById(R.id.toggleLayout);
        chaticon = findViewById(R.id.chaticon);
        chat = findViewById(R.id.chat);
        rootView = findViewById(R.id.root_view);
        emojIcon = new EmojIconActions(this, rootView, messageEditText, emojiButton);
        emojIcon.ShowEmojIcon();
        pdfTotalPages = findViewById(R.id.pdfTotalPages);
        padTotalPages = findViewById(R.id.padTotalPages);

        if (CHATOCKED == 1) {
            chatdisabledtext.setVisibility(View.VISIBLE);
            chatcontrolslayout.setVisibility(View.GONE);
        } else {
            chatdisabledtext.setVisibility(View.GONE);
            chatcontrolslayout.setVisibility(View.VISIBLE);
        }

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Drawable currentDrawable = chaticon.getDrawable();

                Drawable noChatIconDrawable = getResources().getDrawable(R.drawable.no_chat);
                Drawable chatIconDrawable = getResources().getDrawable(R.drawable.chat);

                Drawable.ConstantState noChatIconConstantState = noChatIconDrawable.getConstantState();
                Drawable.ConstantState removeIconConstantState = chatIconDrawable.getConstantState();
                Drawable.ConstantState currentIconConstantState = currentDrawable.getConstantState();
                if (currentIconConstantState.equals(removeIconConstantState)) {
                    whiteboardlayout.setVisibility(View.GONE);
                    participantslayout.setVisibility(View.GONE);
                    participantsBtn.setImageDrawable(getResources().getDrawable(R.drawable.group_foreground));
                    chaticon.setImageDrawable(noChatIconDrawable);

                    if (objects.size() == 0 && isNetworkAvailable(videoStreamContext)) {
                        Toast toast = Toast.makeText(videoStreamContext, "Checking for new chats...", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } else {
                    whiteboardlayout.setVisibility(View.VISIBLE);
                    participantslayout.setVisibility(View.GONE);
                    participantsBtn.setImageDrawable(getResources().getDrawable(R.drawable.group_foreground));
                    chaticon.setImageDrawable(chatIconDrawable);
                }
            }
        });

        txtMsgFrame = findViewById(R.id.txt_msg_frame);
        txtMsg = findViewById(R.id.txt_msg);

        linkPrevFrame = findViewById(R.id.link_prev_frame);
        linkImage = findViewById(R.id.link_img);
        linkTextArea = findViewById(R.id.link_text_area);
        linkTitle = findViewById(R.id.link_title);
        linkDesc = findViewById(R.id.link_desc);
        close = findViewById(R.id.close);

        replyPrevFrame = findViewById(R.id.reply_prev_frame);
        replyTextArea = findViewById(R.id.reply_text_area);
        replyName = findViewById(R.id.reply_name);
        replyBody = findViewById(R.id.reply_body);
        replyClose = findViewById(R.id.replyClose);

        searchView = findViewById(R.id.searchView);

        emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
            }

            @Override
            public void onKeyboardClose() {

            }
        });
        chatAdapter = new ChatAdapter(objects, this, COURSEPATH);
        chatAdapter.setHasStableIds(true);
        mLinearLayoutManager = new LinearLayoutManager(videoStreamContext);
        recylerView.setLayoutManager(mLinearLayoutManager);
        setRecyclerViewAdapter();
        mLinearLayoutManager.scrollToPositionWithOffset(objects.size() - 1, 0);
        recylerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recylerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (objects.get(position) instanceof RealmChat) {
                    if (isMultiSelect)
                        multi_select(position);

                    if (mActionMode != null) {
                        mActionMode.invalidate();
                    }
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (objects.get(position) instanceof RealmChat) {
                    if (!isMultiSelect) {
                        multiselect_list = new ArrayList<Object>();
                        isMultiSelect = true;

                        if (mActionMode == null) {
                            mActionMode = startSupportActionMode(mActionModeCallback);
                        }
                    }
                    multi_select(position);
                    mActionMode.invalidate();
                }
            }
        }));

        participants.clear();
        populateParticipants(getApplicationContext());
        participants.addAll(newParticipants);
        participantsAdapter = new ParticipantsAdapter(participants);
        recyrlerView_participant.setLayoutManager(new LinearLayoutManager(videoStreamContext));
        recyrlerView_participant.setAdapter(participantsAdapter);

        close.setOnClickListener(v -> {
            linkPrevFrame.setVisibility(View.GONE);
            if (replyPrevFrame.getVisibility() == View.GONE && messageEditText.getText().toString().trim().length() == 0) {
                sendButton.setVisibility(View.GONE);
            }
        });

        replyClose.setOnClickListener(v -> {
            replyPrevFrame.setVisibility(View.GONE);
            if (linkPrevFrame.getVisibility() == View.GONE && messageEditText.getText().toString().trim().length() == 0) {
                sendButton.setVisibility(View.GONE);
            }
        });

        profileimg.setOnClickListener(v -> {

        });

        profileimg_whiteboard.setOnClickListener(v -> {

        });

        participantsBtn.setOnClickListener(v -> {
            Drawable currentDrawable = participantsBtn.getDrawable();

            Drawable groupForegroundIconDrawable = getResources().getDrawable(R.drawable.group_foreground);
            Drawable groupBackgroundIconDrawable = getResources().getDrawable(R.drawable.group_background);

            Drawable.ConstantState groupForegroundIconConstantState = groupForegroundIconDrawable.getConstantState();
            Drawable.ConstantState groupBackgroundIconConstantState = groupBackgroundIconDrawable.getConstantState();
            Drawable.ConstantState currentIconConstantState = currentDrawable.getConstantState();
            if (currentIconConstantState.equals(groupBackgroundIconConstantState)) {
                participantslayout.setVisibility(View.GONE);
                searchView.setVisibility(View.VISIBLE);
                participantsBtn.setImageDrawable(groupForegroundIconDrawable);
            } else {
                participantslayout.setVisibility(View.VISIBLE);
                searchView.setVisibility(View.GONE);
                participantsBtn.setImageDrawable(groupBackgroundIconDrawable);
            }
        });

        downbtn.setOnClickListener(v -> {
            pdfView.setVisibility(View.GONE);
            downbtn.setVisibility(View.GONE);
            upbtn.setVisibility(View.VISIBLE);
            searchView.setVisibility(View.VISIBLE);
        });

        if (docFile == null) {
            downbtn.setVisibility(View.GONE);
            upbtn.setVisibility(View.GONE);
            searchView.setVisibility(View.VISIBLE);
        } else {
            searchView.setVisibility(View.GONE);


            pdfpageCount = efficientPDFPageCount(docFile);

            pdfTotalPages.setText(String.valueOf(pdfpageCount));
        }

        upbtn.setOnClickListener(v -> {
            pdfView.setVisibility(View.VISIBLE);
            downbtn.setVisibility(View.VISIBLE);
            upbtn.setVisibility(View.GONE);
            participantslayout.setVisibility(View.GONE);
            participantsBtn.setImageDrawable(getResources().getDrawable(R.drawable.group_foreground));
            searchView.setVisibility(View.GONE);
        });

        linkPrevFrame.setOnClickListener(v -> {
            if (!link.equals("")) {
                Uri webpage = Uri.parse(link);
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });


        doc.setOnClickListener(v -> pickDocClicked());

        gal.setOnClickListener(v -> {
            if (mMediaSelectedList != null) {
                mMediaSelectedList.clear();
            }
            MediaOptions.Builder builder = new MediaOptions.Builder();
            MediaOptions options = builder.canSelectMultiPhoto(true)
                    .canSelectMultiVideo(true).canSelectBothPhotoVideo()
                    .setMediaListSelected(mMediaSelectedList).build();

            if (options != null) {
                MediaPickerActivity.Companion.open(this, REQUEST_MEDIA, options);
            }
        });

        audio.setOnClickListener(v -> recAudioClicked());

        loc.setOnClickListener(v -> {
            try {
                PlacePicker.IntentBuilder intentBuilder =
                        new PlacePicker.IntentBuilder();
                intentBuilder.setLatLngBounds(BOUNDS_MOUNTAIN_VIEW);
                Intent intent = intentBuilder.build(VideoStreamActivity.this);
                startActivityForResult(intent, RC_PLACE_PICKER);

            } catch (GooglePlayServicesRepairableException
                    | GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
        });
        attach.setOnClickListener(view -> {
            if (cardview.getVisibility() == View.GONE) {
                cardview.setVisibility(View.VISIBLE);
            } else {
                cardview.setVisibility(View.GONE);
            }
        });
        // Enable Send button when there's text to send
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    sendButton.setVisibility(View.VISIBLE);
                } else {
                    sendButton.setVisibility(View.GONE);
                }

                textCrawler.cancel();
                textCrawler.makePreview(mLinkPreviewCallback, s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Send button sends a message and clears the EditText
        sendButton.setOnClickListener(view -> {
            String text = StringEscapeUtils.escapeJava(messageEditText.getText().toString().trim());
            if (text.length() > 0) {

                RealmChat realmChat = new RealmChat(
                        UUID.randomUUID().toString(),
                        replyPrevFrame.getVisibility() == View.VISIBLE ? replyChatId : null,
                        UUID.randomUUID().toString(),
                        text,
                        linkPrevFrame.getVisibility() == View.VISIBLE && !link.equals("") ? link : null,
                        linkPrevFrame.getVisibility() == View.VISIBLE ? linkTitle.getText().toString() : null,
                        linkPrevFrame.getVisibility() == View.VISIBLE ? linkDesc.getText().toString() : null,
                        linkPrevFrame.getVisibility() == View.VISIBLE && !linkimgurl.equals("") ? linkimgurl : null,
                        null,
                        null,
                        null,
                        0,
                        INSTRUCTORCOURSEID,
                        PreferenceManager.getDefaultSharedPreferences(videoStreamContext).getString(MYUSERID, ""),
                        null,
                        null,
                        null
                );

                saveTempChatToRealm(realmChat);

                String json_string = new Gson().toJson(realmChat);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(json_string);
                    SendChatMsg(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                messageEditText.setText("");
                linkimgurl = "";
                link = "";
                linkPrevFrame.setVisibility(View.GONE);
                replyPrevFrame.setVisibility(View.GONE);
            }
        });

        messageEditText.setOnClickListener(v -> cardview.setVisibility(View.GONE));

        recylerView.setOnClickListener(v -> cardview.setVisibility(View.GONE));

        init();

        initSearchView1(objects, chatAdapter);
        filter(objects, "");

        pad_canvas = findViewById(R.id.pad_canvas);
        pdf_canvas = findViewById(R.id.pdf_canvas);

        zoom_layout = findViewById(R.id.zoom_layout);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int widthPixels = displayMetrics.widthPixels;
        int heightPixels = displayMetrics.heightPixels;

        pdf_canvas.setLayoutParams(new FrameLayout.LayoutParams(widthPixels, (int) (heightPixels - convertDpToPx(videoStreamContext, 200))));
        zoom_layout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        pad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pad.setVisibility(View.GONE);
                pdf.setVisibility(View.VISIBLE);

                pad_canvas.setVisibility(View.VISIBLE);
                pdf_canvas.setVisibility(View.INVISIBLE);
                zoom_layout.setVisibility(View.INVISIBLE);
                padPagePanel.setVisibility(View.VISIBLE);
                pdfPagePanel.setVisibility(View.GONE);

                if (padCanvasCleared) {
                    padCurpage.setText(String.valueOf(padPageNum));
                    redrawOnPad(padPageNum);
                }
            }
        });

        pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (docFile == null) {
                    Toast.makeText(VideoStreamActivity.this, "There is currently no edited document file for this class session.", Toast.LENGTH_SHORT).show();
                } else {
                    pad.setVisibility(View.VISIBLE);
                    pdf.setVisibility(View.GONE);

                    pad_canvas.setVisibility(View.INVISIBLE);
                    pdf_canvas.setVisibility(View.VISIBLE);
                    zoom_layout.setVisibility(View.VISIBLE);

                    padPagePanel.setVisibility(View.GONE);
                    pdfPagePanel.setVisibility(View.VISIBLE);

                    if (pdfCanvasCleared) {

                        pdfCurpage.setText(String.valueOf(pdfPageNum));
                        redrawOnPdf(pdfPageNum);
                    }

                    File pdfBackgoundFile = new File(videoStreamContext.getFilesDir() + "/SchoolDirectStudent/PDF", "PDF-temp" + ".jpg");

                    new GenerateImageFromPdfAsync(new GenerateImageFromPdfAsync.OnTaskPreExecuteInterface() {
                        @Override
                        public void onPreExecute() {
                            if (pdfBackgoundFile.exists()) {
                                pdfBackgoundFile.delete();
                            }
                        }
                    },
                            new GenerateImageFromPdfAsync.OnTaskCompletedInterface() {
                                @Override
                                public void onTaskCompleted(Bitmap bitmap) {
                                    if (bitmap != null) {
                                        Drawable drawable = new BitmapDrawable(getResources(), bitmap);

                                        imgview.setVisibility(View.VISIBLE);
                                        imgview.setImageDrawable(drawable);

                                        final int imageViewHeight = imgview.getHeight(), imageViewWidth = imgview.getWidth();
                                        final int bitmapHeight = drawable.getIntrinsicHeight(), bitmapWidth = drawable.getIntrinsicWidth();

                                        int actualWidth;
                                        int actualHeight;
                                        if (imageViewHeight * bitmapWidth <= imageViewWidth * bitmapHeight) {
                                            actualWidth = bitmapWidth * imageViewHeight / bitmapHeight;
                                            actualHeight = imageViewHeight;
                                        } else {
                                            actualHeight = bitmapHeight * imageViewWidth / bitmapWidth;
                                            actualWidth = imageViewWidth;
                                        }
                                        pdf_canvas.setLayoutParams(new FrameLayout.LayoutParams(actualWidth, actualHeight));
                                        pdf_canvas.setBackground(drawable);
                                        imgview.setVisibility(View.GONE);
                                    }
                                }
                            }, new GenerateImageFromPdfAsync.OnTaskProgressUpdateInterface() {

                        @Override
                        public void onTaskProgressUpdate(int progress) {

                        }
                    }, new GenerateImageFromPdfAsync.OnTaskCancelledInterface() {

                        @Override
                        public void onTaskCancelled() {

                        }
                    }, videoStreamActivity, Uri.fromFile(docFile), pdfPageNum, pdfBackgoundFile).execute();
                }
            }
        });

        padPagePanel = findViewById(R.id.padPagePanel);
        pdfPagePanel = findViewById(R.id.pdfPagePanel);
        padCurpage = findViewById(R.id.padCurpage);
        pdfCurpage = findViewById(R.id.pdfCurpage);
        padPrevBtn = findViewById(R.id.wbPrevBtn);
        padNextBtn = findViewById(R.id.wbNextBtn);
        pdfPrevBtn = findViewById(R.id.pdfPrevBtn);
        pdfNextBtn = findViewById(R.id.pdfNextBtn);

        gotopage = findViewById(R.id.gotopage);

        horizontalPBar = findViewById(R.id.horizontalPBar);

        padNextBtn.setOnClickListener(view -> {
            padPageNum = padPageNum == padpageCount ? 1 : padPageNum + 1;
            pad_canvas.clearHistory();
            padCanvasCleared = true;
            padCurpage.setText(String.valueOf(padPageNum));
            redrawOnPad(padPageNum);
        });

        padPrevBtn.setOnClickListener(view -> {
            padPageNum = padPageNum == 1 ? padpageCount : padPageNum - 1;
            pad_canvas.clearHistory();
            padCanvasCleared = true;
            padCurpage.setText(String.valueOf(padPageNum));
            redrawOnPad(padPageNum);
        });

        pdfNextBtn.setOnClickListener(view -> {
            pdfPageNum = pdfPageNum >= pdfpageCount ? 1 : ++pdfPageNum;

            File pdfBackgoundFile = new File(videoStreamContext.getFilesDir() + "/SchoolDirectStudent/PDF", "PDF-temp" + ".jpg");

            new GenerateImageFromPdfAsync(new GenerateImageFromPdfAsync.OnTaskPreExecuteInterface() {
                @Override
                public void onPreExecute() {
                    if (pdfBackgoundFile.exists()) {
                        pdfBackgoundFile.delete();
                    }

                    horizontalPBar.setVisibility(View.VISIBLE);
                    horizontalPBar.animate();

                    pdfNextBtn.setEnabled(false);
                    pdfPrevBtn.setEnabled(false);
                }
            },
                    new GenerateImageFromPdfAsync.OnTaskCompletedInterface() {
                        @Override
                        public void onTaskCompleted(Bitmap bitmap) {
                            horizontalPBar.setVisibility(View.INVISIBLE);
                            pdfNextBtn.setEnabled(true);
                            pdfPrevBtn.setEnabled(true);
                            if (bitmap != null) {
                                pdf_canvas.clearHistory();
                                pdfCanvasCleared = true;
                                pdfCurpage.setText(String.valueOf(pdfPageNum));

                                // Redraw on pdf
                                for (int i = 0; i < drawingCoordinatesJsonArray.length(); i++) {
                                    JSONObject coordinateJson = null;
                                    try {
                                        coordinateJson = (JSONObject) drawingCoordinatesJsonArray.get(i);

                                        if (coordinateJson.has("ispdf") && coordinateJson.getInt("ispdf") == 1) {
                                            if (pdf_canvas.getVisibility() == View.VISIBLE) {
                                                if (coordinateJson.getInt("sessionId") == pdfPageNum) {
                                                    if (docFile != null && URLUtil.guessFileName(docFile.getAbsolutePath(), null, null).equals(URLUtil.guessFileName(coordinateJson.getString("pdfpath"), null, null))) {
                                                        drawOnPdf(coordinateJson);
                                                    }
                                                }
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                Drawable drawable = new BitmapDrawable(getResources(), bitmap);

                                imgview.setVisibility(View.VISIBLE);
                                imgview.setImageDrawable(drawable);

                                final int imageViewHeight = imgview.getHeight(), imageViewWidth = imgview.getWidth();
                                final int bitmapHeight = drawable.getIntrinsicHeight(), bitmapWidth = drawable.getIntrinsicWidth();

                                int actualWidth;
                                int actualHeight;
                                if (imageViewHeight * bitmapWidth <= imageViewWidth * bitmapHeight) {
                                    actualWidth = bitmapWidth * imageViewHeight / bitmapHeight;
                                    actualHeight = imageViewHeight;
                                } else {
                                    actualHeight = bitmapHeight * imageViewWidth / bitmapWidth;
                                    actualWidth = imageViewWidth;
                                }
                                pdf_canvas.setLayoutParams(new FrameLayout.LayoutParams(actualWidth, actualHeight));
                                pdf_canvas.setBackground(drawable);
                                imgview.setVisibility(View.GONE);
                            }
                        }
                    }, new GenerateImageFromPdfAsync.OnTaskProgressUpdateInterface() {

                @Override
                public void onTaskProgressUpdate(int progress) {

                }
            }, new GenerateImageFromPdfAsync.OnTaskCancelledInterface() {

                @Override
                public void onTaskCancelled() {

                }
            }, videoStreamActivity, Uri.fromFile(docFile), pdfPageNum, pdfBackgoundFile).execute();
        });

        pdfPrevBtn.setOnClickListener(view -> {
            pdfPageNum = pdfPageNum >= 1 ? pdfpageCount : --pdfPageNum;

            File pdfBackgoundFile = new File(videoStreamContext.getFilesDir() + "/SchoolDirectStudent/PDF", "PDF-temp" + ".jpg");

            new GenerateImageFromPdfAsync(new GenerateImageFromPdfAsync.OnTaskPreExecuteInterface() {
                @Override
                public void onPreExecute() {
                    if (pdfBackgoundFile.exists()) {
                        pdfBackgoundFile.delete();
                    }

                    horizontalPBar.setVisibility(View.VISIBLE);
                    horizontalPBar.animate();

                    pdfNextBtn.setEnabled(false);
                    pdfPrevBtn.setEnabled(false);
                }
            },
                    new GenerateImageFromPdfAsync.OnTaskCompletedInterface() {
                        @Override
                        public void onTaskCompleted(Bitmap bitmap) {
                            horizontalPBar.setVisibility(View.INVISIBLE);
                            pdfNextBtn.setEnabled(true);
                            pdfPrevBtn.setEnabled(true);
                            if (bitmap != null) {
                                pdf_canvas.clearHistory();
                                pdfCanvasCleared = true;
                                pdfCurpage.setText(String.valueOf(pdfPageNum));

                                // Redraw on pdf
                                for (int i = 0; i < drawingCoordinatesJsonArray.length(); i++) {
                                    JSONObject coordinateJson = null;
                                    try {
                                        coordinateJson = (JSONObject) drawingCoordinatesJsonArray.get(i);

                                        if (coordinateJson.has("ispdf") && coordinateJson.getInt("ispdf") == 1) {
                                            if (pdf_canvas.getVisibility() == View.VISIBLE) {
                                                if (coordinateJson.getInt("sessionId") == pdfPageNum) {
                                                    if (docFile != null && URLUtil.guessFileName(docFile.getAbsolutePath(), null, null).equals(URLUtil.guessFileName(coordinateJson.getString("pdfpath"), null, null))) {
                                                        drawOnPdf(coordinateJson);
                                                    }
                                                }
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                Drawable drawable = new BitmapDrawable(getResources(), bitmap);

                                imgview.setVisibility(View.VISIBLE);
                                imgview.setImageDrawable(drawable);

                                final int imageViewHeight = imgview.getHeight(), imageViewWidth = imgview.getWidth();
                                final int bitmapHeight = drawable.getIntrinsicHeight(), bitmapWidth = drawable.getIntrinsicWidth();

                                int actualWidth;
                                int actualHeight;
                                if (imageViewHeight * bitmapWidth <= imageViewWidth * bitmapHeight) {
                                    actualWidth = bitmapWidth * imageViewHeight / bitmapHeight;
                                    actualHeight = imageViewHeight;
                                } else {
                                    actualHeight = bitmapHeight * imageViewWidth / bitmapWidth;
                                    actualWidth = imageViewWidth;
                                }
                                pdf_canvas.setLayoutParams(new FrameLayout.LayoutParams(actualWidth, actualHeight));
                                pdf_canvas.setBackground(drawable);
                                imgview.setVisibility(View.GONE);
                            }
                        }
                    }, new GenerateImageFromPdfAsync.OnTaskProgressUpdateInterface() {

                @Override
                public void onTaskProgressUpdate(int progress) {

                }
            }, new GenerateImageFromPdfAsync.OnTaskCancelledInterface() {

                @Override
                public void onTaskCancelled() {

                }
            }, videoStreamActivity, Uri.fromFile(docFile), pdfPageNum, pdfBackgoundFile).execute();
        });
        gotopage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (padPagePanel.getVisibility() == View.VISIBLE) {
                    if (!TextUtils.isEmpty(padCurpage.getText().toString())) {
                        int enteredPage = Integer.parseInt(padCurpage.getText().toString());
                        if (enteredPage <= padpageCount && enteredPage >= 1) {
                            padPageNum = enteredPage;
                            pad_canvas.clearHistory();
                            padCanvasCleared = true;
                            redrawOnPad(padPageNum);
                        } else {
                            padCurpage.setText(String.valueOf(padPageNum));
                        }
                    }
                } else {
                    if (!TextUtils.isEmpty(pdfCurpage.getText().toString())) {
                        int enteredPage = Integer.parseInt(pdfCurpage.getText().toString());
                        if (enteredPage <= pdfpageCount && enteredPage >= 1) {
                            pdfPageNum = enteredPage;

                            File pdfBackgoundFile = new File(videoStreamContext.getFilesDir() + "/SchoolDirectStudent/PDF", "PDF-temp" + ".jpg");

                            new GenerateImageFromPdfAsync(() -> {
                                if (pdfBackgoundFile.exists()) {
                                    pdfBackgoundFile.delete();
                                }
                                horizontalPBar.setVisibility(View.VISIBLE);
                                horizontalPBar.animate();
                            },
                                    bitmap -> {
                                        horizontalPBar.setVisibility(View.INVISIBLE);
                                        if (bitmap != null) {
                                            pdf_canvas.clearHistory();
                                            pdfCanvasCleared = true;

                                            // Redraw on pdf
                                            for (int i = 0; i < drawingCoordinatesJsonArray.length(); i++) {
                                                JSONObject coordinateJson = null;
                                                try {
                                                    coordinateJson = (JSONObject) drawingCoordinatesJsonArray.get(i);

                                                    if (coordinateJson.has("ispdf") && coordinateJson.getInt("ispdf") == 1) {
                                                        if (pdf_canvas.getVisibility() == View.VISIBLE) {
                                                            if (coordinateJson.getInt("sessionId") == pdfPageNum) {
                                                                if (docFile != null && URLUtil.guessFileName(docFile.getAbsolutePath(), null, null).equals(URLUtil.guessFileName(coordinateJson.getString("pdfpath"), null, null))) {
                                                                    drawOnPdf(coordinateJson);
                                                                }
                                                            }
                                                        }
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                            Drawable drawable = new BitmapDrawable(getResources(), bitmap);

                                            imgview.setVisibility(View.VISIBLE);
                                            imgview.setImageDrawable(drawable);

                                            final int imageViewHeight = imgview.getHeight(), imageViewWidth = imgview.getWidth();
                                            final int bitmapHeight = drawable.getIntrinsicHeight(), bitmapWidth = drawable.getIntrinsicWidth();

                                            int actualWidth;
                                            int actualHeight;
                                            if (imageViewHeight * bitmapWidth <= imageViewWidth * bitmapHeight) {
                                                actualWidth = bitmapWidth * imageViewHeight / bitmapHeight;
                                                actualHeight = imageViewHeight;
                                            } else {
                                                actualHeight = bitmapHeight * imageViewWidth / bitmapWidth;
                                                actualWidth = imageViewWidth;
                                            }
                                            pdf_canvas.setLayoutParams(new FrameLayout.LayoutParams(actualWidth, actualHeight));
                                            pdf_canvas.setBackground(drawable);
                                            imgview.setVisibility(View.GONE);

                                            pdfCurpage.clearFocus();
                                        }
                                    }, progress -> {

                            }, () -> {

                            }, videoStreamActivity, Uri.fromFile(docFile), pdfPageNum, pdfBackgoundFile).execute();
                        } else {
                            pdfCurpage.setText(String.valueOf(pdfPageNum));
                        }
                    }
                }
            }
        });

        /*padCurpage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (padCurpage.getText().toString().trim().equals("")) {
                    padCurpage.setText(String.valueOf(padPageNum));
                }
            }
        });

        pdfCurpage.setOnFocusChangeListener((view, b) -> {
            if (pdfCurpage.getText().toString().trim().equals("")) {
                pdfCurpage.setText(String.valueOf(pdfPageNum));
            }
        });*/

        player.getPlayer().addListener(new Player.DefaultEventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playWhenReady && playbackState == Player.STATE_READY) {
                    // media actually playing
                    if (ISRECORDED) {
                        coordinateIndex = getCoordinateIndex();
                        redraw();
                        replay();
                    } else {
                        if (sessionupdated) {
                            sessionupdated = false;
                        }
                    }

                } else if (playWhenReady) {
                    // might be idle (plays after prepare()),
                    // buffering (plays when data available)
                    // or ended (plays when seek away from end)
                } else {
                    Log.d("243968", "getDuration: " + String.valueOf(player.getPlayer().getDuration()));
                    Log.d("243968", "getContentDuration: " + String.valueOf(player.getPlayer().getContentDuration()));
                    Log.d("243968", "getBufferedPosition: " + String.valueOf(player.getPlayer().getBufferedPosition()));
                    Log.d("243968", "getTotalBufferedDuration: " + String.valueOf(player.getPlayer().getTotalBufferedDuration()));
                    Log.d("243968", "getContentBufferedPosition: " + String.valueOf(player.getPlayer().getContentBufferedPosition()));
                    Log.d("243968", "getCurrentPosition: " + String.valueOf(player.getPlayer().getCurrentPosition()));
                    Log.d("243968", "getContentPosition: " + String.valueOf(player.getPlayer().getContentPosition()));
                    // player paused in any state
                    if (myTimer != null) {
                        myTimer.cancel();
                        myTimer = null;
                    }
                }
            }
        });

        if (!ISRECORDED) {
            ViewTreeObserver vto = pad_canvas.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        pad_canvas.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        pad_canvas.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    onPlayInit();
                }
            });
        }

        initChatSocket();

        ConferenceCallInfoMaterialDialog conferenceCallInfoMaterialDialog = new ConferenceCallInfoMaterialDialog();
        conferenceCallInfoMaterialDialog.setDial_code(DIALCODE);
        conferenceCallInfoMaterialDialog.setConference_id(CONFERENCEID);

        if (!ISRECORDED) {
            conferenceCallInfoMaterialDialog.show(getSupportFragmentManager(), "");
        }

        KeyboardUtils.addKeyboardToggleListener(this, new KeyboardUtils.SoftKeyboardToggleListener() {
            @Override
            public void onToggleSoftKeyboard(boolean isVisible) {
                ViewTreeObserver vto = pad_canvas.getViewTreeObserver();
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            pad_canvas.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            pad_canvas.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                        if (!ISRECORDED) {
                            if (pad.getVisibility() == View.VISIBLE) {
                                pdf_canvas.clearHistory();
                                pdfCanvasCleared = true;
                                redrawOnPdf(pdfPageNum);
                            } else {
                                pad_canvas.clearHistory();
                                padCanvasCleared = true;
                                redrawOnPad(padPageNum);
                            }
                        }
                    }
                });
            }
        });

        connectTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                videoStreamActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonData = new JSONObject()
                                    .put(
                                            "connected_video", PreferenceManager.getDefaultSharedPreferences(videoStreamContext).getString(MYUSERID, "")
                                    );
//                                        broadcastWithSocket(jsonData.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }, 5000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatSocket != null) {
            chatSocket.leave("chat:" + INSTRUCTORCOURSEID);
            chatSocket.clearListeners();
            chatSocket.close();
            chatSocket.terminate();
            chatSocket = null;
        }

        if (connectTimer != null) {
            connectTimer.cancel();
        }

        File appDir = new File(getFilesDir() + "/SchoolDirectStudent");
        if (appDir.exists()) {
            deleteDirectory(appDir);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkReceiver);
        if (player != null) {
            player.pausePlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        activeActivity = this;
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        messageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter((int) PreferenceManager.getDefaultSharedPreferences(this).getLong(PREF_MSG_LENGTH_KEY, DEFAULT_MSG_LENGTH))});
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (Map.Entry me : downFileAsyncMap.entrySet()) {
            AsyncTask<String, Integer, String> downloadFileAsync = (AsyncTask<String, Integer, String>) me.getValue();
            if (downloadFileAsync != null && downloadFileAsync.getStatus() != AsyncTask.Status.FINISHED) {
                // This would not cancel downloading from httpClient
                //  we have do handle that manually in onCancelled event inside AsyncTask
                downloadFileAsync.cancel(true);
            }
        }
        if (downloadFileAsync != null && downloadFileAsync.getStatus() != AsyncTask.Status.FINISHED) {
            // This would not cancel downloading from httpClient
            //  we have do handle that manually in onCancelled event inside AsyncTask
            downloadFileAsync.cancel(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        cardview.setVisibility(View.GONE);
        String chatid = UUID.randomUUID().toString();
        switch (requestCode) {
            case REQUEST_MEDIA:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        mMediaSelectedList = MediaPickerActivity.Companion
                                .getMediaItemSelected(data);
                        if (mMediaSelectedList != null) {
                            cardview.setVisibility(View.GONE);

                            for (MediaItem mediaItem : mMediaSelectedList) {
                                String path = mediaItem.getPathOrigin(videoStreamContext);
                                File file = new File(path);
                                if (file.length() > 10000000L) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.file_size_of) + " " + fileSize(file.length()) + " " + getString(R.string.larger_than_limit), Toast.LENGTH_SHORT).show();
                                } else {
                                    RealmChat realmChat = new RealmChat(
                                            chatid,
                                            null,
                                            UUID.randomUUID().toString(),
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            "URI" + file.getAbsolutePath(),
                                            mediaItem.getType() == 1 ? "image" : "video",
                                            Uri.fromFile(file).getLastPathSegment(),
                                            0,
                                            INSTRUCTORCOURSEID,
                                            PreferenceManager.getDefaultSharedPreferences(videoStreamContext).getString(MYUSERID, ""),
                                            null,
                                            null,
                                            null
                                    );

                                    if (isExternalStorageWritable()) {
                                        String folder = mediaItem.getType() == 1 ? "Images" : "Video";
                                        File file_dir = new File(videoStreamContext.getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Chats/" + folder, "Sent");
                                        if (!file_dir.exists()) {
                                            file_dir.mkdirs();
                                        }
                                        String ext = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
                                        String destinationPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Chats/" + folder + "/Sent/" + chatid + ext;

                                        try {
                                            FileUtils.copyFile(file.getAbsolutePath(), destinationPath);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        saveTempChatToRealm(realmChat);
                                        new SendChatAttachmentAsyncTask(videoStreamContext, realmChat).execute();
                                    }
                                }
                            }
                        } else {
                            Log.e(TAG, "Error to get media, NULL");
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
                break;
            case FilePickerConst.REQUEST_CODE_DOC:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (data != null) {
                            docUris = new ArrayList<>();
                            docUris.addAll(data.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
                            String filePath = "";
                            for (final Uri selectedDocUri : docUris) {
                                File file = new File(selectedDocUri.getPath());
                                try {
                                    filePath = ContentUriUtils.INSTANCE.getFilePath(videoStreamContext, selectedDocUri);
                                } catch (URISyntaxException e) {
                                    e.printStackTrace();
                                }
                                if (file.length() > 10000000L) {
                                    Toast.makeText(this, getString(R.string.file_size_of) + " " + fileSize(file.length()) + " " + getString(R.string.larger_than_limit), Toast.LENGTH_SHORT).show();
                                } else {

                                    RealmChat realmChat = new RealmChat(
                                            chatid,
                                            null,
                                            UUID.randomUUID().toString(),
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            "URI" + file.getAbsolutePath(),
                                            "document",
                                            "URI" + filePath,
                                            0,
                                            INSTRUCTORCOURSEID,
                                            PreferenceManager.getDefaultSharedPreferences(videoStreamContext).getString(MYUSERID, ""),
                                            null,
                                            null,
                                            null
                                    );
                                    if (isExternalStorageWritable()) {
                                        File file_dir = new File(Environment.getExternalStorageDirectory() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Chats/Documents", "Sent");
                                        if (!file_dir.exists()) {
                                            file_dir.mkdirs();
                                        }
                                        String ext = filePath.substring(filePath.lastIndexOf("."));
                                        String destinationPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Chats/Documents/Sent/" + chatid + ext;

                                        try {
                                            FileUtils.copyFile(filePath, destinationPath);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        saveTempChatToRealm(realmChat);
                                        new SendChatAttachmentAsyncTask(videoStreamContext, realmChat).execute();
                                    }
                                }
                            }
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        // some stuff that will happen if there's no result
                        break;
                }
                break;

            case RC_PLACE_PICKER:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Place place = PlacePicker.getPlace(videoStreamContext, data);

                        RealmChat realmChat = new RealmChat(
                                chatid,
                                null,
                                UUID.randomUUID().toString(),
                                null,
                                null,
                                null,
                                "geo:" + place.getLatLng().latitude + "," + place.getLatLng().longitude,
                                null,
                                "https://maps.googleapis.com/maps/api/staticmap?center=" + place.getLatLng().latitude + "," + place.getLatLng().longitude + "&zoom=13&size=600x300&maptype=roadmap&markers=color:blue%7Clabel:S%7C40.702147,-74.015794&markers=color:green%7Clabel:G%7C40.711614,-74.012318&markers=color:red%7Clabel:C%7C40.718217,-73.998284&key=" + getResources().getString(R.string.google_maps_key2),
                                "map",
                                String.valueOf(place.getName()),
                                0,
                                INSTRUCTORCOURSEID,
                                PreferenceManager.getDefaultSharedPreferences(videoStreamContext).getString(MYUSERID, ""),
                                null,
                                null,
                                null
                        );

                        saveTempChatToRealm(realmChat);

                        String json_string = new Gson().toJson(realmChat);
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(json_string);
                            SendChatMsg(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
                break;

            case RC_AUDIO_PICKER:
                switch (resultCode) {
                    case Activity.RESULT_OK:

                        if (wavFile.length() > 10000000L) {
                            Toast.makeText(getApplicationContext(), getString(R.string.file_size_of) + " " + fileSize(wavFile.length()) + " " + getString(R.string.larger_than_limit), Toast.LENGTH_SHORT).show();
                        } else {
                            if (isExternalStorageWritable()) {
                                File file_dir = new File(Environment.getExternalStorageDirectory() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Chats/Audio", "Sent");
                                if (!file_dir.exists()) {
                                    file_dir.mkdirs();
                                }
                                String destinationPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Chats/Audio/Sent/" + chatid + ".aac";

                                long executionId = FFmpeg.executeAsync("-i " + wavFile.getAbsolutePath() + " -acodec aac " + destinationPath, new ExecuteCallback() {

                                    @Override
                                    public void apply(final long executionId, final int returnCode) {
                                        if (returnCode == RETURN_CODE_SUCCESS) {
                                            Log.i(Config.TAG, "Async command execution completed successfully.");

                                            if (isExternalStorageWritable()) {
                                                File file_dir = new File(Environment.getExternalStorageDirectory() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Chats/Audio", "Sent");
                                                if (!file_dir.exists()) {
                                                    file_dir.mkdirs();
                                                }
                                                RealmChat realmChat = new RealmChat(
                                                        chatid,
                                                        null,
                                                        UUID.randomUUID().toString(),
                                                        null,
                                                        null,
                                                        null,
                                                        null,
                                                        null,
                                                        "URI" + destinationPath,
                                                        "audio",
                                                        Uri.fromFile(wavFile).getLastPathSegment(),
                                                        0,
                                                        INSTRUCTORCOURSEID,
                                                        PreferenceManager.getDefaultSharedPreferences(videoStreamContext).getString(MYUSERID, ""),
                                                        null,
                                                        null,
                                                        null
                                                );

                                                saveTempChatToRealm(realmChat);
                                                new VideoStreamActivity.SendChatAttachmentAsyncTask(videoStreamContext, realmChat).execute();
                                            }
                                        } else if (returnCode == RETURN_CODE_CANCEL) {
                                            Log.i(Config.TAG, "Async command execution cancelled by user.");
                                        } else {
                                            Log.i(Config.TAG, String.format("Async command execution failed with returnCode=%d.", returnCode));
                                        }
                                    }
                                });
                            }
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(videoStreamContext, getString(R.string.audio_recording_cancelled), Toast.LENGTH_SHORT).show();
                        break;
                }
                break;

            case FILE_PICKER_REQUEST_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        uri = data.getData();
                        Log.d("engineer", uri.toString());
                        pdfView.setVisibility(View.VISIBLE);
                        downbtn.setVisibility(View.VISIBLE);
                        upbtn.setVisibility(View.GONE);
                        break;
                    case Activity.RESULT_CANCELED:
                        // some stuff that will happen if there's no result
                        break;
                }
                break;

            default:
                break;

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Log.d(TAG, "onBackPressed: ");
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("Are you sure you want to leave class?");

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "onClick: YES");
                finish();
            }
        })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: NO");
                    }
                })
                .show();

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewTreeObserver vto = pad_canvas.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    pad_canvas.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    pad_canvas.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                if (pad.getVisibility() == View.VISIBLE) {
                    pdf_canvas.clearHistory();
                    pdfCanvasCleared = true;
                    redrawOnPdf(pdfPageNum);
                } else {
                    pad_canvas.clearHistory();
                    padCanvasCleared = true;
                    redrawOnPad(padPageNum);
                }
            }
        });
    }

    @Override
    public void onPositiveClick(int from) {
        if (from == 1) {
            if (multiselect_list.size() > 0) {
                Realm.init(getApplicationContext());
                Realm.getInstance(RealmUtility.getDefaultConfig(videoStreamContext)).executeTransaction(realm -> {
                    for (int i = 0; i < multiselect_list.size(); i++) {
                        RealmChat tempRealmChat = Realm.getInstance(RealmUtility.getDefaultConfig(videoStreamContext))
                                .where(RealmChat.class)
                                .equalTo("tempid", ((RealmChat) multiselect_list.get(i)).getTempid())
                                .findFirst();
                        objects.remove(multiselect_list.get(i));
                        if (tempRealmChat != null) {
                            tempRealmChat.deleteFromRealm();
                        }
                        chatAdapter.notifyDataSetChanged();
                    }
                });


                if (mActionMode != null) {
                    mActionMode.finish();
                }
            }
        } else if (from == 2) {
//            if (mActionMode != null) {
//                mActionMode.finish();
//            }

        }
    }

    @Override
    public void onNegativeClick(int from) {

    }

    @Override
    public void onNeutralClick(int from) {

    }

    @AfterPermissionGranted(RC_FILE_PICKER_PERM)
    public void pickDocClicked() {
        if (EasyPermissions.hasPermissions(this, FilePickerConst.PERMISSIONS_FILE_PICKER)) {
            docUris.clear();
            FilePickerBuilder.getInstance()
                    .setMaxCount(MAX_ATTACHMENT_COUNT)
                    .setSelectedFiles(docUris)
                    .enableSelectAll(true)
                    .setActivityTheme(R.style.FilePickerTheme)
                    .pickFile(this);
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_doc_picker),
                    RC_FILE_PICKER_PERM, FilePickerConst.PERMISSIONS_FILE_PICKER);
        }
    }

    @AfterPermissionGranted(RC_AUDIO_AND_STORAGE)
    public void recAudioClicked() {
        String[] perms = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            wavFile = new File(getFilesDir() + "/SchoolDirectStudent", "temp.wav");
            File parentFile = wavFile.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            int color = getResources().getColor(R.color.colorPrimaryDark);
            int requestCode = 0;
            AndroidAudioRecorder.with(VideoStreamActivity.this)
                    // Required
                    .setFilePath(wavFile.getAbsolutePath())
                    .setColor(color)
                    .setRequestCode(requestCode)

                    // Optional
                    .setSource(AudioSource.MIC)
                    .setChannel(AudioChannel.STEREO)
                    .setSampleRate(AudioSampleRate.HZ_8000)
                    .setAutoStart(false)
                    .setKeepDisplayOn(true)

                    // Start recording
                    .record();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_doc_picker),
                    RC_AUDIO_AND_STORAGE, perms);
        }
    }

    @AfterPermissionGranted(RC_STORAGE)
    public void setRecyclerViewAdapter() {
        if (EasyPermissions.hasPermissions(this, WRITE_EXTERNAL_STORAGE)) {
            recylerView.setAdapter(chatAdapter);
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.we_need_this_permission_to_display_chat_messages),
                    RC_STORAGE, WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void loadComplete(int nbPages) {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        printBookmarksTree(pdfView.getTableOfContents(), "-");

        if (meta != null) {
            Log.e(TAG, "title = " + meta.getTitle());
            Log.e(TAG, "author = " + meta.getAuthor());
            Log.e(TAG, "subject = " + meta.getSubject());
            Log.e(TAG, "keywords = " + meta.getKeywords());
            Log.e(TAG, "creator = " + meta.getCreator());
            Log.e(TAG, "producer = " + meta.getProducer());
            Log.e(TAG, "creationDate = " + meta.getCreationDate());
            Log.e(TAG, "modDate = " + meta.getModDate());
        }
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
    }

    @Override
    public void onPageError(int page, Throwable t) {
        Log.e(TAG, "Cannot load offset " + page);
    }

    private static ArrayList<Object> filter(ArrayList<Object> models, String search_txt) {

        if (search_txt.equals("")) {
            return models;
        }
        search_txt = search_txt.toLowerCase();
        final ArrayList<Object> filteredModelList = new ArrayList<>();
        for (Object model : models) {

            if (model instanceof RealmChat && ((RealmChat) model).getText() != null) {
                final String text1 = ((RealmChat) model).getText().toLowerCase();

                if (text1.contains(search_txt)) {
                    filteredModelList.add(model);
                }
            }
        }
        return filteredModelList;
    }

    public void refreshAdapter() {
        chatAdapter.selected_usersList = multiselect_list;
        chatAdapter.consolidatedList = objects;
        chatAdapter.notifyDataSetChanged();
    }

    public void multi_select(int position) {
        if (mActionMode != null) {
            if (multiselect_list.contains(objects.get(position)))
                multiselect_list.remove(objects.get(position));
            else
                multiselect_list.add(objects.get(position));

            if (multiselect_list.size() > 0)
                mActionMode.setTitle("" + multiselect_list.size());
            else
                mActionMode.setTitle("");

            refreshAdapter();

        }
    }

    public static void initSearchView1(final ArrayList<Object> searchchats, final ChatAdapter chatAdapter) {

        searchView.setSubmitButtonEnabled(true);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                final ArrayList<Object> filteredModelList = filter(searchchats, query);
                chatAdapter.setFilter(filteredModelList);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {


                ArrayList<Object> filteredModelList = new ArrayList<Object>();
                filteredModelList = filter(searchchats, newText);
                chatAdapter.setFilter(filteredModelList);
                // EventFrag.filter(filteredModelList);

                // chatAdapter = new MessageAdapter(videoStreamContext, R.layout.item_message, filteredModelList, mUserId);
                // mMessageListView.getAdapter().
                //  mMessageListView.setAdapter(chatAdapter);
                Log.d("Text", "Canging" + filteredModelList.size());
                return true;
            }

        });
        searchView.setOnSearchClickListener(v -> coursepath.setVisibility(View.GONE));
        searchView.setOnCloseListener(() -> {
            coursepath.setVisibility(View.VISIBLE);
            return false;
        });
    }

    public void SendChatMsg(JSONObject request) {
        final String[] URL = {null};
        try {
            URL[0] = API_URL + "chats";

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    POST,
                    URL[0],
                    request,
                    response -> {
                        Log.d("Cyrilll", response.toString());
                        if (response != null) {
                            final RealmChat[] realmChat = new RealmChat[1];
                            Realm.getInstance(RealmUtility.getDefaultConfig(videoStreamContext)).executeTransaction(realm -> {
                                try {
                                    realmChat[0] = realm.createOrUpdateObjectFromJson(RealmChat.class, response.getJSONObject("chat"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            });
                            updateSentChatStatus(realmChat[0]);
                            broadcastWithSocket(response.toString());
                            new broadcastWithFirebase(response).execute();
                        }
                    },
                    error -> error.printStackTrace()
            ) {
                /** Passing some request headers* */
                @Override
                public Map getHeaders() throws AuthFailureError {
                    HashMap headers = new HashMap();
                    headers.put("accept", "application/json");
                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(videoStreamContext).getString(APITOKEN, ""));
                    return headers;
                }
            };
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(jsonObjectRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateSentChatStatus(RealmChat realmChat) {
        for (int i = 0; i < objects.size(); i++) {
            Object obj = objects.get(i);
            if (obj instanceof RealmChat) {
                if (((RealmChat) obj).getChatid().equals(realmChat.getChatid())) {
                    objects.set(i, realmChat);
                    chatAdapter.notifyItemChanged(i);
                    mLinearLayoutManager.scrollToPositionWithOffset(objects.size() - 1, 0);
                }
            }
        }
    }

    public static void redrawOnPad(int page) {
        for (int i = 0; i < drawingCoordinatesJsonArray.length(); i++) {
            JSONObject coordinateJson = null;
            try {
                coordinateJson = (JSONObject) drawingCoordinatesJsonArray.get(i);

                if (!(coordinateJson.has("ispdf") && coordinateJson.getInt("ispdf") == 1)) {
                    if (pad_canvas.getVisibility() == View.VISIBLE) {
                        if (coordinateJson.getInt("sessionId") == page) {
                            drawOnPad(coordinateJson);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void redrawOnPdf(int page) {
        boolean backgroundset = false;
        for (int i = 0; i < drawingCoordinatesJsonArray.length(); i++) {
            JSONObject coordinateJson = null;
            try {
                coordinateJson = (JSONObject) drawingCoordinatesJsonArray.get(i);

                if (coordinateJson.has("ispdf") && coordinateJson.getInt("ispdf") == 1) {
                    if (pdf_canvas.getVisibility() == View.VISIBLE) {
                        if (coordinateJson.getInt("sessionId") == page) {
                            if (docFile != null && URLUtil.guessFileName(docFile.getAbsolutePath(), null, null).equals(URLUtil.guessFileName(coordinateJson.getString("pdfpath"), null, null))) {
                                drawOnPdf(coordinateJson);
                                if (!backgroundset) {
                                    backgroundset = true;

                                    File pdfBackgoundFile = new File(videoStreamContext.getFilesDir() + "/SchoolDirectStudent/PDF", "PDF-temp" + ".jpg");

                                    new GenerateImageFromPdfAsync(new GenerateImageFromPdfAsync.OnTaskPreExecuteInterface() {
                                        @Override
                                        public void onPreExecute() {
                                            if (pdfBackgoundFile.exists()) {
                                                pdfBackgoundFile.delete();
                                            }
                                        }
                                    },
                                            new GenerateImageFromPdfAsync.OnTaskCompletedInterface() {
                                                @Override
                                                public void onTaskCompleted(Bitmap bitmap) {
                                                    if (bitmap != null) {
                                                        Drawable drawable = new BitmapDrawable(getResources(), bitmap);

                                                        imgview.setVisibility(View.VISIBLE);
                                                        imgview.setImageDrawable(drawable);

                                                        final int imageViewHeight = imgview.getHeight(), imageViewWidth = imgview.getWidth();
                                                        final int bitmapHeight = drawable.getIntrinsicHeight(), bitmapWidth = drawable.getIntrinsicWidth();

                                                        int actualWidth;
                                                        int actualHeight;
                                                        if (imageViewHeight * bitmapWidth <= imageViewWidth * bitmapHeight) {
                                                            actualWidth = bitmapWidth * imageViewHeight / bitmapHeight;
                                                            actualHeight = imageViewHeight;
                                                        } else {
                                                            actualHeight = bitmapHeight * imageViewWidth / bitmapWidth;
                                                            actualWidth = imageViewWidth;
                                                        }
                                                        pdf_canvas.setLayoutParams(new FrameLayout.LayoutParams(actualWidth, actualHeight));
                                                        pdf_canvas.setBackground(drawable);
                                                        imgview.setVisibility(View.GONE);
                                                    }
                                                }
                                            }, new GenerateImageFromPdfAsync.OnTaskProgressUpdateInterface() {

                                        @Override
                                        public void onTaskProgressUpdate(int progress) {

                                        }
                                    }, new GenerateImageFromPdfAsync.OnTaskCancelledInterface() {

                                        @Override
                                        public void onTaskCancelled() {

                                        }
                                    }, videoStreamActivity, Uri.fromFile(docFile), pdfPageNum, pdfBackgoundFile).execute();
                                }
                            } else {
//                                setPdf(coordinateJson.getString("pdfpath"), coordinateJson);
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void redraw() {
        for (int i = 0; i < coordinateIndex; i++) {
            try {
                final JSONObject[] coordinateJson = {drawingCoordinatesJsonArray.getJSONObject(i)};
                JSONObject finalCoordinateJson = coordinateJson[0];
                try {
                    if (finalCoordinateJson.has("ispdf") && finalCoordinateJson.getInt("ispdf") == 1) {
                        if (docFile != null && URLUtil.guessFileName(docFile.getAbsolutePath(), null, null).equals(URLUtil.guessFileName(finalCoordinateJson.getString("pdfpath"), null, null))) {
                            pad.setVisibility(View.VISIBLE);
                            pdf.setVisibility(View.GONE);

                            pad_canvas.setVisibility(View.INVISIBLE);
                            pdf_canvas.setVisibility(View.VISIBLE);
                            zoom_layout.setVisibility(View.VISIBLE);

                            padPagePanel.setVisibility(View.GONE);
                            pdfPagePanel.setVisibility(View.VISIBLE);

                            boolean samePage = finalCoordinateJson.getInt("sessionId") == Integer.parseInt(pdfCurpage.getText().toString());
                            if (!samePage || (samePage && pdfCanvasCleared)) {
                                try {
                                    pdfPageNum = finalCoordinateJson.getInt("sessionId");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                pdf_canvas.clearHistory();
                                pdfCanvasCleared = true;
                                pdfCurpage.setText(String.valueOf(pdfPageNum));
//                                                redrawOnPdf(pdfPageNum);


                                File pdfBackgoundFile = new File(videoStreamContext.getFilesDir() + "/SchoolDirectStudent/PDF", "PDF-temp" + ".jpg");

                                new GenerateImageFromPdfAsync(() -> {
                                    if (pdfBackgoundFile.exists()) {
                                        pdfBackgoundFile.delete();
                                    }
                                },
                                        bitmap -> {
                                            if (bitmap != null) {
                                                Drawable drawable = new BitmapDrawable(getResources(), bitmap);

                                                imgview.setVisibility(View.VISIBLE);
                                                imgview.setImageDrawable(drawable);

                                                final int imageViewHeight = imgview.getHeight(), imageViewWidth = imgview.getWidth();
                                                final int bitmapHeight = drawable.getIntrinsicHeight(), bitmapWidth = drawable.getIntrinsicWidth();

                                                int actualWidth;
                                                int actualHeight;
                                                if (imageViewHeight * bitmapWidth <= imageViewWidth * bitmapHeight) {
                                                    actualWidth = bitmapWidth * imageViewHeight / bitmapHeight;
                                                    actualHeight = imageViewHeight;
                                                } else {
                                                    actualHeight = bitmapHeight * imageViewWidth / bitmapWidth;
                                                    actualWidth = imageViewWidth;
                                                }
                                                pdf_canvas.setLayoutParams(new FrameLayout.LayoutParams(actualWidth, actualHeight));
                                                pdf_canvas.setBackground(drawable);
                                                imgview.setVisibility(View.GONE);

                                            }
                                        }, progress -> {

                                }, () -> {

                                }, videoStreamActivity, Uri.fromFile(docFile), pdfPageNum, pdfBackgoundFile).execute();

                                try {
                                    drawOnPdf(finalCoordinateJson);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                drawOnPdf(finalCoordinateJson);
                            }
                        } else {
                            pdf_canvas.clearHistory();
                            pdfCanvasCleared = true;
                            pdf_canvas.setBackground(null);
                            setPdf(finalCoordinateJson.getString("pdfpath"), finalCoordinateJson);
                        }
                    } else {
                        pad.setVisibility(View.GONE);
                        pdf.setVisibility(View.VISIBLE);

                        pad_canvas.setVisibility(View.VISIBLE);
                        pdf_canvas.setVisibility(View.INVISIBLE);
                        zoom_layout.setVisibility(View.INVISIBLE);

                        padPagePanel.setVisibility(View.VISIBLE);
                        pdfPagePanel.setVisibility(View.GONE);

                        boolean samePage = finalCoordinateJson.getInt("sessionId") == Integer.parseInt(padCurpage.getText().toString());
                        if (!samePage || (samePage && padCanvasCleared)) {
                            padPageNum = finalCoordinateJson.getInt("sessionId");
                            pad_canvas.clearHistory();
                            padCanvasCleared = true;
                            padCurpage.setText(String.valueOf(padPageNum));
//                                            redrawOnPad(padPageNum);
                        }
                        drawOnPad(finalCoordinateJson);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void replay() {
        if (myTimer != null) {
            myTimer.cancel();
            myTimer = null;
        }
        myTimer = new Timer();
        int initialIndex = coordinateIndex;
        for (int i = coordinateIndex; i < drawingCoordinatesJsonArray.length(); i++) {
            try {
                final JSONObject[] coordinateJson = {drawingCoordinatesJsonArray.getJSONObject(i)};

                int finalI = i;
                int delay = (int) timeDiff;
                JSONObject finalCoordinateJson = coordinateJson[0];
                if (finalI > coordinateIndex) {
                    delay = (int) (Const.preciseDateTimeFormat.parse(drawingCoordinatesJsonArray.getJSONObject(finalI).getString("created_at")).getTime() - Const.dateTimeFormat.parse(drawingCoordinatesJsonArray.getJSONObject(initialIndex).getString("created_at")).getTime() - timeDiff);
                    if (delay < 0) {
                        delay = (int) timeDiff;
                    }
                }
                myTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        videoStreamActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (finalCoordinateJson.has("ispdf") && finalCoordinateJson.getInt("ispdf") == 1) {
                                        if (docFile != null && URLUtil.guessFileName(docFile.getAbsolutePath(), null, null).equals(URLUtil.guessFileName(finalCoordinateJson.getString("pdfpath"), null, null))) {
                                            pad.setVisibility(View.VISIBLE);
                                            pdf.setVisibility(View.GONE);

                                            pad_canvas.setVisibility(View.INVISIBLE);
                                            pdf_canvas.setVisibility(View.VISIBLE);
                                            zoom_layout.setVisibility(View.VISIBLE);

                                            padPagePanel.setVisibility(View.GONE);
                                            pdfPagePanel.setVisibility(View.VISIBLE);

                                            boolean samePage = finalCoordinateJson.getInt("sessionId") == Integer.parseInt(pdfCurpage.getText().toString());
                                            if (!samePage || (samePage && pdfCanvasCleared)) {
                                                try {
                                                    pdfPageNum = finalCoordinateJson.getInt("sessionId");
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                pdf_canvas.clearHistory();
                                                pdfCanvasCleared = true;
                                                pdfCurpage.setText(String.valueOf(pdfPageNum));
//                                                redrawOnPdf(pdfPageNum);


                                                File pdfBackgoundFile = new File(videoStreamContext.getFilesDir() + "/SchoolDirectStudent/PDF", "PDF-temp" + ".jpg");

                                                new GenerateImageFromPdfAsync(() -> {
                                                    if (pdfBackgoundFile.exists()) {
                                                        pdfBackgoundFile.delete();
                                                    }
                                                },
                                                        bitmap -> {
                                                            if (bitmap != null) {
                                                                Drawable drawable = new BitmapDrawable(getResources(), bitmap);

                                                                imgview.setVisibility(View.VISIBLE);
                                                                imgview.setImageDrawable(drawable);

                                                                final int imageViewHeight = imgview.getHeight(), imageViewWidth = imgview.getWidth();
                                                                final int bitmapHeight = drawable.getIntrinsicHeight(), bitmapWidth = drawable.getIntrinsicWidth();

                                                                int actualWidth;
                                                                int actualHeight;
                                                                if (imageViewHeight * bitmapWidth <= imageViewWidth * bitmapHeight) {
                                                                    actualWidth = bitmapWidth * imageViewHeight / bitmapHeight;
                                                                    actualHeight = imageViewHeight;
                                                                } else {
                                                                    actualHeight = bitmapHeight * imageViewWidth / bitmapWidth;
                                                                    actualWidth = imageViewWidth;
                                                                }
                                                                pdf_canvas.setLayoutParams(new FrameLayout.LayoutParams(actualWidth, actualHeight));
                                                                pdf_canvas.setBackground(drawable);
                                                                imgview.setVisibility(View.GONE);

                                                            }
                                                        }, progress -> {

                                                }, () -> {

                                                }, videoStreamActivity, Uri.fromFile(docFile), pdfPageNum, pdfBackgoundFile).execute();

                                                try {
                                                    drawOnPdf(finalCoordinateJson);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                drawOnPdf(finalCoordinateJson);
                                            }
                                        } else {
                                            pdf_canvas.clearHistory();
                                            pdfCanvasCleared = true;
                                            pdf_canvas.setBackground(null);
                                            setPdf(finalCoordinateJson.getString("pdfpath"), finalCoordinateJson);
                                        }
                                    } else {
                                        pad.setVisibility(View.GONE);
                                        pdf.setVisibility(View.VISIBLE);

                                        pad_canvas.setVisibility(View.VISIBLE);
                                        pdf_canvas.setVisibility(View.INVISIBLE);
                                        zoom_layout.setVisibility(View.INVISIBLE);

                                        padPagePanel.setVisibility(View.VISIBLE);
                                        pdfPagePanel.setVisibility(View.GONE);

                                        boolean samePage = finalCoordinateJson.getInt("sessionId") == Integer.parseInt(padCurpage.getText().toString());
                                        if (!samePage || (samePage && padCanvasCleared)) {
                                            padPageNum = finalCoordinateJson.getInt("sessionId");
                                            pad_canvas.clearHistory();
                                            padCanvasCleared = true;
                                            padCurpage.setText(String.valueOf(padPageNum));
                                        }
                                        drawOnPad(finalCoordinateJson);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }, delay);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public int getCoordinateIndex() {
        for (int i = 0; i < drawingCoordinatesJsonArray.length(); i++) {
            try {
                long timeDiff = Const.preciseDateTimeFormat.parse(drawingCoordinatesJsonArray.getJSONObject(i).getString("created_at")).getTime() - Const.dateTimeFormat.parse(drawingCoordinatesJsonArray.getJSONObject(0).getString("created_at")).getTime();
                Log.d("243968", String.valueOf(timeDiff));
                if (timeDiff > player.getPlayer().getCurrentPosition()) {
                   /*Log.d("243968", "Index: " + String.valueOf(i));
                    Log.d("243968", "Diff: " + String.valueOf(timeDiff));
                    Log.d("243968", "Current position: " + String.valueOf(player.getPlayer().getDuration()));*/
                    return i;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private class SendChatAttachmentAsyncTask extends AsyncTask<Void, Integer, String> {

        HttpClient httpClient = new DefaultHttpClient();
        RealmChat realmChat;
        private Context context;
        private Exception exception;
        // private ProgressDialog progressDialog;

        private SendChatAttachmentAsyncTask(Context context, RealmChat realmChat) {
            this.context = context;
            this.realmChat = realmChat;
        }

        @Override
        protected String doInBackground(Void... params) {

            HttpResponse httpResponse = null;
            HttpEntity httpEntity = null;
            String responseString = null;
            String URL = keyConst.API_URL + "chats";
            try {
                HttpPost httpPost = new HttpPost(URL);
                MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();

                // Add the file to be uploaded
                Uri uri = Uri.parse(realmChat.getAttachmenturl().substring(3));
                File file = new File(uri.getPath());
                multipartEntityBuilder.addPart("file", new FileBody(file));

                multipartEntityBuilder.addTextBody("attachmenttype", realmChat.getAttachmenttype());
                multipartEntityBuilder.addTextBody("attachmenttitle", realmChat.getAttachmenttitle());
                multipartEntityBuilder.addTextBody("chatid", realmChat.getChatid());
                multipartEntityBuilder.addTextBody("tempid", realmChat.getTempid());
                multipartEntityBuilder.addTextBody("instructorcourseid", INSTRUCTORCOURSEID);
                multipartEntityBuilder.addTextBody("senderid", PreferenceManager.getDefaultSharedPreferences(context).getString(MYUSERID, ""));
                // Progress listener - updates task's progress
                MyHttpEntity.ProgressListener progressListener =
                        progress -> publishProgress((int) progress);

                // POST
                httpPost.setEntity(new MyHttpEntity(multipartEntityBuilder.build(),
                        progressListener));
                httpPost.setHeader("accept", "application/json");
                httpPost.setHeader("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(context).getString(APITOKEN, ""));


                httpResponse = httpClient.execute(httpPost);
                httpEntity = httpResponse.getEntity();

                int statusCode = httpResponse.getStatusLine().getStatusCode();
//                Log.d("56876", EntityUtils.toString(httpEntity));
                if (statusCode == 200 || statusCode == 201) {
                    // Server response
                    responseString = EntityUtils.toString(httpEntity);
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                }
            } catch (UnsupportedEncodingException | ClientProtocolException e) {
                e.printStackTrace();
                Log.e("UPLOAD", e.getMessage());
                this.exception = e;
            } catch (IOException e) {
                if (e.toString().contains("HttpHostConnectException")) {
                    responseString = "Connection error";
                } else {
                    responseString = "Error occurred! Http Status Code: ";
                }
                e.printStackTrace();
            }

            return responseString;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                if (result.contains("Error occurred! Http Status Code: ")) {
                    Toast.makeText(context, context.getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
                } else if (result.equals("Connection error")) {
                    Toast.makeText(context, context.getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                } else {
                    JSONObject responseJson = null;
                    try {
                        responseJson = new JSONObject(result);
                        final RealmChat[] realmChat = new RealmChat[1];
                        JSONObject finalResponseJson1 = responseJson;
                        Realm.getInstance(RealmUtility.getDefaultConfig(videoStreamContext)).executeTransaction(realm -> {
                            try {
                                realmChat[0] = realm.createOrUpdateObjectFromJson(RealmChat.class, finalResponseJson1.getJSONObject("chat"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        });
                        updateSentChatStatus(realmChat[0]);
                        broadcastWithSocket(responseJson.toString());
                        new broadcastWithFirebase(responseJson).execute();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
        }
    }

    public void init() {
        statusMsg.setText(NO_CLASS_CHATS);
        controls.setVisibility(View.VISIBLE);
        recylerView.setVisibility(View.VISIBLE);
        objects.clear();
        populateObjects(videoStreamContext);
        objects.addAll(newObjects);
        chatAdapter.notifyDataSetChanged();
        mLinearLayoutManager.scrollToPositionWithOffset(objects.size() - 1, 0);
    }

    public void initChatSocket() {
        chatSocket = Socket
                .Builder.with(NODESERVER)
                .build();
        chatSocket.connect();

        chatSocket.onEvent(EVENT_OPEN, new Socket.OnEventListener() {
            @Override
            public void onMessage(String event) {
                Log.d("mywebsocket1", "Connected");

                chatSocket.join("chat:" + INSTRUCTORCOURSEID);

                chatSocket.onEventResponse("chat:" + INSTRUCTORCOURSEID, new Socket.OnEventResponseListener() {
                    @Override
                    public void onMessage(String event, String data) {
                        Log.d("mywebsocket1", "socket id:");
                    }
                });

                chatSocket.setMessageListener(new Socket.OnMessageListener() {
                    @Override
                    public void onMessage(String data) {
                        JSONObject jsonObject = null;
                        JSONObject jsonResponse = null;
                        String message = "";
                        try {
                            jsonObject = new JSONObject(data);
                            switch (jsonObject.getInt("t")) {
                                case 0:
                                    break;
                                case 1:
                                    break;
                                case 2:
                                    break;
                                case 3:
                                    break;
                                case 4:
                                    break;
                                case 5:
                                    break;
                                case 6:
                                    break;
                                case 7:
                                    jsonResponse = jsonObject.getJSONObject("d");
                                    Log.d("mywebsocket1", jsonResponse.toString());
                                    Realm.init(videoStreamContext);
                                    JSONObject finalJsonResponse = jsonResponse;
                                    if (finalJsonResponse.getJSONObject("data").has("connected_video")) {
                                        Realm.getInstance(RealmUtility.getDefaultConfig(videoStreamContext)).executeTransaction(realm -> {
                                            try {
                                                updateParticipantConnectionStatus(finalJsonResponse, realm, 1);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        });

                                    } else if (finalJsonResponse.getJSONObject("data").has("disconnected_video")) {
                                        Realm.getInstance(RealmUtility.getDefaultConfig(videoStreamContext)).executeTransaction(realm -> {
                                            try {
                                                updateParticipantConnectionStatus(finalJsonResponse, realm, 0);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        });
                                    } else if (finalJsonResponse.getJSONObject("data").has("chat")) {
                                        Realm.getInstance(RealmUtility.getDefaultConfig(videoStreamContext)).executeTransaction(realm -> {
                                            try {
                                                RealmChat realmChat = realm.createOrUpdateObjectFromJson(RealmChat.class, finalJsonResponse.getJSONObject("data").getJSONObject("chat"));
                                                RealmUser realmUser = realm.createOrUpdateObjectFromJson(RealmUser.class, finalJsonResponse.getJSONObject("data").getJSONObject("sender"));
                                                RealmStudent realmStudent = null;
                                                RealmInstructor realmInstructor = null;
                                                RealmChat realmReferencedChat = null;

                                                if (finalJsonResponse.getJSONObject("data").has("student")) {
                                                    realmStudent = realm.createOrUpdateObjectFromJson(RealmStudent.class, finalJsonResponse.getJSONObject("data").getJSONObject("student"));
                                                    realmChat.setName(realmStudent != null ? realmStudent.getFirstname() : realmUser.getPhonenumber());
                                                } else if (finalJsonResponse.getJSONObject("data").has("instructor")) {
                                                    realmInstructor = realm.createOrUpdateObjectFromJson(RealmInstructor.class, finalJsonResponse.getJSONObject("data").getJSONObject("instructor"));
                                                    realmChat.setName(realmInstructor != null ? realmInstructor.getFirstname() : realmUser.getPhonenumber());
                                                    realmChat.setInstructor(true);
                                                }

                                                if (finalJsonResponse.getJSONObject("data").has("referenced_chat")) {
                                                    realmReferencedChat = realm.createOrUpdateObjectFromJson(RealmChat.class, finalJsonResponse.getJSONObject("data").getJSONObject("referenced_chat"));
                                                    if (realmReferencedChat != null) {
                                                        realmChat.setReplybody(realmReferencedChat.getText() != null ? realmReferencedChat.getText() : realmReferencedChat.getAttachmenttitle());
                                                        if (!realmReferencedChat.getSenderid().equals(PreferenceManager.getDefaultSharedPreferences(videoStreamContext).getString(MYUSERID, ""))) {
                                                            if (realmStudent != null) {
                                                                realmChat.setReplyname(realmStudent.getFirstname() != null ? realmStudent.getFirstname() : realmUser.getPhonenumber());
                                                            } else if (realmInstructor != null) {
                                                                realmChat.setReplyname(realmInstructor.getFirstname() != null ? realmInstructor.getFirstname() : realmUser.getPhonenumber());
                                                            }
                                                        } else {
                                                            realmChat.setReplyname(videoStreamContext.getString(R.string.me));
                                                        }
                                                    }
                                                }


                                                addMsgToChat(finalJsonResponse, realmChat);
                                            } catch (JSONException e) {

                                                e.printStackTrace();
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                        });
                                    } else if (finalJsonResponse.getJSONObject("data").has("sessionId")) {
                                        int sessionId = finalJsonResponse.getJSONObject("data").getInt("sessionId");
                                        if (sessionId > padpageCount) {
                                            padpageCount = sessionId;
                                            padTotalPages.setText(String.valueOf(padpageCount));
                                        }
                                        boolean coordinateBeloangsToClassSession = finalJsonResponse.getJSONObject("data").getString("classsessionid").equals(SESSIONID);
                                        if (coordinateBeloangsToClassSession) {
                                            Log.d("96849848ojo", finalJsonResponse.getJSONObject("data").toString());
                                            finalJsonResponse.getJSONObject("data").put("ispdf", finalJsonResponse.getJSONObject("data").has("ispdf") ? 1 : 0);
                                            drawingCoordinatesJsonArray.put(finalJsonResponse.getJSONObject("data"));

                                            if (finalJsonResponse.getJSONObject("data").has("ispdf") && finalJsonResponse.getJSONObject("data").getInt("ispdf") == 1) {
                                                if (docFile != null && URLUtil.guessFileName(docFile.getAbsolutePath(), null, null).equals(URLUtil.guessFileName(finalJsonResponse.getJSONObject("data").getString("pdfpath"), null, null))) {
                                                    pad.setVisibility(View.VISIBLE);
                                                    pdf.setVisibility(View.GONE);

                                                    pad_canvas.setVisibility(View.INVISIBLE);
                                                    pdf_canvas.setVisibility(View.VISIBLE);
                                                    zoom_layout.setVisibility(View.VISIBLE);

                                                    padPagePanel.setVisibility(View.GONE);
                                                    pdfPagePanel.setVisibility(View.VISIBLE);

                                                    boolean samePage = finalJsonResponse.getJSONObject("data").getInt("sessionId") == Integer.parseInt(pdfCurpage.getText().toString());
                                                    if (!samePage || (samePage && pdfCanvasCleared)) {
                                                        try {
                                                            pdfPageNum = finalJsonResponse.getJSONObject("data").getInt("sessionId");
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                        pdf_canvas.clearHistory();
                                                        pdfCanvasCleared = true;
                                                        pdfCurpage.setText(String.valueOf(pdfPageNum));
                                                        redrawOnPdf(pdfPageNum);


                                                        File pdfBackgoundFile = new File(videoStreamContext.getFilesDir() + "/SchoolDirectStudent/PDF", "PDF-temp" + ".jpg");

                                                        new GenerateImageFromPdfAsync(() -> {
                                                            if (pdfBackgoundFile.exists()) {
                                                                pdfBackgoundFile.delete();
                                                            }
                                                        },
                                                                bitmap -> {
                                                                    if (bitmap != null) {
                                                                        Drawable drawable = new BitmapDrawable(getResources(), bitmap);

                                                                        imgview.setVisibility(View.VISIBLE);
                                                                        imgview.setImageDrawable(drawable);

                                                                        final int imageViewHeight = imgview.getHeight(), imageViewWidth = imgview.getWidth();
                                                                        final int bitmapHeight = drawable.getIntrinsicHeight(), bitmapWidth = drawable.getIntrinsicWidth();

                                                                        int actualWidth;
                                                                        int actualHeight;
                                                                        if (imageViewHeight * bitmapWidth <= imageViewWidth * bitmapHeight) {
                                                                            actualWidth = bitmapWidth * imageViewHeight / bitmapHeight;
                                                                            actualHeight = imageViewHeight;
                                                                        } else {
                                                                            actualHeight = bitmapHeight * imageViewWidth / bitmapWidth;
                                                                            actualWidth = imageViewWidth;
                                                                        }
                                                                        pdf_canvas.setLayoutParams(new FrameLayout.LayoutParams(actualWidth, actualHeight));
                                                                        pdf_canvas.setBackground(drawable);
                                                                        imgview.setVisibility(View.GONE);

                                                                    }
                                                                }, progress -> {

                                                        }, () -> {

                                                        }, videoStreamActivity, Uri.fromFile(docFile), pdfPageNum, pdfBackgoundFile).execute();

                                                        try {
                                                            drawOnPdf(finalJsonResponse.getJSONObject("data"));
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    } else {
                                                        drawOnPdf(finalJsonResponse.getJSONObject("data"));
                                                    }
                                                } else {
                                                    pdf_canvas.clearHistory();
                                                    pdfCanvasCleared = true;
                                                    pdf_canvas.setBackground(null);
                                                    setPdf(finalJsonResponse.getJSONObject("data").getString("pdfpath"), finalJsonResponse.getJSONObject("data"));
                                                }
                                            } else {
                                                pad.setVisibility(View.GONE);
                                                pdf.setVisibility(View.VISIBLE);

                                                pad_canvas.setVisibility(View.VISIBLE);
                                                pdf_canvas.setVisibility(View.INVISIBLE);
                                                zoom_layout.setVisibility(View.INVISIBLE);

                                                padPagePanel.setVisibility(View.VISIBLE);
                                                pdfPagePanel.setVisibility(View.GONE);

                                                boolean samePage = finalJsonResponse.getJSONObject("data").getInt("sessionId") == Integer.parseInt(padCurpage.getText().toString());
                                                if (!samePage || (samePage && padCanvasCleared)) {
                                                    padPageNum = finalJsonResponse.getJSONObject("data").getInt("sessionId");
                                                    pad_canvas.clearHistory();
                                                    padCanvasCleared = true;
                                                    padCurpage.setText(String.valueOf(padPageNum));
                                                    redrawOnPad(padPageNum);
                                                }
                                                drawOnPad(finalJsonResponse.getJSONObject("data"));
                                            }
                                        }
                                    } else if (finalJsonResponse.getJSONObject("data").has("socketid")) {
                                        try {
                                            JSONObject jsonData = new JSONObject()
                                                    .put(
                                                            "socketid", finalJsonResponse.getJSONObject("data").getString("socketid")
                                                    )
                                                    .put(
                                                            "instructorcourseid", INSTRUCTORCOURSEID
                                                    )
                                                    .put(
                                                            "accesstoken", PreferenceManager.getDefaultSharedPreferences(videoStreamActivity).getString(ACCESSTOKEN, "")
                                                    )
                                                    .put(
                                                            "type", "video"
                                                    )
                                                    .put(
                                                            "sessionid", SESSIONID
                                                    )
                                                    .put(
                                                            "studentid", PreferenceManager.getDefaultSharedPreferences(videoStreamActivity).getString(MYUSERID, "")
                                                    );
                                            broadcastWithSocket(jsonData.toString());
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else if (finalJsonResponse.getJSONObject("data").has("classhasended")) {
//                                        sessionupdated = false;
                                    }
                                    break;
                                case 8:
                                    break;
                                case 9:
                                    break;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                final String[] id = {"0"};
                Realm.init(videoStreamContext);
                Realm.getInstance(RealmUtility.getDefaultConfig(videoStreamContext)).executeTransaction(realm -> {
                    RealmChat realmChat = Realm.getInstance(RealmUtility.getDefaultConfig(videoStreamContext))
                            .where(RealmChat.class)
                            .sort("id", Sort.DESCENDING)
                            .findFirst();
                    if (realmChat != null) {
                        id[0] = String.valueOf(realmChat.getId());
                    }
                });

                StringRequest stringRequest = new StringRequest(
                        com.android.volley.Request.Method.POST,
                        API_URL + "live-class-refresh-data",
                        response -> {
                            mProgress.dismiss();
                            if (response != null) {
                                try {
                                    JSONObject jsonObjectResponse = new JSONObject(response);

                                    Realm.init(videoStreamContext);
                                    Realm.getInstance(RealmUtility.getDefaultConfig(videoStreamContext)).executeTransaction(realm -> {
                                        try {
                                            /*RealmResults<RealmUser> realmUsers = realm.where(RealmUser.class).findAll();
                                            realmUsers.deleteAllFromRealm();

                                            RealmResults<RealmStudent> realmStudents = realm.where(RealmStudent.class).findAll();
                                            realmStudents.deleteAllFromRealm();

                                            RealmResults<RealmInstructor> realmInstructors = realm.where(RealmInstructor.class).findAll();
                                            realmInstructors.deleteAllFromRealm();

                                            RealmResults<RealmInstructorCourse> realmInstructorCourses = realm.where(RealmInstructorCourse.class).findAll();
                                            realmInstructorCourses.deleteAllFromRealm();

                                            RealmResults<RealmEnrolment> realmEnrolments = realm.where(RealmEnrolment.class).findAll();
                                            realmEnrolments.deleteAllFromRealm();

                                            RealmResults<RealmChat> realmChats = realm.where(RealmChat.class).findAll();
                                            realmChats.deleteAllFromRealm();*/

                                            realm.createOrUpdateAllFromJson(RealmUser.class, jsonObjectResponse.getJSONArray("users"));
                                            realm.createOrUpdateAllFromJson(RealmStudent.class, jsonObjectResponse.getJSONArray("students"));
                                            realm.createOrUpdateAllFromJson(RealmInstructor.class, jsonObjectResponse.getJSONArray("instructors"));
                                            realm.createOrUpdateAllFromJson(RealmInstructorCourse.class, jsonObjectResponse.getJSONArray("instructor_courses"));
                                            realm.createOrUpdateAllFromJson(RealmEnrolment.class, jsonObjectResponse.getJSONArray("enrolments"));
                                            realm.createOrUpdateAllFromJson(RealmChat.class, jsonObjectResponse.getJSONArray("chats"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    });
                                    try {
                                        JSONObject jsonData = new JSONObject()
                                                .put(
                                                        "connected_video", PreferenceManager.getDefaultSharedPreferences(videoStreamContext).getString(MYUSERID, "")
                                                );
//                                        broadcastWithSocket(jsonData.toString());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    init();
                                    sendUnsentChats();

                                    participants.clear();
                                    populateParticipants(videoStreamContext);
                                    participants.addAll(newParticipants);
                                    participantsAdapter.notifyDataSetChanged();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        error -> {
                            error.printStackTrace();
                            Log.d("Cyrilll", error.toString());
                            myVolleyError(videoStreamContext, error);
                        }
                ) {
                    @Override
                    public Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("instructorcourseid", INSTRUCTORCOURSEID);
                        params.put("columntobeupdated", "connectedtovideo");
                        params.put("id", id[0]);
                        return params;
                    }

                    /** Passing some request headers* */
                    @Override
                    public Map getHeaders() throws AuthFailureError {
                        HashMap headers = new HashMap();
                        headers.put("accept", "application/json");
                        headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(videoStreamContext).getString(APITOKEN, ""));
                        return headers;
                    }
                };

                stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                        0,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                InitApplication.getInstance().addToRequestQueue(stringRequest);
            }
        });

        chatSocket.onEvent(EVENT_RECONNECT_ATTEMPT, new Socket.OnEventListener() {
            @Override
            public void onMessage(String event) {
                Log.d("mywebsocket1", "reconnecting");
            }
        });
        chatSocket.onEvent(EVENT_CLOSED, new Socket.OnEventListener() {
            @Override
            public void onMessage(String event) {
                Log.d("mywebsocket1", "connection closed");
            }
        });
    }

    public static void drawOnPad(JSONObject dataJsonObject) throws JSONException {
        padCanvasCleared = false;
        int sessionId = dataJsonObject.getInt("sessionId");

        if (!padCurpage.getText().toString().trim().equals(String.valueOf(sessionId))) {
            pad_canvas.clearHistory();
            padCanvasCleared = true;
        }

        if (!dataJsonObject.isNull("color") && !dataJsonObject.getString("color").equals("")) {
            String color = dataJsonObject.getString("color");
            if (color.contains("white")) {
                pad_canvas.setDrawingMode(DrawingMode.ERASER);
                pad_canvas.setDrawWidth(40);
                pad_canvas.drawFromServer(dataJsonObject);
            } else {
                pad_canvas.setDrawingMode(DrawingMode.DRAW);
                pad_canvas.setDrawWidth(dataJsonObject.getInt("strokeWidth"));
                pad_canvas.drawFromServer(dataJsonObject);
            }
        }
        if (!dataJsonObject.isNull("clearpage") && !dataJsonObject.get("clearpage").equals("")) {
            pad_canvas.clearHistory();
            padCanvasCleared = true;
        }
        if (!dataJsonObject.isNull("background")) {
            String background = dataJsonObject.getString("background");
            if (background.contains("graph")) {
                pad_canvas.setBackground(videoStreamContext.getResources().getDrawable(R.drawable.trygraph));
            } else {
                pad_canvas.setBackgroundResource((R.color.actual_white));
            }
        }
    }

    public static void drawOnPdf(JSONObject dataJsonObject) throws JSONException {
        pdfCanvasCleared = false;
        int sessionId = dataJsonObject.getInt("sessionId");

        if (!pdfCurpage.getText().toString().trim().equals(String.valueOf(sessionId))) {
            pdf_canvas.clearHistory();
            pdfCanvasCleared = true;
        }

        if (!dataJsonObject.isNull("color") && !dataJsonObject.getString("color").equals("")) {
            String color = dataJsonObject.getString("color");
            if (color.contains("white")) {
                pdf_canvas.setDrawingMode(DrawingMode.DRAW.ERASER);
                pdf_canvas.setDrawWidth(40);
                pdf_canvas.drawFromServer(dataJsonObject);
            } else {
                pdf_canvas.setDrawingMode(DrawingMode.DRAW);
                pdf_canvas.setDrawWidth(dataJsonObject.getInt("strokeWidth"));
                pdf_canvas.drawFromServer(dataJsonObject);
            }
        }
        if (!dataJsonObject.isNull("clearpage") && !dataJsonObject.get("clearpage").equals("")) {
            pdf_canvas.clearHistory();
            pdfCanvasCleared = true;
        }
    }

    public static void addMsgToChat(JSONObject finalJsonResponse, RealmChat realmChat) throws JSONException, ParseException {
        for (int i = 0; i < objects.size(); i++) {
            Object obj = objects.get(i);
            if (obj instanceof RealmChat) {

                if (((RealmChat) obj).getChatid().equals(finalJsonResponse.getJSONObject("data").getJSONObject("chat").getString("chatid"))) {
                    objects.set(i, realmChat);
                    chatAdapter.notifyItemChanged(i);
                    mLinearLayoutManager.scrollToPositionWithOffset(objects.size() - 1, 0);
                    return;
                }
            }
        }


        long time = 0;
        long prevTime = 0;
        String dateString = realmChat.getCreated_at();
        String prevDateString = null;
        if (dateString != null) {
            time = dateFormat.parse(dateString).getTime();
        }

        String formattedDate = getFormattedDate(videoStreamContext, time);
        String prevFormattedDate = null;

        if (objects.size() == 0) {
            if (!formattedDate.equals("January 1, 1970")) {
                objects.add(new DateItem(formattedDate));
            }
        } else {
            prevDateString = ((RealmChat) objects.get(objects.size() - 1)).getCreated_at();
            if (prevDateString != null) {
                prevTime = dateFormat.parse(prevDateString).getTime();
                prevFormattedDate = getFormattedDate(videoStreamContext, prevTime);
            }
            if (prevFormattedDate != null && !prevFormattedDate.equals(formattedDate)) {
                if (!formattedDate.equals("January 1, 1970")) {
                    objects.add(new DateItem(formattedDate));
                }
            }
        }


        objects.add(realmChat);
        chatAdapter.notifyItemChanged(objects.size() - 1);
        mLinearLayoutManager.scrollToPositionWithOffset(objects.size() - 1, 0);
    }

    public void populateObjects(Context context) {
        Realm.init(context);
        Realm.getInstance(RealmUtility.getDefaultConfig(videoStreamContext)).executeTransaction(realm -> {
            RealmResults<RealmChat> results = realm.where(RealmChat.class).equalTo("instructorcourseid", INSTRUCTORCOURSEID)
                    .sort("created_at", Sort.ASCENDING)
                    .findAll();
            if (results.size() > 0) {
                statusMsg.setVisibility(View.GONE);
            }
            newObjects.clear();
            for (int i = 0; i < results.size(); i++) {
                RealmChat realmChat = results.get(i);

                RealmUser realmUser = Realm.getInstance(RealmUtility.getDefaultConfig(videoStreamContext)).where(RealmUser.class).equalTo("userid", realmChat.getSenderid()).findFirst();
                if (realmUser == null) {
                    continue;
                }
                if (realmUser.getRole().equals("student")) {
                    RealmStudent realmStudent = Realm.getInstance(RealmUtility.getDefaultConfig(videoStreamContext)).where(RealmStudent.class).equalTo("infoid", realmChat.getSenderid()).findFirst();
                    if (realmStudent.getFirstname() != null && !realmStudent.getFirstname().trim().equals("")) {
                        realmChat.setName(realmStudent.getFirstname());
                    } else {
                        String phonenumber = realmUser.getPhonenumber();
                        realmChat.setName(phonenumber);
                    }
                } else if (realmUser.getRole().equals("instructor")) {
                    realmChat.setInstructor(true);
                    RealmInstructor realmInstructor = Realm.getInstance(RealmUtility.getDefaultConfig(videoStreamContext)).where(RealmInstructor.class).equalTo("infoid", realmChat.getSenderid()).findFirst();
                    if (realmInstructor.getFirstname() != null && !realmInstructor.getFirstname().trim().equals("")) {
                        realmChat.setName(realmInstructor.getFirstname());
                    } else {
                        realmChat.setName(realmUser.getPhonenumber());
                    }
                }

                setChatRefParams(realmChat, context);

                if (results.size() > 0) {
                    try {
                        long time = 0;
                        long prevTime = 0;
                        String dateString = realmChat.getCreated_at();
                        String prevDateString = null;
                        if (dateString != null) {
                            time = dateFormat.parse(dateString).getTime();
                        }

                        String formattedDate = getFormattedDate(context, time);
                        String prevFormattedDate = null;

                        if (i == 0) {
                            if (formattedDate != null && !formattedDate.equals("January 1, 1970")) {
                                newObjects.add(new DateItem(formattedDate));
                            }
                        } else {
                            prevDateString = results.get(i - 1).getCreated_at();
                            if (prevDateString != null) {
                                prevTime = dateFormat.parse(prevDateString).getTime();
                                prevFormattedDate = getFormattedDate(context, prevTime);
                            }
                            if (prevFormattedDate != null && !prevFormattedDate.equals(formattedDate)) {
                                if (formattedDate != null && !formattedDate.equals("January 1, 1970")) {
                                    newObjects.add(new DateItem(formattedDate));
                                }
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                newObjects.add(realmChat);
            }
        });
    }

    public void sendUnsentChats() {
        if (videoStreamActivity != null) {
            Realm.init(videoStreamContext);
            Realm.getInstance(RealmUtility.getDefaultConfig(videoStreamContext)).executeTransaction(realm -> {
                RealmResults<RealmChat> results = realm.where(RealmChat.class)
                        .equalTo("instructorcourseid", INSTRUCTORCOURSEID)
                        .isNull("created_at")
                        .findAll();
                for (RealmChat realmChat : results) {
                    JSONObject jsonObject = null;
                    RealmChat recreatedRealChat = new RealmChat(
                            realmChat.getChatid(),
                            realmChat.getChatrefid(),
                            realmChat.getTempid(),
                            realmChat.getText(),
                            realmChat.getLink(),
                            realmChat.getLinktitle(),
                            realmChat.getLinkdescription(),
                            realmChat.getLinkimage(),
                            realmChat.getAttachmenturl(),
                            realmChat.getAttachmenttype(),
                            realmChat.getAttachmenttitle(),
                            realmChat.getReadbyrecepient(),
                            realmChat.getInstructorcourseid(),
                            realmChat.getSenderid(),
                            realmChat.getRecepientid(),
                            realmChat.getCreated_at(),
                            realmChat.getUpdated_at()
                    );
                    if (realmChat.getAttachmenturl() != null && realmChat.getAttachmenturl().startsWith("URI")) {
                        new SendChatAttachmentAsyncTask(videoStreamContext, recreatedRealChat).execute();
                    } else {
                        try {
                            String json_string = new Gson().toJson(recreatedRealChat);
                            jsonObject = new JSONObject(json_string);
                            SendChatMsg(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }

    public void saveTempChatToRealm(RealmChat realmChat) {
        Realm.init(videoStreamContext);
        Realm.getInstance(RealmUtility.getDefaultConfig(videoStreamContext)).executeTransaction(realm -> {
            setChatRefParams(realmChat, videoStreamContext);
            RealmChat chat = realm.where(RealmChat.class)
                    .equalTo("instructorcourseid", INSTRUCTORCOURSEID)
                    .sort("created_at", Sort.DESCENDING)
                    .findFirst();
            if (chat == null) {
                realmChat.setCreated_at("z1975-05-05 14:24:16");
            } else {
                String created_at = chat
                        .getCreated_at();
                if (String.valueOf(created_at.charAt(0)).toLowerCase().equals("z")) {
                    created_at = created_at.substring(1);
                }
                try {
                    realmChat.setCreated_at("z" + Const.dateTimeFormat.format(Const.dateTimeFormat.parse(created_at).getTime() + 1));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            realm.copyToRealmOrUpdate(realmChat);
            statusMsg.setVisibility(View.GONE);
            objects.add(realmChat);
            chatAdapter.notifyItemInserted(objects.size() - 1);
            mLinearLayoutManager.scrollToPositionWithOffset(objects.size() - 1, 0);
            if (realmChat.getAttachmenturl() != null) {
                Toast.makeText(videoStreamContext, videoStreamContext.getString(R.string.uploading_file), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void setChatRefParams(RealmChat realmChat, Context context) {
        if (realmChat.getChatrefid() != null) {
            RealmChat realmReferencedChat = Realm.getInstance(RealmUtility.getDefaultConfig(videoStreamContext)).where(RealmChat.class).equalTo("chatid", realmChat.getChatrefid()).findFirst();
            realmChat.setReplybody(realmReferencedChat.getText() != null ? realmReferencedChat.getText() : realmReferencedChat.getAttachmenttitle());

            if (!realmReferencedChat.getSenderid().equals(PreferenceManager.getDefaultSharedPreferences(context).getString(MYUSERID, ""))) {
                RealmUser referencedChatUser = Realm.getInstance(RealmUtility.getDefaultConfig(videoStreamContext))
                        .where(RealmUser.class)
                        .equalTo("userid", realmReferencedChat.getSenderid())
                        .findFirst();
                RealmStudent realmStudent = null;
                RealmInstructor realmInstructor = null;
                if (referencedChatUser.getRole().equals("student")) {
                    realmStudent = Realm.getInstance(RealmUtility.getDefaultConfig(videoStreamContext))
                            .where(RealmStudent.class)
                            .equalTo("infoid", realmReferencedChat.getSenderid())
                            .findFirst();
                } else if (referencedChatUser.getRole().equals("instructor")) {
                    realmInstructor = Realm.getInstance(RealmUtility.getDefaultConfig(videoStreamContext))
                            .where(RealmInstructor.class)
                            .equalTo("infoid", realmReferencedChat.getSenderid())
                            .findFirst();
                }
                RealmUser realmUser = Realm.getInstance(RealmUtility.getDefaultConfig(videoStreamContext)).where(RealmUser.class).equalTo("userid", realmReferencedChat.getSenderid()).findFirst();
                if (realmStudent != null) {
                    realmChat.setReplyname(realmStudent.getFirstname() != null ? realmStudent.getFirstname() : realmUser.getPhonenumber());
                } else if (realmInstructor != null) {
                    realmChat.setReplyname(realmInstructor.getFirstname() != null ? realmInstructor.getFirstname() : realmUser.getPhonenumber());
                }
            } else {
                realmChat.setReplyname(context.getString(R.string.me));
            }
        }
    }

    private boolean isDeleteIconShowable() {
        for (int i = 0; i < multiselect_list.size(); i++) {
            String created_at = ((RealmChat) multiselect_list.get(i)).getCreated_at();
            if (!created_at.startsWith("z")) {
                return false;
            }
        }
        return true;
    }

    public void broadcastWithSocket(String result) {
        if (isNetworkAvailable(videoStreamContext)) {

            if (chatSocket.getState() == Socket.State.OPEN) {
                if (chatSocket != null) {
                    chatSocket.send("chat:" + INSTRUCTORCOURSEID, result);
                }
            }
        }
    }

    public class broadcastWithFirebase extends AsyncTask<Void, Integer, String> {


        // private ProgressDialog progressDialog;
        private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
        private final String[] SCOPES = {MESSAGING_SCOPE};
        JSONObject chatJson = null;

        public broadcastWithFirebase(JSONObject chatJson) {
            this.chatJson = chatJson;
        }

        @Override
        protected String doInBackground(Void... params) {
            JSONObject jsonObject = null;
            try {
                String[] split = COURSEPATH.split((" >> "));
                jsonObject = new JSONObject().put(
                        "message", new JSONObject()
                                .put("topic", INSTRUCTORCOURSEID)
                                /*.put("notification", new JSONObject()
                                        .put("body", jsonObject.getJSONObject("chat").has("attachmenturl") ? "Attachment" : jsonObject.getJSONObject("chat").getString("text"))
                                        .put("title", PreferenceManager.getDefaultSharedPreferences(chatActivity).getString(COURSEPATH, "") + " Chat")
                                )*/
                                .put("data", new JSONObject()
                                        .put("type", "chat")
                                        .put("chatresponse", chatJson.toString())
                                        .put("coursepath", COURSEPATH)
                                        .put("body", chatJson.getJSONObject("chat").has("attachmenturl") ? "Attachment" : chatJson.getJSONObject("chat").getString("text"))
                                        .put("title", "Chat message from " + split[split.length - 1] + " class")
                                )
                );
            } catch (JSONException e) {
                e.printStackTrace();
            }


            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
            String content = jsonObject.toString();
            RequestBody body = RequestBody.create(mediaType, content);
            Request request = new Request.Builder()

                    .url("https://fcm.googleapis.com/v1/projects/instructorapp-c6c95/messages:send")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(videoStreamActivity).getString(ACCESSTOKEN, ""))
                    .build();
            try {
                okhttp3.Response response = client.newCall(request).execute();
                String s = response.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {

            // Init and show dialog

        }

        @Override
        protected void onPostExecute(String result) {

        }
    }

    public void populateParticipants(Context context) {
        newParticipants.clear();
        Realm.init(context);
        Realm.getInstance(RealmUtility.getDefaultConfig(videoStreamContext)).executeTransaction(realm -> {

            RealmInstructorCourse realmInstructorCourse = realm.where(RealmInstructorCourse.class)
                    .equalTo("instructorcourseid", INSTRUCTORCOURSEID)
                    .findFirst();
            RealmInstructor realmInstructor = realm.where(RealmInstructor.class).equalTo("infoid", realmInstructorCourse.getInstructorid()).findFirst();
            newParticipants.add(new Participant(realmInstructor.getInfoid(), StringUtils.normalizeSpace((realmInstructor.getTitle() + " " + realmInstructor.getFirstname() + " " + realmInstructor.getOthername() + " " + realmInstructor.getLastname()).replace("null", "")), realmInstructor.getProfilepicurl(), true, realmInstructorCourse.getConnectedtovideo()));

            RealmResults<RealmEnrolment> results = realm.where(RealmEnrolment.class)
                    .equalTo("instructorcourseid", INSTRUCTORCOURSEID)
                    .findAll()
                    .sort("id", Sort.DESCENDING);
            for (RealmEnrolment realmEnrolment : results) {
                RealmStudent realmStudent = realm.where(RealmStudent.class).equalTo("infoid", realmEnrolment.getStudentid()).findFirst();
                newParticipants.add(new Participant(realmStudent.getInfoid(), StringUtils.normalizeSpace((realmStudent.getTitle() + " " + realmStudent.getFirstname() + " " + realmStudent.getOthername() + " " + realmStudent.getLastname()).replace("null", "")), realmStudent.getProfilepicurl(), false, realmEnrolment.getConnectedtovideo()));
            }
        });
    }

    public void setPdf(String docurl, JSONObject coordinateJson) {
        if (docurl != null && !docurl.replace("null", "").trim().equals("")) {
            docFile = new File(getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Class-sessions/Documents/", URLUtil.guessFileName(docurl, null, null));
            if (docFile.exists()) {
                File pdfBackgoundFile = new File(videoStreamContext.getFilesDir() + "/SchoolDirectStudent/PDF", "PDF-temp" + ".jpg");

                new GenerateImageFromPdfAsync(() -> {
                    if (pdfBackgoundFile.exists()) {
                        pdfBackgoundFile.delete();
                    }
                },
                        bitmap -> {
                            if (bitmap != null) {
                                pdfpageCount = efficientPDFPageCount(docFile);
                                pdfTotalPages.setText(String.valueOf(pdfpageCount));
                                pdfCanvasCleared = false;

                                if (coordinateJson != null) {
                                    pdf_canvas.clearHistory();
                                    pdfCanvasCleared = true;
                                    Drawable drawable = new BitmapDrawable(getResources(), bitmap);

                                    imgview.setVisibility(View.VISIBLE);
                                    imgview.setImageDrawable(drawable);

                                    final int imageViewHeight = imgview.getHeight(), imageViewWidth = imgview.getWidth();
                                    final int bitmapHeight = drawable.getIntrinsicHeight(), bitmapWidth = drawable.getIntrinsicWidth();

                                    int actualWidth;
                                    int actualHeight;
                                    if (imageViewHeight * bitmapWidth <= imageViewWidth * bitmapHeight) {
                                        actualWidth = bitmapWidth * imageViewHeight / bitmapHeight;
                                        actualHeight = imageViewHeight;
                                    } else {
                                        actualHeight = bitmapHeight * imageViewWidth / bitmapWidth;
                                        actualWidth = imageViewWidth;
                                    }
                                    pdf_canvas.setLayoutParams(new FrameLayout.LayoutParams(actualWidth, actualHeight));
                                    pdf_canvas.setBackground(drawable);
                                    imgview.setVisibility(View.GONE);
                                    redrawOnPdf(pdfPageNum);
                                }
                            }


                        }, progress -> {

                }, () -> {

                }, videoStreamActivity, Uri.fromFile(docFile), pdfPageNum, pdfBackgoundFile).execute();
            } else {
                File parentFile = docFile.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                downloadFileAsync = new DownloadFileAsync(download_response -> {
                    if (download_response != null) {
                        // unsuccessful
                        if (docFile.exists()) {
                            docFile.delete();
                        }
                    } else {
                        // successful
                        searchView.setVisibility(View.GONE);

                        File pdfBackgoundFile = new File(videoStreamContext.getFilesDir() + "/SchoolDirectStudent/PDF", "PDF-temp" + ".jpg");

                        new GenerateImageFromPdfAsync(new GenerateImageFromPdfAsync.OnTaskPreExecuteInterface() {
                            @Override
                            public void onPreExecute() {
                                if (pdfBackgoundFile.exists()) {
                                    pdfBackgoundFile.delete();
                                }
                            }
                        },
                                new GenerateImageFromPdfAsync.OnTaskCompletedInterface() {
                                    @Override
                                    public void onTaskCompleted(Bitmap bitmap) {
                                        if (bitmap != null) {
                                            pdfpageCount = efficientPDFPageCount(docFile);
                                            pdfTotalPages.setText(String.valueOf(pdfpageCount));
                                            pdfCanvasCleared = false;

                                            if (coordinateJson != null) {
                                                pdf_canvas.clearHistory();
                                                pdfCanvasCleared = true;
                                                Drawable drawable = new BitmapDrawable(getResources(), bitmap);

                                                imgview.setVisibility(View.VISIBLE);
                                                imgview.setImageDrawable(drawable);

                                                final int imageViewHeight = imgview.getHeight(), imageViewWidth = imgview.getWidth();
                                                final int bitmapHeight = drawable.getIntrinsicHeight(), bitmapWidth = drawable.getIntrinsicWidth();

                                                int actualWidth;
                                                int actualHeight;
                                                if (imageViewHeight * bitmapWidth <= imageViewWidth * bitmapHeight) {
                                                    actualWidth = bitmapWidth * imageViewHeight / bitmapHeight;
                                                    actualHeight = imageViewHeight;
                                                } else {
                                                    actualHeight = bitmapHeight * imageViewWidth / bitmapWidth;
                                                    actualWidth = imageViewWidth;
                                                }
                                                pdf_canvas.setLayoutParams(new FrameLayout.LayoutParams(actualWidth, actualHeight));
                                                pdf_canvas.setBackground(drawable);
                                                imgview.setVisibility(View.GONE);

                                                redrawOnPdf(pdfPageNum);
                                            }
                                        }
                                    }
                                }, new GenerateImageFromPdfAsync.OnTaskProgressUpdateInterface() {

                            @Override
                            public void onTaskProgressUpdate(int progress) {

                            }
                        }, new GenerateImageFromPdfAsync.OnTaskCancelledInterface() {

                            @Override
                            public void onTaskCancelled() {

                            }
                        }, videoStreamActivity, Uri.fromFile(docFile), pdfPageNum, pdfBackgoundFile).execute();
                    }
                }, progress -> {
                }, () -> {
                    if (docFile.exists()) {

                        docFile.delete();
                    }
                }).execute(docurl, docFile.getAbsolutePath());

            }
        } else {
            downbtn.setVisibility(View.GONE);
            upbtn.setVisibility(View.GONE);
            searchView.setVisibility(View.VISIBLE);
        }
    }

    public void onPlayInit() {
        try {
            for (int i = 0; i < drawingCoordinatesJsonArray.length(); i++) {
                try {
                    boolean pdfCoordinate = drawingCoordinatesJsonArray.getJSONObject(i).has("ispdf") && drawingCoordinatesJsonArray.getJSONObject(i).getInt("ispdf") == 1;
                    int sessionId = drawingCoordinatesJsonArray.getJSONObject(i).getInt("sessionId");
                    if (!pdfCoordinate && sessionId > padpageCount) {
                        padpageCount = sessionId;
                        padTotalPages.setText(String.valueOf(padpageCount));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            pad_canvas.clearHistory();
            padCanvasCleared = true;

            pdf_canvas.clearHistory();
            pdfCanvasCleared = true;

            JSONObject drawingCoordinate;
            if (drawingCoordinatesJsonArray.length() == 0) {
                return;
            } else {
                drawingCoordinate = drawingCoordinatesJsonArray.getJSONObject(drawingCoordinatesJsonArray.length() - 1);
            }
            if (drawingCoordinate.has("ispdf") && drawingCoordinate.getInt("ispdf") == 1) {
                pad.setVisibility(View.VISIBLE);
                pdf.setVisibility(View.GONE);

                pad_canvas.setVisibility(View.INVISIBLE);
                pdf_canvas.setVisibility(View.VISIBLE);
                zoom_layout.setVisibility(View.VISIBLE);

                padPagePanel.setVisibility(View.GONE);
                pdfPagePanel.setVisibility(View.VISIBLE);


                docFile = new File(getFilesDir() + "/SchoolDirectStudent/" + COURSEPATH.replace(" >> ", "/") + "/Class-sessions/Documents/", URLUtil.guessFileName(drawingCoordinate.getString("pdfpath"), null, null));

                pdfpageCount = efficientPDFPageCount(docFile);

                pdfTotalPages.setText(String.valueOf(pdfpageCount));

                try {
                    pdfPageNum = drawingCoordinate.getInt("sessionId");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                pdf_canvas.clearHistory();
                pdfCanvasCleared = true;
                pdfCurpage.setText(String.valueOf(pdfPageNum));
                redrawOnPdf(pdfPageNum);
            } else {
                pad.setVisibility(View.GONE);
                pdf.setVisibility(View.VISIBLE);

                pad_canvas.setVisibility(View.VISIBLE);
                pdf_canvas.setVisibility(View.INVISIBLE);
                zoom_layout.setVisibility(View.INVISIBLE);

                padPagePanel.setVisibility(View.VISIBLE);
                pdfPagePanel.setVisibility(View.GONE);

                padPageNum = drawingCoordinate.getInt("sessionId");
                pad_canvas.clearHistory();
                padCanvasCleared = true;
                padCurpage.setText(String.valueOf(padPageNum));
                redrawOnPad(padPageNum);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void updateParticipantConnectionStatus(JSONObject finalJsonResponse, Realm realm, int connected) throws JSONException {
        String connected_type = "";
        if (connected == 1) {
            connected_type = "connected_video";
        } else {
            connected_type = "disconnected_video";
        }
        RealmUser realmUser = realm.where(RealmUser.class).equalTo("userid", finalJsonResponse.getJSONObject("data").getString(connected_type)).findFirst();
        if (realmUser == null) {
            return;
        }
        String role = realmUser.getRole();
        if (role.equals("student")) {
            RealmEnrolment realmEnrolment = realm.where(RealmEnrolment.class).equalTo("studentid", finalJsonResponse.getJSONObject("data").getString(connected_type)).findFirst();
            realmEnrolment.setConnectedtovideo(connected);
            RealmStudent realmStudent = realm.where(RealmStudent.class).equalTo("infoid", realmEnrolment.getStudentid()).findFirst();
            newParticipants.add(new Participant(realmStudent.getInfoid(), StringUtils.normalizeSpace((realmStudent.getTitle() + " " + realmStudent.getFirstname() + " " + realmStudent.getOthername() + " " + realmStudent.getLastname()).replace("null", "")), realmStudent.getProfilepicurl(), false, realmEnrolment.getConnectedtovideo()));
            for (int i = 0; i < participants.size(); i++) {
                if (participants.get(i).getParticipantid().equals(realmStudent.getInfoid())) {
                    participants.set(i, new Participant(realmStudent.getInfoid(), StringUtils.normalizeSpace((realmStudent.getTitle() + " " + realmStudent.getFirstname() + " " + realmStudent.getOthername() + " " + realmStudent.getLastname()).replace("null", "")), realmStudent.getProfilepicurl(), false, realmEnrolment.getConnectedtovideo()));
                    participantsAdapter.notifyItemChanged(i);
                    break;
                }
            }
        } else if (role.equals("instructor")) {
            RealmInstructorCourse realmInstructorCourse = realm.where(RealmInstructorCourse.class).equalTo("instructorid", finalJsonResponse.getJSONObject("data").getString(connected_type)).findFirst();
            realmInstructorCourse.setConnectedtovideo(connected);
            RealmInstructor realmInstructor = realm.where(RealmInstructor.class).equalTo("infoid", realmInstructorCourse.getInstructorid()).findFirst();
            for (int i = 0; i < participants.size(); i++) {
                if (participants.get(i).getParticipantid().equals(realmInstructor.getInfoid())) {
                    participants.set(i, new Participant(realmInstructor.getInfoid(), StringUtils.normalizeSpace((realmInstructor.getTitle() + " " + realmInstructor.getFirstname() + " " + realmInstructor.getOthername() + " " + realmInstructor.getLastname()).replace("null", "")), realmInstructor.getProfilepicurl(), true, realmInstructorCourse.getConnectedtovideo()));
                    participantsAdapter.notifyItemChanged(i);
                    break;
                }
            }
        }
    }
}
