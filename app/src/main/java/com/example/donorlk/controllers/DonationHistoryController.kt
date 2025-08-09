package com.example.donorlk.controllers

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.donorlk.R
import com.example.donorlk.models.DonationModel
import com.example.donorlk.adapters.DonationAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

class DonationHistoryController : BaseActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DonationAdapter
    private lateinit var noDonationsText: TextView
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation_history)

        recyclerView = findViewById(R.id.donationRecyclerView)
        noDonationsText = findViewById(R.id.noDonationsText)
        recyclerView.layoutManager = LinearLayoutManager(this)

        db = FirebaseFirestore.getInstance()
        adapter = DonationAdapter(mutableListOf())
        recyclerView.adapter = adapter

        loadUserDonations()
    }

    private fun loadUserDonations() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            showNoDonationsMessage()
            return
        }

        Log.d("DonationHistory", "Loading donations for user: ${currentUser.uid}")

        db.collection("blood_donations")
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("DonationHistory", "Found ${documents.size()} donations")

                if (documents.isEmpty) {
                    showNoDonationsMessage()
                    return@addOnSuccessListener
                }

                val donationsList = mutableListOf<DonationModel>()

                // Keep track of completed queries
                var completedQueries = 0
                val totalQueries = documents.size()

                // Process each donation document
                for (document in documents) {
                    val reservationId = document.getString("reservationId")
                    val amount = document.getLong("amount")?.toString() ?: "0"
                    val timestamp = document.getTimestamp("createdAt")
                    val date = timestamp?.toDate()?.toString() ?: "Unknown Date"

                    Log.d("DonationHistory", "Processing donation: Amount=$amount, Date=$date")

                    if (reservationId != null) {
                        // Get the center name from the reservation
                        db.collection("reservations")
                            .document(reservationId)
                            .get()
                            .addOnSuccessListener { reservationDoc ->
                                val centerName = reservationDoc.getString("centerName") ?: "Unknown Center"
                                Log.d("DonationHistory", "Found center: $centerName")

                                donationsList.add(DonationModel(
                                    "Blood Donation",
                                    date,
                                    centerName,
                                    amount
                                ))

                                completedQueries++
                                Log.d("DonationHistory", "Completed $completedQueries of $totalQueries queries")

                                // When all queries are complete, update the UI
                                if (completedQueries == totalQueries) {
                                    // Sort donations by date (newest first)
                                    donationsList.sortByDescending { it.date }
                                    Log.d("DonationHistory", "Displaying ${donationsList.size} donations")

                                    runOnUiThread {
                                        adapter.updateDonations(donationsList)
                                        showDonationsList()
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("DonationHistory", "Error getting reservation: ${e.message}")
                                completedQueries++
                                if (completedQueries == totalQueries) {
                                    updateUI(donationsList)
                                }
                            }
                    } else {
                        completedQueries++
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("DonationHistory", "Error getting donations: ${e.message}")
                showNoDonationsMessage()
            }
    }

    private fun updateUI(donationsList: List<DonationModel>) {
        runOnUiThread {
            if (donationsList.isEmpty()) {
                showNoDonationsMessage()
            } else {
                adapter.updateDonations(donationsList)
                showDonationsList()
            }
        }
    }

    private fun showNoDonationsMessage() {
        recyclerView.visibility = View.GONE
        noDonationsText.visibility = View.VISIBLE
    }

    private fun showDonationsList() {
        recyclerView.visibility = View.VISIBLE
        noDonationsText.visibility = View.GONE
    }
}
