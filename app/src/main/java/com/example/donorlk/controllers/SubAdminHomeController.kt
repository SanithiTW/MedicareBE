package com.example.donorlk.controllers

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.donorlk.CreateOperatorsActivity
import com.example.donorlk.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SubAdminHomeController : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var donationCenterCard: CardView
    private lateinit var createOperatorCard: CardView
    private lateinit var deleteOperatorCard: CardView
    private lateinit var welcomeText: TextView
    private lateinit var logoutContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub_admin_home_page)

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize views
        initializeViews()

        // Set click listeners
        setupClickListeners()

        // Load admin name
        loadAdminName()
    }

    private fun initializeViews() {
        donationCenterCard = findViewById(R.id.donationCenterCard)
        createOperatorCard = findViewById(R.id.createOperatorCard)
        deleteOperatorCard = findViewById(R.id.deleteOperatorCard)
        welcomeText = findViewById(R.id.welcomeText)
        logoutContainer = findViewById(R.id.logoutContainer)
    }

    private fun setupClickListeners() {
        // Donation Center Card - Navigate to Add/Edit Donation Center
        donationCenterCard.setOnClickListener {
            val intent = Intent(this, AddDonationCenterActivity::class.java)
            startActivity(intent)
        }
        // Create operator Card - Navigate to Add/Edit Donation Center
        createOperatorCard.setOnClickListener {
            val intent = Intent(this, CreateOperatorsActivity::class.java)
            startActivity(intent)
        }

        deleteOperatorCard.setOnClickListener {
            val intent = Intent(this, EditOperatorController::class.java)
            startActivity(intent)
        }

        // Logout Container - Handle logout
        logoutContainer.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun loadAdminName() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Show default welcome text while loading
        welcomeText.text = "Welcome, Sub Admin!"

        // Fetch user name from Firebase
        db.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userName = document.getString("name")
                    if (!userName.isNullOrEmpty()) {
                        welcomeText.text = "Welcome, $userName!"
                    } else {
                        welcomeText.text = "Welcome, Sub Admin!"
                    }
                } else {
                    // Document doesn't exist, keep default text
                    welcomeText.text = "Welcome, Sub Admin!"
                }
            }
            .addOnFailureListener { e ->
                // Error fetching data, keep default text
                welcomeText.text = "Welcome, Sub Admin!"
                // Optionally log the error for debugging
                // Log.e("SubAdminHome", "Error fetching user name: ${e.message}")
            }
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { dialog, which ->
                // Perform logout
                performLogout()
            }
            .setNegativeButton("No") { dialog, which ->
                // User cancelled the logout
                dialog.dismiss()
            }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun performLogout() {
        try {
            // Sign out from Firebase Auth
            auth.signOut()

            // Show logout success message
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

            // Navigate to login screen
            val intent = Intent(this, LoginController::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

        } catch (e: Exception) {
            // Handle any logout errors
            Toast.makeText(this, "Error during logout: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh any data if needed when returning from other activities
    }
}
