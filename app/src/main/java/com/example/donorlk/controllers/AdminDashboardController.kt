package com.example.donorlk.controllers

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.donorlk.R
import com.example.donorlk.CreateAdminActivity

class AdminDashboardController : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        // ✅ Setup button click to go to CreateAdminActivity
        val addAdminButton = findViewById<Button>(R.id.addAdmin)
        addAdminButton.setOnClickListener {
            val intent = Intent(this, CreateAdminActivity::class.java)
            startActivity(intent)
        }
    }
}
