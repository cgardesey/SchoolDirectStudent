package com.univirtual.student.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.univirtual.student.R;
import static com.univirtual.student.activity.HomeActivity.versionGuidCheck;
import static com.univirtual.student.constants.keyConst.API_URL;
import static com.univirtual.student.constants.keyConst.GUID_WS_URL;
import static com.univirtual.student.constants.Const.isNetworkAvailable;


public class NetworkReceiver extends BroadcastReceiver {

    public static String CONNECTEDTONETWORK = "CONNECTEDTONETWORK";
    public static Activity activeActivity;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    long cacheExpiration = 3600;
    String API_URL_KEY = "API_URL_KEY";
    String GUID_WS_URL_KEY = "GUID_WS_URL_KEY";
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcastWithFirebase.
        //throw new UnsupportedOperationException("Not yet implemented");

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        boolean networkAvailable = isNetworkAvailable(context);
        Log.d("09876", Boolean.toString(networkAvailable));

        if (networkAvailable) {
            if (activeActivity != null) {
                versionGuidCheck(activeActivity);
//                fetchAllMyData(activeActivity);

                if (networkAvailable != PreferenceManager.getDefaultSharedPreferences(context).getBoolean(CONNECTEDTONETWORK, false)) {
                    Snackbar.make(activeActivity.findViewById(android.R.id.content), "Connected to internet", Snackbar.LENGTH_LONG)
                            .setBackgroundTint(activeActivity.getResources().getColor(android.R.color.holo_green_dark))
                            .show();
                }
            }
        } else {
            if (activeActivity != null) {
                if (networkAvailable != PreferenceManager.getDefaultSharedPreferences(context).getBoolean(CONNECTEDTONETWORK, false)) {
                    Snackbar.make(activeActivity.findViewById(android.R.id.content), "Disconnected from internet", Snackbar.LENGTH_LONG)
                            .setBackgroundTint(activeActivity.getResources().getColor(android.R.color.holo_red_dark))
                            .show();
                }
            }
        }

        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(CONNECTEDTONETWORK, networkAvailable)
                .apply();

        mFirebaseRemoteConfig.setConfigSettingsAsync(new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(cacheExpiration)
                .build());

        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(activeActivity, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            boolean updated = task.getResult();
                            if (true) {
                                API_URL = mFirebaseRemoteConfig.getString(API_URL_KEY);
                                GUID_WS_URL = mFirebaseRemoteConfig.getString(GUID_WS_URL_KEY);
                                Log.d("API_URL", API_URL);
                                Log.d("GUID_WS_URL", GUID_WS_URL);
                            }
                        }
                    }
                });
    }
}
