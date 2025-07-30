package com.example.donorlk.controllers

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import com.example.donorlk.R

class ProfileSetupController : BaseActivity() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

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
        citySpinner = findViewById(R.id.citySpinner)
        nextButton = findViewById(R.id.nextButton)
        backButton = findViewById(R.id.backButton)
    }

    private fun setupClickListeners() {
        nextButton.setOnClickListener {
            if (validateInput()) {
                val intent = Intent(this, VerificationController::class.java)
                intent.putExtra("user_email", getIntent().getStringExtra("user_email"))
                startActivity(intent)
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

        // City spinner will be updated based on province selection
        provinceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                updateCitySpinner(pos)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    private fun updateCitySpinner(provincePosition: Int) {
        val cityArrayName = when (provincePosition) {
            0 -> "western_cities"
            1 -> "central_cities"
            2 -> "southern_cities"
            3 -> "northern_cities"
            4 -> "eastern_cities"
            5 -> "north_western_cities"
            6 -> "north_central_cities"
            7 -> "uva_cities"
            8 -> "sabaragamuwa_cities"
            else -> "western_cities" // Default to Western Province cities
        }

        val resourceId = resources.getIdentifier(cityArrayName, "array", packageName)
        ArrayAdapter.createFromResource(
            this,
            resourceId,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            citySpinner.adapter = adapter
        }
    }

    private fun showDatePicker() {
        // TODO: Implement date picker dialog
        Toast.makeText(this, "Date picker will be implemented", Toast.LENGTH_SHORT).show()
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
        if (dobEditText.text.toString().isEmpty()) {
            dobEditText.error = "Date of birth is required"
            return false
        }
        if (nicEditText.text.toString().isEmpty()) {
            nicEditText.error = "NIC is required"
            return false
        }
        return true
    }
}
