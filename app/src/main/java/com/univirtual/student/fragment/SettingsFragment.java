package com.univirtual.student.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.greysonparrelli.permiso.Permiso;
import com.univirtual.student.R;
import com.univirtual.student.activity.AccountActivity;
import com.univirtual.student.activity.AssignmentActivity;
import com.univirtual.student.activity.AttendanceActivity;
import com.univirtual.student.activity.HelpActivity;
import com.univirtual.student.activity.PaymentActivity;
import com.univirtual.student.activity.SigninActivity;
import com.univirtual.student.activity.TimetableActivity;
import com.univirtual.student.constants.Const;
import com.univirtual.student.materialDialog.PasswordResetMaterialDialog;
import com.univirtual.student.materialDialog.PhonenumberMaterialDialog;
import com.univirtual.student.materialDialog.WebPortalMaterialDialog;
import com.univirtual.student.other.InitApplication;
import com.univirtual.student.realm.RealmStudent;
import com.univirtual.student.util.RealmUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import droidninja.filepicker.utils.ContentUriUtils;
import io.realm.Realm;

import static com.univirtual.student.activity.HomeActivity.APITOKEN;
import static com.univirtual.student.activity.HomeActivity.GUID;
import static com.univirtual.student.activity.HomeActivity.MYUSERID;
import static com.univirtual.student.activity.HomeActivity.getDefaultDialerPackage;
import static com.univirtual.student.constants.keyConst.API_URL;
import static com.univirtual.student.constants.Const.changeDefaultDialer;
import static com.univirtual.student.constants.Const.getPackagesOfDialerApps;
import static com.univirtual.student.constants.Const.myVolleyError;
import static com.univirtual.student.receiver.NetworkReceiver.activeActivity;
import static java.nio.file.Files.copy;


/**
 * Created by Nana on 11/26/2017.
 */

public class SettingsFragment extends Fragment {

    public static final String ISNIGHTMODE = "ISNIGHTMODE";
    private static final int PICKFILE_REQUEST_CODE = 327;

    CardView attendancebtn, homeworkbtn, paymentsbtn, website, timetablebtn, profilebtn, displaybtn, permission, apppermission, logout, groups, faqs, webportal, resetpassword, conferencecallno, backup, restore;
    LinearLayout detailnightmode;
    ImageView displayright;
    Switch aSwitch;
    ProgressDialog dialog;
    public static Callbacks mCallbacks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.settingsfrag, container, false);
        timetablebtn = rootView.findViewById(R.id.timetablebtn);
        homeworkbtn = rootView.findViewById(R.id.homeworkbtn);
        groups = rootView.findViewById(R.id.groups);
        permission = rootView.findViewById(R.id.permission);
        apppermission = rootView.findViewById(R.id.apppermission);
//        subscriptiontime = rootView.findViewById(R.id.subscriptiontime);
        paymentsbtn = rootView.findViewById(R.id.paymentsbtn);
        website = rootView.findViewById(R.id.website);
        profilebtn = rootView.findViewById(R.id.profilebtn);
        attendancebtn = rootView.findViewById(R.id.attendancebtn);
        displaybtn = rootView.findViewById(R.id.displaybtn);
        detailnightmode = rootView.findViewById(R.id.detailnightmode);
        displayright = rootView.findViewById(R.id.displayright);
        faqs = rootView.findViewById(R.id.faqs);
        aSwitch = rootView.findViewById(R.id.day_night_switch);
        logout = rootView.findViewById(R.id.logout);
        webportal = rootView.findViewById(R.id.webportal);
        resetpassword = rootView.findViewById(R.id.resetpassword);
        conferencecallno = rootView.findViewById(R.id.conferencecallno);
        backup = rootView.findViewById(R.id.backup);
        restore = rootView.findViewById(R.id.restore);

        webportal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri webpage = Uri.parse("http://41.189.178.40:55554");
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), getString(R.string.page_not_found), Toast.LENGTH_LONG).show();
                }
            }
        });

        resetpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PasswordResetMaterialDialog passwordResetMaterialDialog = new PasswordResetMaterialDialog();
                passwordResetMaterialDialog.setInitiatedBySettingsFrag(true);
                if (passwordResetMaterialDialog != null && passwordResetMaterialDialog.isAdded()) {

                } else {
                    passwordResetMaterialDialog.show(getFragmentManager(), "PasswordResetMaterialDialog");
                }
            }
        });

        conferencecallno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String[] phonenumber = {""};
                Realm.init(getActivity());
                Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
                    phonenumber[0] = realm.where(RealmStudent.class).equalTo("infoid", PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(MYUSERID, "")).findFirst().getPrimarycontact();
                });


                PhonenumberMaterialDialog phonenumberMaterialDialog = new PhonenumberMaterialDialog();
                if (phonenumberMaterialDialog != null && phonenumberMaterialDialog.isAdded()) {

                } else {

                    phonenumberMaterialDialog.setType("");
                    phonenumberMaterialDialog.setPhonenumber(phonenumber[0]);
                    phonenumberMaterialDialog.show(getFragmentManager(), "");
                }
            }
        });

        faqs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), HelpActivity.class));
            }
        });

        attendancebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AttendanceActivity.class));
            }
        });


        logout.setOnClickListener(view -> {
            Toast.makeText(getContext(), getString(R.string.successfully_logged_out), Toast.LENGTH_SHORT).show();

            PreferenceManager
                    .getDefaultSharedPreferences(getActivity())
                    .edit()
                    .putString(MYUSERID, "")
                    .putString(APITOKEN, "")
                    .putString(GUID, "")
                    .apply();
            Realm.init(getContext());
            Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> realm.deleteAll());
            startActivity(new Intent(getActivity(), SigninActivity.class));
            getActivity().finish();
        });

        profilebtn.setOnClickListener(view -> {
            clickview(view);
            startActivity(new Intent(getActivity(), AccountActivity.class));

        });

        timetablebtn.setOnClickListener(view -> {
            clickview(view);
            startActivity(new Intent(getActivity(), TimetableActivity.class));
        });

        homeworkbtn.setOnClickListener(view -> {
            clickview(view);
            startActivity(new Intent(getActivity(), AssignmentActivity.class));
        });
        paymentsbtn.setOnClickListener(view -> {
            clickview(view);
            startActivity(new Intent(getActivity(), PaymentActivity.class));
        });
        website.setOnClickListener(view -> {
            clickview(view);
            Uri webpage = Uri.parse("https://www.univirtualschools.com/");
            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), getString(R.string.page_not_found), Toast.LENGTH_LONG).show();
            }
        });
        permission.setOnClickListener(view -> {
            clickview(view);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return;
            }
            String defaultDialerPackage = getDefaultDialerPackage(getContext());
            String firstIndexPackage = getPackagesOfDialerApps(getContext()).get(0);
            if (defaultDialerPackage.equals(firstIndexPackage)) {
                Toast.makeText(getContext(), getString(R.string.you_are_already_using_system_default), Toast.LENGTH_SHORT).show();
            } else {
                changeDefaultDialer(getActivity(), getPackagesOfDialerApps(getContext()).get(0));
            }
        });
        homeworkbtn.setOnClickListener(view -> {

            clickview(view);
            startActivity(new Intent(getActivity(), AssignmentActivity.class));
        });
        homeworkbtn.setOnClickListener(view -> {
            clickview(view);
            startActivity(new Intent(getActivity(), AssignmentActivity.class));
        });
        displaybtn.setOnClickListener(view -> {
            clickview(view);
            aSwitch.setChecked(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(ISNIGHTMODE, false));
            if (detailnightmode.getVisibility() == View.VISIBLE) {
                detailnightmode.setVisibility(View.GONE);
                displayright.setImageResource(R.drawable.right);
            } else {
                detailnightmode.setVisibility(View.VISIBLE);
                displayright.setImageResource(R.drawable.arrowdown);
            }
        });
        aSwitch.setOnClickListener(v -> {
            PreferenceManager
                    .getDefaultSharedPreferences(getContext())
                    .edit()
                    .putBoolean(ISNIGHTMODE, !PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(ISNIGHTMODE, false))
                    .apply();

            mCallbacks.onChangeNightMOde();
        });
        backup.setOnClickListener(v -> {
            Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
                                                         @Override
                                                         public void onPermissionResult(Permiso.ResultSet resultSet) {
                                                             if (resultSet.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE) && resultSet.isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                                                 // create a backup file
                                                                 String timestamp = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss.SSS").format(Calendar.getInstance().getTime());
                                                                 File exportRealmFile = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/SchoolDirectStudent/Backups/" + timestamp + ".realm");
                                                                 if (!exportRealmFile.getParentFile().exists()) {
                                                                     exportRealmFile.getParentFile().mkdirs();
                                                                 }
                                                                 // if backup file already exists, delete it
                                                                 if (exportRealmFile.exists()) {
                                                                     exportRealmFile.delete();
                                                                 }

                                                                 // copy current realm to backup file
                                                                 Realm.init(getActivity());
                                                                 Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
                                                                     realm.writeEncryptedCopyTo(exportRealmFile, new byte[]{42, -17, -94, 54, 44, 122, -71, 110, -80, 23, 53, 6, 102, 67, -24, -63, -93, -45, 64, -25, 90, -6, 125, -64, 74, 53, -41, -81, -12, 7, -27, 34, 84, 86, 108, -46, -126, -6, 59, 64, -23, -56, 2, -89, 28, -47, -43, -70, 94, -1, -110, 126, -14, -31, 51, -23, -120, -50, -70, -104, -100, -39, 52, 77});
                                                                 });
                                                                 File destinationFile = new File(Environment.getExternalStorageDirectory() + "/SchoolDirectStudent/Backups/" + timestamp + ".realm");
                                                                 try {
                                                                     if (destinationFile.exists()) {
                                                                         destinationFile.delete();
                                                                     }
                                                                     if (destinationFile.getParentFile().exists()) {
                                                                         destinationFile.getParentFile().mkdirs();
                                                                     }
                                                                     Const.copy(exportRealmFile, destinationFile);
                                                                     exportRealmFile.delete();

                                                                     AlertDialog.Builder builder = new AlertDialog.Builder(activeActivity);
                                                                     builder.setTitle("Backup Successful!");
                                                                     builder.setMessage("Backup file saved to /storage/SchoolDirectStudent/Backups/"  + timestamp + ".realm");
                                                                     builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                                                         dialog.dismiss();
                                                                     });
                                                                     builder.show();
                                                                 } catch (IOException e) {
                                                                     e.printStackTrace();
                                                                 }
                                                             }
                                                         }

                                                         @Override
                                                         public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                                                             Permiso.getInstance().showRationaleInDialog(getActivity().getString(R.string.permissions), getActivity().getString(R.string.this_permission_is_mandatory_pls_allow_access), null, callback);
                                                         }
                                                     },
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
        });
        restore.setOnClickListener(v -> {
            Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
                                                         @Override
                                                         public void onPermissionResult(Permiso.ResultSet resultSet) {
                                                             if (resultSet.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE) && resultSet.isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                                                 Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                                                 intent.setType("*/*");
                                                                 startActivityForResult(intent, PICKFILE_REQUEST_CODE);
                                                             }
                                                         }

                                                         @Override
                                                         public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                                                             Permiso.getInstance().showRationaleInDialog(getActivity().getString(R.string.permissions), getActivity().getString(R.string.this_permission_is_mandatory_pls_allow_access), null, callback);
                                                         }
                                                     },
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
        });
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Activities containing this fragment must implement its callbacks
        mCallbacks = (Callbacks) activity;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            FileOutputStream outputStream = new FileOutputStream(new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/sdsRealmDataBase.realm"));
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(new File(ContentUriUtils.INSTANCE.getFilePath(getActivity(), data.getData())));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, bytesRead);
            }
            outputStream.close();

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Backup successfully restored");
            builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                dialog.dismiss();
            });
            builder.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clickview(View v) {
        Animation animation1 = AnimationUtils.loadAnimation(v.getContext(), R.anim.click);
        v.startAnimation(animation1);

    }

    public interface Callbacks {
        //Callback for when button clicked.
        void onChangeNightMOde();
    }

    public void generatePin() {
        String URL = null;
        dialog = ProgressDialog.show(getContext(), null, getActivity().getResources().getString(R.string.pls_wait), true);

        try {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    API_URL + "generate-pin",
                    null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response == null) {
                        return;
                    }
                    dialog.dismiss();
                    try {
                        WebPortalMaterialDialog webPortalMaterialDialog = new WebPortalMaterialDialog();
                        webPortalMaterialDialog.setAccesscode(response.getString("pin"));
                        webPortalMaterialDialog.show(getFragmentManager(), "");
                    } catch (JSONException e) {
                        e.printStackTrace();
//                        webcodeView.setText(e.getMessage());
                    }
//                    webcodeView.setText(message);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    dialog.dismiss();
//                       loadimg.setVisibility(View.GONE);
//                    webcodeView.setText(getActivity().getResources().getString(R.string.error_occured));
                    myVolleyError(getContext(), error);
                    Log.d("Obeng", error.toString());

                }
            }) {
                @Override
                public Map getHeaders() throws AuthFailureError {
                    HashMap headers = new HashMap();
                    headers.put("accept", "application/json");
                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getContext()).getString(APITOKEN, ""));
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
        // Toast.makeText(getApplicationContext(), "done", Toast.LENGTH_LONG).show();


    }
}
