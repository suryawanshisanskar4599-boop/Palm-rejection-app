package com.example.palmrejection.model

data class TouchData(
    val x: Float,
    val y: Float,
    val size: Float,
    val pressure: Float,
    val timestamp: Long,
    val pointerId: Int,
    val toolType: Int,
    val action: Int
)
