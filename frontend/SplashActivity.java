package com.example.distributedsystemsapp.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;


import androidx.appcompat.app.AppCompatActivity;


import com.example.distributedsystemsapp.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // waits 2.5s and goes to the main screen
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainMenu.class));
            finish(); // finishes the splash activity
        }, 2500);
    }
}