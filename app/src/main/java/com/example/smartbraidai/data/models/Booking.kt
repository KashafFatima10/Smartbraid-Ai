package com.example.smartbraidai.data.models

data class Booking(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val artistId: String = "",
    val artistName: String = "",
    val serviceSelected: String = "",
    val date: String = "",
    val time: String = "",
    val status: String = "Upcoming", // e.g., Upcoming, Completed, Cancelled
    val paymentStatus: String = "Pending", // e.g., Paid, Pending
    val amount: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
