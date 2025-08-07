package com.example.donorlk.controllers

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.donorlk.CreateAdminActivity
import com.example.donorlk.R

class AdminDashboardController : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        // ✅ Setup click on the custom Create Admin Card
        val createAdminCard = findViewById<LinearLayout>(R.id.createAdminCard)
        createAdminCard.setOnClickListener {
            val intent = Intent(this, CreateAdminActivity::class.java)
            startActivity(intent)
        }
    }
}
