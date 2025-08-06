package com.example.donorlk.controllers

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.donorlk.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SubAdminHomeController : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var donationCenterCard: CardView
    private lateinit var reservationsCard: CardView
    private lateinit var reportsCard: CardView
    private lateinit var welcomeText: TextView

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
        reservationsCard = findViewById(R.id.reservationsCard)
        reportsCard = findViewById(R.id.reportsCard)
        welcomeText = findViewById(R.id.welcomeText)
    }

    private fun setupClickListeners() {
        // Donation Center Card - Navigate to Add/Edit Donation Center
        donationCenterCard.setOnClickListener {
            val intent = Intent(this, AddDonationCenterActivity::class.java)
            startActivity(intent)
        }

        // Reservations Card - Navigate to Reservations Management
        reservationsCard.setOnClickListener {
            // TODO: Create ReservationsManagementActivity
            Toast.makeText(this, "Reservations Management - Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        // Reports Card - Navigate to Reports
        reportsCard.setOnClickListener {
            // TODO: Create ReportsActivity
            Toast.makeText(this, "Reports - Coming Soon!", Toast.LENGTH_SHORT).show()
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

    override fun onResume() {
        super.onResume()
        // Refresh any data if needed when returning from other activities
    }
}
