package com.example.donorlk.controllers

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.donorlk.R
import com.example.donorlk.SaveDonationsRecorderController
import com.google.firebase.auth.FirebaseAuth

class OperatorDashboardController : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operator_dashboard)

        auth = FirebaseAuth.getInstance()

        val saveDonationsCard = findViewById<CardView>(R.id.saveDonationCard)
        val logoutContainer = findViewById<LinearLayout>(R.id.logoutContainer)

        saveDonationsCard.setOnClickListener {
            val intent = Intent(this, SaveDonationsRecorderController::class.java)
            startActivity(intent)
        }

        logoutContainer.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginController::class.java) // Change if your login activity class is named differently
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
