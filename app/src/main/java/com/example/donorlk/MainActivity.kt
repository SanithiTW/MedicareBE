package com.example.donorlk

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.donorlk.controller.HomePageController

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        enableEdgeToEdge()
//        setContentView(R.layout.my_reservations)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
        // 1. Create an Intent to start HomePageController
        // The '.class.java' is required to reference the class for the Intent.
        val intent = Intent(this, HomePageController::class.java)

        // 2. Start the HomePageController activity
        startActivity(intent)

        // 3. Finish the MainActivity so the user cannot navigate back to it.
        // This effectively makes HomePageController the first screen of your app.
        finish()

    }
}