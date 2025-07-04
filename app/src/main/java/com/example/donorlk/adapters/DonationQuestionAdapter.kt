package com.example.donorlk.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.donorlk.R
import com.example.donorlk.models.DonationQuestion
import com.google.android.material.button.MaterialButton

class DonationQuestionAdapter(
    private val questions: List<DonationQuestion>
) : RecyclerView.Adapter<DonationQuestionAdapter.QuestionViewHolder>() {

    private val defaultColor = Color.TRANSPARENT
    private val yesColor = Color.parseColor("#63E7A1")
    private val noColor = Color.parseColor("#E76363")

    class QuestionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val questionText: TextView = view.findViewById(R.id.questionText)
        val yesButton: MaterialButton = view.findViewById(R.id.yesButton)
        val noButton: MaterialButton = view.findViewById(R.id.noButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_donation_question, parent, false)
        return QuestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val question = questions[position]
        holder.questionText.text = question.question

        holder.yesButton.setOnClickListener {
            question.answer = true
            updateButtonStates(holder, true)
        }

        holder.noButton.setOnClickListener {
            question.answer = false
            updateButtonStates(holder, false)
        }

        // Reset button states based on current answer
        question.answer?.let { updateButtonStates(holder, it) }
    }

    private fun updateButtonStates(holder: QuestionViewHolder, isYes: Boolean) {
        // Update Yes button
        holder.yesButton.apply {
            backgroundTintList = ColorStateList.valueOf(if (isYes) yesColor else defaultColor)
            setTextColor(if (isYes) Color.WHITE else Color.BLACK)
        }

        // Update No button
        holder.noButton.apply {
            backgroundTintList = ColorStateList.valueOf(if (!isYes) noColor else defaultColor)
            setTextColor(if (!isYes) Color.WHITE else Color.BLACK)
        }
    }

    override fun getItemCount() = questions.size
}
