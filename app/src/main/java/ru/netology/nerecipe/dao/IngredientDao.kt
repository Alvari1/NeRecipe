package ru.netology.nerecipe.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.netology.nerecipe.entity.IngredientEntity
import java.util.*

@Dao
interface IngredientDao {
    @Query("SELECT * FROM IngredientEntity ORDER BY recipeId, position ASC")
    fun getAll(): LiveData<List<IngredientEntity>>

    @Insert
    fun insert(ingredient: IngredientEntity)

    fun save(ingredients: List<IngredientEntity>) {
        if (ingredients.isNotEmpty()) {
            deleteByRecipeId(ingredients.first().recipeId)
            ingredients.forEach { insert(it) }
        }
    }

    @Query("DELETE FROM IngredientEntity WHERE recipeId = :id")
    fun deleteByRecipeId(id: UUID)
}