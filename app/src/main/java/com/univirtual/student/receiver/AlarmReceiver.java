package com.univirtual.student.receiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.webkit.URLUtil;

import androidx.core.app.NotificationCompat;

import com.univirtual.student.R;
import com.univirtual.student.activity.AssignmentActivity;
import com.univirtual.student.activity.ChatActivity;
import com.univirtual.student.activity.ClassReplaysActivity;
import com.univirtual.student.activity.DistinctRecordedStreamActivity;
import com.univirtual.student.activity.ExternalLinksActivity;
import com.univirtual.student.activity.HomeActivity;
import com.univirtual.student.activity.RecordedVideosActivity;
import com.univirtual.student.activity.SelectResourceActivity;
import com.univirtual.student.activity.FileListActivity;
import com.univirtual.student.activity.QuizzesActivity;
import com.univirtual.student.pojo.MyFile;
import com.univirtual.student.realm.RealmAudio;
import com.univirtual.student.realm.RealmClassSessionDoc;
import com.univirtual.student.realm.RealmEnrolment;
import com.univirtual.student.realm.RealmInstructor;
import com.univirtual.student.realm.RealmInstructorCourse;
import com.univirtual.student.realm.RealmRecommendedDoc;
import com.univirtual.student.realm.RealmRecordedAudioStream;
import com.univirtual.student.realm.RealmRecordedVideoStream;
import com.univirtual.student.util.RealmUtility;

import org.apache.commons.lang3.StringUtils;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.univirtual.student.activity.FileListActivity.myFiles;
import static com.univirtual.student.activity.HomeActivity.MYUSERID;

/**
 * Created by Andy on 11/8/2019.
 */


public class AlarmReceiver extends BroadcastReceiver {

    private static final String NOTIFICATION_CHANNEL_ID = "channel_id";
    private static final String CHANNEL_NAME = "NOTIFICATION_CHANNEL";

    @Override
    public void onReceive(Context context, Intent intent) {
        //Remember in the SetAlarm file we made an intent to this, this is way this work, otherwise you would have to put an action
        /*Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(2000);*/


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        // Importance applicable to all the notifications in this Channel
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        // Notification channel should only be created for devices running Android 26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, CHANNEL_NAME, importance);
            //Boolean value to set if lights are enabled for Notifications from this Channel
            notificationChannel.enableLights(true);
            //Boolean value to set if vibration are enabled for Notifications from this Channel
            notificationChannel.enableVibration(true);
            //Sets the color of Notification Light
            notificationChannel.setLightColor(Color.GREEN);
            //Set the vibration pattern for notifications. Pattern is in milliseconds with the format {delay,play,sleep,play,sleep...}
            notificationChannel.setVibrationPattern(new long[]{500, 500, 500, 500, 500});
            notificationManager.createNotificationChannel(notificationChannel);
            //Sets whether notifications from these Channel should be visible on Lockscreen or not
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        }

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_splash);
        String type = intent.getStringExtra("TYPE");
        String title = "";
        String body = "";

        String coursepath = intent.getStringExtra("COURSEPATH");
        String instructorcourseid = intent.getStringExtra("INSTRUCTORCOURSEID");


        switch (type) {
            case "class":
                title = "Conference call class started";
                body = "Click to enter" + " " + coursepath + " " + "class";
                break;
            case "audio_stream":
                title = "Audio class started";
                body = "Click to enter" + " " + coursepath + " " + "class";
                break;
            case "video_stream":
                title = "Video class started";
                body = "Click to enter" + " " + coursepath + " " + "class";
                break;
            case "recorded_audio_stream":
                title = "Class playback ready";
                body = "1. Click to go to class playback. \n2. Click on the refresh icon at the upper right corner to see newly added playbacks in" + " " + coursepath + " " + "class.";
                break;
            case "recorded_video_stream":
                title = "Class playback ready";
                body = "1. Click to go to class playback. \n2. Click on the refresh icon at the upper right corner to see newly added playbacks in" + " " + coursepath + " " + "class.";
                break;
            case "recorded_class_call":
                title = "Class playback ready";
                body = "1. Click to go to class playback. \n2. Click on the refresh icon at the upper right corner to see newly added playbacks in" + " " + coursepath + " " + "class.";
                break;
            case "doc":
                title = "Class document available for download";
                body = "1. Click to go to class library files. \n2. Click on the refresh icon at the upper right corner to see newly added documents in" + " " + coursepath + " " + "class.";
                break;
            case "video":
                title = "Recorded class video ready";
                body = "1 .Click to go to recorded class videos. \n2. Click on the refresh icon at the upper right corner to see newly added videos in" + " " + coursepath + " " + "class";
                break;
            case "chat":
                title = intent.getStringExtra("TITLE");
                body = intent.getStringExtra("BODY");
                break;
            case "assignment":
                title = "Class assignment available for download";
                body = "1 .Click to go to assignments. \n2. Click on the refresh icon at the upper right corner to see newly added assignments in" + " " + coursepath + " " + "class";
                break;
            case "markedassignment":
                title = "Marked assignment available for download";
                body = "1 .Click to go to assignments. \n2. Click on the refresh icon at the upper right corner to see newly marked assignments in" + " " + coursepath + " " + "class";
                break;
            case "quiz":
                title = "Class quiz available";
                body = "1. Click to go to class quizzes \n2. Click the refresh icon at the top right corner of the page to see newly added quizzes in" + " " + coursepath + " " + "class";
                break;
            case "externallink":
                title = "New recommended external link added";
                body = "1. Click to go to class recommended external links \n2. Click the refresh icon at the top right corner of the page to see newly added links in" + " " + coursepath + " " + "class";
                break;
            /*case "custom":
                title = "Message from instructor";
                body = "Class:" + " " + coursepath + "\n" + "Message:" + " " + intent.getStringExtra("MESSAGE");
                break;*/
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_splash)
                .setContentTitle(title)
                .setContentText(body)
                .setLargeIcon(icon)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true);

        Intent notificationIntent = null;

        switch (type) {
            case "chat":
                notificationIntent = new Intent(context, ChatActivity.class);
                break;
            case "assignment":
                notificationIntent = new Intent(context, AssignmentActivity.class);
                break;
            case "markedassignment":
                notificationIntent = new Intent(context, AssignmentActivity.class)
                        .putExtra("SHOWMARKEDFRAGMENT", true);
                break;
            case "quiz":
                notificationIntent = new Intent(context, QuizzesActivity.class);
                break;
            case "video":
                notificationIntent = new Intent(context, RecordedVideosActivity.class);
                break;
            case "recorded_audio_stream":
            case "recorded_video_stream":
            case "recorded_class_call":
                final boolean[] distincTitleExistsInRecordedClassCall = new boolean[1];
                final boolean[] distincTitleExistsInRecordedAudio = new boolean[1];
                final boolean[] distincTitleExistsInRecordedVideo = new boolean[1];
                Realm.init(context);
                Realm.getInstance(RealmUtility.getDefaultConfig(context)).executeTransaction(realm -> {
                    distincTitleExistsInRecordedClassCall[0] = realm.where(RealmAudio.class)
                            .distinct("title")
                            .equalTo("instructorcourseid", instructorcourseid)
                            .isNotNull("audiourl")
                            .notEqualTo("audiourl", "")
                            .sort("id", Sort.DESCENDING)
                            .findAll()
                            .size() > 0;

                    distincTitleExistsInRecordedAudio[0] = realm.where(RealmRecordedAudioStream.class)
                            .distinct("title")
                            .equalTo("instructorcourseid", instructorcourseid)
                            .isNotNull("url")
                            .notEqualTo("url", "")
                            .sort("id", Sort.DESCENDING)
                            .findAll()
                            .size() > 0;

                    distincTitleExistsInRecordedVideo[0] = realm.where(RealmRecordedVideoStream.class)
                            .distinct("title")
                            .equalTo("instructorcourseid", instructorcourseid)
                            .isNotNull("url")
                            .notEqualTo("url", "")
                            .sort("id", Sort.DESCENDING)
                            .findAll()
                            .size() > 0;
                });
                if (distincTitleExistsInRecordedClassCall[0] || distincTitleExistsInRecordedAudio[0] || distincTitleExistsInRecordedVideo[0]) {
                    notificationIntent = new Intent(context, ClassReplaysActivity.class)
                            .putExtra("TITLE", intent.getStringExtra("TITLE"));
                } else {
                    notificationIntent = new Intent(context, DistinctRecordedStreamActivity.class);
                }
                break;
            /*case "custom":
                notificationIntent = new Intent();
                break;*/
            case "doc":
                myFiles.clear();

                Realm.init(context);
                Realm.getInstance(RealmUtility.getDefaultConfig(context)).executeTransaction(realm -> {

                    RealmResults<RealmAudio> realmAudios = realm.where(RealmAudio.class)
                            .isNotNull("url")
                            .notEqualTo("url", "")
                            .distinct("url")
                            .equalTo("instructorcourseid", instructorcourseid)
                            .sort("id", Sort.DESCENDING)
                            .findAll();

                    RealmResults<RealmRecordedAudioStream> realmRecordedAudioStreams = realm.where(RealmRecordedAudioStream.class)
                            .isNotNull("docurl")
                            .notEqualTo("docurl", "")
                            .distinct("docurl")
                            .equalTo("instructorcourseid", instructorcourseid)
                            .sort("id", Sort.DESCENDING)
                            .findAll();

                    RealmResults<RealmRecordedVideoStream> realmRecordedVideoStreams = realm.where(RealmRecordedVideoStream.class)
                            .isNotNull("docurl")
                            .notEqualTo("docurl", "")
                            .distinct("docurl")
                            .equalTo("instructorcourseid", instructorcourseid)
                            .sort("id", Sort.DESCENDING)
                            .findAll();

                    RealmResults<RealmRecommendedDoc> realmRecommendedDocs = realm.where(RealmRecommendedDoc.class)
                            .isNotNull("url")
                            .notEqualTo("url", "")
                            .distinct("url")
                            .equalTo("instructorcourseid", instructorcourseid)
                            .sort("id", Sort.DESCENDING)
                            .findAll();


                    for (RealmAudio realmAudio : realmAudios) {
                        RealmClassSessionDoc realmClassSessionDoc = new RealmClassSessionDoc(realmAudio.getUrl(), instructorcourseid);
                        realmClassSessionDoc.setSessionid(realmAudio.getSessionid());
                        realm.copyToRealmOrUpdate(realmClassSessionDoc);
                    }

                    for (RealmRecordedAudioStream recordedAudioStream : realmRecordedAudioStreams) {
                        RealmClassSessionDoc realmClassSessionDoc = new RealmClassSessionDoc(recordedAudioStream.getDocurl(), instructorcourseid);
                        realmClassSessionDoc.setSessionid(recordedAudioStream.getSessionid());
                        realm.copyToRealmOrUpdate(realmClassSessionDoc);
                    }

                    for (RealmRecordedVideoStream recordedVideoStream : realmRecordedVideoStreams) {
                        RealmClassSessionDoc realmClassSessionDoc = new RealmClassSessionDoc(recordedVideoStream.getDocurl(), instructorcourseid);
                        realmClassSessionDoc.setSessionid(recordedVideoStream.getSessionid());
                        realm.copyToRealmOrUpdate(realmClassSessionDoc);
                    }

                    for (RealmRecommendedDoc realmRecommendedDoc : realmRecommendedDocs) {
                        RealmClassSessionDoc realmClassSessionDoc = new RealmClassSessionDoc(realmRecommendedDoc.getUrl(), instructorcourseid);
                        realm.copyToRealmOrUpdate(realmClassSessionDoc);
                    }

                    RealmResults<RealmClassSessionDoc> classSessionDocs = realm.where(RealmClassSessionDoc.class)
                            .equalTo("instructorcourseid", instructorcourseid)
                            .findAll();

                    for (RealmClassSessionDoc classSessionDoc : classSessionDocs) {
                        if (classSessionDoc.getSessionid() == null || classSessionDoc.getSessionid() == "") {
                            myFiles.add(new MyFile(context.getFilesDir() + "/SchoolDirectStudent/" + coursepath.replace(" >> ", "/") + "/Recommended-documents/" + URLUtil.guessFileName(classSessionDoc.getUrl(), null, null),
                                    classSessionDoc.getUrl(),
                                    null));
                        } else {
                            myFiles.add(new MyFile(context.getFilesDir() + "/SchoolDirectStudent/" + coursepath.replace(" >> ", "/") + "/Class-sessions/Documents/" + URLUtil.guessFileName(classSessionDoc.getUrl(), null, null),
                                    classSessionDoc.getUrl(),
                                    null));
                        }
                    }
                });
                notificationIntent = new Intent(context, FileListActivity.class)
                        .putExtra("activitytitle", coursepath)
                        .putExtra("assignmenttitle", R.string.classdocs)
                        .putExtra("LAUNCHED_FROM_NOTIFICATION", true);
                break;
            case "external":
                notificationIntent = new Intent(context, ExternalLinksActivity.class);
                break;
            case "class":
            case "audio_stream":
            case "video_stream":
                notificationIntent = new Intent(context, SelectResourceActivity.class)
                        .putExtra("LAUNCHED_FROM_NOTIFICATION", true)
                        .putExtra("TYPE", type);
                break;
        }

        final String[] enrolmentid = new String[1];
        final String[] instructorname = new String[1];
        final String[] profilepicurl = new String[1];
        final String[] roomid = new String[1];
        final String[] nodeserver = new String[1];

        Realm.init(context);
        Realm.getInstance(RealmUtility.getDefaultConfig(context)).executeTransaction(realm -> {
            enrolmentid[0] = realm.where(RealmEnrolment.class)
                    .equalTo("studentid", PreferenceManager.getDefaultSharedPreferences(context).getString(MYUSERID, ""))
                    .equalTo("instructorcourseid", instructorcourseid)
                    .equalTo("enrolled", 1)
                    .findFirst()
                    .getEnrolmentid();
            RealmInstructorCourse realmInstructorCourse = realm.where(RealmInstructorCourse.class)
                    .equalTo("instructorcourseid", instructorcourseid)
                    .findFirst();

            roomid[0] = realmInstructorCourse.getRoom_number();

            RealmInstructor realmInstructor = realm.where(RealmInstructor.class)
                    .equalTo("infoid", realmInstructorCourse.getInstructorid())
                    .findFirst();
            nodeserver[0] = realmInstructorCourse.getNodeserver();
            instructorname[0] = StringUtils.normalizeSpace(realmInstructor.getTitle() + " " + realmInstructor.getFirstname() + " " + realmInstructor.getOthername() + " " + realmInstructor.getLastname());
            profilepicurl[0] = realmInstructor.getProfilepicurl();
        });

        /*INSTRUCTORCOURSEID = instructorcourseid;
        COURSEPATH = coursepath;
        ENROLMENTID = enrolmentid[0];
        PROFILEIMGURL = profilepicurl[0];
        NODESERVER = nodeserver[0];
        ROOMID = roomid[0];*/

        notificationIntent.putExtra("INSTRUCTORCOURSEID", instructorcourseid);
        notificationIntent.putExtra("COURSEPATH", coursepath);
        notificationIntent.putExtra("ENROLMENTID", enrolmentid[0]);
        notificationIntent.putExtra("PROFILEIMGURL", profilepicurl[0]);
        notificationIntent.putExtra("INSTRUCTORNAME", instructorname[0]);
        notificationIntent.putExtra("NODESERVER", nodeserver[0]);
        notificationIntent.putExtra("ROOMID", roomid[0]);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 1000, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        // Add as notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1000, builder.build());
    }
}

