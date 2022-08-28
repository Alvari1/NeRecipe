package ru.netology.nerecipe.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import ru.netology.nerecipe.dto.Recipe
import java.util.*

@Entity
data class RecipeEntity(
    @PrimaryKey()
    val id: UUID,
    val author: String,
    val title: String,
    val description: String? = null,
    val published: Long = 0,
    val likes: Long = 0,
    val likedByMe: Boolean = false,
    val shares: Long = 0,
    val views: Long = 0,
    val cuisine: Int = 0,
    val image: String? = null,
    @Ignore val imageBitmap: String? = null,

    ) {
    constructor(
        id: UUID,
        author: String,
        title: String,
        description: String?,
        published: Long,
        likes: Long,
        likedByMe: Boolean,
        shares: Long,
        views: Long,
        cuisine: Int,
        image: String?
    ) : this(
        id,
        author,
        title,
        description,
        published,
        likes,
        likedByMe,
        shares,
        views,
        cuisine,
        image,
        null,
    )

    fun toDto() =
        Recipe(
            id,
            author,
            title,
            description,
            published,
            likes,
            likedByMe,
            shares,
            views,
            cuisine,
            image,
        )

    companion object {
        fun fromDto(dto: Recipe) =
            RecipeEntity(
                dto.id,
                dto.author,
                dto.title,
                dto.description,
                dto.published,
                dto.likes,
                dto.likedByMe,
                dto.shares,
                dto.views,
                dto.cuisine,
                dto.image,
            )
    }
}