package com.univirtual.student.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.potyvideo.library.AndExoPlayerView;
import com.univirtual.student.R;
import com.univirtual.student.other.InitApplication;
import com.univirtual.student.realm.RealmPastQuestion;
import com.univirtual.student.receiver.NetworkReceiver;
import com.univirtual.student.util.RealmUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import uk.co.senab.photoview.PhotoViewAttacher;

import static com.univirtual.student.activity.HomeActivity.APITOKEN;
import static com.univirtual.student.constants.keyConst.API_URL;
import static com.univirtual.student.constants.Const.myVolleyError;
import static com.univirtual.student.receiver.NetworkReceiver.activeActivity;

public class PastQuestionsActivity extends AppCompatActivity{

    String TITLE;
    String INSTRUCTORCOURSEID;
    String COURSEPATH;
    String ENROLMENTID;
    String PROFILEIMGURL;
    String ROOMID;
    String NODESERVER;
    String INSTRUCTORNAME;
    String SESSIONID;

    String title = "";
    int total;

    private ScrollView scrollView;
    private TextView mScoreView, titleText;
    private ImageView mQuestionView;
    private LinearLayout linearLayout;
    private TextView why;
    private LinearLayout fullscreen;
    private int mScore = 0;
    public static int mQuestionNumber = 1;
    RadioGroup rg;
    RadioButton rb;
    LinearLayout next, prev;
    ImageView backbtn;
    private String YOUR_DIALOG_TAG = "";
    private AndExoPlayerView player;
    public static int VIDEOVISIBILITY = View.GONE;

    Activity mActivity;
    private ProgressDialog progressDialog;

    static RealmPastQuestion question;
    HashMap<String, Integer> resultsMap = new HashMap<>();
    NetworkReceiver networkReceiver;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_questions);

        mActivity = PastQuestionsActivity.this;

        String instructorcourseid = getIntent().getStringExtra("INSTRUCTORCOURSEID");
        String coursepathstring = getIntent().getStringExtra("COURSEPATH");
        String enrolmentid = getIntent().getStringExtra("ENROLMENTID");
        String profileimgurl = getIntent().getStringExtra("PROFILEIMGURL");
        String nodeserver = getIntent().getStringExtra("NODESERVER");
        String roomid = getIntent().getStringExtra("ROOMID");


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

        networkReceiver = new NetworkReceiver();

        progressDialog = new ProgressDialog(mActivity);
        progressDialog.setTitle("Loading question");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);

        Realm.init(this);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras.containsKey("title")) {
            title = extras.getString("title");
        }

        scrollView = findViewById(R.id.scrollView);
        fullscreen = findViewById(R.id.fullscreen);
        next = findViewById(R.id.next);
        prev = findViewById(R.id.prev);
        linearLayout = findViewById(R.id.add);
        why = findViewById(R.id.why);
        player = findViewById(R.id.player);
        rg = findViewById(R.id.radio_group);
        mQuestionView = findViewById(R.id.questionImg);
        titleText = findViewById(R.id.titleText);

        backbtn = findViewById(R.id.search);

        backbtn.setOnClickListener(v -> finish());


        /*fullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VideoActivity.AUDIO_URL = question.getVideopath();
                Intent intent = new Intent(getApplicationContext(), VideoActivity.class);
                intent.putExtra("SEEK_POSITION", player.getCurrentPosition());
                startActivity(intent);
            }
        });*/

        player.onClick(player);


        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.stopPlayer();
                why.setText("Why?");
                //player.pause();
                VIDEOVISIBILITY = View.GONE;
                player.setVisibility(VIDEOVISIBILITY);
                mQuestionNumber = mQuestionNumber - 1;
                updateQuestion_prev();
                rg.clearCheck();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.stopPlayer();
                why.setText("Why?");
                //player.pause();
                VIDEOVISIBILITY = View.GONE;
                player.setVisibility(VIDEOVISIBILITY);
                mQuestionNumber = mQuestionNumber + 1;
                updateQuestion_next();
                rg.clearCheck();
            }
        });

        why.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (why.getText().toString().equals("Why?")) {
                    VIDEOVISIBILITY = View.VISIBLE;
                    why.setText("Click to hide answer");
                    if (question.getVideopath() == null || question.getVideopath().trim().equals("")) {
                        player.setSource(null);
                        Toast.makeText(mActivity, "Video not available.", Toast.LENGTH_SHORT).show();
                    } else {
                        player.setSource(question.getVideopath());
                    }
                    if (scrollView != null) {
                        Handler h = new Handler();

                        h.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                scrollView.smoothScrollTo(0, scrollView.getBottom());
                            }
                        }, 250); // 250 ms delay
                    }
                } else {
                    VIDEOVISIBILITY = View.GONE;
                    why.setText("Why?");
                    player.stopPlayer();
                }
                player.setVisibility(VIDEOVISIBILITY);
            }
        });
        //mScoreView = (TextView)findViewById(R.id.score);


        PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(mQuestionView);
        photoViewAttacher.setScale((float) 1.5);


        // savedInstanceState != null ===>>> possible orientation change
        if (VIDEOVISIBILITY == View.VISIBLE) {
            why.setText("Click to hide answer");
            player.setSource(question.getVideopath());

            if (scrollView != null) {
                Handler h = new Handler();

                h.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        scrollView.smoothScrollTo(0, scrollView.getBottom());
                    }
                }, 250); // 250 ms delay
            }
        } else {
            why.setText("Why?");
            player.stopPlayer();
        }
        player.setVisibility(VIDEOVISIBILITY);

        rg.setVisibility(View.GONE);
        prev.setVisibility(View.GONE);

        updateQuestion_next();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activeActivity = this;
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkReceiver);
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


    public static void saveToRealm(final RealmPastQuestion realmPastQuestion) {
        final Realm realm = Realm.getDefaultInstance();

        realm.executeTransactionAsync(new Realm.Transaction() {
                                          @Override
                                          public void execute(Realm mRealm) {
                                              // mRealm.createObject(RealmPastQuestion.class, clickedAgent);
                                              Number num = mRealm.where(RealmPastQuestion.class).max("id");
                                              int id = 0;
                                              if (num == null) {
                                                  id = 1;
                                              } else {
                                                  id = num.intValue() + 1;
                                              }
                                              realmPastQuestion.setId(id);
                                              mRealm.copyToRealmOrUpdate(realmPastQuestion);
                                          }
                                      },
                new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        realm.close();
                    }
                }, new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        Log.d("asd", error.toString());
                    }
                }
        );
    }

    private int getCorrectAnswerIndex() {
        int index = 0;
        switch (question.getAnswer()) {
            case "A":
                index = 0;
                break;
            case "B":
                index = 1;
                break;
            case "C":
                index = 2;
                break;
            case "D":
                index = 3;
                break;
            default:
                break;
        }
        return index;
    }

    private void initializeRadioButtons() {
        int count = rg.getChildCount();

        for (int i = 0; i < count; i++) {
            View o = rg.getChildAt(i);
            if (o instanceof RadioButton) {
                //((RadioButton) o).setTextColor(Color.BLACK);
                ((RadioButton) o).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
        }
    }

    private void onNextClickInit() {

        initializeRadioButtons();
        if (question.getIsfirstquestion() == 1) {
            prev.setVisibility(View.GONE);
        } else {
            prev.setVisibility(View.VISIBLE);
        }

        if (question.getIslastquestion() == 1) {
            next.setVisibility(View.GONE);
        } else {
            next.setVisibility(View.VISIBLE);
        }

        if (question.getIsendofquestion() == 1) {
            why.setVisibility(View.VISIBLE);
            if (question.getIsmcq() == 1) {
                rg.setVisibility(View.VISIBLE);
            } else {
                rg.setVisibility(View.GONE);
            }
        } else {
            why.setVisibility(View.GONE);
            rg.setVisibility(View.GONE);
        }

        if (question.getIslastmcq() == 1) {
            mScore = 0;
            Collection<Integer> values = resultsMap.values();
            for (Integer value : values) {
                mScore += value;
            }
            Toast toast = Toast.makeText(getApplicationContext(),
                    "You answered " + String.valueOf(mScore) + " out of " + String.valueOf(resultsMap.size()) + " multiple choice questions correctly.", Toast.LENGTH_LONG);

            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    private void onPrevClickInit() {

        initializeRadioButtons();
        if (question.getIslastquestion() == 1) {
            next.setVisibility(View.GONE);
        } else {
            next.setVisibility(View.VISIBLE);
        }

        if (question.getIsfirstquestion() == 1) {
            prev.setVisibility(View.GONE);
        } else {
            prev.setVisibility(View.VISIBLE);
        }

        if (question.getIsendofquestion() == 1) {
            if (question.getIsmcq() == 1) {
                rg.setVisibility(View.VISIBLE);
                why.setVisibility(View.VISIBLE);
            } else {
                rg.setVisibility(View.GONE);
                why.setVisibility(View.GONE);
            }

        } else {
            why.setVisibility(View.GONE);
            rg.setVisibility(View.GONE);
        }
    }

    private void updateQuestion_next() {

        final String questionid = title + " >> " + mQuestionNumber;
        question = null;

        Realm.init(getApplicationContext());
        Realm.getInstance(RealmUtility.getDefaultConfig(PastQuestionsActivity.this)).executeTransaction(realm -> {
            question = realm.where(RealmPastQuestion.class).equalTo("questionid", questionid).findFirst();
        });
        if (question == null) {
            progressDialog.show();

            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    API_URL + "fetch-question",
                    response -> {
                        progressDialog.dismiss();
                        if (response != null) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                Realm.init(getApplicationContext());
                                Realm.getInstance(RealmUtility.getDefaultConfig(PastQuestionsActivity.this)).executeTransaction(realm -> {
                                    question = realm.createOrUpdateObjectFromJson(RealmPastQuestion.class, jsonObject);
                                    byte[] decodedBytes = Base64.decode(question.getPicture(), 0);
                                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                    mQuestionView.setImageBitmap(decodedBitmap);
                                    titleText.setText(questionid);
                                    onNextClickInit();
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            mQuestionView.setImageBitmap(null);
                        }
                    },
                    error -> {
                        progressDialog.dismiss();
                        error.printStackTrace();
                        myVolleyError(getApplicationContext(), error);
                    }
            )
            {
                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("questionid", questionid);
                    return params;
                }
                @Override
                public Map getHeaders() throws AuthFailureError {
                    HashMap headers = new HashMap();
                    headers.put("accept", "application/json");
                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(APITOKEN, ""));
                    return headers;
                }
            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(stringRequest);
        } else {
            byte[] decodedBytes = Base64.decode(question.getPicture(), 0);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            mQuestionView.setImageBitmap(decodedBitmap);
            titleText.setText(questionid);

            onNextClickInit();
        }
    }

    private void updateQuestion_prev() {
        final String questionid = title + " >> " + mQuestionNumber;
        question = null;
        Realm.init(getApplicationContext());
        Realm.getInstance(RealmUtility.getDefaultConfig(PastQuestionsActivity.this)).executeTransaction(realm -> {
            question = realm.where(RealmPastQuestion.class).equalTo("questionid", questionid).findFirst();
        });
        if (question == null) {
            progressDialog.show();
            JsonObjectRequest jsonOblect = new JsonObjectRequest(
                    Request.Method.POST,
                    API_URL + "fetch-question",
                    null,
                    response -> {
                        progressDialog.dismiss();
                        if (response != null) {
                            Realm.init(getApplicationContext());
                            Realm.getInstance(RealmUtility.getDefaultConfig(PastQuestionsActivity.this)).executeTransaction(realm -> {
                                question = realm.createOrUpdateObjectFromJson(RealmPastQuestion.class, response);
                                byte[] decodedBytes = Base64.decode(question.getPicture(), 0);
                                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                mQuestionView.setImageBitmap(decodedBitmap);
                                titleText.setText(questionid);
                                onNextClickInit();
                            });
                        } else {
                            mQuestionView.setImageBitmap(null);
                        }
                    }, error -> {
                error.printStackTrace();
                Log.d("Cyrilll", error.toString());
                progressDialog.dismiss();
                myVolleyError(getApplicationContext(), error);
            })
            {
                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("questionid", questionid);
                    return params;
                }
                @Override
                public Map getHeaders() throws AuthFailureError {
                    HashMap headers = new HashMap();
                    headers.put("accept", "application/json");
                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(APITOKEN, ""));
                    return headers;
                }
            };
            jsonOblect.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(jsonOblect);
        } else {
            byte[] decodedBytes = Base64.decode(question.getPicture(), 0);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            mQuestionView.setImageBitmap(decodedBitmap);
            titleText.setText(questionid);

            onPrevClickInit();
        }
    }

    private void updateScore(int point) {
        if (mQuestionNumber == 0) {
            mScore = 0;
        }
        mScoreView.setText("" + mScore);
    }

    public void rbclick(View view) {
        if (question.getIsendofquestion() == 1 && question.getIsmcq() == 1) {
            initializeRadioButtons();
            int radiobuttonid = rg.getCheckedRadioButtonId();
            rb = findViewById(radiobuttonid);
            resultsMap.put(String.valueOf(mQuestionNumber), rb.getText().toString().trim().equals(question.getAnswer().trim()) ? 1 : 0);
            if (rb.getText().toString().trim().equals(question.getAnswer().trim())) {
                rb.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_tick, 0);
            } else {
                rb.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_clear_red_24dp, 0);

                ((RadioButton) rg.getChildAt(getCorrectAnswerIndex())).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_tick, 0);
            }
        }
    }
}
