package com.aicaries.app

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("AICariesSession", Context.MODE_PRIVATE)

    fun saveUser(userId: Int, name: String, email: String, token: String) {
        prefs.edit().apply {
            putInt("user_id", userId)
            putString("name", name)
            putString("email", email)
            putString("token", token)
            putBoolean("is_logged_in", true)
            apply()
        }
    }

    fun getUserId(): Int = prefs.getInt("user_id", -1)
    fun getName(): String = prefs.getString("name", "") ?: ""
    fun getEmail(): String = prefs.getString("email", "") ?: ""
    fun getToken(): String = prefs.getString("token", "") ?: ""
    fun isLoggedIn(): Boolean = prefs.getBoolean("is_logged_in", false)

    fun getLanguage(): String = prefs.getString("language", "en") ?: ""
    fun saveLanguage(lang: String) =
        prefs.edit().putString("language", lang).apply()

    // NEW
    fun saveAssessmentScore(score: Int) {
        prefs.edit().putInt("assessment_score", score).apply()
    }

    // NEW
    fun getAssessmentScore(): Int {
        return prefs.getInt("assessment_score", 0)
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}