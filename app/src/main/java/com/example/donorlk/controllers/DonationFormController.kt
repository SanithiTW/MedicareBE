package com.example.donorlk.controllers

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.Toast
import com.example.donorlk.R
import com.example.donorlk.adapters.DonationQuestionAdapter
import com.example.donorlk.models.DonationQuestion

class DonationFormController : BaseActivity() {
    private lateinit var questionsAdapter: DonationQuestionAdapter
    private val questions = mutableListOf<DonationQuestion>()

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
            DonationQuestion(10, "Are you feeling healthy and well today?")
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

            // Check eligibility
            val isEligible = checkEligibility()
            val message = if (isEligible) "You are eligible to donate!" else "Sorry, you are not eligible to donate at this time."
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun checkEligibility(): Boolean {
        // Basic eligibility logic - can be customized based on specific requirements
        return questions[0].answer == true && // Age requirement
               questions[1].answer == true && // Weight requirement
               questions[3].answer == false && // No recent surgery
               questions[8].answer == false && // No chronic conditions
               questions[9].answer == true     // Feeling healthy today
    }
}
