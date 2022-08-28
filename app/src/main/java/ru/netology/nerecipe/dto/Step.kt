package ru.netology.nerecipe.dto

import android.graphics.Bitmap
import java.util.*

data class Step(
    val id: UUID,
    val recipeId: UUID,
    val position: Int,
    val description: String,
    val image: String? = null,
    val imageBitmap: Bitmap? = null,
)
