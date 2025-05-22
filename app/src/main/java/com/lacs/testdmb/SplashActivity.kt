package com.lacs.testdmb

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Run animation
        val fadeImage = findViewById<ImageView>(R.id.bg_fade)
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        fadeImage.visibility = View.VISIBLE
        fadeImage.startAnimation(fadeIn)

        // Check authentication state after animation
        Handler(Looper.getMainLooper()).postDelayed({
            checkAuthState()
        }, 2000) // 2 sec splash
    }

    private fun checkAuthState() {
        // Check if user is signed in
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // User is already signed in, redirect to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // No user is signed in, redirect to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Close this activity
        finish()
    }
}