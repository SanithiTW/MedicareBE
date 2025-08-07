package com.example.donorlk

import GmailSender
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateOperatorsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var currentUserRole: String = "Operator"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_operator)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val nameField = findViewById<EditText>(R.id.NameEditText)
        val NICField = findViewById<EditText>(R.id.nicEditText)
        val emailField = findViewById<EditText>(R.id.emailEditText)
        val passwordField = findViewById<EditText>(R.id.passwordEditText)
        val confirmButton = findViewById<Button>(R.id.confirmButton)
        val backButton = findViewById<ImageView>(R.id.backButton)

        // Get role of current user (optional for auditing)
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    currentUserRole = document.getString("role") ?: "Operator"
                }
        }

        confirmButton.setOnClickListener {
            val name = nameField.text.toString().trim()
            val NIC = NICField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            val role = "Operator" // 🔐 Fixed role

            if (name.isEmpty() || NIC.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidNIC(NIC)) {
                Toast.makeText(this, "Invalid NIC format", Toast.LENGTH_SHORT).show()
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
                        "NIC" to NIC,
                        "role" to role,
                        "createdAt" to createdAt,
                        "createdBy" to (currentUser?.uid ?: "unknown")
                    )

                    db.collection("users").document(uid).set(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Operator account created!", Toast.LENGTH_LONG).show()
                            nameField.text.clear()
                            NICField.text.clear()
                            emailField.text.clear()
                            passwordField.text.clear()

                            // Send Email Notification
                            val senderEmail = "donorlk.system@gmail.com"
                            val senderPassword = "cwgp qczk zapa qzik" // App-specific password

                            val subject = "DonorLK Operator Account Created"
                            val body = """
                                Hello,

                                Your operator account has been created.

                                Email: $email
                                User Id: $uid
                                User Name: $generatedUsername
                                Password: $password

                                Please login to DonorLK using these credentials.

                                Regards,
                                DonorLK Team
                            """.trimIndent()

                            GmailSender(senderEmail, senderPassword).sendEmail(email, subject, body)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Firestore error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Auth error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    // ✅ NIC Validation: Old + New Format
    private fun isValidNIC(nic: String): Boolean {
        val oldNICPattern = Regex("^[0-9]{9}[vVxX]$")
        val newNICPattern = Regex("^[0-9]{12}$")
        return nic.matches(oldNICPattern) || nic.matches(newNICPattern)
    }
}
