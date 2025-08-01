package com.example.donorlk.controllers

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.donorlk.R
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordController : BaseActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var resetPasswordButton: Button
    private lateinit var backToLoginText: TextView

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize views
        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        emailEditText = findViewById(R.id.emailEditText)
        resetPasswordButton = findViewById(R.id.resetPasswordButton)
        backToLoginText = findViewById(R.id.backToLoginText)
    }

    private fun setupClickListeners() {
        resetPasswordButton.setOnClickListener {
            sendPasswordResetEmail()
        }

        backToLoginText.setOnClickListener {
            navigateBackToLogin()
        }
    }

    private fun sendPasswordResetEmail() {
        val email = emailEditText.text.toString().trim()

        // Validate email input
        if (email.isEmpty()) {
            emailEditText.error = "Email is required"
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Enter a valid email address"
            return
        }

        // Show loading state
        resetPasswordButton.isEnabled = false
        resetPasswordButton.text = "Sending..."

        // Send password reset email using Firebase Auth
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                // Reset button state
                resetPasswordButton.isEnabled = true
                resetPasswordButton.text = "Send Reset Email"

                if (task.isSuccessful) {
                    Log.d("ForgotPassword", "Password reset email sent successfully")

                    // Show success message
                    Toast.makeText(
                        this,
                        "Password reset email sent to $email. Please check your inbox.",
                        Toast.LENGTH_LONG
                    ).show()

                    // Clear the email field
                    emailEditText.text?.clear()

                    // Optionally navigate back to login after a delay
                    // You can uncomment the next line if you want automatic navigation
                    // Handler(Looper.getMainLooper()).postDelayed({ navigateBackToLogin() }, 2000)

                } else {
                    Log.w("ForgotPassword", "Password reset email failed", task.exception)

                    // Handle specific error cases
                    val errorMessage = when (task.exception?.message) {
                        "There is no user record corresponding to this identifier. The user may have been deleted." ->
                            "No account found with this email address."
                        "The email address is badly formatted." ->
                            "Please enter a valid email address."
                        else ->
                            "Failed to send reset email. Please try again."
                    }

                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun navigateBackToLogin() {
        val intent = Intent(this, LoginController::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        navigateBackToLogin()
    }
}
