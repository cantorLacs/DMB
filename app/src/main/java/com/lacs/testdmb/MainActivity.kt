    package com.lacs.testdmb

    import android.content.Intent
    import android.os.Bundle
    import android.widget.Button
    import androidx.activity.enableEdgeToEdge
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.view.ViewCompat
    import androidx.core.view.WindowInsetsCompat
    import com.google.android.gms.auth.api.signin.GoogleSignIn
    import com.google.android.gms.auth.api.signin.GoogleSignInOptions
    import com.google.firebase.auth.FirebaseAuth



    class MainActivity : AppCompatActivity() {


        private lateinit var auth: FirebaseAuth


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
        }

        private fun initializeAuth() {
            // Initialize Firebase Auth
            auth = FirebaseAuth.getInstance()

            // Sign-Out setup
            setupLogout()
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

    }