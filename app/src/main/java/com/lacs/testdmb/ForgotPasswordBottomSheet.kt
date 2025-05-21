package com.lacs.testdmb

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordBottomSheet : BottomSheetDialogFragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_forgot_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val editTextResetEmail = view.findViewById<TextInputEditText>(R.id.editTextResetEmail)
        val buttonResetPassword = view.findViewById<Button>(R.id.buttonResetPassword)

        // Pre-populate email if available from arguments
        arguments?.getString("email")?.let {
            editTextResetEmail.setText(it)
        }

        buttonResetPassword.setOnClickListener {
            val email = editTextResetEmail.text.toString().trim()
            
            if (email.isEmpty()) {
                Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show loading state
            buttonResetPassword.isEnabled = false
            buttonResetPassword.text = "Sending..."

            // Send password reset email
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            context,
                            "Reset link sent to your email",
                            Toast.LENGTH_LONG
                        ).show()
                        dismiss() // Close bottom sheet
                    } else {
                        Toast.makeText(
                            context,
                            "Error: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        // Reset button state
                        buttonResetPassword.isEnabled = true
                        buttonResetPassword.text = "Send Reset Link"
                    }
                }
        }
    }

    companion object {
        const val TAG = "ForgotPasswordBottomSheet"
        
        fun newInstance(email: String?): ForgotPasswordBottomSheet {
            val fragment = ForgotPasswordBottomSheet()
            val args = Bundle()
            if (email != null) {
                args.putString("email", email)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
