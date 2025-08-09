package com.example.donorlk.controllers

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.donorlk.R
import com.example.donorlk.views.MakeReservationFragment
import com.example.donorlk.views.MyReservationsFragment
import com.example.donorlk.views.OverviewFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth

class HomePageController : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_page)

        // --- View Setup ---
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setTitleTextColor(android.graphics.Color.BLACK)
        supportActionBar?.title = "Home"

        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNav.itemBackground = null

        // --- Controller Logic ---
        bottomNav.setOnItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            var title: String = getString(R.string.app_name)

            when (item.itemId) {
                R.id.nav_overview -> {
                    selectedFragment = OverviewFragment()
                    title = "Overview"
                }
                R.id.nav_make_reservation -> {
                    selectedFragment = MakeReservationFragment()
                    title = "Make Reservation"
                }
                R.id.nav_my_reservations -> {
                    selectedFragment = MyReservationsFragment()
                    title = "My Reservations"
                }
            }

            if (selectedFragment != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit()
                supportActionBar?.title = title

            }

            true
        }

        // --- Initial State ---
        // Set the default fragment when the app starts
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_overview
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.profile -> {
                // Navigate to ProfileController
                val intent = Intent(this, ProfileController::class.java)
                startActivity(intent)
                true
            }
            R.id.logout -> {
                // Handle logout
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginController::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}