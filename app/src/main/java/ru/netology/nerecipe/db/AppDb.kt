package ru.netology.nerecipe.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.netology.nerecipe.dao.CuisineDao
import ru.netology.nerecipe.dao.IngredientDao
import ru.netology.nerecipe.dao.RecipeDao
import ru.netology.nerecipe.dao.StepDao
import ru.netology.nerecipe.entity.CuisineEntity
import ru.netology.nerecipe.entity.IngredientEntity
import ru.netology.nerecipe.entity.RecipeEntity
import ru.netology.nerecipe.entity.StepEntity

@Database(
    entities = [RecipeEntity::class, StepEntity::class, IngredientEntity::class, CuisineEntity::class],
    version = 4
)
abstract class AppDb : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun stepDao(): StepDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun cuisineDao(): CuisineDao


    companion object {
        @Volatile
        private var instance: AppDb? = null

        fun getInstance(context: Context): AppDb {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, AppDb::class.java, "app.db")
//                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .createFromAsset("database/skeleton.db")
                .build()
    }
}