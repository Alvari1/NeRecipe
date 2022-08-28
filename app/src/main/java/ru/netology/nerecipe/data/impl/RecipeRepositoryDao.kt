package ru.netology.nerecipe.data.impl

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import ru.netology.nerecipe.dao.RecipeDao
import ru.netology.nerecipe.data.RecipeRepository
import ru.netology.nerecipe.dto.Recipe
import ru.netology.nerecipe.entity.RecipeEntity
import ru.netology.nerecipe.util.ImageStorageManager.Companion.isImageExistInExternalStorage
import java.util.*

class RecipeRepositoryDao(
    private val dao: RecipeDao,
) : RecipeRepository {
    override val currentRecipe = MutableLiveData<Recipe?>(null)

    override fun getAll(context: Context): LiveData<List<Recipe>> =
        Transformations.map(dao.getAll()) { list ->
            list.map {
                it.toDto().copy(
                    image = if (it.image == null) null
                    else isImageExist(context, it.image)
                )
            }
        }

    private fun isImageExist(context: Context, imageFileName: String?): String? =
        if (imageFileName.isNullOrEmpty())
            null
        else
//            if (isImageExistInInternalStorage(context, imageFileName))
            if (isImageExistInExternalStorage(imageFileName))
                imageFileName
            else null

    override fun save(recipe: Recipe) {
        dao.save(RecipeEntity.fromDto(recipe))
    }

    override fun like(recipeId: UUID) {
        dao.like(recipeId)
    }

    override fun share(recipeId: UUID) {
        dao.share(recipeId)
    }

    override fun view(recipeId: UUID) {
        dao.view(recipeId)
    }

    override fun delete(context: Context, recipe: Recipe) {
        dao.delete(recipe.id)
    }

    override fun getRecipeById(recipeId: UUID): Recipe {
        return dao.getRecipeById(recipeId).toDto()
    }

    override fun cuisine(recipeId: UUID, cuisine: Int) {
        dao.cuisine(recipeId, cuisine)
    }

    override fun searchDatabase(searchQuery: String): List<UUID> {
        return dao.searchDatabase(searchQuery)
    }
}