package com.example.donorlk.controllers

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.example.donorlk.R
import com.example.donorlk.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ProfileSetupController : BaseActivity() {
    private lateinit var fullNameEditText: EditText
    private lateinit var mobileEditText: EditText
    private lateinit var dobEditText: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var bloodGroupSpinner: Spinner
    private lateinit var nicEditText: EditText
    private lateinit var provinceSpinner: Spinner
    private lateinit var cityEditText: EditText  // Changed from citySpinner to cityEditText
    private lateinit var nextButton: Button
    private lateinit var backButton: ImageView

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize views
        initializeViews()
        setupSpinners()

        // Get data from intent
        val userEmail = intent.getStringExtra("user_email") ?: ""
        val userName = intent.getStringExtra("user_name") ?: ""

        // Pre-fill name if available
        fullNameEditText.setText(userName)

        setupClickListeners()
    }

    private fun initializeViews() {
        fullNameEditText = findViewById(R.id.fullNameEditText)
        mobileEditText = findViewById(R.id.mobileEditText)
        dobEditText = findViewById(R.id.dobEditText)
        genderSpinner = findViewById(R.id.genderSpinner)
        bloodGroupSpinner = findViewById(R.id.bloodGroupSpinner)
        nicEditText = findViewById(R.id.nicEditText)
        provinceSpinner = findViewById(R.id.provinceSpinner)
        cityEditText = findViewById(R.id.cityEditText)  // Changed from citySpinner
        nextButton = findViewById(R.id.nextButton)
        backButton = findViewById(R.id.backButton)
    }

    private fun setupClickListeners() {
        nextButton.setOnClickListener {
            if (validateInput()) {
                saveCompleteProfile()
            }
        }

        backButton.setOnClickListener {
            finish()
        }

        dobEditText.setOnClickListener {
            showDatePicker()
        }
    }

    private fun setupSpinners() {
        // Setup Gender Spinner
        ArrayAdapter.createFromResource(
            this,
            resources.getIdentifier("gender_array", "array", packageName),
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            genderSpinner.adapter = adapter
        }

        // Setup Blood Group Spinner
        ArrayAdapter.createFromResource(
            this,
            resources.getIdentifier("blood_groups_array", "array", packageName),
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            bloodGroupSpinner.adapter = adapter
        }

        // Setup Province Spinner
        ArrayAdapter.createFromResource(
            this,
            resources.getIdentifier("provinces_array", "array", packageName),
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            provinceSpinner.adapter = adapter
        }

        // Remove city spinner logic since it's now a text field
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Format the date as DD/MM/YYYY
                val formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                dobEditText.setText(formattedDate)
            },
            year,
            month,
            day
        )

        // Set maximum date to current date (can't select future dates)
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

        // Set minimum date to 120 years ago (reasonable age limit)
        val minCalendar = Calendar.getInstance()
        minCalendar.add(Calendar.YEAR, -120)
        datePickerDialog.datePicker.minDate = minCalendar.timeInMillis

        datePickerDialog.show()
    }

    private fun validateInput(): Boolean {
        if (fullNameEditText.text.toString().isEmpty()) {
            fullNameEditText.error = "Name is required"
            return false
        }
        if (mobileEditText.text.toString().isEmpty()) {
            mobileEditText.error = "Mobile number is required"
            return false
        }

        // Validate mobile number format (Sri Lankan format)
        val mobile = mobileEditText.text.toString().trim()
        if (!mobile.matches(Regex("^[0-9]{10}$")) && !mobile.matches(Regex("^\\+94[0-9]{9}$"))) {
            mobileEditText.error = "Enter a valid mobile number (10 digits or +94xxxxxxxxx)"
            return false
        }

        if (dobEditText.text.toString().isEmpty()) {
            dobEditText.error = "Date of birth is required"
            return false
        }
        if (nicEditText.text.toString().isEmpty()) {
            nicEditText.error = "NIC is required"
            return false
        }

        // Validate NIC format (Sri Lankan NIC)
        val nic = nicEditText.text.toString().trim()
        if (!nic.matches(Regex("^[0-9]{9}[vVxX]$")) && !nic.matches(Regex("^[0-9]{12}$"))) {
            nicEditText.error = "Enter a valid NIC (9 digits + V/X or 12 digits)"
            return false
        }

        if (cityEditText.text.toString().isEmpty()) {
            cityEditText.error = "City is required"
            return false
        }

        return true
    }

    private fun saveCompleteProfile() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading
        nextButton.isEnabled = false
        nextButton.text = "Saving..."

        // Get all form data
        val fullName = fullNameEditText.text.toString().trim()
        val mobile = mobileEditText.text.toString().trim()
        val dob = dobEditText.text.toString().trim()
        val gender = genderSpinner.selectedItem?.toString() ?: ""
        val bloodGroup = bloodGroupSpinner.selectedItem?.toString() ?: ""
        val nic = nicEditText.text.toString().trim()
        val province = provinceSpinner.selectedItem?.toString() ?: ""
        val city = cityEditText.text.toString().trim()  // Changed from citySpinner

        // Create updated user object
        val updatedUser = User(
            uid = currentUser.uid,
            name = fullName,
            email = currentUser.email ?: "",
            role = "donator",
            mobile = mobile,
            dateOfBirth = dob,
            gender = gender,
            bloodGroup = bloodGroup,
            nic = nic,
            province = province,
            city = city,
            createdAt = System.currentTimeMillis()
        )

        // Save to Firestore
        firestore.collection("users").document(currentUser.uid)
            .set(updatedUser)
            .addOnSuccessListener {
                Log.d("ProfileSetup", "Profile updated successfully")
                nextButton.isEnabled = true
                nextButton.text = "Next"

                Toast.makeText(this, "Profile setup completed!", Toast.LENGTH_SHORT).show()

                // Navigate to verification or home page
                val intent = Intent(this, HomePageController::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Log.w("ProfileSetup", "Error updating profile", e)
                nextButton.isEnabled = true
                nextButton.text = "Next"

                Toast.makeText(this, "Failed to save profile: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
