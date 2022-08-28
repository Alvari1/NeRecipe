package ru.netology.nerecipe.dto

import android.graphics.Bitmap
import android.view.View

data class LargeImageStore(
    val thumbView: View? = null,
    val imageBitmap: Bitmap? = null,
    val appBarState: AppBarState
)
