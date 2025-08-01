package com.example.donorlk

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CreateAdminActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var currentUserRole: String? = null  // For storing creator's role

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

        val roles = arrayOf("Admin", "Sub Admin", "Super Admin")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roleSpinner.adapter = adapter

        backButton.setOnClickListener { finish() }

        // Get current logged-in user role from Firestore
        val currentUid = auth.currentUser?.uid
        if (currentUid != null) {
            db.collection("users").document(currentUid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val role = document.getString("role")
                        if (role in listOf("Admin", "Sub Admin", "Super Admin")) {
                            currentUserRole = role
                        } else {
                            Toast.makeText(this, "Access denied: Not an admin", Toast.LENGTH_LONG).show()
                            finish()
                        }
                    } else {
                        Toast.makeText(this, "User document not found", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to load user data", Toast.LENGTH_LONG).show()
                    finish()
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_LONG).show()
            finish()
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

            if (currentUserRole == null) {
                Toast.makeText(this, "Your role is not authorized to create admins", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val createdAt = sdf.format(Date())
            val generatedUsername = "${role}_${System.currentTimeMillis()}"

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
