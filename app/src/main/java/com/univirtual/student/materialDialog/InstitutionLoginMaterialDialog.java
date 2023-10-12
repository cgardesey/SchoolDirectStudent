package com.univirtual.student.materialDialog;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.makeramen.roundedimageview.RoundedImageView;
import com.univirtual.student.R;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static com.univirtual.student.activity.MyListActivity.institutionLogin;

public class InstitutionLoginMaterialDialog extends DialogFragment {
    TextView scholname;
    RoundedImageView schoollogo;
    EditText studentidno, password;

    Button login, cancel;

    String institutionid = "";
    String schol_name = "";
    String logo_url = "";

    String title;
    ArrayList<String> courses;
    JSONArray jsonArray;
    RecyclerView recyclerView;
    ImageView passwordIcon;

    boolean passwordShow = false;

    public String getInstitutionid() {
        return institutionid;
    }

    public TextView getScholname() {
        return scholname;
    }

    public void setScholname(TextView scholname) {
        this.scholname = scholname;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<String> getCourses() {
        return courses;
    }

    public void setCourses(ArrayList<String> courses) {
        this.courses = courses;
    }

    public JSONArray getJsonArray() {
        return jsonArray;
    }

    public void setJsonArray(JSONArray jsonArray) {
        this.jsonArray = jsonArray;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public void setInstitutionid(String institutionid) {
        this.institutionid = institutionid;
    }

    public String getSchol_name() {
        return schol_name;
    }

    public void setSchol_name(String schol_name) {
        this.schol_name = schol_name;
    }

    public String getLogo_url() {
        return logo_url;
    }

    public void setLogo_url(String logo_url) {
        this.logo_url = logo_url;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.list_item_institution_login,null);
        scholname = view.findViewById(R.id.schoolname);
        studentidno = view.findViewById(R.id.studentidno);

        password = view.findViewById(R.id.password);

        login = view.findViewById(R.id.login);
        cancel = view.findViewById(R.id.cancel);

        schoollogo = view.findViewById(R.id.schoollogo);
        passwordIcon = view.findViewById(R.id.passwordIcon);
        scholname.setText(schol_name);

        Glide.with(getActivity()).load(logo_url).apply(new RequestOptions().centerCrop()).into(schoollogo);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(studentidno.getText().toString())) {
                    studentidno.setError(getString(R.string.error_field_required));
                }
                else if (TextUtils.isEmpty(password.getText().toString())) {
                    password.setError(getString(R.string.error_field_required));
                }
                else {
                    institutionLogin(getActivity(), studentidno.getText().toString(), password.getText().toString(), institutionid, title, courses, jsonArray, recyclerView);
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                getActivity().finish();

            }
        });

        passwordIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passwordShow = !passwordShow;
                if (passwordShow) {
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hide_password);
                    passwordIcon.setImageBitmap(bitmap);
                } else {
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.see_password);
                    passwordIcon.setImageBitmap(bitmap);
                }
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
}