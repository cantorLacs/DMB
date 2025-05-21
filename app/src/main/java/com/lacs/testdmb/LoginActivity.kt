package com.lacs.testdmb

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    
    private lateinit var auth: FirebaseAuth
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        val editTextEmail = findViewById<EditText>(R.id.editTextEmail)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val buttonLogin = findViewById<Button>(R.id.buttonLogin)
        val buttonGoToRegister = findViewById<Button>(R.id.buttonGoToRegister)

        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Show loading message
                Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()
                
                // Sign in with Firebase Authentication
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Login successful
                            Toast.makeText(this, "Logged in successfully", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            // Login failed
                            Toast.makeText(this, "Authentication failed: ${task.exception?.message}", 
                                Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        buttonGoToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        
        // Add "I forgot my password" functionality
        val textViewForgotPassword = findViewById<TextView>(R.id.textViewForgotPassword)
        
        textViewForgotPassword.setOnClickListener {
            // Get email from the input field (if provided)
            val email = editTextEmail.text.toString().trim()
            
            // Show the bottom sheet
            val forgotPasswordSheet = ForgotPasswordBottomSheet.newInstance(email)
            forgotPasswordSheet.show(supportFragmentManager, ForgotPasswordBottomSheet.TAG)
        }
    }
}
