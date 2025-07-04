package com.example.donorlk.models

data class DonationQuestion(
    val id: Int,
    val question: String,
    var answer: Boolean? = null
)
