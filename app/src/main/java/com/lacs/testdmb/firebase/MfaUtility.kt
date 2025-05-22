package com.lacs.testdmb.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.MultiFactorInfo
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneMultiFactorGenerator
import com.google.firebase.auth.PhoneMultiFactorInfo

import java.util.concurrent.TimeUnit

/**
 * Utility class for handling Firebase Multi-Factor Authentication operations
 */
class MfaUtility {
    companion object {
        private val auth: FirebaseAuth = FirebaseAuth.getInstance()

        /**
         * Check if the user has MFA enabled
         */
        fun isUserMfaEnabled(user: FirebaseUser): Boolean {
            return user.multiFactor.enrolledFactors.isNotEmpty()
        }

        /**
         * Get user's enrolled second factors
         */
        fun getEnrolledFactors(user: FirebaseUser): List<MultiFactorInfo> {
            return user.multiFactor.enrolledFactors
        }

        /**
         * Get the phone number associated with a PhoneMultiFactorInfo
         */
        fun getPhoneNumberForFactor(multiFactorInfo: MultiFactorInfo): String? {
            if (multiFactorInfo is PhoneMultiFactorInfo) {
                return multiFactorInfo.phoneNumber
            }
            return null
        }

        /**
         * Start the phone number verification process for MFA enrollment
         */
        fun startMfaPhoneVerification(
            user: FirebaseUser,
            phoneNumber: String,
            activity: android.app.Activity,
            callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
        ) {
            val session = user.multiFactor.session
            session.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val multiFactorSession = task.result

                    val options = PhoneAuthOptions.newBuilder()
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(activity)
                        .setMultiFactorSession(multiFactorSession)
                        .setCallbacks(callbacks)
                        .build()

                    PhoneAuthProvider.verifyPhoneNumber(options)
                }
            }
        }

        /**
         * Finalize the MFA enrollment with the verification code
         */
        fun finalizeMfaEnrollment(
            user: FirebaseUser,
            verificationId: String,
            code: String,
            displayName: String,
            onComplete: (Boolean, Exception?) -> Unit
        ) {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            val multiFactorAssertion = PhoneMultiFactorGenerator.getAssertion(credential)

            user.multiFactor.enroll(multiFactorAssertion, displayName)
                .addOnCompleteListener { task ->
                    onComplete(task.isSuccessful, task.exception)
                }
        }

        /**
         * Unenroll an MFA factor
         */
        fun unenrollFactor(
            user: FirebaseUser,
            factorInfo: MultiFactorInfo,
            onComplete: (Boolean, Exception?) -> Unit
        ) {
            user.multiFactor.unenroll(factorInfo)
                .addOnCompleteListener { task ->
                    onComplete(task.isSuccessful, task.exception)
                }
        }
    }
}
