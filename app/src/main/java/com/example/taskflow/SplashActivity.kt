package com.example.taskflow

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        findViewById<Button>(R.id.btnGetStarted).setOnClickListener {
            openHomeScreen()
        }
    }

    private fun openHomeScreen() {
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra(IntentKeys.EXTRA_USER_NAME, "Hari")
            putExtra(IntentKeys.EXTRA_WORKSPACE_NAME, "Sports App")
            putExtra(IntentKeys.EXTRA_WORKSPACE_DATE, "20 May 2022")
        }
        startActivity(intent)
    }
}
