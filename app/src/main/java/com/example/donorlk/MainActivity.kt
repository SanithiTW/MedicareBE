package com.example.donorlk

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.donorlk.controllers.HomePageController
import com.example.donorlk.controllers.LoginController
import com.example.donorlk.controllers.SubAdminHomeController

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start with LoginController
        val intent = Intent(this, HomePageController::class.java)
        startActivity(intent)

        // Finish MainActivity so user cannot navigate back
        finish()
    }
}