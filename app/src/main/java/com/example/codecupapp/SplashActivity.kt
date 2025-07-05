package com.example.codecupapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.codecupapp.data.SharedPrefsManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        lifecycleScope.launch {
            delay(100) //

            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val localProfile = SharedPrefsManager.loadUserProfile(this@SplashActivity)

            if (firebaseUser != null && localProfile != null) {
                Log.d("SplashActivity", "Auto-login with local profile: $localProfile")

                // Go directly to main
                val intent = Intent(this@SplashActivity, MainActivity::class.java).apply {
                    putExtra("startFromHome", true)
                }
                startActivity(intent)

            } else {
                Log.w(
                    "SplashActivity",
                    "No saved profile or not authenticated. Redirecting to Intro."
                )
                // Send user to login flow
                startActivity(Intent(this@SplashActivity, IntroActivity::class.java))
            }

            finish()
        }
    }
}
