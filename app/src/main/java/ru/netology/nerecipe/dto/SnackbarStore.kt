package ru.netology.nerecipe.dto

import com.google.android.material.snackbar.Snackbar

data class SnackbarStore(
    val resId: Int? = null,
    val text: CharSequence? = null,
    val duration: Int = Snackbar.LENGTH_SHORT,
)
