package ru.netology.nerecipe.db

import android.database.Cursor
import ru.netology.nerecipe.dto.Ingredient
import java.util.*

fun Cursor.toIngredient() = Ingredient(
    id = UUID.fromString(getString(getColumnIndexOrThrow(IngredientsTable.Column.ID.columnName))),
    recipeId = UUID.fromString(getString(getColumnIndexOrThrow(IngredientsTable.Column.RecipeId.columnName))),
    description = getString(getColumnIndexOrThrow(IngredientsTable.Column.Description.columnName)),
    position = getInt(getColumnIndexOrThrow(IngredientsTable.Column.Position.columnName)),
)