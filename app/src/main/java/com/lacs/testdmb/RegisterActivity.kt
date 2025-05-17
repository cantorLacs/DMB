package com.lacs.testdmb

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import model.User


private lateinit var auth: FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val email = findViewById<EditText>(R.id.editTextEmail)
        val password = findViewById<EditText>(R.id.editTextPassword)
        val confirmPassword = findViewById<EditText>(R.id.editTextConfirmPassword)
        val registerButton = findViewById<Button>(R.id.buttonRegister)

        registerButton.setOnClickListener {

            val emailText = email.text.toString()
            val passwordText = password.text.toString()
            val confirmText = confirmPassword.text.toString()

            if (emailText.isEmpty() || passwordText.isEmpty() || confirmText.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            } else if (passwordText != confirmText) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                val user = User(email = emailText, password = passwordText)


                val key = Users.push().key

                if (key != null) {

                    Users.child(key).setValue(user)
                        .addOnCompleteListener { task ->

                            if (task.isSuccessful) {
                                Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT).show()

                            } else {
                                Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }

                        }
                } else {
                    Toast.makeText(this, "Database key generation failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
