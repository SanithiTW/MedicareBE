package com.example.donorlk.controller

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.donorlk.R
import com.example.donorlk.model.DonationModel
import com.example.donorlk.adapters.DonationAdapter

class DonationHistoryController : AppCompatActivity() {
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
                "Blood Donation Camp",
                "2025-07-01",
                "National Hospital, Colombo"
            ),
            DonationModel(
                "Emergency Blood Drive",
                "2025-06-28",
                "General Hospital, Kandy"
            ),
            DonationModel(
                "Community Blood Donation",
                "2025-06-15",
                "Red Cross Center, Galle"
            ),
            DonationModel(
                "University Blood Camp",
                "2025-06-10",
                "University of Colombo"
            )
        )

        adapter = DonationAdapter(dummyDonations)
        recyclerView.adapter = adapter
    }
}
