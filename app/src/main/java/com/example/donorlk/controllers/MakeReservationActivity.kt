package com.example.donorlk.controllers

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.donorlk.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.TextView
import java.util.*

class MakeReservationActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var selectedCenterName: TextView
    private lateinit var reservationDateInput: TextInputEditText
    private lateinit var reservationTimeInput: TextInputEditText
    private lateinit var notesInput: TextInputEditText
    private lateinit var confirmReservationButton: MaterialButton

    private var centerId: String? = null
    private var centerName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_reservation)

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Get center information from intent
        centerId = intent.getStringExtra("centerId")
        centerName = intent.getStringExtra("centerName")

        // Initialize views
        initializeViews()

        // Setup click listeners
        setupClickListeners()

        // Display selected center
        displaySelectedCenter()
    }

    private fun initializeViews() {
        selectedCenterName = findViewById(R.id.selectedCenterName)
        reservationDateInput = findViewById(R.id.reservationDateInput)
        reservationTimeInput = findViewById(R.id.reservationTimeInput)
        notesInput = findViewById(R.id.notesInput)
        confirmReservationButton = findViewById(R.id.confirmReservationButton)
    }

    private fun setupClickListeners() {
        reservationDateInput.setOnClickListener {
            showDatePicker()
        }

        reservationTimeInput.setOnClickListener {
            showTimePicker()
        }

        confirmReservationButton.setOnClickListener {
            confirmReservation()
        }
    }

    private fun displaySelectedCenter() {
        selectedCenterName.text = "Selected Center: ${centerName ?: "Unknown Center"}"
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                reservationDateInput.setText(selectedDate)
            },
            year, month, day
        )

        // Set minimum date to today
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()

        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                reservationTimeInput.setText(selectedTime)
            },
            hour, minute, true
        )

        timePickerDialog.show()
    }

    private fun confirmReservation() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val date = reservationDateInput.text.toString().trim()
        val time = reservationTimeInput.text.toString().trim()
        val notes = notesInput.text.toString().trim()

        // Validate inputs
        if (date.isEmpty()) {
            reservationDateInput.error = "Please select a date"
            return
        }

        if (time.isEmpty()) {
            reservationTimeInput.error = "Please select a time"
            return
        }

        if (centerId == null) {
            Toast.makeText(this, "Center information missing", Toast.LENGTH_SHORT).show()
            return
        }

        // Create reservation data
        val reservation = hashMapOf(
            "userId" to currentUserId,
            "centerId" to centerId,
            "centerName" to centerName,
            "reservationDate" to date,
            "reservationTime" to time,
            "notes" to notes,
            "status" to "pending",
            "createdAt" to com.google.firebase.Timestamp.now()
        )

        // Save reservation to Firebase
        confirmReservationButton.isEnabled = false
        confirmReservationButton.text = "Confirming..."

        db.collection("reservations")
            .add(reservation)
            .addOnSuccessListener {
                Toast.makeText(this, "Reservation confirmed successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to confirm reservation: ${e.message}", Toast.LENGTH_LONG).show()
                confirmReservationButton.isEnabled = true
                confirmReservationButton.text = "Confirm Reservation"
            }
    }
}
