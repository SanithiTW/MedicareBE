package com.example.donorlk.controllers

import TimeSlotAdapter
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.donorlk.R
import com.example.donorlk.models.TimeSlot
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*


class MakeReservationActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var selectedCenterName: TextView
    private lateinit var reservationDateInput: TextInputEditText
    private lateinit var slotsLabel: TextView
    private lateinit var timeSlotsRecyclerView: RecyclerView
    private lateinit var noSlotsMessage: TextView
    private lateinit var notesInput: TextInputEditText
    private lateinit var confirmReservationButton: MaterialButton

    private lateinit var timeSlotAdapter: TimeSlotAdapter

    private var centerId: String? = null
    private var centerName: String? = null
    private var selectedDate: String? = null
    private var slotLimit: Int = 0

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

        // Setup RecyclerView
        setupRecyclerView()

        // Setup click listeners
        setupClickListeners()

        // Display selected center
        displaySelectedCenter()

        // Load center details to get slot limit
        loadCenterDetails()
    }

    private fun initializeViews() {
        selectedCenterName = findViewById(R.id.selectedCenterName)
        reservationDateInput = findViewById(R.id.reservationDateInput)
        slotsLabel = findViewById(R.id.slotsLabel)
        timeSlotsRecyclerView = findViewById(R.id.timeSlotsRecyclerView)
        noSlotsMessage = findViewById(R.id.noSlotsMessage)
        notesInput = findViewById(R.id.notesInput)
        confirmReservationButton = findViewById(R.id.confirmReservationButton)

        // Setup back button
        val backButton = findViewById<android.widget.ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        timeSlotAdapter = TimeSlotAdapter(mutableListOf()) { timeSlot, position ->
            onTimeSlotSelected(timeSlot)
        }
        timeSlotsRecyclerView.adapter = timeSlotAdapter
        timeSlotsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupClickListeners() {
        reservationDateInput.setOnClickListener {
            showDatePicker()
        }

        confirmReservationButton.setOnClickListener {
            confirmReservation()
        }
    }

    private fun displaySelectedCenter() {
        selectedCenterName.text = "Selected Center: ${centerName ?: "Unknown Center"}"
    }

    private fun loadCenterDetails() {
        if (centerId == null) return

        db.collection("donation_centers")
            .document(centerId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    slotLimit = document.getLong("slotLimitPerSession")?.toInt() ?: 10
                } else {
                    Toast.makeText(this, "Center details not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading center details: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Check if selected date is a weekday
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
                val dayOfWeek = selectedCalendar.get(Calendar.DAY_OF_WEEK)

                if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                    Toast.makeText(this, "Please select a weekday (Monday-Friday)", Toast.LENGTH_SHORT).show()
                    return@DatePickerDialog
                }

                selectedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                reservationDateInput.setText(selectedDate)
                loadAvailableSlots()
            },
            year, month, day
        )

        // Set minimum date to today
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()

        datePickerDialog.show()
    }

    private fun loadAvailableSlots() {
        if (selectedDate == null || centerId == null) return

        // Show loading state
        val timeSlotsCard = findViewById<com.google.android.material.card.MaterialCardView>(R.id.timeSlotsCard)
        timeSlotsCard.visibility = View.GONE
        noSlotsMessage.text = "Loading available slots..."
        noSlotsMessage.visibility = View.VISIBLE

        // Generate time slots
        val timeSlots = generateTimeSlots()

        // Get existing reservations for the selected date
        db.collection("reservations")
            .whereEqualTo("centerId", centerId)
            .whereEqualTo("reservationDate", selectedDate)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { documents ->
                // Count reservations for each time slot
                val reservationCounts = mutableMapOf<String, Int>()

                for (document in documents) {
                    val time = document.getString("reservationTime") ?: continue
                    reservationCounts[time] = (reservationCounts[time] ?: 0) + 1
                }

                // Update available slots
                val availableSlots = timeSlots.map { slot ->
                    val usedSlots = reservationCounts[slot.time] ?: 0
                    val availableCount = maxOf(0, slotLimit - usedSlots)
                    slot.copy(availableCount = availableCount)
                }

                // Update UI
                if (availableSlots.any { it.availableCount > 0 }) {
                    timeSlotAdapter.updateSlots(availableSlots)
                    timeSlotsCard.visibility = View.VISIBLE
                    noSlotsMessage.visibility = View.GONE
                } else {
                    noSlotsMessage.text = "No available slots for this date"
                    noSlotsMessage.visibility = View.VISIBLE
                    timeSlotsCard.visibility = View.GONE
                }

                updateReservationButtonState()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading slots: ${e.message}", Toast.LENGTH_SHORT).show()
                noSlotsMessage.text = "Error loading slots"
                noSlotsMessage.visibility = View.VISIBLE
                timeSlotsCard.visibility = View.GONE
            }
    }

    private fun generateTimeSlots(): List<TimeSlot> {
        val slots = mutableListOf<TimeSlot>()
        val calendar = Calendar.getInstance()

        // Start at 9:00 AM
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 0)

        val endTime = Calendar.getInstance()
        endTime.set(Calendar.HOUR_OF_DAY, 17) // 5:00 PM
        endTime.set(Calendar.MINUTE, 0)

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        while (calendar.before(endTime)) {
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            // Skip lunch hour (12:00 PM to 1:30 PM)
            if (!(hour == 12 || (hour == 13 && minute < 30))) {
                val timeString = timeFormat.format(calendar.time)
                slots.add(TimeSlot(timeString, slotLimit, slotLimit))
            }

            // Add 30 minutes
            calendar.add(Calendar.MINUTE, 30)
        }

        return slots
    }

    private fun onTimeSlotSelected(timeSlot: TimeSlot) {
        updateReservationButtonState()
    }

    private fun updateReservationButtonState() {
        val hasSelectedDate = selectedDate != null
        val hasSelectedSlot = timeSlotAdapter.getSelectedSlot() != null

        confirmReservationButton.isEnabled = hasSelectedDate && hasSelectedSlot
    }

    private fun confirmReservation() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedSlot = timeSlotAdapter.getSelectedSlot()
        if (selectedSlot == null || selectedDate == null) {
            Toast.makeText(this, "Please select a date and time slot", Toast.LENGTH_SHORT).show()
            return
        }

        val notes = notesInput.text.toString().trim()

        // Create reservation data
        val reservation = hashMapOf(
            "userId" to currentUserId,
            "centerId" to centerId,
            "centerName" to centerName,
            "reservationDate" to selectedDate,
            "reservationTime" to selectedSlot.time,
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
                confirmReservationButton.text = "Make Reservation"
            }
    }
}
