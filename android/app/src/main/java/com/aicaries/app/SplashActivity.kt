package com.aicaries.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val session = SessionManager(this)

        // If already logged in, go to loading screen
        if (session.isLoggedIn()) {
            startActivity(Intent(this, LoadingActivity::class.java))
            finish()
            return
        }

        findViewById<Button>(R.id.btnGetStarted).setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            intent.putExtra("START_SCREEN", "signup")
            startActivity(intent)
        }

        findViewById<TextView>(R.id.tvSignIn).setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            intent.putExtra("START_SCREEN", "signin")
            startActivity(intent)
        }
    }
}