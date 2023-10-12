package com.univirtual.student.materialDialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.univirtual.student.R;
import com.univirtual.student.activity.AudioStreamActivity;
import com.univirtual.student.activity.ConferenceCallActivity;
import com.univirtual.student.activity.VideoStreamActivity;

import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.MODE_PRIVATE;
import static com.univirtual.student.activity.AudioStreamActivity.audioStreamActivity;
import static com.univirtual.student.activity.ConferenceCallActivity.conferenceCallActivity;
import static com.univirtual.student.activity.VideoStreamActivity.videoStreamActivity;
import static com.univirtual.student.receiver.NetworkReceiver.activeActivity;

public class ConferenceCallInfoMaterialDialog extends DialogFragment {
    TextView dialcode, conferenceid, classended;
    LinearLayout info;
    String dial_code = "";
    String conference_id = "";

    public String getDial_code() {
        return dial_code;
    }

    public void setDial_code(String dial_code) {
        this.dial_code = dial_code;
    }

    public String getConference_id() {
        return conference_id;
    }

    public void setConference_id(String conference_id) {
        this.conference_id = conference_id;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.list_item_conference_call_info,null);
        Button cancelbtn = view.findViewById(R.id.cancelbtn);
        dialcode = view.findViewById(R.id.dialcode);
        conferenceid = view.findViewById(R.id.conferenceid);
        info = view.findViewById(R.id.info);
        classended = view.findViewById(R.id.classended);
        dialcode.setMovementMethod(LinkMovementMethod.getInstance());

        dialcode.setText(getDial_code());
        conferenceid.setText(getConference_id() + "#");

        if (conference_id.equals("")) {
            info.setVisibility(View.GONE);
            classended.setVisibility(View.VISIBLE);
        }
        else {
            info.setVisibility(View.VISIBLE);
            classended.setVisibility(View.GONE);
        }
        cancelbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // view.setVisibility(View.GONE);
                dismiss();
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // If you want to modify a view in your Activity
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    }
                });
            }
        }, 5);
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (activeActivity instanceof  AudioStreamActivity) {
            AudioStreamActivity.gotopage.performClick();
        }
        else if (activeActivity instanceof  VideoStreamActivity) {
            VideoStreamActivity.gotopage.performClick();
        }
        else if (activeActivity instanceof  ConferenceCallActivity) {
            ConferenceCallActivity.gotopage.performClick();
        }
    }
}