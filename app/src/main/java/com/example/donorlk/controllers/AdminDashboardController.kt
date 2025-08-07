package com.example.donorlk.controllers

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.donorlk.CreateAdminActivity
import com.example.donorlk.controllers.LoginController
import com.example.donorlk.R
import com.google.firebase.auth.FirebaseAuth

class AdminDashboardController : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        auth = FirebaseAuth.getInstance()

        val createAdminCard = findViewById<LinearLayout>(R.id.createAdminCard)
        createAdminCard.setOnClickListener {
            val intent = Intent(this, CreateAdminActivity::class.java)
            startActivity(intent)
        }

        val logoutContainer = findViewById<LinearLayout>(R.id.logoutContainer)
        logoutContainer.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginController::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
