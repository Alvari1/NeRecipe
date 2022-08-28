package ru.netology.nerecipe.db

import android.database.Cursor
import ru.netology.nerecipe.dto.Step
import java.util.*

fun Cursor.toStep() = Step(
    id = UUID.fromString(getString(getColumnIndexOrThrow(StepsTable.Column.ID.columnName))),
    recipeId = UUID.fromString(getString(getColumnIndexOrThrow(StepsTable.Column.RecipeId.columnName))),
    position = getInt(getColumnIndexOrThrow(StepsTable.Column.Position.columnName)),
    description = getString(getColumnIndexOrThrow(StepsTable.Column.Description.columnName)),
    image = getString(getColumnIndexOrThrow(StepsTable.Column.Image.columnName)),
)