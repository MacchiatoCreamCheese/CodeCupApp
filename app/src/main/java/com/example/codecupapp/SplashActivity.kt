package com.example.codecupapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // ✅ Launch coroutine on main thread
        lifecycleScope.launch {
            delay(1000) // ⏳ Splash screen delay

            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                try {
                    // 🧠 Suspend and wait for profile load
                    val profile = ProfileRepository.loadUserProfileSuspend()
                    Log.d("SplashActivity", "User profile loaded: $profile")

                    // ✅ Navigate to Main with profile
                    val intent = Intent(this@SplashActivity, MainActivity::class.java).apply {
                        putExtra("startFromHome", true)
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("SplashActivity", "Error loading profile: ${e.message}")

                    // ⚠️ Still let user into app even if profile load fails
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                }
            } else {
                // 🔓 Not logged in → Go to intro screen
                startActivity(Intent(this@SplashActivity, IntroActivity::class.java))
            }

            finish()
        }
    }
}
