package com.example.donorlk.controllers

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.*
import com.example.donorlk.R
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class VerificationController : BaseActivity() {
    private lateinit var emailDisplay: TextView
    private lateinit var verificationInput: EditText
    private lateinit var verifyButton: Button
    private lateinit var timerText: TextView
    private lateinit var resendButton: TextView
    private lateinit var backButton: ImageView
    private var countDownTimer: CountDownTimer? = null

    private lateinit var auth: FirebaseAuth
    private var userName: String = ""
    private var userUid: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification_page)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize views
        initializeViews()
        setupClickListeners()

        // Get data from intent and display email
        val email = intent.getStringExtra("email") ?: ""
        userName = intent.getStringExtra("user_name") ?: ""
        userUid = intent.getStringExtra("user_uid") ?: ""

        emailDisplay.text = email

        // Start initial timer
        startResendTimer()
    }

    private fun initializeViews() {
        emailDisplay = findViewById(R.id.emailDisplay)
        verificationInput = findViewById(R.id.verificationCodeInput)
        verifyButton = findViewById(R.id.verifyButton)
        timerText = findViewById(R.id.timerText)
        resendButton = findViewById(R.id.resendButton)
        backButton = findViewById(R.id.backButton)

        // Hide the verification code input since Firebase uses email links
        verificationInput.visibility = android.view.View.GONE

        // Update button text to be more clear
        verifyButton.text = "Check Verification Status"
    }

    private fun setupClickListeners() {
        verifyButton.setOnClickListener {
            checkEmailVerification()
        }

        backButton.setOnClickListener {
            finish()
        }

        resendButton.setOnClickListener {
            resendVerificationEmail()
        }
    }

    private fun checkEmailVerification() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not found. Please register again.", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading
        verifyButton.isEnabled = false
        verifyButton.text = "Checking..."

        // Reload user to get updated email verification status
        currentUser.reload().addOnCompleteListener { reloadTask ->
            if (reloadTask.isSuccessful) {
                val isEmailVerified = currentUser.isEmailVerified

                verifyButton.isEnabled = true
                verifyButton.text = "Verify Email"

                if (isEmailVerified) {
                    Log.d("EmailVerification", "Email verified successfully")

                    // Now create the Firestore record since email is verified
                    createUserInFirestore(currentUser)
                } else {
                    Toast.makeText(this, "Email not verified yet. Please check your email and click the verification link.", Toast.LENGTH_LONG).show()
                }
            } else {
                verifyButton.isEnabled = true
                verifyButton.text = "Verify Email"
                Log.w("EmailVerification", "Failed to reload user", reloadTask.exception)
                Toast.makeText(this, "Failed to check verification status. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createUserInFirestore(firebaseUser: com.google.firebase.auth.FirebaseUser) {
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        // Create user document in Firestore now that email is verified
        val userData = com.example.donorlk.models.User(
            uid = firebaseUser.uid,
            name = userName,
            email = firebaseUser.email ?: "",
            role = "donator" // Default role for self-registration
        )

        firestore.collection("users").document(firebaseUser.uid)
            .set(userData)
            .addOnSuccessListener {
                Log.d("Firestore", "User document created successfully after email verification")
                Toast.makeText(this, "Email verified successfully!", Toast.LENGTH_SHORT).show()

                // Navigate to ProfileSetupController
                val intent = Intent(this, ProfileSetupController::class.java).apply {
                    putExtra("user_email", firebaseUser.email)
                    putExtra("user_name", userName)
                    putExtra("user_uid", userUid)
                }
                startActivity(intent)
                finishAffinity()
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error creating user document", e)
                Toast.makeText(this, "Email verified but failed to create profile. Please try again.", Toast.LENGTH_LONG).show()
            }
    }

    private fun resendVerificationEmail() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not found. Please register again.", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("EmailVerification", "Attempting to resend verification email to: ${currentUser.email}")

        // Send email verification with additional settings for better delivery
        val actionCodeSettings = com.google.firebase.auth.ActionCodeSettings.newBuilder()
            .setHandleCodeInApp(false) // Handle in email client, not in app
            .build()

        currentUser.sendEmailVerification(actionCodeSettings)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("EmailVerification", "Verification email resent successfully to: ${currentUser.email}")
                    Toast.makeText(this, "Verification email sent! Please check your email and spam folder.", Toast.LENGTH_LONG).show()
                    startResendTimer()
                } else {
                    Log.e("EmailVerification", "Failed to resend verification email", task.exception)
                    val errorMessage = when {
                        task.exception?.message?.contains("too-many-requests") == true ->
                            "Too many requests. Please wait before requesting another email."
                        task.exception?.message?.contains("user-disabled") == true ->
                            "Your account has been disabled. Please contact support."
                        else -> "Failed to resend verification email. Please try again later."
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun startResendTimer() {
        countDownTimer?.cancel()
        resendButton.isEnabled = false

        countDownTimer = object : CountDownTimer(5 * 60 * 1000, 1000) { // 5 minutes
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = millisUntilFinished / 1000 % 60
                timerText.text = String.format(
                    Locale.getDefault(),
                    getString(R.string.timer_format),
                    minutes,
                    seconds
                )
            }

            override fun onFinish() {
                timerText.text = getString(R.string.timer_finished)
                resendButton.isEnabled = true
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
