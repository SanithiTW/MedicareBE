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
import com.example.donorlk.models.User
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
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
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth, Firestore and Credential Manager
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
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
            loginWithEmailPassword()
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

    private fun loginWithEmailPassword() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (email.isEmpty()) {
            emailEditText.error = "Email is required"
            return
        }
        if (password.isEmpty()) {
            passwordEditText.error = "Password is required"
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Enter a valid email"
            return
        }

        // Show loading (you can add a progress bar if needed)
        loginButton.isEnabled = false
        loginButton.text = "Logging in..."

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                loginButton.isEnabled = true
                loginButton.text = "Login"

                if (task.isSuccessful) {
                    Log.d("EmailLogin", "signInWithEmail:success")
                    val user = auth.currentUser
                    user?.let {
                        checkUserRoleAndRedirect(it.uid)
                    }
                } else {
                    Log.w("EmailLogin", "signInWithEmail:failure", task.exception)
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkUserRoleAndRedirect(uid: String) {
        firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    val role = user?.role ?: "donator"

                    when (role) {
                        "donator" -> {
                            startActivity(Intent(this, HomePageController::class.java))
                            finish()
                        }
                        // Add more roles here as needed
                        // "admin" -> startActivity(Intent(this, AdminDashboardController::class.java))
                        else -> {
                            // Default to donator home page
                            startActivity(Intent(this, HomePageController::class.java))
                            finish()
                        }
                    }
                } else {
                    // User document doesn't exist, create one with default role
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        val newUser = User(
                            uid = currentUser.uid,
                            name = currentUser.displayName ?: "",
                            email = currentUser.email ?: "",
                            role = "donator"
                        )

                        firestore.collection("users").document(currentUser.uid)
                            .set(newUser)
                            .addOnSuccessListener {
                                startActivity(Intent(this, HomePageController::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error creating user document", e)
                                startActivity(Intent(this, HomePageController::class.java))
                                finish()
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error getting user document", e)
                Toast.makeText(this, "Error retrieving user data", Toast.LENGTH_SHORT).show()
                // Default redirect to home page
                startActivity(Intent(this, HomePageController::class.java))
                finish()
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
                    Log.d("GoogleSignIn", "signInWithCredential:success")
                    val user = auth.currentUser
                    user?.let {
                        Toast.makeText(this, "Welcome ${user.displayName}", Toast.LENGTH_SHORT).show()
                        checkUserRoleAndRedirect(it.uid)
                    }
                } else {
                    Log.w("GoogleSignIn", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
