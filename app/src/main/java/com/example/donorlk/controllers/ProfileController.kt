package com.example.donorlk.controllers

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.donorlk.R
import com.example.donorlk.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.util.*

class ProfileController : BaseActivity() {

    private lateinit var profileImageView: ImageView
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
    private val imageFileName = "profile_image.png"
    private val IMAGE_REQUEST_CODE = 1001
    private val CAMERA_REQUEST_CODE = 1002
    private val CAMERA_PERMISSION_CODE = 1003

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

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initializeViews()
        setupSpinners()
        setupClickListeners()
        loadUserProfile()
        loadImageFromInternalStorage()
    }

    private fun initializeViews() {
        profileImageView = findViewById(R.id.profileImageView)
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

        profileImageView.setOnClickListener {
            showImagePickOptions()
        }
    }

    // New function: show dialog with options to pick image from gallery or camera
    private fun showImagePickOptions() {
        val options = arrayOf("Choose from Gallery", "Take Photo")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Set Profile Picture")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openImagePicker() // existing gallery picker
                1 -> checkCameraPermissionAndOpenCamera() // new camera option
            }
        }
        builder.show()
    }

    // New function: Check camera permission, request if needed, else open camera
    private fun checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            openCamera()
        }
    }

    // New function: Start camera intent
    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show()
        }
    }

    // New override: Handle permission result for camera
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Existing function unchanged
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }

    // Updated override: handle both gallery and camera results
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                IMAGE_REQUEST_CODE -> {
                    val imageUri: Uri? = data?.data
                    if (imageUri != null) {
                        profileImageView.setImageURI(imageUri)
                        saveImageToInternalStorage(imageUri)
                    }
                }
                CAMERA_REQUEST_CODE -> {
                    val photoBitmap = data?.extras?.get("data") as? Bitmap
                    if (photoBitmap != null) {
                        profileImageView.setImageBitmap(photoBitmap)
                        saveBitmapToInternalStorage(photoBitmap)
                    }
                }
            }
        }
    }

    // New helper function to save Bitmap from camera to internal storage
    private fun saveBitmapToInternalStorage(bitmap: Bitmap) {
        try {
            val file = File(filesDir, imageFileName)
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    }

    // Existing function unchanged
    private fun saveImageToInternalStorage(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(filesDir, imageFileName)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    }

    // Existing function unchanged
    private fun loadImageFromInternalStorage() {
        try {
            val file = File(filesDir, imageFileName)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                profileImageView.setImageBitmap(bitmap)
            } else {
                profileImageView.setImageResource(R.drawable.ic_person)
            }
        } catch (e: Exception) {
            profileImageView.setImageResource(R.drawable.ic_person)
        }
    }

    // --- All your original functions below unchanged (copied exactly) ---

    private fun setupSpinners() {
        ArrayAdapter.createFromResource(
            this, R.array.gender_array, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            genderSpinner.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this, R.array.blood_groups_array, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            bloodGroupSpinner.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this, R.array.provinces_array, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            provinceSpinner.adapter = adapter
        }
    }

    private fun updateCitySpinner(province: String) {
        val cityArrayId = cityArrays[province]
        if (cityArrayId != null) {
            ArrayAdapter.createFromResource(
                this, cityArrayId, android.R.layout.simple_spinner_item
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

        setSpinnerSelection(genderSpinner, user.gender)
        setSpinnerSelection(bloodGroupSpinner, user.bloodGroup)
        setSpinnerSelection(provinceSpinner, user.province)

        if (user.province.isNotEmpty()) {
            updateCitySpinner(user.province)
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

        val currentDob = dobEditText.text.toString()
        if (currentDob.isNotEmpty()) {
            try {
                val parts = currentDob.split("/")
                if (parts.size == 3) {
                    calendar.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt())
                }
            } catch (e: Exception) {}
        }

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                dobEditText.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

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
        val mobile = mobileEditText.text.toString().trim()
        if (!mobile.matches(Regex("^[0-9]{10}$")) && !mobile.matches(Regex("^\\+94[0-9]{9}$"))) {
            mobileEditText.error = "Enter a valid mobile number"
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
        val nic = nicEditText.text.toString().trim()
        if (!nic.matches(Regex("^[0-9]{9}[vVxX]$")) && !nic.matches(Regex("^[0-9]{12}$"))) {
            nicEditText.error = "Enter a valid NIC"
            return false
        }
        return true
    }

    private fun updateProfile() {
        val currentFirebaseUser = auth.currentUser ?: return

        nextButton.isEnabled = false
        nextButton.text = "Saving..."

        val fullName = fullNameEditText.text.toString().trim()
        val mobile = mobileEditText.text.toString().trim()
        val dob = dobEditText.text.toString().trim()
        val gender = genderSpinner.selectedItem?.toString() ?: ""
        val bloodGroup = bloodGroupSpinner.selectedItem?.toString() ?: ""
        val nic = nicEditText.text.toString().trim()
        val province = provinceSpinner.selectedItem?.toString() ?: ""
        val city = citySpinner.selectedItem?.toString() ?: ""

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

        firestore.collection("users").document(currentFirebaseUser.uid)
            .set(updatedUser)
            .addOnSuccessListener {
                currentUser = updatedUser
                nextButton.isEnabled = true
                nextButton.text = "Save"
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                nextButton.isEnabled = true
                nextButton.text = "Save"
                Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
