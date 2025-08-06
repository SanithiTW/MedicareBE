package com.example.donorlk.views

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.donorlk.R
import com.example.donorlk.controllers.DonationFormController
import com.example.donorlk.controllers.DonationHistoryController
import com.example.donorlk.RecordDonationActivity

class OverviewFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_overview, container, false)

        // Navigate to Donation History
        view.findViewById<CardView>(R.id.donationHistoryCard).setOnClickListener {
            val intent = Intent(activity, DonationHistoryController::class.java)
            startActivity(intent)
        }

        // Navigate to Donation Form
        view.findViewById<CardView>(R.id.donationFormCard).setOnClickListener {
            val intent = Intent(activity, DonationFormController::class.java)
            startActivity(intent)
        }

        // Navigate to Record Donation page
        view.findViewById<Button>(R.id.btnOpenRecord).setOnClickListener {
            val intent = Intent(activity, RecordDonationActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}
