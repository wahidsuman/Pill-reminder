package com.mypills

import androidx.compose.ui.graphics.Color

data class Pill(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val dosage: String,
    val time: String,
    val color: String,
    val taken: Boolean = false
) {
    fun getColorValue(): Color {
        return when (color.lowercase()) {
            "blue" -> Color(0xFF2563EB)
            "red" -> Color(0xFFEF4444)
            "green" -> Color(0xFF10B981)
            "orange" -> Color(0xFFF59E0B)
            "purple" -> Color(0xFF8B5CF6)
            "pink" -> Color(0xFFEC4899)
            else -> Color(0xFF2563EB) // Default to blue
        }
    }
    
    fun getLightColorValue(): Color {
        return when (color.lowercase()) {
            "blue" -> Color(0xFFDBEAFE)
            "red" -> Color(0xFFFEE2E2)
            "green" -> Color(0xFFD1FAE5)
            "orange" -> Color(0xFFFED7AA)
            "purple" -> Color(0xFFE9D5FF)
            "pink" -> Color(0xFFFCE7F3)
            else -> Color(0xFFDBEAFE) // Default to light blue
        }
    }
}