package ru.netology.nerecipe.entity

import androidx.room.Entity
import androidx.room.Ignore
import ru.netology.nerecipe.dto.Step
import java.util.*

@Entity(primaryKeys = ["id"])
data class StepEntity(
    val id: UUID,
    val recipeId: UUID,
    val position: Int,
    val description: String,
    val image: String? = null,
    @Ignore val imageBitmap: String? = null,
) {
    constructor(
        id: UUID,
        recipeId: UUID,
        position: Int,
        description: String,
        image: String?
    ) : this(
        id,
        recipeId,
        position,
        description,
        image,
        null,
    )

    fun toDto() = Step(id, recipeId, position, description, image)

    companion object {
        fun fromDto(dto: Step) =
            StepEntity(
                dto.id,
                dto.recipeId,
                dto.position,
                dto.description,
                dto.image,
            )
    }
}