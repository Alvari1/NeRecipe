package ru.netology.nerecipe.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import ru.netology.nerecipe.entity.RecipeEntity
import java.util.*


@Dao
interface RecipeDao {
    @Query("SELECT * FROM RecipeEntity ORDER BY published DESC")
    fun getAll(): LiveData<List<RecipeEntity>>

    //using with insert -> exception -> update
    // Insert(onConflict = OnConflictStrategy.ABORT)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(recipe: RecipeEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(recipe: RecipeEntity)

    fun save(recipe: RecipeEntity) {
        insert(recipe)
//        try {
//            insert(recipe)
//        } catch (exception: SQLiteConstraintException) {
//            update(recipe)
//        }
    }

    @Query(
        """
        UPDATE RecipeEntity SET
        likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
        WHERE id = :id
        """
    )
    fun like(id: UUID)

    @Query(
        """
        UPDATE RecipeEntity SET
        shares = shares + 1
        WHERE id = :id
        """
    )
    fun share(id: UUID)

    @Query(
        """
        UPDATE RecipeEntity SET
        views = views + 1
        WHERE id = :id
        """
    )
    fun view(id: UUID)

    @Query("DELETE FROM RecipeEntity WHERE id = :id")
    fun delete(id: UUID)

    @Query("SELECT * FROM RecipeEntity WHERE id = :id LIMIT 1")
    fun getRecipeById(id: UUID): RecipeEntity

    @Query(
        """
        UPDATE RecipeEntity SET
        cuisine = :cuisine
        WHERE id = :id
        """
    )
    fun cuisine(id: UUID, cuisine: Int)

    @Query(
        """SELECT DISTINCT id FROM (SELECT DISTINCT id FROM RecipeEntity WHERE title LIKE :searchQuery 
        OR description LIKE :searchQuery 
        OR author LIKE :searchQuery
        UNION 
        SELECT DISTINCT recipeId FROM StepEntity WHERE description LIKE :searchQuery
        UNION
        SELECT DISTINCT recipeId FROM IngredientEntity WHERE description LIKE :searchQuery)
        """
    )
    fun searchDatabase(searchQuery: String): List<UUID>
}