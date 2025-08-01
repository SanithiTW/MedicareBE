package com.example.donorlk.controllers

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.example.donorlk.R
import com.example.donorlk.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegistrationController : BaseActivity() {
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var googleLoginButton: Button
    private lateinit var loginPrompt: TextView
    private lateinit var backButton: ImageView

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize views first
        initializeViews()
        // Then set up click listeners
        setupClickListeners()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        registerButton = findViewById(R.id.loginButton) // Note: ID is loginButton in layout
        googleLoginButton = findViewById(R.id.googleLoginButton)
        loginPrompt = findViewById(R.id.loginPrompt)
    }

    private fun setupClickListeners() {
        registerButton.setOnClickListener {
            registerWithEmailPassword()
        }

        googleLoginButton.setOnClickListener {
            Toast.makeText(this, "Google sign up clicked", Toast.LENGTH_SHORT).show()
        }

        backButton.setOnClickListener {
            finish()
        }

        loginPrompt.setOnClickListener {
            // Go back to login
            finish()
        }
    }

    private fun registerWithEmailPassword() {
        if (!validateInput()) return

        val name = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Show loading
        registerButton.isEnabled = false
        registerButton.text = "Registering..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("EmailRegistration", "createUserWithEmail:success")
                    val firebaseUser = auth.currentUser

                    firebaseUser?.let { user ->
                        Log.d("EmailVerification", "User created: ${user.email}, UID: ${user.uid}")
                        Log.d("EmailVerification", "User email verified status: ${user.isEmailVerified}")

                        // Try simple email verification first (without ActionCodeSettings)
                        user.sendEmailVerification()
                            .addOnCompleteListener { verificationTask ->
                                registerButton.isEnabled = true
                                registerButton.text = "Register"

                                if (verificationTask.isSuccessful) {
                                    Log.d("EmailVerification", "✅ Email verification sent successfully to: ${user.email}")
                                    Toast.makeText(this, "Email sending successful! Please check your email (including spam folder) for verification.", Toast.LENGTH_LONG).show()

                                    val intent = Intent(this, VerificationController::class.java).apply {
                                        putExtra("email", email)
                                        putExtra("user_name", name)
                                        putExtra("user_uid", user.uid)
                                    }
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Log.e("EmailVerification", "❌ Failed to send verification email", verificationTask.exception)
                                    Log.e("EmailVerification", "Error details: ${verificationTask.exception?.message}")

                                    val errorMessage = verificationTask.exception?.message ?: "Unknown error"
                                    Toast.makeText(this, "Failed to send verification email: $errorMessage", Toast.LENGTH_LONG).show()

                                    // Still navigate to verification page so user can try resend
                                    val intent = Intent(this, VerificationController::class.java).apply {
                                        putExtra("email", email)
                                        putExtra("user_name", name)
                                        putExtra("user_uid", user.uid)
                                    }
                                    startActivity(intent)
                                    finish()
                                }
                            }
                    }
                } else {
                    Log.w("EmailRegistration", "createUserWithEmail:failure", task.exception)
                    registerButton.isEnabled = true
                    registerButton.text = "Register"

                    val errorMessage = when {
                        task.exception?.message?.contains("email address is already in use") == true ->
                            "An account with this email already exists"
                        task.exception?.message?.contains("weak password") == true ->
                            "Password is too weak. Please choose a stronger password"
                        else -> "Registration failed: ${task.exception?.message}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun validateInput(): Boolean {
        if (nameEditText.text.isEmpty()) {
            nameEditText.error = "Name is required"
            return false
        }
        if (emailEditText.text.isEmpty()) {
            emailEditText.error = "Email is required"
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailEditText.text).matches()) {
            emailEditText.error = "Enter a valid email"
            return false
        }
        if (passwordEditText.text.isEmpty()) {
            passwordEditText.error = "Password is required"
            return false
        }
        if (passwordEditText.text.length < 6) {
            passwordEditText.error = "Password must be at least 6 characters"
            return false
        }
        if (confirmPasswordEditText.text.isEmpty()) {
            confirmPasswordEditText.error = "Please confirm your password"
            return false
        }
        if (passwordEditText.text.toString() != confirmPasswordEditText.text.toString()) {
            confirmPasswordEditText.error = "Passwords do not match"
            return false
        }
        return true
    }
}
