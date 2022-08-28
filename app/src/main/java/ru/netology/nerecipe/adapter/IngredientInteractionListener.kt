package ru.netology.nerecipe.adapter

import java.util.*

interface IngredientInteractionListener {
    fun addEmptyIngredient(recipeId: UUID)
    fun removeIngredient(id: UUID)

}