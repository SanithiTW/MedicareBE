package com.example.donorlk.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.donorlk.R
import com.example.donorlk.models.DonationQuestion

class DonationQuestionAdapter(
    private val questions: List<DonationQuestion>
) : RecyclerView.Adapter<DonationQuestionAdapter.QuestionViewHolder>() {

    class QuestionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val questionText: TextView = view.findViewById(R.id.questionText)
        val yesButton: Button = view.findViewById(R.id.yesButton)
        val noButton: Button = view.findViewById(R.id.noButton)
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
        holder.yesButton.isSelected = isYes
        holder.noButton.isSelected = !isYes
    }

    override fun getItemCount() = questions.size
}
