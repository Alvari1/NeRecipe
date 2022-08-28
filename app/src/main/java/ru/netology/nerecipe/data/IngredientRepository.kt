package ru.netology.nerecipe.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nerecipe.dto.Ingredient
import ru.netology.nerecipe.util.EMPTY_UUID
import java.util.*

interface IngredientRepository {
    fun getAll(): LiveData<List<Ingredient>>
    val currentIngredients: MutableLiveData<List<Ingredient>?>

    fun deleteByRecipeId(recipeId: UUID)
    fun save(ingredients: List<Ingredient>)

    companion object {
        val EMPTY_INGREDIENT = Ingredient(
            id = EMPTY_UUID,
            recipeId = EMPTY_UUID,
            description = "",
            position = 0,
        )
    }
}