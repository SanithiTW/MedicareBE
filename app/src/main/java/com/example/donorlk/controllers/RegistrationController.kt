package com.example.donorlk.controllers

import android.content.Intent
import android.os.Bundle
import android.widget.*
import com.example.donorlk.R

class RegistrationController : BaseActivity() {
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var googleLoginButton: Button
    private lateinit var loginPrompt: TextView
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

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
        registerButton = findViewById(R.id.loginButton) // Note: ID is loginButton in layout
        googleLoginButton = findViewById(R.id.googleLoginButton)
        loginPrompt = findViewById(R.id.loginPrompt)
    }

    private fun setupClickListeners() {
        registerButton.setOnClickListener {
            if (validateInput()) {
                val intent = Intent(this, ProfileSetupController::class.java).apply {
                    putExtra("user_email", emailEditText.text.toString())
                    putExtra("user_name", nameEditText.text.toString())
                }
                startActivity(intent)
            }
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
        return true
    }
}
