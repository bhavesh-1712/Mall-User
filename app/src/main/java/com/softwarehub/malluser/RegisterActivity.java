package com.softwarehub.malluser;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextView btnSwitchLogin;
    private TextInputEditText etEmail,etMobileNo,etPassword,etCofirmPassword;
    private Button btnRegister;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        dialog = new ProgressDialog(RegisterActivity.this);

        btnSwitchLogin = findViewById(R.id.btn_switch_login);
        etEmail = findViewById(R.id.et_date_schedule);
        etMobileNo = findViewById(R.id.et_mobile_number_register);
        etPassword = findViewById(R.id.et_password_register);
        etCofirmPassword = findViewById(R.id.et_confirm_password_register);
        btnRegister = findViewById(R.id.btn_register);

        btnSwitchLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                String mobileNo = etMobileNo.getText().toString();
                String pass = etPassword.getText().toString();
                String confirmPass = etCofirmPassword.getText().toString();

                if (!TextUtils.isEmpty(email) || verifyEmail(email)) {
                    if (!TextUtils.isEmpty(mobileNo) || verifyMobileNo(mobileNo)) {
                        if (pass.equals(confirmPass)) {

                            createUser(email, mobileNo, pass);

                        } else {
                            etCofirmPassword.setError("Password not match");
                        }
                    } else {
                        etMobileNo.setError("Invalid Moblie Number");
                    }
                } else {
                    etEmail.setError("Invalid Email");
                }
            }
        });
    }

    private void createUser(String email, String mobileNo, String password) {
        dialog.setMessage("Creating Your Account..");
        dialog.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("email", email);
                            userData.put("mobile_no", mobileNo);

                            firebaseFirestore.collection("USERS")
                                    .document(mAuth.getUid())
                                    .set(userData)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            dialog.dismiss();
                                            if (task.isSuccessful()) {
                                                Toast.makeText(RegisterActivity.this, "Account Created Successfully !", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                String error = task.getException().getMessage();
                                                Log.d("FIREBASE_EXCPATION", error);
                                                Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            dialog.dismiss();
                            String error = task.getException().getMessage();
                            Log.d("FIREBASE_EXCPATION", error);
                            Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean verifyMobileNo(String moNo) {
        String regex = "(0/91)?[7-9][0-9]{9}";
        return moNo.matches(regex);
    }

    private boolean verifyEmail(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }
}