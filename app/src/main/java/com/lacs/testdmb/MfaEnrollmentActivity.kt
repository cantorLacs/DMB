package com.lacs.testdmb

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.lacs.testdmb.firebase.MfaUtility
import java.util.concurrent.TimeUnit

class MfaEnrollmentActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var phoneEditText: EditText
    private lateinit var verificationCodeEditText: EditText
    private lateinit var sendCodeButton: Button
    private lateinit var verifyCodeButton: Button
    private lateinit var statusTextView: TextView
    private lateinit var progressBar: ProgressBar

    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mfa_enrollment)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize UI components
        initUI()

        // Set up listeners
        setupClickListeners()
    }

    private fun initUI() {
        phoneEditText = findViewById(R.id.editTextPhone)
        verificationCodeEditText = findViewById(R.id.editTextVerificationCode)
        sendCodeButton = findViewById(R.id.buttonSendCode)
        verifyCodeButton = findViewById(R.id.buttonVerifyCode)
        statusTextView = findViewById(R.id.textViewStatus)
        progressBar = findViewById(R.id.progressBar)

        // Initially hide the verification code section
        verificationCodeEditText.visibility = View.GONE
        verifyCodeButton.visibility = View.GONE
    }

    private fun setupClickListeners() {
        sendCodeButton.setOnClickListener {
            val phoneNumber = phoneEditText.text.toString().trim()
            if (phoneNumber.isEmpty()) {
                Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Format the phone number if needed (ensure it has a country code)
            val formattedNumber = if (!phoneNumber.startsWith("+")) {
                "+1$phoneNumber" // Default to US country code if none provided
            } else {
                phoneNumber
            }
            
            startPhoneVerification(formattedNumber)
        }

        verifyCodeButton.setOnClickListener {
            val code = verificationCodeEditText.text.toString().trim()
            if (code.isEmpty()) {
                Toast.makeText(this, "Please enter verification code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            verifyCode(code)
        }
    }

    private fun startPhoneVerification(phoneNumber: String) {
        showProgress(true)
        statusTextView.text = "Sending verification code..."

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This is called if the phone number is verified automatically
                // This typically won't happen for MFA enrollment
                showProgress(false)
                statusTextView.text = "Verification completed automatically"
                Log.d(TAG, "onVerificationCompleted: $credential")
            }

            override fun onVerificationFailed(e: FirebaseException) {
                showProgress(false)
                statusTextView.text = "Verification failed: ${e.message}"
                Log.e(TAG, "onVerificationFailed", e)
                Toast.makeText(this@MfaEnrollmentActivity, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // Save verification ID and resending token for later
                showProgress(false)
                this@MfaEnrollmentActivity.verificationId = verificationId
                this@MfaEnrollmentActivity.resendToken = token
                
                statusTextView.text = "Verification code sent to $phoneNumber"
                Toast.makeText(this@MfaEnrollmentActivity, "Verification code sent", Toast.LENGTH_SHORT).show()
                
                // Show verification code input
                verificationCodeEditText.visibility = View.VISIBLE
                verifyCodeButton.visibility = View.VISIBLE
            }
        }

        // Get current user
        val user = auth.currentUser
        if (user != null) {
            MfaUtility.startMfaPhoneVerification(user, phoneNumber, this, callbacks)
        } else {
            showProgress(false)
            statusTextView.text = "Error: No authenticated user found"
            Toast.makeText(this, "You need to be logged in to enable MFA", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verifyCode(code: String) {
        showProgress(true)
        statusTextView.text = "Verifying code..."

        val user = auth.currentUser
        if (user != null && verificationId != null) {
            MfaUtility.finalizeMfaEnrollment(
                user,
                verificationId!!,
                code,
                "My Phone Number", // Display name for this factor
            ) { isSuccessful, exception ->
                showProgress(false)

                if (isSuccessful) {
                    statusTextView.text = "MFA enrolled successfully!"
                    Toast.makeText(this, "MFA enrolled successfully!", Toast.LENGTH_SHORT).show()
                    finish() // Return to previous screen
                } else {
                    statusTextView.text = "MFA enrollment failed: ${exception?.message}"
                    Toast.makeText(this, "MFA enrollment failed: ${exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            showProgress(false)
            statusTextView.text = "Error: Missing verification ID or user"
            Toast.makeText(this, "Error: Missing verification ID or user", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showProgress(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        sendCodeButton.isEnabled = !show
        verifyCodeButton.isEnabled = !show
    }

    companion object {
        private const val TAG = "MfaEnrollmentActivity"
    }
}
