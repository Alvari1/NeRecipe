package ru.netology.nerecipe.data.impl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import ru.netology.nerecipe.dao.IngredientDao
import ru.netology.nerecipe.data.IngredientRepository
import ru.netology.nerecipe.dto.Ingredient
import ru.netology.nerecipe.entity.IngredientEntity
import java.util.*

class IngredientRepositoryDao(
    private val dao: IngredientDao,
) : IngredientRepository {
    override val currentIngredients = MutableLiveData<List<Ingredient>?>(null)

    override fun getAll(): LiveData<List<Ingredient>> = Transformations.map(dao.getAll()) { list ->
        list.map {
            it.toDto()
        }
    }

    override fun save(ingredients: List<Ingredient>) {
        dao.save(ingredients.map {
            IngredientEntity.fromDto(it)
        })
    }

    override fun deleteByRecipeId(recipeId: UUID) {
        dao.deleteByRecipeId(recipeId)
    }
}