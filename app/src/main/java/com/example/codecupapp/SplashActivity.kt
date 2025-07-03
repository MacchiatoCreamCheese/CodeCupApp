package com.example.codecupapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val auth = FirebaseAuth.getInstance()

        Handler(mainLooper).postDelayed({
            if (auth.currentUser != null) {
                // 🔐 Already logged in → Skip intro/auth and go to home
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("startFromHome", true)
                }
                startActivity(intent)
            } else {
                // 🔓 Not logged in → Show intro screen
                startActivity(Intent(this, IntroActivity::class.java))
            }
            finish()
        }, 1500) // Optional: can be 500ms if you want it faster
    }
}
