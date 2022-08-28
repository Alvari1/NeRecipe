package ru.netology.nerecipe.entity

import androidx.room.Entity
import ru.netology.nerecipe.dto.Ingredient
import java.util.*

@Entity(primaryKeys = ["id"])
data class IngredientEntity(
    val id: UUID,
    val recipeId: UUID,
    val description: String,
    val position: Int,
) {

    fun toDto() = Ingredient(id, recipeId, description, position)

    companion object {
        fun fromDto(dto: Ingredient) =
            IngredientEntity(
                dto.id,
                dto.recipeId,
                dto.description,
                dto.position,
            )
    }
}