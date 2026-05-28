package com.example.smartbraidai.data.models

data class Artist(
    val id: String = "",
    val name: String = "",
    val role: String = "", 
    val rating: Double = 0.0,
    val imageUrl: String = "",
    val services: List<String> = emptyList(),
    val experience: String = "",
    val description: String = "",
    val status: String = "AVAILABLE",
    val isAvailable: Boolean = true,
    val availability: Map<String, Map<String, String>> = emptyMap()
)
