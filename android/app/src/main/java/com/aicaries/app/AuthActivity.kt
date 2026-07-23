package com.aicaries.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val startScreen = intent.getStringExtra("START_SCREEN") ?: "signin"

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.auth_nav_host) as NavHostFragment
        val navController = navHostFragment.navController

        if (startScreen == "signup") {
            navController.navigate(R.id.signUpFragment)
        }
    }
}