package com.example.donorlk.controller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.donorlk.R
import com.example.donorlk.view.MakeReservationFragment
import com.example.donorlk.view.MyReservationsFragment
import com.example.donorlk.view.OverviewFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomePageController : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_page)

        // --- View Setup ---
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Home"

        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)

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
}