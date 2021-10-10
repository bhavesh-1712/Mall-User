package com.softwarehub.malluser;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {
    private Handler handler;
    private ProgressBar progressBar;
    private static int checkNet = 0;

    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        firebaseAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.progressBar);
        Snackbar mySnackbar = Snackbar.make(findViewById(R.id.my_constraint_layout_splash_activity), "Your Internet Connection Is Off", Snackbar.LENGTH_INDEFINITE);
        mySnackbar.setAction("Retry", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInternetOn() == false) {
                    progressBar.setVisibility(View.GONE);
                    checkNet = 1;
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    mySnackbar.dismiss();
                }

            }
        });

        if (isInternetOn() == false) {
            progressBar.setVisibility(View.GONE);
            mySnackbar.show();
            checkNet = 1;
        } else {
            progressBar.setVisibility(View.VISIBLE);
        }
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isInternetOn()) {
                    if (checkNet == 0) {
                        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                        if(currentUser==null){
                            Intent registerIntent = new Intent(SplashActivity.this, LoginActivity.class);
                            startActivity(registerIntent);
                            finish();
                        }else{
                            Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                            startActivity(mainIntent);
                            finish();
                        }
                    }
                }
            }
        }, 1000);
    }

    public boolean isInternetOn() {
        ConnectivityManager connec = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTING) {
            return true;
        } else if (connec.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED ||
                connec.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTING) {
            return false;
        }
        return false;
    }
}