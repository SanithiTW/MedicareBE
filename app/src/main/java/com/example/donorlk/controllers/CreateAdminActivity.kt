package com.example.donorlk

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateAdminActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var currentUserRole: String = "unknown"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_admin)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val nameField = findViewById<EditText>(R.id.NameEditText)
        val locationField = findViewById<EditText>(R.id.locationEditText)
        val locationIdField = findViewById<EditText>(R.id.locationIdEditText)
        val roleSpinner = findViewById<Spinner>(R.id.roleSpinner)
        val emailField = findViewById<EditText>(R.id.emailEditText)
        val passwordField = findViewById<EditText>(R.id.passwordEditText)
        val confirmButton = findViewById<Button>(R.id.confirmButton)
        val backButton = findViewById<ImageView>(R.id.backButton)

        val roles = arrayOf("Admin", "Sub Admin")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roleSpinner.adapter = adapter

        backButton.setOnClickListener { finish() }

        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    currentUserRole = document.getString("role") ?: "unknown"
                }
        }

        confirmButton.setOnClickListener {
            val name = nameField.text.toString().trim()
            val location = locationField.text.toString().trim()
            val locationId = locationIdField.text.toString().trim()
            val role = roleSpinner.selectedItem.toString()
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (name.isEmpty() || location.isEmpty() || locationId.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val createdAt = System.currentTimeMillis()
            val generatedUsername = "${role}_${createdAt}"

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: return@addOnSuccessListener
                    val userMap = hashMapOf(
                        "uid" to uid,
                        "username" to generatedUsername,
                        "name" to name,
                        "email" to email,
                        "location" to location,
                        "locationId" to locationId,
                        "role" to role,
                        "createdAt" to createdAt,
                        "createdBy" to currentUserRole
                    )

                    db.collection("users").document(uid).set(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Admin account created!", Toast.LENGTH_LONG).show()
                            nameField.text.clear()
                            locationField.text.clear()
                            locationIdField.text.clear()
                            emailField.text.clear()
                            passwordField.text.clear()
                            roleSpinner.setSelection(0)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Firestore error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Auth error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
