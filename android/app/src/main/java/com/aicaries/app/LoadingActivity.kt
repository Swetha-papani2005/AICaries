package com.aicaries.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoadingActivity : AppCompatActivity() {

    private var progress = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        val progressBar    = findViewById<ProgressBar>(R.id.progressBar)
        val tvLoadingText  = findViewById<TextView>(R.id.tvLoadingText)
        val handler        = Handler(Looper.getMainLooper())

        val runnable = object : Runnable {
            override fun run() {
                progress += 3
                progressBar.progress = progress

                tvLoadingText.text = when {
                    progress < 40 -> "Loading your dental profile..."
                    progress < 75 -> "Preparing your smile experience..."
                    else          -> "Almost ready..."
                }

                if (progress < 100) {
                    handler.postDelayed(this, 50)
                } else {
                    startActivity(Intent(this@LoadingActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
        handler.post(runnable)
    }
}