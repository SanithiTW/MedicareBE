package com.example.donorlk.controllers

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.donorlk.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class LoginController : BaseActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var googleLoginButton: Button
    private lateinit var forgotPasswordText: TextView
    private lateinit var signupText: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth and Credential Manager
        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

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
                startActivity(Intent(this, HomePageController::class.java))
                finish()
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        googleLoginButton.setOnClickListener {
            signInWithGoogle()
        }

        forgotPasswordText.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordController::class.java))
        }

        signupText.setOnClickListener {
            startActivity(Intent(this, RegistrationController::class.java))
        }
    }

    private fun signInWithGoogle() {
        try {
            // Use hardcoded fallback for now - this will be replaced when you update google-services.json
            val webClientId = try {
                getString(R.string.default_web_client_id)
            } catch (e: Exception) {
                "YOUR_WEB_CLIENT_ID_HERE"
            }

            if (webClientId == "YOUR_WEB_CLIENT_ID_HERE") {
                Toast.makeText(this, "Please configure Google Sign-In in Firebase Console first", Toast.LENGTH_LONG).show()
                return
            }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            lifecycleScope.launch {
                try {
                    val result = credentialManager.getCredential(
                        request = request,
                        context = this@LoginController,
                    )

                    val credential = result.credential
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val googleIdToken = googleIdTokenCredential.idToken

                    firebaseAuthWithGoogle(googleIdToken)

                } catch (e: GetCredentialException) {
                    Log.e("GoogleSignIn", "Google Sign-In failed", e)
                    Toast.makeText(this@LoginController, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Google Sign-In configuration error", e)
            Toast.makeText(this, "Please complete Google Sign-In setup in Firebase Console", Toast.LENGTH_LONG).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    Log.d("GoogleSignIn", "signInWithCredential:success")
                    val user = auth.currentUser
                    Toast.makeText(this, "Welcome ${user?.displayName}", Toast.LENGTH_SHORT).show()

                    // Navigate to home page
                    startActivity(Intent(this, HomePageController::class.java))
                    finish()
                } else {
                    // Sign in failed
                    Log.w("GoogleSignIn", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
