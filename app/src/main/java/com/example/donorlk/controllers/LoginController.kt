package com.example.donorlk.controllers

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.donorlk.R

class LoginController : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var googleLoginButton: Button
    private lateinit var forgotPasswordText: TextView
    private lateinit var signupText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        googleLoginButton = findViewById(R.id.googleLoginButton)
        forgotPasswordText = findViewById(R.id.forgotPasswordText)
        signupText = findViewById(R.id.signupText)
    }

    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            // For display purposes, just check if fields are not empty
            if (emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()) {
                startActivity(Intent(this, DonationFormController::class.java))
                finish()
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        googleLoginButton.setOnClickListener {
            Toast.makeText(this, "Google login clicked", Toast.LENGTH_SHORT).show()
        }

        forgotPasswordText.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordController::class.java))
        }

        signupText.setOnClickListener {
            startActivity(Intent(this, RegistrationController::class.java))
        }
    }
}
