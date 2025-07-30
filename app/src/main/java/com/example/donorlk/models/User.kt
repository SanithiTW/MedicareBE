package com.example.donorlk.models

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "donator", // Default role for self-registration
    val mobile: String = "",
    val dateOfBirth: String = "",
    val gender: String = "",
    val bloodGroup: String = "",
    val nic: String = "",
    val province: String = "",
    val city: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
