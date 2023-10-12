package com.univirtual.student.activity;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.byox.enums.DrawingMode;
import com.byox.views.DrawView;
import com.univirtual.student.R;
import com.univirtual.student.util.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import static com.univirtual.student.constants.Const.isNetworkAvailable;
import static com.univirtual.student.util.Socket.EVENT_CLOSED;
import static com.univirtual.student.util.Socket.EVENT_OPEN;
import static com.univirtual.student.util.Socket.EVENT_RECONNECT_ATTEMPT;

public class Drawing extends AppCompatActivity {
    public static final String TAG = "drawing";

    ImageButton brush, ibEraser, ibSave, ibClear;
    String hostname = "41.189.178.19:1935/nanatv/android";
    public byte[] buffer;

    private int port = 1935;
    AudioRecord recorder;
    private boolean status = true;
    private int sampleRate = 48000;
    /**
     * 8000, 16000, 22050, 24000, 32000, 44100, 48000 Choose the best for the device and the bandwidth
     **/
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
    static Context context;
    static int pageNum = 1;
    static Socket socket;
    public static String geocode = "instructcourse-92738c8863895a50eb307ad018c561b9";
    private String drawingType = "drawing";
    ImageView previousBtn, nextBtn;
    static DrawView testCanvas;
    static TextView curpage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_stream);
        context = this;

        testCanvas = findViewById(R.id.pad_canvas);

        curpage = findViewById(R.id.padCurpage);
        previousBtn = findViewById(R.id.wbPrevBtn);
        nextBtn = findViewById(R.id.wbNextBtn);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (pageNum) {
                    case 5:
                        //drawingCanvas.drawFromServer(finalJsonResponse);
                        pageNum = 1;
                        curpage.setText(String.valueOf(1));
                        break;
                    case 1:
                        //   drawing_canvas2.drawFromServer(finalJsonResponse);
                        pageNum = 2;
                        curpage.setText(String.valueOf(2));
                        break;
                    case 2:
                        //    drawing_canvas3.drawFromServer(finalJsonResponse);
                        pageNum = 3;
                        curpage.setText(String.valueOf(3));
                        break;
                    case 3:
                        //    drawing_canvas4.drawFromServer(finalJsonResponse);
                        pageNum = 4;
                        curpage.setText(String.valueOf(4));
                        break;
                    case 4:
                        //   drawing_canvas5.drawFromServer(finalJsonResponse);
                        pageNum = 5;
                        curpage.setText(String.valueOf(5));
                        break;
                }
            }
        });
        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (pageNum) {
                    case 2:
                        //drawingCanvas.drawFromServer(finalJsonResponse);
                        pageNum = 1;
                        curpage.setText(String.valueOf(1));
                        break;
                    case 3:
                        //   drawing_canvas2.drawFromServer(finalJsonResponse);
                        pageNum = 2;
                        curpage.setText(String.valueOf(2));
                        break;
                    case 4:
                        //    drawing_canvas3.drawFromServer(finalJsonResponse);
                        pageNum = 3;
                        curpage.setText(String.valueOf(3));
                        break;
                    case 5:
                        //    drawing_canvas4.drawFromServer(finalJsonResponse);
                        pageNum = 4;
                        curpage.setText(String.valueOf(4));
                        break;
                    case 1:
                        //   drawing_canvas5.drawFromServer(finalJsonResponse);
                        pageNum = 5;
                        curpage.setText(String.valueOf(5));
                        break;
                }
            }
        });
        initSocket(geocode);
    }

    public static class BroadCast_Async extends AsyncTask<String, Void, String> {


        String response;

        String relativeUrl;

        long carouselSlideInterval = 3000; // 3 SECONDS
        Boolean b;
        String data;


        public BroadCast_Async(Boolean b, String data) {

            //    Log.i("AndyObeng","Cox");
            this.b = b;
            this.data = data;


        }

        @Override
        protected void onPreExecute() {
            // pBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            response = "";
            if (isNetworkAvailable(context)) {

                if (socket.getState() == Socket.State.OPEN) {
                    socket.send("chat:" + geocode, data);
                    if (b) {
                        socket.clearListeners();
                        socket.close();
                    }
                } else {
                    // initSocket();
//                Toast.makeText(context, "Socket connection error.", Toast.LENGTH_SHORT).show();
                }
            }

            return socket.getState().toString();
        }

        @Override
        protected void onPostExecute(String response) {
            // RealmResults<ILocation> placeRealmResults = RealmController.getInstance().getAllILocation();
            Log.d("Student", response);


            if (response != null) {
                JSONObject json = null;


            }
            // hideimageview();

        }


    }

    public static void initSocket(final String connectionid) {
        socket = Socket
                .Builder.with(("ws://41.189.178.40:55555/adonis-ws"))
                .addHeader("Authorization", "qEm70o9hrBRhu8F7AB9oTR5YGidUYKZnVEqaZ7Wqs4vThKzHvRXKB3W2VGKbbVlY2XIysUbkAzdiNoJ3")
                .addHeader("instructorcourseid", "instructcourse-92738c8863895a50eb307ad018c561b9")
//                .addHeader("accesstoken", "fyPyOpm7V0E:APA91bGXURsxybPu2GhnQ-uTwBq87iE4KItLdj0tAhAcOhj774hAILkudF48_LymfhTreCvB9M4Knj45jxtu1TY37FdaqjX1buPeoYR9SDcskqNAYIvDqwZJAPedg2r5sGrdHmxFmMmS")
                .addHeader("type", "audio")
                .addHeader("sessionid", "0f6894384f6d8837e6552b5622868c580f6894384f6d8837e6552b5622868c580f6894384f6d8837e6552b5622868c58")
                .addHeader("studentid", "7b5f189c-dc3d-4bc8-ac51-893711f369ac")
                .build();
        socket.connect();

        socket.onEvent(EVENT_OPEN, new Socket.OnEventListener() {
            @Override
            public void onMessage(String event) {
                Log.d("websocket1", "Connected");
                // socket = Socket.Builder.with(CONST.WS_URL).addHeader("Authorization", "qEm70o9hrBRhu8F7AB9oTR5YGidUYKZnVEqaZ7Wqs4vThKzHvRXKB3W2VGKbbVlY2XIysUbkAzdiNoJ3").build();
                //socket.connect();
                if (socket.getState() != Socket.State.OPEN) {
                    socket.connect();
                }
                if (socket != null)
                    socket.join("chat:" + connectionid);

                socket.onEventResponse("test", new Socket.OnEventResponseListener() {
                    @Override
                    public void onMessage(String event, String data) {
                        // Toast.makeText(context, data, Toast.LENGTH_SHORT).show();
                    }
                });

                socket.setMessageListener(new Socket.OnMessageListener() {
                    @Override
                    public void onMessage(String data) {
                        Log.d("stylemapandymywebsocket1", "data");
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
                                    Log.d("mywebsocket1", String.valueOf(jsonResponse));

                                    JSONObject finalJsonResponse = jsonResponse.getJSONObject("data");

                                    int sessionId = finalJsonResponse.getInt("sessionId");
                                    pageNum = sessionId;
                                    curpage.setText(String.valueOf(sessionId));
                                    switch (sessionId) {
                                        case 1:
                                            if (!finalJsonResponse.isNull("color")) {
                                                String color = finalJsonResponse.getString("color");
                                                if (color.contains("white")) {
                                                    testCanvas.setDrawingMode(DrawingMode.ERASER);
                                                    testCanvas.setDrawWidth(40);
                                                    testCanvas.drawFromServer(finalJsonResponse);
                                                } else {
                                                    testCanvas.setDrawingMode(DrawingMode.DRAW);
                                                    testCanvas.setDrawWidth(finalJsonResponse.getInt("strokeWidth"));
                                                    testCanvas.drawFromServer(finalJsonResponse);
                                                }
                                            }

                                            //testCanvas.drawFromServer(finalJsonResponse);
                                            if (!finalJsonResponse.isNull("clearpage")) {
                                                testCanvas.restartDrawing();
                                            }
                                            if (!finalJsonResponse.isNull("background")) {
                                                String background = finalJsonResponse.getString("background");
                                                if (background.contains("graph")) {
                                                    //drawingCanvas.setBackgroundDrawable();
                                                    testCanvas.setBackground(context.getResources().getDrawable(R.drawable.trygraph));
                                                } else {
                                                    testCanvas.setBackgroundResource((R.color.actual_white));
                                                }
                                            }
                                            break;

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
            }
        });

        socket.onEvent(EVENT_RECONNECT_ATTEMPT, new Socket.OnEventListener() {
            @Override
            public void onMessage(String event) {
                Log.d("mywebsocket1", "reconnecting");
            }
        });

        socket.onEvent(EVENT_CLOSED, new Socket.OnEventListener() {
            @Override
            public void onMessage(String event) {
                Log.d("mywebsocket1", "connection closed");
            }
        });
    }
}
