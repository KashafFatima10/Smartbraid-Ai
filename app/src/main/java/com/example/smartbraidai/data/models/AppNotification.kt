package com.example.smartbraidai.data.models

data class AppNotification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val type: String = "Normal", // e.g., "Urgent", "AI Insight", "Reminder"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
