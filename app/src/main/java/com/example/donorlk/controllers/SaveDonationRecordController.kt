package com.example.donorlk

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import android.widget.ImageView

class SaveDonationsRecorderController : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var nicInput: EditText
    private lateinit var searchButton: Button
    private lateinit var reservationContainer: LinearLayout
    private lateinit var donationAmount: EditText
    private lateinit var confirmDonation: Button

    private var selectedReservationId: String? = null
    private var selectedUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_donation_records)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Bind UI
        nicInput = findViewById(R.id.nicInput)
        searchButton = findViewById(R.id.searchButton)
        reservationContainer = findViewById(R.id.reservationContainer)
        donationAmount = findViewById(R.id.donationAmount)
        confirmDonation = findViewById(R.id.confirmDonation)

        // Back button
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish() // Go back to previous screen
        }

        // Initially hide donation input UI
        donationAmount.visibility = View.GONE
        confirmDonation.visibility = View.GONE

        // Search button click
        searchButton.setOnClickListener {
            val nic = nicInput.text.toString().trim()
            if (nic.isEmpty()) {
                showToast("Enter NIC to search")
            } else if (!isValidNIC(nic)) {
                showToast("Invalid NIC format")
            } else {
                searchNIC(nic)
            }
        }

        // Confirm donation click
        confirmDonation.setOnClickListener {
            saveDonation()
        }
    }

    // Validate NIC format
    private fun isValidNIC(nic: String): Boolean {
        val oldNicRegex = Regex("^[0-9]{9}[vVxX]$")
        val newNicRegex = Regex("^[0-9]{12}$")
        return oldNicRegex.matches(nic) || newNicRegex.matches(nic)
    }

    // Search user and their pending reservations
    private fun searchNIC(nic: String) {
        db.collection("users")
            .whereEqualTo("nic", nic)
            .get()
            .addOnSuccessListener { users ->
                if (users.isEmpty) {
                    showToast("No user found with that NIC")
                    return@addOnSuccessListener
                }

                val user = users.documents[0]
                val userId = user.id
                selectedUserId = userId

                db.collection("reservations")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("status", "pending")
                    .get()
                    .addOnSuccessListener { reservations ->
                        reservationContainer.removeAllViews()
                        selectedReservationId = null
                        donationAmount.visibility = View.GONE
                        confirmDonation.visibility = View.GONE

                        if (reservations.isEmpty) {
                            showToast("No pending reservations for this user")
                        } else {
                            for (doc in reservations) {
                                val reservationId = doc.id
                                val centerName = doc.getString("centerName") ?: "Unknown Center"
                                val date = doc.getString("reservationDate") ?: "Unknown Date"
                                val time = doc.getString("reservationTime") ?: "Unknown Time"

                                val card = createReservationCard(
                                    reservationId,
                                    centerName,
                                    date,
                                    time
                                )
                                reservationContainer.addView(card)
                            }
                        }
                    }
                    .addOnFailureListener {
                        showToast("Failed to fetch reservations: ${it.message}")
                    }
            }
            .addOnFailureListener {
                showToast("Failed to search user: ${it.message}")
            }
    }

    // Create UI card for a reservation
    private fun createReservationCard(
        reservationId: String,
        centerName: String,
        date: String,
        time: String
    ): View {
        val card = TextView(this)
        card.text = "Reservation ID: $reservationId\nCenter: $centerName\nDate: $date @ $time\n\nTap to record donation"
        card.setPadding(24, 24, 24, 24)
        card.setBackgroundResource(R.drawable.card_background)
        card.setTextColor(ContextCompat.getColor(this, android.R.color.black))
        card.textSize = 16f

        card.setOnClickListener {
            selectedReservationId = reservationId
            donationAmount.visibility = View.VISIBLE
            confirmDonation.visibility = View.VISIBLE
        }

        return card
    }

    // Save donation and update reservation
    private fun saveDonation() {
        val amountText = donationAmount.text.toString().trim()
        val amount = amountText.toIntOrNull()
        val userId = selectedUserId
        val reservationId = selectedReservationId

        if (amount == null || amount <= 0) {
            showToast("Please enter a valid donation amount")
            return
        }

        if (userId == null || reservationId == null) {
            showToast("Please select a reservation")
            return
        }

        val donation = hashMapOf(
            "userId" to userId,
            "reservationId" to reservationId,
            "amount" to amount,
            "createdAt" to Timestamp.now()
        )

        db.collection("blood_donations").add(donation)
            .addOnSuccessListener {
                // Update reservation status
                db.collection("reservations").document(reservationId)
                    .update("status", "completed")
                    .addOnSuccessListener {
                        showToast("Donation saved and reservation marked as completed.")
                        resetForm()
                    }
                    .addOnFailureListener {
                        showToast("Donation saved, but failed to update reservation: ${it.message}")
                    }
            }
            .addOnFailureListener {
                showToast("Failed to save donation: ${it.message}")
            }
    }

    // Reset form after save
    private fun resetForm() {
        nicInput.text.clear()
        donationAmount.text.clear()
        donationAmount.visibility = View.GONE
        confirmDonation.visibility = View.GONE
        reservationContainer.removeAllViews()
        selectedReservationId = null
        selectedUserId = null
    }

    // Show toast message
    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
