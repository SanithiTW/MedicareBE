package com.example.donorlk.views

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView
import com.example.donorlk.R
import com.example.donorlk.controllers.DonationFormController
import com.example.donorlk.controllers.DonationHistoryController

class OverviewFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_overview, container, false)

        // Set up click listeners for the cards
        view.findViewById<CardView>(R.id.donationHistoryCard).setOnClickListener {
            val intent = Intent(activity, DonationHistoryController::class.java)
            startActivity(intent)
        }

        view.findViewById<CardView>(R.id.donationFormCard).setOnClickListener {
            val intent = Intent(activity, DonationFormController::class.java)
            startActivity(intent)
        }

        return view
    }
}