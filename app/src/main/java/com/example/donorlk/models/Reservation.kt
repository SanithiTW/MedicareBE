package com.example.donorlk.models

import com.google.firebase.Timestamp

data class Reservation(
    val id: String = "",
    val userId: String = "",
    val centerId: String = "",
    val centerName: String = "",
    val reservationDate: String = "",
    val reservationTime: String = "",
    val notes: String = "",
    val status: String = "pending",
    val createdAt: Timestamp? = null
) {
    // Secondary constructor for backwards compatibility
    constructor(time: String, date: String, place: String) : this(
        id = "",
        userId = "",
        centerId = "",
        centerName = place,
        reservationDate = date,
        reservationTime = time,
        notes = "",
        status = "pending",
        createdAt = null
    )
}
