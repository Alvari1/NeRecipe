package ru.netology.nerecipe.db

import android.database.Cursor
import ru.netology.nerecipe.dto.Recipe
import java.util.*

fun Cursor.toRecipe() = Recipe(
    id = UUID.fromString(getString(getColumnIndexOrThrow(RecipesTable.Column.ID.columnName))),
    author = getString(getColumnIndexOrThrow(RecipesTable.Column.Author.columnName)),
    title = getString(getColumnIndexOrThrow(RecipesTable.Column.Title.columnName)),
    description = getString(getColumnIndexOrThrow(RecipesTable.Column.Description.columnName)),
    published = getLong(getColumnIndexOrThrow(RecipesTable.Column.Published.columnName)),
    likedByMe = getInt(getColumnIndexOrThrow(RecipesTable.Column.LikedByMe.columnName)) != 0,
    likes = getLong(getColumnIndexOrThrow(RecipesTable.Column.Likes.columnName)),
    shares = getLong(getColumnIndexOrThrow(RecipesTable.Column.Shares.columnName)),
    views = getLong(getColumnIndexOrThrow(RecipesTable.Column.Views.columnName)),
    cuisine = getInt(getColumnIndexOrThrow(RecipesTable.Column.Cuisine.columnName)),
    image = getString(getColumnIndexOrThrow(RecipesTable.Column.Image.columnName)),
)