package com.example.donorlk

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class RecordDonationActivity : AppCompatActivity() {

    // UI elements
    private lateinit var nicEditText: EditText
    private lateinit var amountEditText: EditText
    private lateinit var confirmButton: Button
    private lateinit var clearButton: Button

    // Firestore instance
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_donations)

        // Initialize views
        nicEditText = findViewById(R.id.nameEditText)
        amountEditText = findViewById(R.id.emailEditText)
        confirmButton = findViewById(R.id.verifyButton)
        clearButton = findViewById(R.id.clearButton)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Handle Confirm Button
        confirmButton.setOnClickListener {
            val nic = nicEditText.text.toString().trim()
            val amountText = amountEditText.text.toString().trim()

            if (nic.isEmpty() || amountText.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountText.toIntOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Please enter a valid amount in ml", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val donationData = hashMapOf(
                "nic" to nic,
                "amount_ml" to amount,
                "donated_at" to Timestamp.now()
            )

            db.collection("blood_donations")
                .add(donationData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Donation recorded successfully!", Toast.LENGTH_SHORT).show()
                    nicEditText.text.clear()
                    amountEditText.text.clear()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error saving donation: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }

        // Handle Clear Button
        clearButton.setOnClickListener {
            nicEditText.text.clear()
            amountEditText.text.clear()
        }
    }
}
