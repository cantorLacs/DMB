    package com.lacs.testdmb

    import android.content.Intent
    import android.os.Bundle
    import android.widget.Button
    import android.widget.TextView
    import android.widget.Toast
    import androidx.activity.enableEdgeToEdge
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.view.ViewCompat
    import androidx.core.view.WindowInsetsCompat
    import com.google.android.gms.auth.api.signin.GoogleSignIn
    import com.google.android.gms.auth.api.signin.GoogleSignInOptions
    import com.google.firebase.auth.FirebaseAuth
    import com.lacs.testdmb.firebase.MfaUtility



    class MainActivity : AppCompatActivity() {


        private lateinit var auth: FirebaseAuth
        private lateinit var textViewMfaStatus: TextView
        private lateinit var buttonEnableMfa: Button


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContentView(R.layout.activity_main)
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

            initializeAuth()
            initializeMfaUI()
            updateMfaStatus()
        }

        private fun initializeAuth() {
            // Initialize Firebase Auth
            auth = FirebaseAuth.getInstance()

            // Sign-Out setup
            setupLogout()
        }

        private fun initializeMfaUI() {
            textViewMfaStatus = findViewById(R.id.textViewMfaStatus)
            buttonEnableMfa = findViewById(R.id.buttonManageMfa)
            
            buttonEnableMfa.setOnClickListener {
                val user = auth.currentUser
                if (user != null) {
                    if (MfaUtility.isUserMfaEnabled(user)) {
                        // Show a message or navigate to a different screen to manage MFA
                        Toast.makeText(this, "MFA is already enabled", Toast.LENGTH_SHORT).show()
                    } else {
                        // Navigate to MFA enrollment activity
                        startActivity(Intent(this, MfaEnrollmentActivity::class.java))
                    }
                } else {
                    Toast.makeText(this, "You need to be logged in to enable MFA", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        private fun updateMfaStatus() {
            val user = auth.currentUser
            if (user != null) {
                val isMfaEnabled = MfaUtility.isUserMfaEnabled(user)
                textViewMfaStatus.text = if (isMfaEnabled) {
                    "Two-Factor Authentication: Enabled"
                } else {
                    "Two-Factor Authentication: Disabled"
                }
                
                buttonEnableMfa.text = if (isMfaEnabled) {
                    "Manage 2FA"
                } else {
                    "Enable 2FA"
                }
            }
        }

        private fun setupLogout() {
            val buttonLogout = findViewById<Button>(R.id.buttonLogout)
            buttonLogout.setOnClickListener {
                // Sign out from Firebase
                FirebaseAuth.getInstance().signOut()

                // Sign out from Google
                val googleSignInClient = GoogleSignIn.getClient(
                    this,
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                )
                googleSignInClient.signOut()

                // Go to login
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
        
        override fun onResume() {
            super.onResume()
            // Update MFA status when returning to this activity
            updateMfaStatus()
        }

    }