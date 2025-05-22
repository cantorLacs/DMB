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
import com.google.firebase.auth.FirebaseAuthMultiFactorException
import com.google.firebase.auth.MultiFactorResolver
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneMultiFactorGenerator
import com.google.firebase.auth.PhoneMultiFactorInfo
import java.util.concurrent.TimeUnit

class MfaVerificationActivity : AppCompatActivity() {

    private lateinit var codeEditText: EditText
    private lateinit var verifyButton: Button
    private lateinit var statusTextView: TextView
    private lateinit var progressBar: ProgressBar

    private var verificationId: String? = null
    private var resolver: MultiFactorResolver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mfa_verification)

        // Initialize UI components
        initUI()

        // Set up listeners
        setupClickListeners()

        // Get the resolver from the intent
        if (savedInstanceState == null) {
            handleIntent()
        }
    }

    private fun initUI() {
        codeEditText = findViewById(R.id.editTextVerificationCode)
        verifyButton = findViewById(R.id.buttonVerifyCode)
        statusTextView = findViewById(R.id.textViewStatus)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupClickListeners() {
        verifyButton.setOnClickListener {
            val code = codeEditText.text.toString().trim()
            if (code.isEmpty()) {
                Toast.makeText(this, "Please enter verification code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            verifyCode(code)
        }
    }

    private fun handleIntent() {
        // Get the serialized resolver from intent
        resolver = intent.getParcelableExtra(EXTRA_RESOLVER)
        
        if (resolver == null) {
            Toast.makeText(this, "Error: Missing multi-factor information", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Display hint about which phone number the code is being sent to
        val selectedHint = resolver?.hints?.get(0)
        if (selectedHint is com.google.firebase.auth.PhoneMultiFactorInfo) {
            val phoneNumber = selectedHint.phoneNumber
            statusTextView.text = "Verifying with $phoneNumber"
        }

        // Start the verification process
        startVerification()
    }

    private fun startVerification() {
        showProgress(true)

        val selectedHint = resolver?.hints?.get(0)
        
        if (selectedHint == null) {
            showProgress(false)
            Toast.makeText(this, "Error: No verification method available", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-verification typically won't happen for MFA
                Log.d(TAG, "onVerificationCompleted: $credential")
                showProgress(false)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e(TAG, "onVerificationFailed", e)
                showProgress(false)
                statusTextView.text = "Verification failed: ${e.message}"
                Toast.makeText(this@MfaVerificationActivity, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                showProgress(false)
                this@MfaVerificationActivity.verificationId = verificationId
                statusTextView.text = "Verification code sent"
                Toast.makeText(this@MfaVerificationActivity, "Verification code sent", Toast.LENGTH_SHORT).show()
            }
        }

        // Get multi-factor session
        val session = resolver?.session ?: return
        
        val phoneAuthOptions = PhoneAuthOptions.newBuilder()
            .setMultiFactorHint(selectedHint as PhoneMultiFactorInfo)
            .setMultiFactorSession(session)
            .setActivity(this)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions)
    }

    private fun verifyCode(code: String) {
        showProgress(true)

        if (verificationId == null || resolver == null) {
            showProgress(false)
            Toast.makeText(this, "Verification process not initialized properly", Toast.LENGTH_SHORT).show()
            return
        }

        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        val multiFactorAssertion = PhoneMultiFactorGenerator.getAssertion(credential)

        resolver!!.resolveSignIn(multiFactorAssertion)
            .addOnCompleteListener { task ->
                showProgress(false)
                
                if (task.isSuccessful) {
                    Toast.makeText(this, "Successfully authenticated!", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Log.e(TAG, "resolveSignIn failed", task.exception)
                    statusTextView.text = "Verification failed: ${task.exception?.message}"
                    Toast.makeText(this, "Verification failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun showProgress(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        verifyButton.isEnabled = !show
        codeEditText.isEnabled = !show
    }

    companion object {
        private const val TAG = "MfaVerificationActivity"
        const val EXTRA_RESOLVER = "multi_factor_resolver"
        const val REQUEST_CODE_MFA = 100
    }
}
