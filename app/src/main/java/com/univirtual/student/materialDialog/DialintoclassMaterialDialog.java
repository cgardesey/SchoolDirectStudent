package com.univirtual.student.materialDialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.univirtual.student.R;
import com.univirtual.student.constants.Const;
import com.univirtual.student.other.InitApplication;
import com.univirtual.student.other.MyHttpEntity;
import com.univirtual.student.realm.RealmStudent;
import com.univirtual.student.util.RealmUtility;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;

import static com.univirtual.student.activity.HomeActivity.APITOKEN;
import static com.univirtual.student.activity.HomeActivity.MYUSERID;
import static com.univirtual.student.constants.keyConst.API_URL;
import static com.univirtual.student.constants.Const.isValidMtnno;
import static com.univirtual.student.constants.Const.myVolleyError;
import static com.univirtual.student.fragment.AccountFragment1.firstName;
import static com.univirtual.student.fragment.AccountFragment1.gender;
import static com.univirtual.student.fragment.AccountFragment1.guardianphonenumber;
import static com.univirtual.student.fragment.AccountFragment1.guardianphonenumber2;
import static com.univirtual.student.fragment.AccountFragment1.lastName;
import static com.univirtual.student.fragment.AccountFragment1.mtnno;
import static com.univirtual.student.fragment.AccountFragment1.otherNames;
import static com.univirtual.student.fragment.AccountFragment1.profile_pic_file;
import static com.univirtual.student.fragment.AccountFragment1.title;

public class DialintoclassMaterialDialog extends DialogFragment {
    public static ProgressDialog dialog1;
    String type, phonenumber;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.list_item_dialintoclass,null);
        TextView dialintoclass = view.findViewById(R.id.dialintoclass);
        TextView number = view.findViewById(R.id.number);
        number.setText(phonenumber);
        dialintoclass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mobileno = number.getText().toString();
                if (!isValidMtnno(mobileno)){
                    Toast.makeText(getActivity(), getString(R.string.invalid_number), Toast.LENGTH_LONG).show();
                }
                else {
                    final RealmStudent[] realmStudent = new RealmStudent[1];
                    Realm.init(getActivity());
                    Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
                        realmStudent[0] = realm.where(RealmStudent.class).equalTo("infoid", PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(MYUSERID, "")).findFirst();
                        realmStudent[0].setPrimarycontact(number.getText().toString().trim());
                    });

                    Map<String, String> params = new HashMap<>();
                    params.put("firstname", realmStudent[0].getFirstname());
                    params.put("lastname", realmStudent[0].getLastname());
                    params.put("othername", realmStudent[0].getOthername());
                    params.put("gender", realmStudent[0].getGender());
                    params.put("emailaddress", realmStudent[0].getEmailaddress());
                    params.put("primarycontact", mobileno);

                    ProgressDialog mProgress;
                    mProgress = new ProgressDialog(getActivity());
                    mProgress.setMessage("Dialing into class...");
                    mProgress.setCancelable(false);
                    mProgress.setIndeterminate(true);
                    mProgress.show();
                    StringRequest stringRequest = new StringRequest(
                            com.android.volley.Request.Method.POST,
                            API_URL +"call-student",
                            response -> {

                                mProgress.dismiss();
                                if (response != null) {
                                    try {
                                        JSONObject jsonObjectResponse = new JSONObject(response);
                                        if (jsonObjectResponse.has("already_in_class")) {
                                            Toast.makeText(getActivity(), "You are already in the class conference call.", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                dismiss();
                            },
                            error -> {
                                dismiss();
                                mProgress.dismiss();
                                error.printStackTrace();
                                Log.d("Cyrilll", error.toString());
                                myVolleyError(getActivity(), error);
                            }
                    ) {
                        /** Passing some request headers* */
                        @Override
                        public Map getHeaders() throws AuthFailureError {
                            HashMap headers = new HashMap();
                            headers.put("accept", "application/json");
                            headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(APITOKEN, ""));
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
        });
        // doneBtn.setOnClickListener(doneAction);
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

    private class updateStudentaAsync extends AsyncTask<Void, Integer, String> {

        HttpClient httpClient = new DefaultHttpClient();
        private Context context;
        private ProgressDialog mProgress;

        private updateStudentaAsync(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String infoid = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(MYUSERID, "");

            HttpResponse httpResponse = null;
            HttpEntity httpEntity = null;
            String responseString = null;
            String URL = API_URL + "students/" + infoid;
            try {
                HttpPost httpPost = new HttpPost(URL);
                MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();

                // Add the file to be uploaded
                if (profile_pic_file != null) {
                    multipartEntityBuilder.addPart("picture", new FileBody(profile_pic_file));
                }
                multipartEntityBuilder.addTextBody("title", title.getSelectedItem().toString());
                String firstname = firstName.getText().toString();
                multipartEntityBuilder.addTextBody("firstname", firstname);
                multipartEntityBuilder.addTextBody("lastname", lastName.getText().toString());
                multipartEntityBuilder.addTextBody("othername", otherNames.getText().toString());
                multipartEntityBuilder.addTextBody("gender", gender.getSelectedItem().toString());
                multipartEntityBuilder.addTextBody("guardianphonenumber", guardianphonenumber.getText().toString());
                multipartEntityBuilder.addTextBody("guardian2phonenumber", guardianphonenumber2.getText().toString());
                multipartEntityBuilder.addTextBody("primarycontact", mtnno.getText().toString());

                // Progress listener - updates task's progress
                MyHttpEntity.ProgressListener progressListener =
                        progress -> publishProgress((int) progress);

                // POST
                httpPost.setEntity(new MyHttpEntity(multipartEntityBuilder.build(),
                        progressListener));
                httpPost.setHeader("accept", "application/json");
                httpPost.setHeader("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(APITOKEN, ""));


                httpResponse = httpClient.execute(httpPost);
                httpEntity = httpResponse.getEntity();

                int statusCode = httpResponse.getStatusLine().getStatusCode();

                if (statusCode == 200 || statusCode == 201) {
                    // Server response
                    responseString = EntityUtils.toString(httpEntity);
                }
            } catch (UnsupportedEncodingException | ClientProtocolException e) {
                responseString = e.getMessage();
                e.printStackTrace();
                Log.e("UPLOAD", e.getMessage());
            } catch (IOException e) {
                responseString = e.getMessage();
                Log.e("gardes", e.toString());
//                e.printStackTrace();
            }

            return responseString;
        }

        @Override
        protected void onPreExecute() {
            mProgress.show();
        }

        @Override
        protected void onPostExecute(String result) {
            mProgress.dismiss();
            if (result != null) {
                if (result.contains("connect")){
                    Toast.makeText(context, context.getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
                            realm.createOrUpdateObjectFromJson(RealmStudent.class, jsonObject);
                            Const.showToast(getActivity(), context.getString(R.string.successfully_updated));
                            dismiss();
                        });
                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }
            else {
                Toast.makeText(getActivity(), getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Update process
            /*progressbar.setProgress(progress[0]);
            statustext.setText(progress[0].toString() + "%  complete");*/
        }
    }
}