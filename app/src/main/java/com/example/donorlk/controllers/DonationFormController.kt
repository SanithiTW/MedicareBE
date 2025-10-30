package com.example.donorlk.controllers

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.Toast
import com.example.donorlk.R
import com.example.donorlk.adapters.DonationQuestionAdapter
import com.example.donorlk.models.DonationQuestion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale

class DonationFormController : BaseActivity() {
    private lateinit var questionsAdapter: DonationQuestionAdapter
    private val questions = mutableListOf<DonationQuestion>()

    // Map of expected answers for each question ID
    private val expectedAnswers = mapOf(
        1 to true,   // "Are you between 18-60 years of age?" - Should be YES
        2 to true,   // "Is your weight above 50kg?" - Should be YES
        3 to true,   // "Have you had enough sleep last night?" - Should be YES
        4 to false,  // "Have you had any major surgery in the last 6 months?" - Should be NO
        5 to false,  // "Are you currently taking any medications?" - Should be NO
        6 to false,  // "Have you consumed alcohol in the last 24 hours?" - Should be NO
        7 to false,  // "Have you had any tattoos or piercings in the last 6 months?" - Should be NO
        8 to false,  // "Do you have any chronic medical conditions?" - Should be NO
        9 to false,  // "Have you donated blood in the last 3 months?" - Should be NO
        10 to true,
        11 to false // "Are you feeling healthy and well today?" - Should be YES
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation_form)

        setupQuestions()
        setupRecyclerView()
        setupSubmitButton()
    }

    private fun setupQuestions() {
        // Adding dummy questions
        questions.addAll(listOf(
            DonationQuestion(1, "Are you between 18-60 years of age?"),
            DonationQuestion(2, "Is your weight above 50kg?"),
            DonationQuestion(3, "Have you had enough sleep last night?"),
            DonationQuestion(4, "Have you had any major surgery in the last 6 months?"),
            DonationQuestion(5, "Are you currently taking any medications?"),
            DonationQuestion(6, "Have you consumed alcohol in the last 24 hours?"),
            DonationQuestion(7, "Have you had any tattoos or piercings in the last 6 months?"),
            DonationQuestion(8, "Do you have any chronic medical conditions?"),
            DonationQuestion(9, "Have you donated blood in the last 3 months?"),
            DonationQuestion(10, "Are you feeling healthy and well today?"),
            DonationQuestion(11, "Are you feeling healthy anipdqhiq?")
        ))
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.questionsRecyclerView)
        questionsAdapter = DonationQuestionAdapter(questions)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@DonationFormController)
            adapter = questionsAdapter
        }
    }

    private fun setupSubmitButton() {
        findViewById<Button>(R.id.submitButton).setOnClickListener {
            val unansweredQuestions = questions.count { it.answer == null }
            if (unansweredQuestions > 0) {
                Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check each answer and collect reasons for ineligibility
            val ineligibilityReasons = mutableListOf<String>()

            questions.forEach { question ->
                val expectedAnswer = expectedAnswers[question.id]
                if (question.answer != expectedAnswer) {
                    val reason = when (question.id) {
                        1 -> "You must be between 18-60 years of age"
                        2 -> "Your weight must be above 50kg"
                        3 -> "You need to have adequate sleep"
                        4 -> "You cannot donate if you had major surgery in last 6 months"
                        5 -> "You cannot donate if you are on medications"
                        6 -> "You cannot donate if you consumed alcohol in last 24 hours"
                        7 -> "You cannot donate if you had tattoos/piercings in last 6 months"
                        8 -> "You cannot donate if you have chronic medical conditions"
                        9 -> "You must wait 3 months between donations"
                        10 -> "You must be feeling healthy and well"
                        else -> "Ineligible based on question ${question.id}"
                    }
                    ineligibilityReasons.add(reason)
                }
            }

            if (ineligibilityReasons.isNotEmpty()) {
                // Create a dialog to show the reasons
                val dialog = android.app.AlertDialog.Builder(this)
                    .setTitle("Donation Eligibility Results")
                    .setMessage("You are not eligible to donate for the following reasons:\n\n• ${ineligibilityReasons.joinToString("\n• ")}\n\nPlease meet an officer for more details.")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                dialog.show()
                return@setOnClickListener
            }

            // If eligible, continue with saving to Firebase
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Format current date and time
            val calendar = Calendar.getInstance()
            val formattedDate = String.format(
                Locale.ENGLISH,
                "%04d-%02d-%02d %02d:%02d:%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND)
            )

            // Save form submission date to user's document
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("users").document(currentUser.uid)
                .update("lastFormSubmission", formattedDate)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "You are eligible to donate! Form submitted successfully.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish() // Close the form activity
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error saving form: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }
}
