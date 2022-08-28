package ru.netology.nerecipe.dto

data class Cuisine(
    val id: Int,
    val resName: String,
    val isSelected: Boolean = true,
)
