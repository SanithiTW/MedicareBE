package com.example.donorlk

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import android.widget.ImageView
import java.text.SimpleDateFormat
import java.util.*

class SaveDonationsRecorderController : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var nicInput: EditText
    private lateinit var searchButton: Button
    private lateinit var reservationContainer: LinearLayout
    private lateinit var donationAmount: EditText
    private lateinit var confirmDonation: Button
    private lateinit var cancelDonation: Button
    private lateinit var donationSection: LinearLayout

    private var selectedReservationId: String? = null
    private var selectedUserId: String? = null
    private var lastFormSubmissionValid: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_donation_records)

        db = FirebaseFirestore.getInstance()

        nicInput = findViewById(R.id.nicInput)
        searchButton = findViewById(R.id.searchButton)
        reservationContainer = findViewById(R.id.reservationContainer)
        donationAmount = findViewById(R.id.donationAmount)
        confirmDonation = findViewById(R.id.confirmDonation)
        cancelDonation = findViewById(R.id.cancelDonation)
        donationSection = findViewById(R.id.donationSection)

        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        donationSection.visibility = View.GONE

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

        confirmDonation.setOnClickListener {
            if (!lastFormSubmissionValid) {
                showToast("Please fill the donation form before the donation")
                return@setOnClickListener
            }
            saveDonation()
        }

        cancelDonation.setOnClickListener {
            resetForm()
        }
    }

    private fun isValidNIC(nic: String): Boolean {
        val oldNicRegex = Regex("^[0-9]{9}[vVxX]$")
        val newNicRegex = Regex("^[0-9]{12}$")
        return oldNicRegex.matches(nic) || newNicRegex.matches(nic)
    }

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

                // Validate lastFormSubmission
                val lastFormStr = user.getString("lastFormSubmission")
                lastFormSubmissionValid = false

                if (!lastFormStr.isNullOrEmpty()) {
                    try {
                        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val parsedDate = formatter.parse(lastFormStr)
                        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val lastFormDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(parsedDate)
                        lastFormSubmissionValid = todayStr == lastFormDateStr
                    } catch (e: Exception) {
                        lastFormSubmissionValid = false
                    }
                }

                db.collection("reservations")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("status", "pending")
                    .get()
                    .addOnSuccessListener { reservations ->
                        reservationContainer.removeAllViews()
                        selectedReservationId = null
                        donationSection.visibility = View.GONE

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
            if (lastFormSubmissionValid) {
                donationSection.visibility = View.VISIBLE
            } else {
                showToast("Please fill the donation form before the donation")
                donationSection.visibility = View.GONE
            }
        }

        return card
    }

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

    private fun resetForm() {
        nicInput.text.clear()
        donationAmount.text.clear()
        donationSection.visibility = View.GONE
        reservationContainer.removeAllViews()
        selectedReservationId = null
        selectedUserId = null
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
