package ru.netology.nerecipe.dto

data class AppBarState(
    val isSearch: Boolean = false,
    val isAdd: Boolean = false,
    val isEdit: Boolean = false,
    val isSave: Boolean = false,
    val isCancel: Boolean = false,
    val isDelete: Boolean = false,
)
