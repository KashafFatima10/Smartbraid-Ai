package com.example.smartbraidai.data.models

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "customer", // "customer" or "admin"
    val profilePic: String = "",
    val rewardPoints: Int = 0,
    val hairType: String = ""
)
