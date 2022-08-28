package ru.netology.nerecipe.dto

import android.graphics.Bitmap
import java.util.*

data class Recipe(
    val id: UUID,
    val author: String,
    val title: String,
    val description: String? = null,
    val published: Long,
    val likes: Long = 0,
    val likedByMe: Boolean = false,
    val shares: Long = 0,
    val views: Long = 0,
    val cuisine: Int = 0,
    val image: String? = null,
    val imageBitmap: Bitmap? = null,
)