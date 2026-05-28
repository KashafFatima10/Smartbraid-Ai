package com.example.smartbraidai.data.models

data class Pattern(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val precision: Int = 0,
    val complexity: Int = 0,
    val symmetry: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
