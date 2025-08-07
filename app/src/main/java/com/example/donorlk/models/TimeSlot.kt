package com.example.donorlk.models

data class TimeSlot(
    val time: String,
    val availableCount: Int,
    val maxSlots: Int,
    val isSelected: Boolean = false
)