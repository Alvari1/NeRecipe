package ru.netology.nerecipe.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nerecipe.dto.Recipe
import ru.netology.nerecipe.util.EMPTY_UUID
import java.util.*

interface RecipeRepository {
    fun getAll(context: Context): LiveData<List<Recipe>>

    val currentRecipe: MutableLiveData<Recipe?>

    fun like(recipeId: UUID)

    fun share(recipeId: UUID)

    fun view(recipeId: UUID)

    fun delete(context: Context, recipe: Recipe)

    fun save(recipe: Recipe)

    fun getRecipeById(recipeId: UUID): Recipe

    fun cuisine(recipeId: UUID, cuisine: Int)

    companion object {
        val EMPTY_RECIPE = Recipe(
            id = EMPTY_UUID,
            title = "",
            author = "",
            published = 0L
        )
    }

    fun searchDatabase(searchQuery: String): List<UUID>
}