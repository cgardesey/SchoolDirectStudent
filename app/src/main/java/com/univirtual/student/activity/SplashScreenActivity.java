package com.univirtual.student.activity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.univirtual.student.R;
import com.univirtual.student.fragment.EnrolmentsFragment;
import com.univirtual.student.realm.RealmStudent;
import com.univirtual.student.util.RealmUtility;

import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;

import static com.univirtual.student.activity.HomeActivity.MYUSERID;


public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        ImageView imageView = findViewById(R.id.logotext);

        Glide.with(this)
                .asGif()
                .load(R.drawable.logogif)

                .listener(new RequestListener<GifDrawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                        Timer myTimer = new Timer();

                        myTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                // If you want to modify a view in your Activity
                                SplashScreenActivity.this.runOnUiThread(() -> {
                                    boolean signedIn = !PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(MYUSERID, "").equals("");
                                    Realm.init(getApplicationContext());
                                    Realm.getInstance(RealmUtility.getDefaultConfig(SplashScreenActivity.this)).executeTransaction(realm -> {
                                        RealmStudent realmStudent = realm.where(RealmStudent.class).equalTo("infoid", PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(MYUSERID, "")).findFirst();
                                        if (signedIn && realmStudent != null) {
                                            startActivity(new Intent(SplashScreenActivity.this, HomeActivity.class));
                                        } else {
                                            startActivity(new Intent(SplashScreenActivity.this, PaperOnboardingActivity.class));
                                        }
                                        finish();

                                    });
                                });
                            }
                        }, 1500);
                        return false;
                    }
                })
                .into(imageView);
    }
}
