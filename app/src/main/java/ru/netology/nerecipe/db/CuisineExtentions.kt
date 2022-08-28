package ru.netology.nerecipe.db

import android.database.Cursor
import ru.netology.nerecipe.dto.Cuisine

fun Cursor.toCuisine() = Cuisine(
    id = getInt(getColumnIndexOrThrow(CuisinesTable.Column.ID.columnName)),
    resName = getString(getColumnIndexOrThrow(CuisinesTable.Column.ResName.columnName)),
    isSelected = getInt(getColumnIndexOrThrow(CuisinesTable.Column.IsSelected.columnName)) != 0,
)