package ru.netology.nerecipe.dto

import java.util.*

data class Ingredient(
    val id: UUID,
    val recipeId: UUID,
    val description: String,
    val position: Int,
)
