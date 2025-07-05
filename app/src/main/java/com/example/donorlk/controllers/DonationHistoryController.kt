package com.example.donorlk.controllers

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.donorlk.R
import com.example.donorlk.models.DonationModel
import com.example.donorlk.adapters.DonationAdapter

class DonationHistoryController : BaseActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DonationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation_history)

        recyclerView = findViewById(R.id.donationRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Create dummy data
        val dummyDonations = listOf(
            DonationModel(
                "Emergency Blood Donation",
                "2025-07-01",
                "National Hospital, Colombo"
            ),
            DonationModel(
                "Normal Blood Donation",
                "2025-04-28",
                "General Hospital, Kandy"
            ),
            DonationModel(
                "Normal Blood Donation",
                "2025-01-02",
                "Red Cross Center, Galle"
            ),
            DonationModel(
                "Normal Blood Donation",
                "2024-06-10",
                "University of Colombo"
            )
        )

        adapter = DonationAdapter(dummyDonations)
        recyclerView.adapter = adapter
    }
}
