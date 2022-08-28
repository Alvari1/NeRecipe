package ru.netology.nerecipe.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nerecipe.dto.Cuisine

@Entity
data class CuisineEntity(
    @PrimaryKey
    val id: Int,
    val resName: String,
    val isSelected: Boolean = true,
) {

    fun toDto() = Cuisine(id, resName, isSelected)

    companion object {
        fun fromDto(dto: Cuisine) =
            CuisineEntity(
                dto.id,
                dto.resName,
                dto.isSelected
            )
    }
}