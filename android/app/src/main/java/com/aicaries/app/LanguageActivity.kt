package com.aicaries.app

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import org.json.JSONObject
import java.util.*

class LanguageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language)

        val session = SessionManager(this)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        val languages = mapOf(
            R.id.cardEnglish  to "en",
            R.id.cardTamil    to "ta",
            R.id.cardHindi    to "hi",
            R.id.cardTelugu   to "te",
            R.id.cardMalayalam to "ml",
            R.id.cardKannada  to "kn"
        )

        languages.forEach { (cardId, code) ->
            findViewById<CardView>(cardId).setOnClickListener {
                session.saveLanguage(code)

                // Save to server
                val params = JSONObject()
                params.put("user_id",  session.getUserId())
                params.put("name",     session.getName())
                params.put("phone",    "")
                params.put("language", code)
                ApiClient.post("update_profile.php", params) { }

                // Apply locale
                val locale = Locale(code)
                Locale.setDefault(locale)
                val config = Configuration(resources.configuration)
                config.setLocale(locale)
                resources.updateConfiguration(config, resources.displayMetrics)

                // Restart app to apply
                val intent = Intent(this, SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }
}