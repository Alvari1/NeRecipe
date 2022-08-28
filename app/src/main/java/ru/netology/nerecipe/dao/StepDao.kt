package ru.netology.nerecipe.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.netology.nerecipe.entity.StepEntity
import java.util.*

@Dao
interface StepDao {
    @Query("SELECT * FROM StepEntity ORDER BY recipeId, position ASC")
    fun getAll(): LiveData<List<StepEntity>>

    @Insert
    fun insert(step: StepEntity)

    fun save(steps: List<StepEntity>) {
        if (steps.isNotEmpty()) {
            deleteByRecipeId(steps.first().recipeId)
            steps.forEach { insert(it) }
        }
    }

    @Query("DELETE FROM StepEntity WHERE id = :id")
    fun delete(id: UUID)

    @Query("DELETE FROM StepEntity WHERE recipeId = :id")
    fun deleteByRecipeId(id: UUID)
}