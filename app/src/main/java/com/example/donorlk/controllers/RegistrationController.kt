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
                        // Create user document in Firestore
                        val userData = User(
                            uid = user.uid,
                            name = name,
                            email = email,
                            role = "donator" // Default role for self-registration
                        )

                        saveUserToFirestore(userData)
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

    private fun saveUserToFirestore(user: User) {
        firestore.collection("users").document(user.uid)
            .set(user)
            .addOnSuccessListener {
                Log.d("Firestore", "User document created successfully")
                registerButton.isEnabled = true
                registerButton.text = "Register"

                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()

                // Navigate to ProfileSetupController to complete profile
                val intent = Intent(this, ProfileSetupController::class.java).apply {
                    putExtra("user_email", user.email)
                    putExtra("user_name", user.name)
                    putExtra("user_uid", user.uid)
                }
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error creating user document", e)
                registerButton.isEnabled = true
                registerButton.text = "Register"

                Toast.makeText(this, "Registration completed but failed to save profile. Please complete your profile later.", Toast.LENGTH_LONG).show()

                // Still navigate to profile setup
                val intent = Intent(this, ProfileSetupController::class.java).apply {
                    putExtra("user_email", user.email)
                    putExtra("user_name", user.name)
                    putExtra("user_uid", user.uid)
                }
                startActivity(intent)
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
