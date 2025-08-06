package com.example.donorlk.controllers

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.donorlk.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddDonationCenterActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var centerNameInput: TextInputEditText
    private lateinit var latitudeInput: TextInputEditText
    private lateinit var longitudeInput: TextInputEditText
    private lateinit var slotLimitInput: TextInputEditText
    private lateinit var getCurrentLocationButton: MaterialButton
    private lateinit var saveButton: MaterialButton

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    // Track if we're editing existing record
    private var isEditMode = false
    private var existingDocumentId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_donation_center)

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize Location Services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize views
        initializeViews()

        // Set click listeners
        setupClickListeners()

        // Check if user already has a donation center
        checkExistingDonationCenter()
    }

    private fun initializeViews() {
        centerNameInput = findViewById(R.id.centerNameInput)
        latitudeInput = findViewById(R.id.latitudeInput)
        longitudeInput = findViewById(R.id.longitudeInput)
        slotLimitInput = findViewById(R.id.slotLimitInput)
        getCurrentLocationButton = findViewById(R.id.getCurrentLocationButton)
        saveButton = findViewById(R.id.saveButton)
    }

    private fun setupClickListeners() {
        getCurrentLocationButton.setOnClickListener {
            requestLocationPermissionAndGetLocation()
        }

        saveButton.setOnClickListener {
            if (isEditMode) {
                updateDonationCenter()
            } else {
                saveDonationCenter()
            }
        }
    }

    private fun checkExistingDonationCenter() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Show loading state
        saveButton.isEnabled = false
        saveButton.text = "Loading..."

        db.collection("donation_centers")
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // User already has a donation center - enter edit mode
                    val document = documents.documents[0]
                    existingDocumentId = document.id
                    isEditMode = true

                    // Populate fields with existing data
                    populateFieldsWithExistingData(document.data!!)

                    // Update UI for edit mode
                    saveButton.text = "Update Donation Center"

                    Toast.makeText(this, "Existing donation center loaded for editing", Toast.LENGTH_SHORT).show()
                } else {
                    // No existing record - add mode
                    isEditMode = false
                    saveButton.text = "Save Donation Center"
                }

                saveButton.isEnabled = true
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error checking existing data: ${e.message}", Toast.LENGTH_LONG).show()
                saveButton.isEnabled = true
                saveButton.text = "Save Donation Center"
            }
    }

    private fun populateFieldsWithExistingData(data: Map<String, Any>) {
        centerNameInput.setText(data["centerName"]?.toString() ?: "")
        latitudeInput.setText(data["latitude"]?.toString() ?: "")
        longitudeInput.setText(data["longitude"]?.toString() ?: "")
        slotLimitInput.setText(data["slotLimitPerSession"]?.toString() ?: "")
    }

    private fun saveDonationCenter() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val validationResult = validateInputs()
        if (!validationResult.isValid) {
            return
        }

        // Create donation center data
        val donationCenter = hashMapOf(
            "userId" to currentUserId,
            "centerName" to validationResult.centerName,
            "latitude" to validationResult.latitude,
            "longitude" to validationResult.longitude,
            "slotLimitPerSession" to validationResult.slotLimit,
            "createdAt" to com.google.firebase.Timestamp.now(),
            "updatedAt" to com.google.firebase.Timestamp.now()
        )

        // Save to Firebase
        saveButton.isEnabled = false
        saveButton.text = "Saving..."

        db.collection("donation_centers")
            .add(donationCenter)
            .addOnSuccessListener { documentReference ->
                // Successfully saved - transition to edit mode
                existingDocumentId = documentReference.id
                isEditMode = true

                Toast.makeText(this, "Donation center added successfully!", Toast.LENGTH_SHORT).show()

                // Update UI to edit mode
                saveButton.text = "Update Donation Center"
                saveButton.isEnabled = true

                // No finish() call - keep user on the page
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add donation center: ${e.message}", Toast.LENGTH_LONG).show()
                saveButton.isEnabled = true
                saveButton.text = "Save Donation Center"
            }
    }

    private fun updateDonationCenter() {
        val validationResult = validateInputs()
        if (!validationResult.isValid || existingDocumentId == null) {
            return
        }

        // Create updated data
        val updatedData = hashMapOf(
            "centerName" to validationResult.centerName,
            "latitude" to validationResult.latitude,
            "longitude" to validationResult.longitude,
            "slotLimitPerSession" to validationResult.slotLimit,
            "updatedAt" to com.google.firebase.Timestamp.now()
        )

        // Update in Firebase
        saveButton.isEnabled = false
        saveButton.text = "Updating..."

        db.collection("donation_centers")
            .document(existingDocumentId!!)
            .update(updatedData as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "Donation center updated successfully!", Toast.LENGTH_SHORT).show()

                // Re-enable the button and keep user on the page
                saveButton.isEnabled = true
                saveButton.text = "Update Donation Center"

                // No finish() call - keep user on the page for further edits
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update donation center: ${e.message}", Toast.LENGTH_LONG).show()
                saveButton.isEnabled = true
                saveButton.text = "Update Donation Center"
            }
    }

    private data class ValidationResult(
        val isValid: Boolean,
        val centerName: String = "",
        val latitude: Double = 0.0,
        val longitude: Double = 0.0,
        val slotLimit: Int = 0
    )

    private fun validateInputs(): ValidationResult {
        val centerName = centerNameInput.text.toString().trim()
        val latitudeStr = latitudeInput.text.toString().trim()
        val longitudeStr = longitudeInput.text.toString().trim()
        val slotLimitStr = slotLimitInput.text.toString().trim()

        // Validate inputs
        if (centerName.isEmpty()) {
            centerNameInput.error = "Center name is required"
            return ValidationResult(false)
        }

        if (latitudeStr.isEmpty()) {
            latitudeInput.error = "Latitude is required"
            return ValidationResult(false)
        }

        if (longitudeStr.isEmpty()) {
            longitudeInput.error = "Longitude is required"
            return ValidationResult(false)
        }

        if (slotLimitStr.isEmpty()) {
            slotLimitInput.error = "Slot limit is required"
            return ValidationResult(false)
        }

        val latitude = latitudeStr.toDoubleOrNull()
        val longitude = longitudeStr.toDoubleOrNull()
        val slotLimit = slotLimitStr.toIntOrNull()

        if (latitude == null || latitude < -90 || latitude > 90) {
            latitudeInput.error = "Invalid latitude (-90 to 90)"
            return ValidationResult(false)
        }

        if (longitude == null || longitude < -180 || longitude > 180) {
            longitudeInput.error = "Invalid longitude (-180 to 180)"
            return ValidationResult(false)
        }

        if (slotLimit == null || slotLimit <= 0) {
            slotLimitInput.error = "Slot limit must be a positive number"
            return ValidationResult(false)
        }

        return ValidationResult(true, centerName, latitude, longitude, slotLimit)
    }

    private fun requestLocationPermissionAndGetLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                latitudeInput.setText(location.latitude.toString())
                longitudeInput.setText(location.longitude.toString())
                Toast.makeText(this, "Location retrieved successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Unable to get current location. Please enter manually.", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to get location. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation()
                } else {
                    Toast.makeText(this, "Location permission denied. Please enter coordinates manually.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
