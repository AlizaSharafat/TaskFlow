package com.example.taskflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        btnGetStarted.setOnClickListener(v -> openHomeScreen());
    }

    private void openHomeScreen() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(IntentKeys.EXTRA_USER_NAME, "Hari");
        intent.putExtra(IntentKeys.EXTRA_WORKSPACE_NAME, "Sports App");
        intent.putExtra(IntentKeys.EXTRA_WORKSPACE_DATE, "20 May 2022");
        startActivity(intent);
    }
}
