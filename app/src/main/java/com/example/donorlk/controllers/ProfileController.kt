package com.example.donorlk.controllers

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.example.donorlk.R
import com.example.donorlk.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ProfileController : BaseActivity() {
    private lateinit var fullNameEditText: EditText
    private lateinit var mobileEditText: EditText
    private lateinit var dobEditText: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var bloodGroupSpinner: Spinner
    private lateinit var nicEditText: EditText
    private lateinit var provinceSpinner: Spinner
    private lateinit var citySpinner: Spinner
    private lateinit var nextButton: Button
    private lateinit var backButton: ImageView

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var currentUser: User? = null
    private val cityArrays = mapOf(
        "Western Province" to R.array.western_cities,
        "Central Province" to R.array.central_cities,
        "Southern Province" to R.array.southern_cities,
        "Northern Province" to R.array.northern_cities,
        "Eastern Province" to R.array.eastern_cities,
        "North Western Province" to R.array.north_western_cities,
        "North Central Province" to R.array.north_central_cities,
        "Uva Province" to R.array.uva_cities,
        "Sabaragamuwa Province" to R.array.sabaragamuwa_cities
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize views
        initializeViews()
        setupSpinners()
        setupClickListeners()

        // Load user data
        loadUserProfile()
    }

    private fun initializeViews() {
        fullNameEditText = findViewById(R.id.fullNameEditText)
        mobileEditText = findViewById(R.id.mobileEditText)
        dobEditText = findViewById(R.id.dobEditText)
        genderSpinner = findViewById(R.id.genderSpinner)
        bloodGroupSpinner = findViewById(R.id.bloodGroupSpinner)
        nicEditText = findViewById(R.id.nicEditText)
        provinceSpinner = findViewById(R.id.provinceSpinner)
        citySpinner = findViewById(R.id.citySpinner)
        nextButton = findViewById(R.id.nextButton)
        backButton = findViewById(R.id.backButton)

        // Change button text to "Save"
        nextButton.text = "Save"
    }

    private fun setupClickListeners() {
        nextButton.setOnClickListener {
            if (validateInput()) {
                updateProfile()
            }
        }

        backButton.setOnClickListener {
            finish()
        }

        dobEditText.setOnClickListener {
            showDatePicker()
        }

        provinceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedProvince = parent.getItemAtPosition(position).toString()
                updateCitySpinner(selectedProvince)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupSpinners() {
        // Setup Gender Spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.gender_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            genderSpinner.adapter = adapter
        }

        // Setup Blood Group Spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.blood_groups_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            bloodGroupSpinner.adapter = adapter
        }

        // Setup Province Spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.provinces_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            provinceSpinner.adapter = adapter
        }
    }

    private fun updateCitySpinner(province: String) {
        val cityArrayId = cityArrays[province]
        if (cityArrayId != null) {
            ArrayAdapter.createFromResource(
                this,
                cityArrayId,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                citySpinner.adapter = adapter
            }
        }
    }

    private fun loadUserProfile() {
        val currentFirebaseUser = auth.currentUser
        if (currentFirebaseUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Show loading state
        nextButton.isEnabled = false
        nextButton.text = "Loading..."

        firestore.collection("users").document(currentFirebaseUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    currentUser = document.toObject(User::class.java)
                    currentUser?.let { user ->
                        populateFields(user)
                    }
                } else {
                    Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show()
                }
                nextButton.isEnabled = true
                nextButton.text = "Save"
            }
            .addOnFailureListener { e ->
                Log.w("ProfileController", "Error loading profile", e)
                Toast.makeText(this, "Failed to load profile: ${e.message}", Toast.LENGTH_LONG).show()
                nextButton.isEnabled = true
                nextButton.text = "Save"
            }
    }

    private fun populateFields(user: User) {
        fullNameEditText.setText(user.name)
        mobileEditText.setText(user.mobile)
        dobEditText.setText(user.dateOfBirth)
        nicEditText.setText(user.nic)

        // Set spinner selections
        setSpinnerSelection(genderSpinner, user.gender)
        setSpinnerSelection(bloodGroupSpinner, user.bloodGroup)
        setSpinnerSelection(provinceSpinner, user.province)

        // Update city spinner based on province, then set city selection
        if (user.province.isNotEmpty()) {
            updateCitySpinner(user.province)
            // Delay setting city selection to allow spinner to populate
            citySpinner.post {
                setSpinnerSelection(citySpinner, user.city)
            }
        }
    }

    private fun setSpinnerSelection(spinner: Spinner, value: String) {
        val adapter = spinner.adapter as? ArrayAdapter<String>
        adapter?.let {
            for (i in 0 until it.count) {
                if (it.getItem(i) == value) {
                    spinner.setSelection(i)
                    break
                }
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        // If DOB is already set, parse it to set initial date
        val currentDob = dobEditText.text.toString()
        if (currentDob.isNotEmpty()) {
            try {
                val parts = currentDob.split("/")
                if (parts.size == 3) {
                    calendar.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt())
                }
            } catch (e: Exception) {
                // Use current date if parsing fails
            }
        }

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

        return true
    }

    private fun updateProfile() {
        val currentFirebaseUser = auth.currentUser
        if (currentFirebaseUser == null) {
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
        val city = citySpinner.selectedItem?.toString() ?: ""

        // Create updated user object, preserving existing data
        val updatedUser = currentUser?.copy(
            name = fullName,
            mobile = mobile,
            dateOfBirth = dob,
            gender = gender,
            bloodGroup = bloodGroup,
            nic = nic,
            province = province,
            city = city
        ) ?: User(
            uid = currentFirebaseUser.uid,
            name = fullName,
            email = currentFirebaseUser.email ?: "",
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

        // Update in Firestore
        firestore.collection("users").document(currentFirebaseUser.uid)
            .set(updatedUser)
            .addOnSuccessListener {
                Log.d("ProfileController", "Profile updated successfully")
                currentUser = updatedUser
                nextButton.isEnabled = true
                nextButton.text = "Save"

                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.w("ProfileController", "Error updating profile", e)
                nextButton.isEnabled = true
                nextButton.text = "Save"

                Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
