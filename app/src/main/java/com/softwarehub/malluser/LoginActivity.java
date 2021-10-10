package com.softwarehub.malluser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private TextView btnSwitchRegister;
    private TextInputEditText etEmail,etPassword;
    private Button btnLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        dialog = new ProgressDialog(LoginActivity.this);

        etEmail = findViewById(R.id.et_email_login);
        etPassword = findViewById(R.id.et_password_login);
        btnSwitchRegister = findViewById(R.id.btn_switch_register);
        btnLogin = findViewById(R.id.btn_login);

        btnSwitchRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();

                if(!TextUtils.isEmpty(email)){
                    if(!TextUtils.isEmpty(password)){
                        signInUser(email,password);
                    }else {
                        etPassword.setError("Fill the blank field");
                    }
                }else {
                    etEmail.setError("Fill the blank field");
                }
            }
        });
    }

    private void signInUser(String email,String password){
        dialog.setMessage("Validating your details...");
        dialog.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        dialog.dismiss();
                        if (task.isSuccessful()) {
//                            Map<String, Object> userData = new HashMap<>();
//                            userData.put("loginIP",deviceIP);
//                            userData.put("loginStatus","ON");
//                            firebaseFirestore.collection("USERS")
//                                    .document(task.getResult().getUser().getUid())
//                                    .update(userData)
//                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            if (task.isSuccessful()) {
                                                Toast.makeText(LoginActivity.this, "Login Successful !", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
//                                            } else {
//                                                Toast.makeText(LoginActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
//                                            }
//                                        }
//                                    });

                        } else {
                            Toast.makeText(LoginActivity.this, "Sorry ! your credentials are wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}